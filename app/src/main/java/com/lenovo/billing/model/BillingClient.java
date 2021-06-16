package com.lenovo.billing.model;

import android.util.Log;

//
// for report status and log.
//

import com.google.gson.Gson;
import com.lenovo.billing.common.Util;
import com.lenovo.billing.protocol.*;
import com.lenovo.billing.entity.*;

import com.lenovo.billing.protocol.BillingConfig;
import com.lenovo.billing.common.DateUtil;
import com.lenovo.billing.common.OkHttpUtil;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

//
// for report status and log.
//

import java.util.Timer;
import java.util.TimerTask;

public class BillingClient {

    public static final String TAG = BillingClient.class.getSimpleName();

    private BillingProcess bp;

    //
    // for report status and log.
    //

    private ArrayList<ReportStatus> mStatusList = new ArrayList<>();
    private ArrayList<ReportLog> logList = new ArrayList<>();
    private Timer timer;

    public void register(BillingProcess bp) {
        this.bp = bp;
    }

    void getCustomerInfo(String faceId) {
        Log.d(TAG, "getCustomerInfo()");
        //deliver the result to RecognitionFragment,
        //accord to the result decide go to PaymentFragment or continue to recognize

        //
        // When the UI update too fast will be set the faceId "" by presenter.setFaceId("")
        // in fragment.
        // So we confirm the param here.
        //

        if ("".equalsIgnoreCase(faceId.trim())) {
            Log.d(TAG, "getCustomerInfo() the faceId is null, set it '-100'.");
            faceId = "-100";
        }

        String jsonStr = "";
        jsonStr += "{\n";
        jsonStr += "    \"appId\": " + "\"" + BillingConfig.BA_APP_ID + "\"" + ",\n";
        jsonStr += "    \"locationId\": " + BillingConfig.BA_LOCATION_ID + ",\n";
        jsonStr += "    \"data\":{" + "\"faceId\":" + "\"" + faceId + "\"" + "}\n";
        jsonStr += "}\n";

        Log.d(TAG, "jsonStr = " + jsonStr);

        OkHttpUtil.post(BillingConfig.BC_GET_CUSTOMER_INFO_URL,
                new OkHttpUtil.ResultCallback<String>() {
                    @Override
                    public void onSuccess(String response) {
                        Log.d(TAG, "getCustomerInfo(): onSuccess(): " + response);
                        bp.getCustomerInfoCallBack(response);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Log.d(TAG, "getCustomerInfo(): onFailure(): " + e.toString());
                        bp.stateCodeCallBack(Breakdown.GET_CUSTOMER_INFO_F);
                    }
                },
                jsonStr);
    }

    private static long firstTime = 0;
    private static final int durationTime = 600;

    /**
     * 获取订单
     *
     * @param tidList    tid list
     * @param customerId customer id
     * @param type       "query": gen bill without qrcode; "create":gen bill with qrcode
     */
    synchronized
    void genBill(String tidList, String customerId, String type) {
        Log.d(TAG, "genBill()");

        long secondTime = System.currentTimeMillis();
        if (secondTime - firstTime < durationTime) {
            Log.d(TAG, "genBill() too fast, wait " + durationTime + "ms");
            Util.delay(durationTime);
        }

        Log.d(TAG, "genBill(): tidList = " + tidList);

        String jsonStr = "";
        jsonStr += "{\n";
        jsonStr += "    \"tidList\": " + tidList + ",\n";
        jsonStr += "    \"customerId\": " + "\"" + customerId + "\"" + ",\n";
        jsonStr += "    \"type\": " + "\"" + type + "\"" + ",\n";
        jsonStr += "    \"locationId\": " + BillingConfig.BA_LOCATION_ID + "\n";
        jsonStr += "}\n";

        Log.d(TAG, "genBill(): params = " + jsonStr);

        firstTime = System.currentTimeMillis();

        OkHttpUtil.post(BillingConfig.BC_GEN_BILL_URL,
                new OkHttpUtil.ResultCallback<String>() {
                    @Override
                    public void onSuccess(String response) {
                        Log.d(TAG, "genBill(): response \n" + response);
                        bp.billGeneratedCallBack(response);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Log.d(TAG, "genBill:onFailure():" + e.toString());
                        e.printStackTrace();
                        bp.stateCodeCallBack(Breakdown.GEN_BILL_F);
                    }
                },
                jsonStr);
    }

    synchronized void customerExit(Bill.Data billData) {

        if (billData == null) {
            Log.d(TAG, "customerExit() Error. billData is null");
            return;
        }

        OkHttpUtil.Param orderId = new OkHttpUtil
                .Param("orderId", billData.getOrderId());
        OkHttpUtil.Param customerId = new OkHttpUtil
                .Param("customerId", billData.getCustomerStatus().getCustomerId());

        List<OkHttpUtil.Param> params = new ArrayList<>();
        params.add(orderId);
        params.add(customerId);

        OkHttpUtil.post(BillingConfig.BC_CUSTOMER_EXIT_URL,
                new OkHttpUtil.ResultCallback<String>() {
                    @Override
                    public void onSuccess(String response) throws JSONException {
                        Log.d(TAG, "customerExit: " + response);
                        bp.customerExitCallback(response);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Log.d(TAG, "onFailure():" + e.toString());
                        bp.stateCodeCallBack(Breakdown.CUSTOMER_EXIT_F);
                    }
                }, params);
    }

    void customerTracking(int trackingType) {

        String jsonStr = "";
        jsonStr += "{\n";
        jsonStr += "    \"appId\": " + "\"" + BillingConfig.BA_APP_ID + "\"" + ",\n";
        jsonStr += "    \"locationId\": " + "\"" + BillingConfig.BA_LOCATION_ID + "\"" + ",\n";
        jsonStr += "    \"data\":{" + "\"trackingType\":" + trackingType + "}\n";
        jsonStr += "}\n";

        Log.d(TAG, "customerTracking(): jsonStr = " + jsonStr);

        OkHttpUtil.post(BillingConfig.BC_CUSTOMER_TRACKING,
                new OkHttpUtil.ResultCallback<String>() {
                    @Override
                    public void onSuccess(String response) {
                        Log.d(TAG, "customerTracking() : response");
                        Log.d(TAG, response);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Log.d(TAG, "customerTracking:onFailure():" + e.toString());
                        bp.stateCodeCallBack(Breakdown.CUSTOMER_TRACKING_F);
                    }
                },
                jsonStr);
    }

    void isChecked(String orderId) {

        Log.d(TAG, "isChecked()");

        OkHttpUtil.Param pOrderId = new OkHttpUtil.Param("orderId", orderId);

        List<OkHttpUtil.Param> params = new ArrayList<>();
        params.add(pOrderId);

        OkHttpUtil.post(BillingConfig.BC_IS_CHECKED_URL,
                new OkHttpUtil.ResultCallback<String>() {
                    @Override
                    public void onSuccess(String response) throws JSONException {
                        Log.d(TAG, "Bill is checked -> " + response);
                        bp.billCheckedCallBack(response);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Log.d(TAG, "isChecked:onFailure():" + e.toString());
                        bp.stateCodeCallBack(Breakdown.IS_CHECKED_F);
                    }
                }, params);
    }

    void checkBill(String orderId, int paymentType) {

        Log.d(TAG, "checkBill()");

        String jsonStr = "";
        jsonStr += "{\n";
        jsonStr += "    \"orderId\": " + orderId + ",\n";
        jsonStr += "    \"paymentType\": " + paymentType;
        jsonStr += "}\n";

        OkHttpUtil.post(BillingConfig.BC_CHECK_BILL_URL,
                new OkHttpUtil.ResultCallback<String>() {
                    @Override
                    public void onSuccess(String response) {
                        Log.d(TAG, "Check bill ->" + response);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Log.d(TAG, "checkBill:onFailure():" + e.toString());
                        bp.stateCodeCallBack(Breakdown.CHECK_BILL_F);
                    }
                }, jsonStr);
    }

    //
    // for report status and log.
    //

    private boolean mFirstErrorsWasReported = false;

    synchronized void reportDeviceStatus() {

        //
        // Clear mStatusList only when it has reported successfully.
        //

        Log.d(TAG, "reportDeviceStatus() mFirstErrorsWasReported = " + mFirstErrorsWasReported);
        if (mFirstErrorsWasReported) {
            int statusListSize = mStatusList.size();
            Log.d(TAG, "reportDeviceStatus() before: statusListSize := " + statusListSize);

            //
            // Only the statusListSize >= 2 remove the reported status. Keep the last reported status.
            // If the statusListSize < 2 we need to report the status to server on time.
            //

            if (statusListSize >= 2) {

                //
                // For bug 111
                //

                ReportStatus lastReportedStatus = mStatusList.get(statusListSize - 1); // Keep the last reported status.
                Log.d(TAG, "reportDeviceStatus() : mStatusList.clear()");
                mStatusList.clear();
                Log.d(TAG, "reportDeviceStatus() : lastReportedStatus is := " + lastReportedStatus);
                mStatusList.add(lastReportedStatus);
            }
            Log.d(TAG, "reportDeviceStatus() after: statusListSize := " + mStatusList.size());
        }

        ArrayList<Report> reportData = new ArrayList<>();
        reportData.addAll(logList);
        reportData.addAll(mStatusList);

        DeviceStatus deviceStatus = new DeviceStatus();
        deviceStatus.setAppId(BillingConfig.BA_APP_ID);
        if (mStatusList.size() > 0) {
            deviceStatus.setCode(1);
        } else {
            deviceStatus.setCode(0);
        }
        deviceStatus.setEmpty(false);   // BUGBUG. Why isEmpty?
        deviceStatus.setLocationId(BillingConfig.BA_LOCATION_ID);
        deviceStatus.setStoreId(BillingConfig.BA_STORE_ID);
        deviceStatus.setData(reportData);

        String jsonParams = new Gson().toJson(deviceStatus);
        Log.d(TAG, "reportDeviceStatus() jsonParams = " + jsonParams);

        OkHttpUtil.post(BillingConfig.BC_DEVICE_STATUS_REPORT_URL,
                new OkHttpUtil.ResultCallback<String>() {
                    @Override
                    public void onSuccess(String response) {
                        Log.d(TAG, "reportDeviceStatus() onSuccess() " + response);

                        if (bp != null) {
                            bp.reportDevicesStateCallBack(true);
                        }

                        if (mStatusList != null) {
                            Log.d(TAG, "reportDeviceStatus() onSuccess() statisList.size() = " + mStatusList.size());
                            if (mStatusList.size() >= 1) {
                                mFirstErrorsWasReported = true;
                                Log.d(TAG, "reportDeviceStatus() onSuccess() mFirstErrorsWasReported := true");
                            }
                        }
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Log.d(TAG, "reportDeviceStatus() onFailure() " + e.toString());
                        //bp.failureCallBack(Breakdown.REPORT_STATUS_F); // BUGBUG. Why disable it?
                        if (bp != null) {
                            bp.reportDevicesStateCallBack(false);
                        }
                    }
                }, jsonParams);

        if (logList != null) {
            logList.clear();
        }
    }

    synchronized void fillStatus(ReportStatus status) {

        if (mStatusList == null) {
            mStatusList = new ArrayList<>();
            mStatusList.add(status);
        } else {
            if (mStatusList.size() < 5) {
                mStatusList.add(status);
            }
        }
    }

    synchronized void fillLog(String tag, String log) {

        ReportLog reportLog = new ReportLog();
        reportLog.setTag(tag);
        reportLog.setText(log);
        reportLog.setTime(DateUtil.getCurrentDate());
        Log.d(TAG, "fillLog() reportLog = " + tag + ": " + log);

        if (logList == null) {
            logList = new ArrayList<>();
            logList.add(reportLog);
        } else {
            logList.add(reportLog);
        }
    }

    //
    // Start a timer to report status when the app runs.
    //

    void startReportStatus() {

        if (timer == null) {
            timer = new Timer();
        }
        timer.schedule(new TimerTask() {
            @Override
            public void run() {

                reportDeviceStatus();
            }
        }, 30000, 30000);
    }

    //
    // Stop the timer when the app exists.
    //

    void stopReportStatus() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    //
    // Recover success : reset billingClient
    //

    synchronized void recoverMalfunctionSuccess() {
        mFirstErrorsWasReported = false;
        if (mStatusList != null) {
            mStatusList.clear();
        }
    }

}
