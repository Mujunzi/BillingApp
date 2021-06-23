package com.lenovo.billing.model;

import java.lang.reflect.Type;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import com.lenovo.billing.common.JsonUtil;
import com.lenovo.billing.common.Util;
import com.lenovo.billing.rfid.RfidReader;
import com.lenovo.billing.entity.*;
import com.lenovo.billing.cardpay.*;
import com.lenovo.billing.smartequipment.*;
import com.lenovo.billing.smartequipment.StateEvent;
import com.lenovo.billing.protocol.*;

public class Billing implements BillingProcess, CardPaymentHandler, SmartEquipmentViewable {

    private static final String TAG = Billing.class.getSimpleName();

    public int beforeTidsCount = -1;

    private Handler mDefaultHandler;

    private RfidReader rfidReader;
    private SmartEquipment se;
    private BillingClient billingClient;
    private RemoteController remoteController;
    private EmployeeCardPayment employeeCardPayment;

    private MyCountDownTimer timer;
    private ExecutorService exService;

    private final BillSub billSub;
    public boolean needTracking = false;

    public Billing() {

        billSub = new BillSub();
        billSub.billState = BillState.CLEARED; // in Billing()
        Log.d(TAG, "Billing() billState := " + billSub.billState);

        //
        // Init the thread pool.
        //

        int processors = Runtime.getRuntime().availableProcessors();
        exService = Executors.newFixedThreadPool(processors);

        //
        // Build a BillingBuilder object with default config.
        //

        BillingBuilder builder = new BillingBuilder();

        //
        // If BillingConfig.json exists, override the BillingConfig.
        //

        String configInfo;
        JSONObject configJson;

        try {
            configInfo = JsonUtil.readConfig();
            Log.d(TAG, "Billing()::" + configInfo);
            configJson = new JSONObject(configInfo);
            BillingConfig.override(configJson);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        BillingConfig.dump();

        //
        // Read config.
        //

        if (BillingConfig.RR_ENABLE) {
            this.rfidReader = builder.buildRfidReader();
            this.rfidReader.register(this);
        }
        rfidReader.netIsAlive();

        this.se = builder.buildSmartEquipment(this);
        this.se.register(this);

        this.remoteController = builder.buildRemoteController();
        this.remoteController.register(this);

        this.billingClient = builder.buildBillingClient();
        this.billingClient.register(this);

        FaceDetection faceDetection = builder.buildFaceDetection();
        faceDetection.register(this);

        if (BillingConfig.EC_ENABLE) {
            this.employeeCardPayment = builder.buildEmployeeCardPayment();
            this.employeeCardPayment.registerCallBack(this);
        }

        long INTERVAL = 1000L;
        long timeout = BillingConfig.FD_DETECT_TIMEOUT;
        timer = new MyCountDownTimer(timeout, INTERVAL);
        se.start();

        //
        // Should make sure that the UDP connection is built, otherwise NULL happenes.
        //

        Util.dispatchEvent(se, StateEvent.APP_STARTED);

        if (BillingConfig.RC_ENABLE) {
            remoteController.start();
        } else {
            remoteController.closeSocket();
            remoteController.interrupt();
        }

        if (BillingConfig.BA_REPORT_STATUS) {
            billingClient.startReportStatus();
        } else {
            billingClient.stopReportStatus();
        }

        // 
        // Promptly report status when starting the app so that the lamp displays green.
        //

        billingClient.reportDeviceStatus();
    }

    public void setDefaultHandler(Handler defaultHandler) {
        this.mDefaultHandler = defaultHandler;
    }

    private void getCustomerInfo(String faceId) {
        dumpBillSub("getCustomerInfo()");

        Log.d(TAG, "getCustomerInfo() send STOPFACEDETECT");
        mDefaultHandler.sendEmptyMessage(BillingViewable.STOPFACEDETECT);
        billingClient.getCustomerInfo(faceId);

        dumpBillSub("getCustomerInfo()", "returned");
    }

    public void handleNotMe() {
        dumpBillSub("handleNotMe()", true);

        billSub.billState = BillState.RECOGNIZING; // in handleNotMe()
        Log.d(TAG, "billState := " + billSub.billState);

        //
        // cancel POS charging.
        //

        if (BillingConfig.EC_ENABLE) {
            dumpBillSub("handleNotMe()", "Call employeeCardPayment sendCancelRequest()");
            employeeCardPayment.sendCancelRequest();
        }

        //
        // Stop to polling to detect QR code payment.
        //

        Log.d(TAG, "remove PAYMENTRESULT");
        mDefaultHandler.removeMessages(BillingViewable.PAYMENTRESULT); // stop polling when handleNotMe()

        //
        // If the user taps "notMe"
        //
        // face id = -100
        // customer id = 00000000000000000000000000010000
        // nick name = ""
        //
        billSub.faceId = "-100";
        billSub.customerInfo.setCustomerId("00000000000000000000000000010000");
        billSub.customerInfo.setCustomerName("");
        Log.d(TAG, "handleNotMe() set faceId := " + billSub.faceId);

        //
        //billSub.mItemsRecognized = true; // skip to recognize items.
        //Log.d(TAG, "handleNotMe() mItemsRecognized := " + billSub.mItemsRecognized);
        //billSub.mUserIdentified = false; // need to identify user. The flag will be changed to true by callback.
        // BUGBUG. Is it necessary?
        //Log.d(TAG, "handleNotMe() mUserIdentified := " + billSub.mUserIdentified);
        //

        getCustomerInfo(billSub.faceId); // get customer info from server.

        //
        // Notify to change GUI.
        //

        Message msg = new Message();
        Bundle bundle = new Bundle();
        bundle.putString("source", "refresh");
        msg.setData(bundle);
        msg.what = BillingViewable.REFRESHISPRESSED;
        Log.d(TAG, "send REFRESHISPRESSED, 0");
        mDefaultHandler.sendMessageDelayed(msg, 0);

        //
        // Send BILL_REFRESHED event to try to close door #3. If the door #3 is closed,
        // the state is changed 010. RFID reader is not started because refreshFrom is notMe.
        //

        billSub.refreshFrom = "notMe";
        Log.d(TAG, "handleNotMe()  refreshFrom := " + billSub.refreshFrom);

        if (billSub.isShopping()) {
            exService.execute(new Runnable() {
                @Override
                public void run() {
                    Util.dispatchEvent(se, StateEvent.BILL_REFRESHED);
                }
            });
        }
    }

    //
    // source
    //      noShopping
    //      recheck
    //      refresh
    //

    public void refreshItems(String source) {
        Log.d(TAG, "refreshItems() source = " + source);
        dumpBillSub("refreshItems()", true);

        billSub.billState = BillState.RECOGNIZING; // in refreshItems()
        Log.d(TAG, "billState := " + billSub.billState);

        //
        // cancel POS charging.
        //

        if (BillingConfig.EC_ENABLE) {
            dumpBillSub("refreshItems()", "call sendCancelRequest()");
            employeeCardPayment.sendCancelRequest();
        }

        //
        // Stop to polling to detect QR code payment.
        //

        Log.d(TAG, "remove PAYMENTRESULT");
        mDefaultHandler.removeMessages(BillingViewable.PAYMENTRESULT); // stop polling when refreshItems()

        //
        // If noShopping, recheck, or refresh
        //

        //billSub.mUserIdentified = true; // skip to detect face.
        //Log.d(TAG, "refreshItems() mUserIdentified := " + billSub.mUserIdentified);

        Log.d(TAG, "refreshItems() isShopping = " + billSub.isShopping()
                + ", isOSP = " + billSub.isOneStepPayment()
                + ", isDebt = " + billSub.isDebt());

        //
        // We suppose that:
        //      1. source is recheck or refresh.
        //      2. door #3 is closed.
        //
        // If the user is following, the door #3 is closed.
        //       1S
        //       2S
        //      D2S
        //      D3S
        //
        //  The following code also fix bug 247. 
        //

        //
        // Use door3MustBeClosed flag to determine if the door #3 must be closed.
        //

        boolean door3MustBeClosed = false; // false means the door #3 is opened, opening, or closed.

        if (billSub.isShopping()) {     // S
            if (billSub.isRealOneStepPayment()) { // 3 
                if (billSub.isDebt()) { // D
                    door3MustBeClosed = true; // if user is D3S
                    Log.d(TAG, "door3MustBeClosed := true (D3S)");
                } else {
                    Log.d(TAG, "userIs3S := true");
                }
            } else {                    // 1 or 2
                door3MustBeClosed = true; // if user is 1S, 2S, or D2S
                Log.d(TAG, "door3MustBeClosed := true (1S, 2S, or D2S)");
            }
        } else {                        // no-shopping.

            if ("refreshAndNoShopping".equalsIgnoreCase(source)) {

                Log.d(TAG, "No shopping, but door #3 opened by customer in payment page Leave store button. " +
                        "door3MustBeClosed := false");
                door3MustBeClosed = false;

            } else if (BillingConfig.SE_NOT_TO_OPEN_DOOR3_IF_NO_SHOPPING) {

                Log.d(TAG, "No shopping door #3 must be closed. door3MustBeClosed := true");
                door3MustBeClosed = true;
            }
        }

        Log.d(TAG, "door3MustBeClosed = " + door3MustBeClosed);

        //
        // Update refreshFrom.
        //

        billSub.refreshFrom = source;
        Log.d(TAG, "refreshItems() refreshFrom := " + billSub.refreshFrom);

        //
        // If the door #3 is closed, don't close the door again, just recognize
        // items with RFID reader.
        //

        if (door3MustBeClosed) {
            Log.d(TAG, "call recognizeItemsCallBack()");
            recognizeItemsCallBack("Billing"); // recognize items with RFID reader.
        }

        //
        // If NOT_TO_OPEN_DOOR3_IF_NO_SHOPPING is true,
        // refresh from "noShopping" needs to recognize items.
        //

        if (BillingConfig.SE_NOT_TO_OPEN_DOOR3_IF_NO_SHOPPING) {
            if ("noShopping".equalsIgnoreCase(source)) {
                Log.d(TAG, "call recognizeItemsCallBack()");
                recognizeItemsCallBack("Billing"); // recognize items with RFID reader.
            }
        }

        //
        // Update GUI for refreshing.
        //

        Message msg = new Message();
        Bundle bundle = new Bundle();
        if (source.equals("recheck")) {
            bundle.putString("source", "noShopping");
        } else {
            bundle.putString("source", source);
        }

        msg.setData(bundle);
        msg.what = BillingViewable.REFRESHISPRESSED;
        if (source.equals("recheck")) {
            Log.d(TAG, "send REFRESHISPRESSED, 3000");
            mDefaultHandler.sendMessageDelayed(msg, 3000);
        } else {
            Log.d(TAG, "send REFRESHISPRESSED, 0");
            mDefaultHandler.sendMessageDelayed(msg, 0);
        }

        //
        // If the door #3 is opened, send BILL_REFRESHED event to try to close door #3. 
        // When the door #3 is closed, the state is changed 010 and then start to RFID reader.
        //
        // BUGBUG. How to solve that the one-step payment user taps refresh
        // when the door3 is opening?
        //

        //
        // Always send BILL_REFRESHED.
        // close door#3 -> enter state010 -> recognizeItemsCallBack("state010") -> startReader
        //

        Log.d(TAG, "send BILL_REFRESHED");
        exService.execute(new Runnable() {
            @Override
            public void run() {
                Util.dispatchEvent(se, StateEvent.BILL_REFRESHED); // send BILL_REFRESHED event
            }
        });
    }

    private void genBillSuccess() {
        dumpBillSub("genBillSuccess()");
        Log.d(TAG, "send RECOGNITIONYOU");
        mDefaultHandler.sendEmptyMessage(BillingViewable.RECOGNITIONYOU);
    }

    //
    // The method handles bill as below.
    //      1. Determine if open door #3.
    //      2. Request charge for POS machine
    //      3. Check QR code payment period.
    //

    private void handleBill(Bill amazingBill) {
        dumpBillSub("handleBill()");

        billSub.payType = PayType.DEFAULT;
        Log.d(TAG, "payType := " + billSub.payType);

        if (null != amazingBill) {

            //
            // 1. If User :=[123],[D2],[3S],[D3] we need open door #3.
            //    Else don not open door #3.
            //
            // Tips: user [123] was handled.
            //

            boolean needOpenDoor3 = false;
            if (billSub.isShopping()) {
                // shopping is true
                if (billSub.isDebt()) {
                    needOpenDoor3 = false;    // debt don't open door #3
                } else {
                    //
                    // no debt
                    // open door #3 by user state
                    //
                    if (billSub.isRealOneStepPayment()) {
                        needOpenDoor3 = true;  // Real one-step payment open door #3 : 3S
                    } else {
                        needOpenDoor3 = false; // Real on-step payment is false,don't open door #3
                    }
                }
            } else {
                // no shopping  open door #3 : D[23]
                if (billSub.isDebt()) {
                    needOpenDoor3 = true;
                } else {  // [123]
                    // Don't need to handle it that was handled.
                    needOpenDoor3 = true;
                }
            }


            //
            // If the SE_NOT_TO_OPEN_DOOR3_IF_NO_SHOPPING is true.
            // the user is D3 that we need not to open door3.
            // because the user is no shopping.
            //

            if (BillingConfig.SE_NOT_TO_OPEN_DOOR3_IF_NO_SHOPPING) {
                if (!billSub.isShopping()) {
                    needOpenDoor3 = false;
                }
            }

            if (needOpenDoor3) {

                openDoor3();
            }

            //
            // 2. Request charge for POS machine if EC_ENABLE is true.
            //

            if (BillingConfig.EC_ENABLE) {
                Log.d(TAG, "request charge");
                employeeCardPayment.requestCharge(amazingBill.getData().getAmount());
            }

            //
            // 3. Check QR code payment per 3 seconds.
            //

            Log.d(TAG, "send PAYMENTRESULT, 3000");
            mDefaultHandler.sendEmptyMessageDelayed(BillingViewable.PAYMENTRESULT, 3000); // start polling when handleBill()
        }
    }

    synchronized
    public void isBillChecked() {
        dumpBillSub("isBillChecked()");
        if (billSub.billData == null) {
            Log.d(TAG, "isBillChecked() Error. billData is null.");
            return;
        }
        billingClient.isChecked(billSub.billData.getOrderId());
    }

    private void paymentSuccess() {
        Log.d(TAG, "paymentSuccess set needTracking true");
        needTracking = true;
        dumpBillSub("paymentSuccess()");

        Log.d(TAG, "send SEEYOU");
        mDefaultHandler.sendEmptyMessage(BillingViewable.SEEYOU);

        billSub.customerInfo = null;// clear customer info when show SEEYOU. BUGBUG it should be managed by Billing
        Log.d(TAG, "paymentSuccess() customerInfo := null");
    }

    private void noShopping() {
        dumpBillSub("noShopping()");

        Log.d(TAG, "send NO_SHOPPING");
        mDefaultHandler.sendEmptyMessage(BillingViewable.NO_SHOPPING);

        //
        // Here. don't set info null because if a customer refreshes it when no-shopping.
        // (info = null)
        //

    }

    private void openDoor3() {
        dumpBillSub("openDoor3()", true);

        exService.execute(new Runnable() {
            @Override
            public void run() {
                Util.dispatchEvent(se, StateEvent.BILL_VERIFIED);
            }
        });
    }

    //
    // Open door #3 from Farewell Fragment's button openDoor3AndLeaveStore.
    //

    public void openDoor3(int rId) {
        dumpBillSub("openDoor3()", true);

        exService.execute(new Runnable() {
            @Override
            public void run() {
                Util.dispatchEvent(se, StateEvent.BILL_VERIFIED);
            }
        });
    }

    //
    // Start the timer of face detection.
    //

    public void startTimerOfFaceDetection() {

        Log.d(TAG, "startTimerOfFaceDetection() -- timer.cancel() before timer.start()");
        timer.cancel();

        dumpBillSub("startTimerOfFaceDetection()");

        billSub.theFirstTimeGetFaceId = true;
        Log.d(TAG, "theFirstTimeGetFaceId := true");
        timer.start();
    }

    //
    // Cancel the timer of face detection.
    //

    private void cancelTimerOfFaceDetection() {
        dumpBillSub("cancelTimerOfFaceDetection()");

        if (timer != null) {
            Log.d(TAG, "timer.cancel()");
            timer.cancel();
        }
    }

    //
    // GUI calls it.
    //

    public void clearFlag() {
        dumpBillSub("clearFlag()");
        billSub.theFirstTimeGetFaceId = false;
        dumpBillSub("clearFlag()", "theFirstTimeGetFaceId := " + billSub.theFirstTimeGetFaceId);
    }

    public void closeAndClear() {
        dumpBillSub("closeAndClear()");

        if (se != null) {
            se.stop();
        }

        if (remoteController != null) {
            remoteController.closeSocket();
            remoteController.interrupt();
        }

        if (employeeCardPayment != null) {
            employeeCardPayment.close();
        }

        billingClient.stopReportStatus();
    }

    public void sendErrorMsg(StateCode breakdown) {
        Log.d(TAG, "sendErrorMsg(): errorCode = " + breakdown.getCode());

        if (breakdown != Breakdown.EMERGENCY_CLOSE_STORE) {

            if (breakdown.getCode() != RecoverCode.RECOVER_CODE.getCode()) {

                ReportStatus status = new ReportStatus();
                status.setDeviceId("BillingApp");
                status.setErrorCode(breakdown.getCode());

                if (se.isOccupied) {
                    status.setErrorMessage(breakdown.getDescribe() + ", OCCUPIED");
                } else {
                    status.setErrorMessage(breakdown.getDescribe() + ", NOT OCCUPIED");
                }

                billingClient.fillStatus(status);

                billingClient.reportDeviceStatus();
            }

        }

        Message msg = new Message();
        msg.what = BillingViewable.MALFUNCTION;
        msg.arg1 = breakdown.getCode();
        if (mDefaultHandler != null) {
            Log.d(TAG, "call sendMessage()");
            mDefaultHandler.sendMessage(msg);
        }
    }

    @SuppressLint("DefaultLocale")
    private void dumpBillSub(String pos, String cmt, boolean report) {

        String text;

        if (cmt != null) {
            text = String.format(
                    "%s Num %d: %s",
                    pos,
                    billSub.number,
                    cmt);
            Log.d(TAG, "dumpBillSub(): " + text);
        }

        text = String.format(
                "%s Num %d: s = %s, f = %s, fid = %s, i = %s, u = %s, p = %s, r = %s\n",
                pos,
                billSub.number,
                billSub.billState.toString(),
                billSub.theFirstTimeGetFaceId,
                billSub.faceId,
                billSub.mItemsRecognized,
                billSub.mUserIdentified,
                billSub.payType,
                billSub.refreshFrom);

        Log.d(TAG, "dumpBillSub(): " + text);
        if (report) {
            if (billingClient != null) {
                billingClient.fillLog(TAG, text);
            }
        }
    }

    private void dumpBillSub(String pos, String cmt) {

        dumpBillSub(pos, cmt, false);
    }

    private void dumpBillSub(String pos) {
        dumpBillSub(pos, null);
    }

    private void dumpBillSub(String pos, boolean report) {
        dumpBillSub(pos, null, report);
    }

    //
    // Implement BillingProcess
    //

    //
    // MSG_READ_STOPPED
    //      tidListReadCallBack()
    //      generateBillCallBack("RecognizeItems");
    //
    // getCustomerInfoCallBack()
    //      generateBillCallBack("IdentifyUser")
    //

    @Override
    synchronized
    public void generateBillCallBack(String from) {
        Log.d(TAG, "generateBillCallBack() from = " + from);
        dumpBillSub("generateBillCallBack()");

        //
        // Wait the processes of detecting face and scanning rfid.
        // BUGBUG. billSub is still not locked.
        //

        if (from.equals("RecognizeItems")) {
            billSub.mItemsRecognized = true;
            Log.d(TAG, "generateBillCallBack() mItemsRecognized := " + billSub.mItemsRecognized);
        }

        if (from.equals("IdentifyUser")) {
            billSub.mUserIdentified = true;
            Log.d(TAG, "generateBillCallBack() mUserIdentified := " + billSub.mUserIdentified);
        }

        dumpBillSub("generateBillCallBack()");

        if (!billSub.mUserIdentified) {
            Log.d(TAG, "generateBillCallBack() return : customer info have not got.");
            return;
        }

//        if (!billSub.mItemsRecognized) {
//            Log.d(TAG, "generateBillCallBack() return : reader is running...");
//            return;
//        }

        billSub.billState = BillState.RECOGNIZED;
        dumpBillSub("generateBillCallBack()", "billState := " + billSub.billState);

        //
        // Here the RecognizeItems and IdentifyUser are finished.
        //

        if (billSub.customerInfo != null) {
            try {

                //
                // Don't clear the flags otherwise the bug happens:
                // The GUI may hang when the 3S user taps NotMe and Refresh.
                //
                // billSub.mItemsRecognized = false;
                // Log.d(TAG, "generateBillCallBack() mItemsRecognized := " + billSub.mItemsRecognized);
                // billSub.mUserIdentified = false;
                // Log.d(TAG, "generateBillCallBack() mUserIdentified := " + billSub.mUserIdentified);
                //

                if (billSub.aTidList == null) {
                    Log.d(TAG, "generateBillCallBack() Error. aTidList is null.");
                } else if (from.equals("RecognizeItemsIng")) {
                    billingClient.genBill(billSub.aTidList.toString(), billSub.customerInfo.getCustomerId(), "query");
                } else if (from.equals("RecognizeItems")) {
                    billingClient.genBill(billSub.aTidList.toString(), billSub.customerInfo.getCustomerId(), "create");
                }

            } catch (NullPointerException e) {
                sendErrorMsg(Breakdown.TRY_GEN_BILL_NPE);
            }
        } else {
            Log.d(TAG, "generateBillCallBack() Error. customerInfo is null.");
        }
    }

    @Override
    public void enableBillCallBack() {
    }

    @Override
    public void disableBillCallBack() {
    }

    //
    // identifyUserCallBack()
    //      send TORECOGNITION
    //          startTimerOfFaceDetection()      
    //

    @Override
    public void identifyUserCallBack(int code) {
        dumpBillSub("identifyUserCallBack()");

        billSub.billState = BillState.RECOGNIZING; // in identifyUserCallBack()
        Log.d(TAG, "billState := " + billSub.billState);

        //
        // Detect a face to get customerInfo.
        //

        Message msg = new Message();
        msg.what = BillingViewable.TORECOGNITION;
        msg.arg1 = BillingViewable.DETECTZONEOCCUPIED;
        msg.arg2 = code;
        Log.d(TAG, "send TORECOGNITION, DETECTZONEOCCUPIED");
        mDefaultHandler.sendMessage(msg);
    }

    //
    // caller
    //      Billing
    //      State010
    //
    // refreshOrder()
    //      refreshItems()
    //          recognizeItemsCallBack()    
    //
    // State010
    //      recognizeItemsCallBack()
    //

    @Override
    public void recognizeItemsCallBack(String caller) {
        dumpBillSub("recognizeItemsCallBack()");
        Log.d(TAG, "caller = " + caller);

        //
        // Need not to change bill state in this case:
        //      1.User is 3S and click notMe button
        //      2.Bill state is RECOGNIZED
        //      3.The caller is State010 (means door#3/door#2 closed, not occ)
        //

        if (caller.equalsIgnoreCase("State010")
                && billSub.billState == BillState.RECOGNIZED
                && billSub.refreshFrom.equalsIgnoreCase("notMe")) {
            Log.d(TAG, "recognizeItemsCallBack(): billSub refresh from is notMe and bill state is RECOGNIZED, " +
                    "so doing nothing when door #3 closed.");
        } else {

            billSub.billState = BillState.RECOGNIZING; // in recognizeItemsCallBack()
        }
        Log.d(TAG, "billState := " + billSub.billState);

        //
        // If entering State010, delay to start RFID reader.
        //

        if (caller.equals("State010")) {
            try {
                Thread.sleep(BillingConfig.RR_START_DELAY);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        //
        // Read EPCs and generate BuyItem List.
        //

        if (billSub.refreshFrom.equals("notMe")) {
            Log.d(TAG, "recognizeItemsCallBack() Error. refreshFrom should not be notMe.");

        } else {
            if (this.rfidReader != null) {
                for (int i = 1; i <= 3; i++) {

                    if (this.rfidReader.readTidList(BillingConfig.RR_START_DELAY, BillingConfig.RR_READ_DURATION)) {
                        dumpBillSub("startReader()", true);
                        break;
                    }

                    Log.d(TAG, "read tid list fail, fail count := " + i);

                    Util.delay(100);

                }
            }
        }

        billSub.refreshFrom = "";
        dumpBillSub("recognizeItemsCallBack()", "clear refreshFrom");

    }

    @Override
    public void clearBillCallBack() {
        dumpBillSub("clearBillCallBack()");

        //
        // Switch to default GUI.
        //

        Log.d(TAG, "send TODEFAULT");
        mDefaultHandler.sendEmptyMessage(BillingViewable.TODEFAULT);

        Log.d(TAG, "clearBillCallBack set needTracking false");
        needTracking = false;

        //
        // stop polling when clearBillCallBack()
        //

        Log.d(TAG, "remove PAYMENTRESULT");
        mDefaultHandler.removeMessages(BillingViewable.PAYMENTRESULT);

        //
        // Clear billSub and increase the number.
        //

        billSub.clear();
        dumpBillSub("clearBillCallBack()");

        //
        // Cancel the timer of face detection.
        //

        cancelTimerOfFaceDetection();

        //
        // Stop RFID reading.
        //

        if (this.rfidReader != null) {

            //
            // Add try catch for RFID rfidReader.terminateToRead() throws NullPointException.
            // Fixed bug 129: RFID SDK NullPointException.
            //

            try {
                dumpBillSub("stopReader() -> other case", true);
                this.rfidReader.terminateToRead();
            } catch (Exception e) {

                Log.d(TAG, "rfidReader.terminateToRead() throws exception.");
                e.printStackTrace();
            }
        }

        //
        // If employee card is enabled, cancel the request to POS bridge.
        //

        if (BillingConfig.EC_ENABLE) {
            Log.d(TAG, "clearBillCallBack() call sendCancelRequest()");
            employeeCardPayment.sendCancelRequest();
        }
    }

    private int TIDS_NOT_CHANGE_COUNT = 0;
    private final int TIDS_NOT_CHANGE_COUNT_MAX = 3;

    @Override
    synchronized
    public void tidListReadCallBack(JSONObject tidList) {
        dumpBillSub("tidListReadCallBack()");

        try {

            //
            // find new tids add all in
            //

            if (billSub.aTidList != null && billSub.aTidList.length() > 0) {
                JSONArray tempTids = tidList.getJSONArray("tidList");
                for (int i = 0; i < tempTids.length(); i++) {

                    String tid = tempTids.getString(i);

                    boolean addNewTids = false;

                    for (int j = 0; j < billSub.aTidList.length(); j++) {
                        if (tid.equals(billSub.aTidList.getString(j))) {
                            break;
                        }
                        if (j == billSub.aTidList.length() - 1) {
                            addNewTids = true;
                        }
                    }
                    if (addNewTids) {
                        billSub.aTidList.put(tid);
                    }
                }
            } else {
                billSub.aTidList = tidList.getJSONArray("tidList");
            }

            Log.d(TAG, "tidListReadCallBack() tidList = " + billSub.aTidList.toString()
                    + " Current length = " + billSub.aTidList.length()
                    + " Old length = " + beforeTidsCount
            );

            if (beforeTidsCount != billSub.aTidList.length()) {
                generateBillCallBack("RecognizeItemsIng");

                beforeTidsCount = billSub.aTidList.length();
            } else {
                TIDS_NOT_CHANGE_COUNT += 1;
                if (TIDS_NOT_CHANGE_COUNT > TIDS_NOT_CHANGE_COUNT_MAX) {
                    generateBillCallBack("RecognizeItemsIng");
                    TIDS_NOT_CHANGE_COUNT += 0;
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void faceDetectedCallBack(String faceId) {
    }

    @Override
    public void faceDetectedErrorCallBack(int errCode) {
        dumpBillSub("FaceDetection: FaceRecogListenError() : errCode = " + errCode, true);
    }

    //
    // getCustomerInfoCallBack()
    //      generateBillCallBack()
    //

    @Override
    synchronized
    public void getCustomerInfoCallBack(String customerInfo) {
        dumpBillSub("getCustomerInfoCallBack()");

        try {

            Log.d(TAG, "getCustomerInfoCallBack() customerInfo = " + customerInfo);

            Type type = new TypeToken<CustomerInfo>() {
            }.getType();
            CustomerInfo info = new Gson().fromJson(customerInfo, type);

            Log.d(TAG, "info = " + info.toString());

            if (info.getData().getInfo() == null) { // avoid to crash if having face id but no customer id. BUGBUG. infinite loop.
                Log.d(TAG, "getCustomerInfoCallBack() Error. getInfo() is null.");

            } else {

                billSub.customerInfo = info.getData().getInfo();

                //mDefaultHandler.sendEmptyMessage(MainActivity.PORTRAITISREADY);
                //billSub.mUserIdentified = true; // BUGBUG. It is not necessary.
                //Log.d(TAG, "getCustomerInfoCallBack() mUserIdentified := " + billSub.mUserIdentified);
                generateBillCallBack("IdentifyUser");
            }

            //
            // error code 40004 ===> faceId not found in server DB.
            // so, we think the man is a visitor.
            //

            if (info.getStatus().equals("failure") && info.getCode() != 40004) {
                sendErrorMsg(Breakdown.CUSTOMER_STATUS_FAILURE);
            }

            //
            // We always get customer ID.
            // 0: correct customer ID.
            // 40004: default customer ID for wrong.
            //

            //
            // BUBBUG. Abnormal event handling.
            //
        } catch (NullPointerException e) {

            Log.d(TAG, "genCustomerInfoCallback : " + e.toString());
            sendErrorMsg(Breakdown.GET_CUSTOMER_INFO_CB_NPE);

        } catch (JsonSyntaxException e) {

            Log.d(TAG, "genCustomerInfoCallback : " + e.getMessage());
            sendErrorMsg(Breakdown.ILLEGAL_STATE_EXCEPTION);

        }
    }

    @Override
    public void billGeneratedCallBack(String bill) {
        dumpBillSub("billGeneratedCallBack()");

        if (BillingConfig.FD_STOP_CAMERA_ONLY_FACE_DETECTED) {
            Log.d(TAG, "send STOPCAMERA");
            mDefaultHandler.sendEmptyMessage(BillingViewable.STOPCAMERA);
        }

        //
        // We suppose that billState must be RECOGNIZED. If not, return the function.
        //

        if (billSub.billState != BillState.RECOGNIZED) { // in billGeneratedCallBack()
            Log.d(TAG, "billGeneratedCallBack() Error. billState is wrong");
            return;
        }

        //
        // Here the billState must be RECOGNIZED
        //

        try {

            if (bill.isEmpty()) {
                Log.d(TAG, "Bill is empty.");
                sendErrorMsg(Breakdown.BILL_EMPTY);
                return;
            }

            Type type = new TypeToken<Bill>() {
            }.getType();
            Bill realBill = new Gson().fromJson(bill, type);

            Log.d(TAG, "billGeneratedCallBack(): " + realBill.toString());

            //
            // BUGBUG. Should report the exception.
            // It happens when we cannot get a customer ID by a face ID.
            //

            if (!realBill.getStatus().equals("success")) {
                Log.d(TAG, "Bill callback status : " + realBill.getStatus());
                sendErrorMsg(Breakdown.BILL_NOT_SUCCESS);
                return;
            }

            if (realBill.getCode() != 0) {
                Log.d(TAG, "Bill callback code : " + realBill.getCode());
                sendErrorMsg(Breakdown.BILL_NOT_ZERO);
                return;
            }

            if (realBill.getData() == null) {
                Log.d(TAG, "Bill missing data, Bill callback status : " + realBill.getStatus());
                sendErrorMsg(Breakdown.BILL_NOT_SUCCESS);
                return;
            }

            //billSub.isValidBill = true;
            billSub.billState = BillState.GENERATED;
            dumpBillSub("billGeneratedCallback()", "billState := " + billSub.billState);
            billSub.billData = realBill.getData();
            Log.d(
                    TAG,
                    "billGeneratedCallback() isShopping = " + billSub.isShopping() +
                            ", isOneStepPayment = " + billSub.isOneStepPayment() +
                            ", oneStepPaymentFalseCode = " + billSub.getCustomerStatus().getOneStepPaymentFalseCode() +
                            ", isDebt = " + billSub.isDebt());

            //
            // Add check for Bill.
            //  -- orderId
            //  -- amount
            //  -- itemList
            //  -- customerStatus
            //

            if (billSub.isShopping() && !realBill.getData().isUseable()) {
                Log.d(TAG, "Is shopping, Bill is not right:");
                sendErrorMsg(Breakdown.BILL_NOT_SUCCESS);
                return;
            }

            //
            // We need customer status to judge the customer is debt, isShopping or not.
            // If the user is [123], switch to SEEYOU and open door #3.
            //

            if (!billSub.isDebt() && !billSub.isShopping()) {

                Log.d(TAG, "billGeneratedCallBack(), maybe the commodities are sold before, or no shopping and no debt.");
                billSub.billState = BillState.CHECKED; // in billGeneratedCallBack(): the user is [123] (no shopping and no debt)
                dumpBillSub("billGeneratedCallback()", "billState := " + billSub.billState);
                noShopping();           // switch to SEEYOU GUI
                if (!BillingConfig.SE_NOT_TO_OPEN_DOOR3_IF_NO_SHOPPING) {
                    openDoor3();           // open door #3.
                }

                return;
            }

            //
            // Here the user is 3S, D3S, D?2S, 1S, or D[23]
            //

            genBillSuccess();           // switch to a page to display the bill

            //
            // if bill qrcode is empty, means the customer have not click the confirm to gen-bill with "type = create"
            //

            if (realBill.getData().getQrCodeText() != null && !realBill.getData().getQrCodeText().isEmpty()) {
                handleBill(realBill);
            }
            dumpBillSub("billGeneratedCallback()");

        } catch (NullPointerException e) {

            Log.d(TAG, "billGeneratedCallBack : " + e.toString());
            sendErrorMsg(Breakdown.GEN_BILL_CB_NPE);
        } catch (JsonSyntaxException e) {
            Log.d(TAG, "genCustomerInfoCallback : " + e.toString());
            sendErrorMsg(Breakdown.ILLEGAL_STATE_EXCEPTION);
        }
    }

    @Override
    public void billCheckedCallBack(String result) {
        dumpBillSub("billCheckedCallBack()");
        Log.d(TAG, "result = " + result);

        //
        // We suppose that billState must be GENERATED. If not, log the error and return it.
        //

        if (billSub.billState != BillState.GENERATED) { // in billCheckedCallBack()
            Log.d(TAG, "billCheckedCallBack() Error. billState is wrong");
            Log.d(TAG, "billCheckedCallBack() return");
            return;
        }

        //
        // Here the billState must be RECOGNIZED
        //

        try {
            JSONObject object = new JSONObject(result);
            int code = (int) object.get("code");
            Log.d(TAG, "billCheckedCallBack() code = " + code);

            if (BillingConfig.TE_FORCE_QR_CODE_PAY_FAIL && code == 0) {
                code = 40107;
            }

            if (code == 0) {

                Log.d(TAG, "remove PAYMENTRESULT");
                mDefaultHandler.removeMessages(BillingViewable.PAYMENTRESULT); // stop polling when QR code is paid successfully.

                billSub.billState = BillState.CHECKED; // in billCheckedCallBack(): pay with QR code.
                dumpBillSub("billCheckedCallBack()", "billState := " + billSub.billState);

                if (BillingConfig.EC_ENABLE) {
                    Log.d(TAG, "billCheckedCallBack() call sendCancelRequest()");
                    employeeCardPayment.sendCancelRequest();
                }

                //
                // If the bill was paid with QR code or card,
                //

                //if (billSub.payType != PayType.ONE_STEP_PAYMENT) {

                billSub.payType = PayType.QR_CODE;
                dumpBillSub("billCheckedCallBack()", "payType := " + billSub.payType);

                paymentSuccess();   // payment is success.

                openDoor3(); // open door #3

                //
                // If one-step payment , it is no need turn to FarewellFragment
                // and open door#3 because door#3 is opened before.
                //

                //}

            } else if (code == 40106) {

                Log.d(TAG, "send PAYMENTRESULT, 1000");
                mDefaultHandler.sendEmptyMessageDelayed(BillingViewable.PAYMENTRESULT, 1000); // start polling when 40106. BUGBUG why 1000?

            } else if (code == 40107) {

                Log.d(TAG, "send SEEYOU");
                mDefaultHandler.sendEmptyMessage(BillingViewable.SEEYOU);

                //
                // Remove the message PAYMENTRESULT that is enabled by handleBill().
                //
                // (Stop to polling is-checked otherwise the door #3 will be opened
                // when TE_FORCE_QR_CODE_PAY_FAIL is enabled because the item was really
                // checked before.)
                //

                Log.d(TAG, "remove PAYMENTRESULT");
                mDefaultHandler.removeMessages(BillingViewable.PAYMENTRESULT); // stop polling when 40107

                //payment failure
                billSub.billState = BillState.GENERATED;
                dumpBillSub("billCheckedCallBack()", "billState := " + billSub.billState);
            }

        } catch (JSONException e) {

            e.printStackTrace();

        } catch (NullPointerException e) { // for object.

            Log.d(TAG, "billCheckedCallBack() e = " + e.toString());
            sendErrorMsg(Breakdown.IS_CHECKED_CB_NPE);
        }
    }

    @Override
    public void exitDoorNum3CallBack(boolean tracking) {
        dumpBillSub("exitDoorNum3CallBack()");

        //
        // Report one customer exits.
        //

        if (tracking) {
            Log.d(TAG, "send tracking, from state110");
            billingClient.customerTracking(1); // 1 means exiting the store.
        } else if (needTracking) {
            Log.d(TAG, "send tracking, needTracking is true");
            billingClient.customerTracking(1); // 1 means exiting the store.
        }
        Log.d(TAG, "exitDoorNum3CallBack set needTracking false");
        needTracking = false;

        //
        // Switch to default GUI.
        //        

        Log.d(TAG, "send TODEFAULT");
        mDefaultHandler.sendEmptyMessage(BillingViewable.TODEFAULT);

        //
        // stop polling when clearBillCallBack()
        //        

        Log.d(TAG, "remove PAYMENTRESULT");
        mDefaultHandler.removeMessages(BillingViewable.PAYMENTRESULT); // stop polling when exitDoorNum3CallBack()

        //
        // Cancel the timer of face detection.
        //
        // Need to cancel detecting face when the user stands up and sits down in the zone.
        // 010 ---> 000
        //  

        cancelTimerOfFaceDetection();

        //
        // Stop RFID reading.
        //
        // Need to stop RFID reading when the user stands up and sits down in the zone.
        // 010 ---> 000        
        //

        if (this.rfidReader != null) {

            //
            // Add try catch for RFID rfidReader.terminateToRead() throws NullPointException.
            // Fixed bug 129: RFID SDK NullPointException.
            //

            try {
                this.rfidReader.terminateToRead();
            } catch (Exception e) {

                Log.d(TAG, "rfidReader.terminateToRead() throws exception.");
                e.printStackTrace();
            }
        }

        //
        // If employee card is enabled, cancel the request to POS bridge.
        //

        if (BillingConfig.EC_ENABLE) {
            Log.d(TAG, "exitDoorNum3CallBack() call sendCancelRequest()");
            employeeCardPayment.sendCancelRequest();
        }

        boolean needDeduct = false;

        //
        // If the user is 3S, deduct.
        //

        Log.d(TAG, "If 3S");
        if (billSub.isShopping() && billSub.isRealOneStepPayment()) {

            Log.d(TAG, "User is 3S");
            if (billSub.billState == BillState.GENERATED) {
                needDeduct = true;
                Log.d(TAG, "User is 3S need deduct");
            }
        }

        //
        // If the user is D3, deduct.
        //

        Log.d(TAG, "If D3");
        if (!billSub.isShopping() && billSub.isRealOneStepPayment() && billSub.isDebt()) {

            Log.d(TAG, "User is D3");
            if (billSub.billState == BillState.GENERATED) {
                needDeduct = true;
                Log.d(TAG, "User is D3 need deduct");
            }
        }

        Log.d(TAG, "needDeduct = " + needDeduct);
        if (needDeduct) {

            synchronized (billSub) {
                if (billSub.billData == null) {
                    Log.d(TAG, "exitDoorNum3CallBack() Error. billData is null.");
                } else {
                    billingClient.customerExit(billSub.billData); // call customerExit API to deduct.
                }
            }

            billSub.payType = PayType.ONE_STEP_PAYMENT;
            Log.d(TAG, "payType := " + billSub.payType);
            billSub.billState = BillState.CLEARED; // in exitDoorNum3CallBack()
            Log.d(TAG, "billState := " + billSub.billState);
        }

        //
        // Clear billSub and increase the number.
        //
        // Fix the bug that GUI hagns on preview if the user stand up / sit down
        // in zone. (101809z2.log)        
        //        

        billSub.clear();
        dumpBillSub("exitDoorNum3CallBack()");
    }

    @Override
    public void customerExitCallback(String result) {
        dumpBillSub("customerExitCallback()");
        Log.d(TAG, "customerExitCallback() result = " + result);
    }

    @Override
    public void recognizeFaceCallback(int faceId) {
        billSub.faceId = String.valueOf(faceId);
        dumpBillSub("recognizeFaceCallback()", "faceId := " + billSub.faceId);

        if (billSub.theFirstTimeGetFaceId) {

            billSub.theFirstTimeGetFaceId = false;
            Log.d(TAG, "theFirstTimeGetFaceId := " + billSub.theFirstTimeGetFaceId);

            //billSub.mUserIdentified = false;
            //Log.d(TAG, "mUserIdentified := " + billSub.mUserIdentified);

            getCustomerInfo(billSub.faceId);

            timer.cancel();
            Log.d(TAG, "recognizeFaceCb, timer.cancel()");
        }
    }

    @Override
    public void remoteControlCallBack(String event) {
        Log.d(TAG, "remoteControlCallBack:" + event);

        //
        // Billing App only handles ACQUIRE_CONTROL and RELEASE_CONTROL.
        //

        if (event.equals(NowGoEvent.ACQUIRE_CONTROL.toString())) {

            exService.execute(new Runnable() {
                @Override
                public void run() {
                    Util.dispatchEvent(se, StateEvent.CONTROL_ACQUIRED);
                }
            });

        } else if (event.equals(NowGoEvent.RELEASE_CONTROL.toString())) {

            exService.execute(new Runnable() {
                @Override
                public void run() {
                    Util.dispatchEvent(se, StateEvent.CONTROL_RELEARSED);
                }
            });

        } else if (event.equals(NowGoEvent.OPEN_STORE.toString())) {
            Log.d(TAG, "Everything is ok now.");

        } else if (event.equals(NowGoEvent.EMERGENCY_CLOSE_STORE.toString())) {
            Log.d(TAG, "WARNING!WARNING!WARNING!CLOSE STORE!");
            sendErrorMsg(Breakdown.EMERGENCY_CLOSE_STORE);

        } else if (event.equals(NowGoEvent.STORE_BREAKDOWN.toString())) {
            Log.d(TAG, "WARNING!WARNING!WARNING!BREAKDOWN!");

        }

    }

    @Override
    public void stateCodeCallBack(StateCode stateCode) {

        Log.d(TAG, "stateCodeCallBack().stateCode = " + stateCode.getCode());
        sendErrorMsg(stateCode);
    }

    @Override
    public void invalidStateCallBack(String state) {
        Log.d(TAG, "invalidStateCallBack() ---> received state=" + state);
        billingClient.fillLog(TAG, "invalidStateCallBack() state=" + state);
    }

    @Override
    public void invalidEventCallBack(String event) {
        Log.d(TAG, "invalidEventCallBack() ---> received event=" + event);
        billingClient.fillLog(TAG, "invalidEventCallBack() event=" + event);
    }

    @Override
    public void abnormalCallBack(String stateId, String event) {
        Log.d(TAG, "abnormalCallBack() --->");
        billingClient.fillLog(TAG, "abnormalCallBack() stateId = " + stateId + " event = " + event);
    }

    @Override
    public void chargeResponseCallBack(int returnCode, String cardNumber, int remainAmount) {
        dumpBillSub("chargeResponseCallBack()");

        Log.d(TAG, "returnCode = " + returnCode +
                ", remainAmount = " + remainAmount);

        //
        // We suppose that billState must be RECOGNIZED. If not, log the error.
        //

        if (billSub.billState != BillState.RECOGNIZED) { // in chargeResponseCallBack().
            Log.d(TAG, "chargeResponseCallBack() Error. billState is wrong");
        }

        //
        // Only for test.
        //

        if (BillingConfig.TE_FORCE_CARD_PAY_SUCCESS && returnCode == PosConfig.CHARGE_FAILURE_REASON_LESS_BALANCE) {
            returnCode = PosConfig.CHARGE_SUCCESS;
        }

        //
        // Here the billState should be RECOGNIZED
        //         

        switch (returnCode) {
            case PosConfig.CHARGE_SUCCESS:
                Log.d(TAG, "Charge success, remain amount is : " + remainAmount + "\n");

                Log.d(TAG, "send CARDPAYSUCCESS");
                mDefaultHandler.sendEmptyMessage(BillingViewable.CARDPAYSUCCESS);

                Log.d(TAG, "remove PAYMENTRESULT");
                mDefaultHandler.removeMessages(BillingViewable.PAYMENTRESULT); // stop polling when card payment is success.

                billSub.payType = PayType.EMPLOYEE_CARD;
                billSub.balance = remainAmount;
                billSub.billState = BillState.CHECKED; // in chargeResponseCallBack(): pay with employee card.
                dumpBillSub("chargeResponseCallBack()", "payType := " + billSub.payType + ", billState := " + billSub.billState);

                paymentSuccess();       // switch to page.

                boolean needOpenDoor3 = false;

                if (!billSub.isShopping()) {
                    // User is 1,2,3,D2, or D3. Door #3 is opening, so we needn't open door #3.
                    needOpenDoor3 = false;
                } else {
                    if (billSub.isRealOneStepPayment() && !billSub.isDebt()) {
                        needOpenDoor3 = false;      // User is 3S
                    } else {
                        needOpenDoor3 = true;       // If user is D3S, D2S, 2S, or 1S. We need to open door #3.
                    }
                }

                if (needOpenDoor3) {
                    openDoor3();       // send billVerified
                }

                synchronized (billSub) {
                    if (billSub.billData == null) {
                        Log.d(TAG, "chargeResponseCallBack() Error. billData is null");
                    } else {
                        billingClient.checkBill(billSub.billData.getOrderId(), 3);
                    }
                }

                break;

            case PosConfig.CHARGE_FAILURE_REASON_BAD_REQUEST_BODY: // POS: params err
            case PosConfig.CHARGE_FAILURE_REASON_DATABASE_ERROR: // POS: sql err
            case PosConfig.CHARGE_FAILURE_REASON_DESTROY_CARD: // destroy card
            case PosConfig.CHARGE_FAILURE_REASON_EXCEED_AUTO_PAY_TIMES:
            case PosConfig.CHARGE_FAILURE_REASON_EXCEED_DAY_LINIT: //
            case PosConfig.CHARGE_FAILURE_REASON_FULL_BALANCE: //
            case PosConfig.CHARGE_FAILURE_REASON_MINUS_BALANCE: //
            case PosConfig.CHARGE_FAILURE_REASON_MISSED_CARD: //
            case PosConfig.CHARGE_FAILURE_REASON_PERMISSION_DENIED_ON_DEVICE: //
            case PosConfig.CHARGE_FAILURE_REASON_PERMISSION_DENIED_TIME_INTERVAL: //
            case PosConfig.CHARGE_FAILURE_REASON_PERMISSION_DENIED_TIME_NOW: //
            case PosConfig.CHARGE_FAILURE_REASON_POS_DEVICE_CONNECT_TIME_OUT: //
            case PosConfig.CHARGE_FAILURE_REASON_RECORD_EXIST: //
            case PosConfig.CHARGE_FAILURE_REASON_UNKNOWN_CARD: //
            case PosConfig.CHARGE_FAILURE_REASON_SWIPE_FAST: //

                cardPayFailure(404);
                break;
            case PosConfig.CHARGE_FAILURE_REASON_OTHER:
                cardPayFailure(19);
                break;

            case PosConfig.CHARGE_FAILURE_REASON_LESS_BALANCE: //
                cardPayFailure(8);
                break;

            case PosConfig.RECHARGE_REQUEST:
                Log.d(TAG, "Recharge request\n");

                if (BillingConfig.EC_ENABLE) {

                    //
                    // BUGBUG. Should not send POSBREAKDOWN when receiving RECHARGE_REQUEST.
                    //
                    // Should call sendCancelRequest() to cancel request
                    // and call requestCharge() to request charge.
                    //
                    // BUGBUG. When POSBREAKDOWN appear, the user stil can pay with card.
                    //

                    Log.d(TAG, "send POSBREAKDOWN");
                    mDefaultHandler.sendEmptyMessage(BillingViewable.POSBREAKDOWN);
                }

                break;

            case PosConfig.CANCEL_CHARGE_RESPONSE_TIME_OUT:
                Log.d(TAG, "Cancel charge request timeout\n");
                break;

            case PosConfig.CANCEL_CHARGE_SUCCESS:
                Log.d(TAG, "Cancel charge success\n");
                break;

            case PosConfig.CANCEL_CHARGE_FAILURE:
                Log.d(TAG, "Cancel charge failure\n");
                break;
        }
    }

    @Override
    public void deviceStatusCallBack(int statusCode) {
        switch (statusCode) {
            case PosConfig.POS_DEVICE_UART_TIMEOUT:
            case PosConfig.POS_DEVICE_SOCKET_IS_TIMEOUT:
            case PosConfig.POS_DEVICE_IS_OFFLINE:
            case PosConfig.POS_Client_SOCKET_IS_NULL:
                Log.d(TAG, "deviceStatusCallBack, statusCode = " + statusCode);

                if (BillingConfig.EC_ENABLE) {
                    Log.d(TAG, "send POSBREAKDOWN");
                    mDefaultHandler.sendEmptyMessage(BillingViewable.POSBREAKDOWN);
                }

                break;
        }
    }

    private boolean mIsReportDevicesStateSuccess = true;

    @Override
    public synchronized void reportDevicesStateCallBack(boolean isReportSuccess) {
        mIsReportDevicesStateSuccess = isReportSuccess;
    }

    synchronized
    private void cardPayFailure(int returnCode) {
        Log.d(TAG, "Charge failure, returnCode = " + returnCode);

        Message msgLess = new Message();
        msgLess.what = BillingViewable.CARDPAYFAILURE;
        msgLess.arg1 = returnCode;
        Log.d(TAG, "send CARDPAYFAILURE");
        mDefaultHandler.sendMessage(msgLess);

        if (billSub.billState == BillState.GENERATED) {
            if (billSub.billData == null) {
                Log.d(TAG, "cardPayFailure() Error. billData is null.");
            } else {
                employeeCardPayment.requestCharge(billSub.billData.getAmount());
            }
        }

    }

    @Override
    public void seUpdateStatus() {

    }

    @Override
    public void seReceiveEvents(String events) {

    }

    @Override
    public void seHandleEvent(String event) {

    }

    @Override
    public void seSendCommand(byte[] cmd) {

    }

    @Override
    public void seCurrentState(String stateId) {
        Log.d("FSM", "");
        String msg = "State: " + stateId + " ----------------";
        Log.d("FSM", msg);
    }

    @Override
    public void seReceiveEvent(String event) {
        String msg = "Event: " + event;
        Log.d("FSM", msg);
    }

    @Override
    public void seTask(String task) {
        String msg = "    " + task;
        Log.d("FSM", msg);
    }

    //
    // Inner class for face recognition count down.
    //

    public class MyCountDownTimer extends CountDownTimer {

        MyCountDownTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long l) {

            Log.d(TAG, l + " onTick() billState = " + billSub.billState +
                    ", theFirstTimeGetFaceId = " + billSub.theFirstTimeGetFaceId);
        }

        //
        // Wait 5 seconds to call onFinish if face is not detected or not recognized.
        //

        @Override
        public void onFinish() {
            Log.d(TAG, "onFinish() billState = " + billSub.billState);
            Log.d(TAG, "step.faceDetected");

            billSub.faceId = "-100";
            Log.d(TAG, "onFinish() faceId := " + billSub.faceId);
            billSub.theFirstTimeGetFaceId = false;
            Log.d(TAG, "onFinish() theFirstTimeGetFaceId := " + billSub.theFirstTimeGetFaceId);
            dumpBillSub("onFinish()");
            getCustomerInfo(billSub.faceId);
        }
    }

    public BillSub getBillSub() {
        return billSub;
    }

    public void stopReader(boolean needCallBack) {
        dumpBillSub("stopReader() -> " + (needCallBack ? "confirm order" : "no shopping"), true);

        Log.e("cpb", "Billing: stopReader: 停止读取");
        this.rfidReader.stopReader(needCallBack);

        //
        // reader is running in loop.
        // No shopping ben is clicked and door#3 be opened.
        // Never parse the gen-bill response at the later time.
        //

        if (!needCallBack) {
            billSub.billState = BillState.PENDING;
        }
    }

    //
    // Return reader, smartEquipment,doorControler state server state.
    //

    public synchronized boolean isDevicesCanWork() {
        boolean readerNormal = rfidReader != null && rfidReader.isAlive();
        boolean smartEquipmentNormal = se != null && se.isAlive();
        boolean remoteControllerNormal = remoteController != null && remoteController.isAlive();
        Log.d(TAG, "DEVICES STATE: " + "readerNormal := " + readerNormal);
        Log.d(TAG, "DEVICES STATE: " + "smartEquipmentNormal := " + smartEquipmentNormal);
        Log.d(TAG, "DEVICES STATE: " + "remoteControllerNormal := " + remoteControllerNormal);
        Log.d(TAG, "DEVICES STATE: " + "------------------------------------");
        return readerNormal && smartEquipmentNormal && remoteControllerNormal && mIsReportDevicesStateSuccess;
    }

    //
    // Reset BillingApp.
    //

    public void resetDevices() {
        if (se != null) {
            Log.d(TAG, "Reset doors");
            se.resetDoors();
        }
        Log.d(TAG, "Reset clear callback");
        clearBillCallBack();
    }

    //
    // Recover result call back.
    // - recoverResult : true    recover success
    //

    public void recoverResultCallBack(boolean recoverResult) {

        //
        // recover malfunction success
        //

        if (recoverResult) {

            if (billingClient != null) {
                billingClient.recoverMalfunctionSuccess();
            }
        }

        //
        // recover malfunction fail
        //

        else {
            ;
        }

    }
}
