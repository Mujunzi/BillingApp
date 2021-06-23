package com.lenovo.billing.presenter;

import android.os.Handler;
import android.util.Log;

import com.lenovo.billing.entity.*;
import com.lenovo.billing.model.*;
import com.lenovo.billing.protocol.PayType;
import com.lenovo.billing.views.protocol.IView;

import java.util.ArrayList;

public class ActPresenterImpl
        implements
        Presenter {

    private static final String TAG = ActPresenterImpl.class.getSimpleName();

    //private MainActivity.DefaultHandler defaultHandler;
    private Handler mDefaultHandler;

    private Billing billing;

    private IView view;

    public ActPresenterImpl(IView view) {
        this.view = view;
    }

    //
    // Implement ActContract.Presenter
    //

    @Override
    public void setDefaultHandler(Handler defaultHandler) {
        mDefaultHandler = defaultHandler;
    }

    @Override
    public void configAndInit() {

        if (billing == null) {
            billing = new Billing();
            billing.setDefaultHandler(mDefaultHandler);
        }

    }

    @Override
    public boolean isShopping() {
        return billing.getBillSub().isShopping();
    }

    @Override
    public boolean isDebt() {
        return billing.getBillSub().isDebt();
    }

    @Override
    public void isBillChecked() {

        billing.isBillChecked();
    }

    @Override
    public void closeAndClear() {

        billing.closeAndClear();
    }

    @Override
    public void setFaceId(String faceId) {

        billing.getBillSub().faceId = faceId;
        Log.d(TAG, "setFaceId() faceId := " + faceId);
    }

    @Override
    public void startTimerOfFaceDetection() {

        billing.startTimerOfFaceDetection();
    }

    @Override
    public void setItemsScanned(boolean isScanned) {

        billing.getBillSub().mItemsRecognized = isScanned;
        Log.d(TAG, "setItemsScanned(): mItemsRecognized := " + isScanned);
    }

    @Override
    public void clearFlag() {

        billing.clearFlag();
    }

    @Override
    synchronized
    public String getPortrait() {
        if (billing == null || billing.getBillSub() == null) {
            Log.d(TAG, "getPortrait() Error. billSub is null.");
            return "";
        }
        if (billing.getBillSub().customerInfo == null) {
            Log.d(TAG, "getPortrait() Error. billData is null.");
            return "";
        }
        return billing.getBillSub().customerInfo.getPortrait();
    }

    @Override
    synchronized
    public ArrayList<BuyItem> getItems() {
        if (billing == null || billing.getBillSub() == null) {
            Log.d(TAG, "getItems() Error. billSub is null.");
            return new ArrayList<>();
        }
        if (billing.getBillSub().billData == null) {
            Log.d(TAG, "getItems() Error. billData is null.");
            return new ArrayList<>();
        }
        return billing.getBillSub().billData.getItemList();
    }

    @Override
    synchronized
    public ArrayList<DebtItem> getDebtItems() {
        if (billing == null || billing.getBillSub() == null) {
            Log.d(TAG, "getDebtItems() Error. billSub is null.");
            return new ArrayList<>();
        }
        if (billing.getBillSub().getCustomerStatus() == null) {
            Log.d(TAG, "getDebtItems() Error. customerStatus is null.");
            return new ArrayList<>();
        }
        return billing.getBillSub().getCustomerStatus().getDebtItems();
    }

    @Override
    synchronized
    public int getDebtAmount() {
        if (billing == null || billing.getBillSub() == null) {
            Log.d(TAG, "getDebtAmount() Error. billSub is null.");
            return 0;
        }
        if (billing.getBillSub().billData == null) {
            Log.d(TAG, "getDebtAmount() Error. billData is null.");
            return 0;
        }
        return billing.getBillSub().billData.getDebtAmount();
    }

    @Override
    synchronized
    public String getName() {
        if (billing == null || billing.getBillSub() == null) {
            Log.d(TAG, "getName() Error. billSub is null.");
            return "";
        }
        if (billing.getBillSub().customerInfo == null) {
            Log.d(TAG, "getName() Error. customerInfo is null.");
            return "";
        }
        return billing.getBillSub().customerInfo.getCustomerName();
    }

    @Override
    synchronized
    public int getAmount() {
        if (billing == null || billing.getBillSub() == null) {
            Log.d(TAG, "getAmount() Error. billSub is null.");
            return 0;
        }
        if (billing.getBillSub().billData == null) {
            Log.d(TAG, "getAmount() Error. billData is null.");
            return 0;
        }
        return billing.getBillSub().billData.getAmount();
    }

    @Override
    synchronized
    public int getTotalAmount() {
        if (billing == null || billing.getBillSub() == null) {
            Log.d(TAG, "getTotalAmount() Error. billSub is null.");
            return 0;
        }
        if (billing.getBillSub().billData == null) {
            Log.d(TAG, "getTotalAmount() Error. billData is null.");
            return 0;
        }
        return billing.getBillSub().billData.getTotalAmount();
    }

    @Override
    synchronized
    public int getFinalAmount() {
        if (billing == null || billing.getBillSub() == null) {
            Log.d(TAG, "getFinalAmount() Error. billSub is null.");
            return 0;
        }
        if (billing.getBillSub().billData == null) {
            Log.d(TAG, "getFinalAmount() Error. billData is null.");
            return 0;
        }
        return billing.getBillSub().billData.getFinalAmount();
    }

    @Override
    synchronized
    public int getCouponAmount() {
        if (billing == null || billing.getBillSub() == null) {
            Log.d(TAG, "getCouponAmount() Error. billSub is null.");
            return 0;
        }
        if (billing.getBillSub().billData == null) {
            Log.d(TAG, "getCouponAmount() Error. billData is null.");
            return 0;
        }
        return billing.getBillSub().billData.getCouponAmount();
    }

    @Override
    synchronized
    public int getTotalCount() {
        if (billing == null || billing.getBillSub() == null) {
            Log.d(TAG, "getTotalCount() Error. billSub is null.");
            return 0;
        }
        if (billing.getBillSub().billData == null) {
            Log.d(TAG, "getTotalCount() Error.  billData is null.");
            return 0;
        }
        return billing.getBillSub().billData.getTotalCount();
    }

    @Override
    synchronized
    public String getQrCodeText() {
        if (billing == null || billing.getBillSub() == null) {
            Log.d(TAG, "getQrCodeText() Error. billSub is null.");
            return "";
        }
        if (billing.getBillSub().billData == null) {
            Log.d(TAG, "getQrCodeText() Error. billData is null.");
            return "";
        }
        if (billing.getBillSub().billData.getQrCodeText() == null) {
            Log.d(TAG, "getQrCodeText() Error. getQrCodeText is null.");
            return "";
        }
//        Log.d(TAG, "getQrCodeText() Error. getQrCodeText is null.");
        return billing.getBillSub().billData.getQrCodeText();
    }

    @Override
    synchronized
    public String getCustomerId() {
        if (billing == null || billing.getBillSub() == null) {
            Log.d(TAG, "getCustomerId() Error. billSub is null.");
            return "";
        }
        if (billing.getBillSub().customerInfo == null) {
            Log.d(TAG, "getCustomerId() Error. customerInfo is null.");
            return "";
        }
        return billing.getBillSub().customerInfo.getCustomerId();
    }

    @Override
    synchronized
    public int getOneStepPaymentFalseCode() {
        if (billing == null || billing.getBillSub() == null) {
            Log.d(TAG, "getOneStepPaymentFalseCode() Error. billSub is null.");
            return 0;
        }
        if (billing.getBillSub().getCustomerStatus() == null) {
            Log.d(TAG, "getOneStepPaymentFalseCode() Error. customerStatus is null.");
            return 0;
        }
        return billing.getBillSub().getCustomerStatus().getOneStepPaymentFalseCode();
    }

    //
    // source
    //      notMe
    //      noShopping
    //      recheck
    //      refresh    
    //
    // refreshOrder(source)
    //      ?handleNotMe()
    //      ?refreshItems(source)
    //          ?recognizeItemsCallBack()
    //          send BILL_REFRESHED    
    //

    @Override
    public void refreshOrder(String source) {

        Log.d(TAG, "refreshOrder() source = " + source);

        //
        // fixed loop in refresh UI bug, happened with refresh from qrcode page.
        //  - root cause: if the tid list size is not changed, we won't to gen-bill "type=query"
        //

        billing.beforeTidsCount = -1;

        if ("notMe".equals(source)) {
            billing.handleNotMe();
        } else {
            billing.refreshItems(source);
        }
    }

    @Override
    public boolean isPaySuccess() {
        return billing.getBillSub().billState == BillState.CHECKED;
    }

    @Override
    public boolean isCardPay() {
        return billing.getBillSub().payType == PayType.EMPLOYEE_CARD;
    }

    @Override
    public int getBalance() {
        return billing.getBillSub().balance;
    }

    @Override
    public void setPaySuccess(boolean paySuccess) {

        if (!paySuccess) {
            billing.getBillSub().billState = BillState.CLEARED;
            Log.d(TAG, "setPaySuccess() billState := " + billing.getBillSub().billState);
        }

        //billing.getBillSub().isPaySuccess = paySuccess;
    }

    @Override
    public void setPayType(PayType payType) {
        billing.getBillSub().payType = payType;
        Log.d(TAG, "setPayType() payType := " + payType);
    }

    @Override
    public void setBalance(int balance) {

        billing.getBillSub().balance = balance;
    }

    @Override
    public Billing getBilling() {
        return billing;
    }

    //
    // Not to open door3 if no shopping.
    // So we add open_door3_leave_store button in Farewell fragment
    // and add the button click function.
    //

    @Override
    public void leaveStore(int rId) {
        if (billing != null) {
            billing.openDoor3(rId);
            billing.clearBillCallBack();
        }
    }


    //
    // Check the devices states if true to set BillingApp remove the errCode.
    //

    @Override
    public boolean isDevicesCanWork() {
        if (billing != null) {
            return billing.isDevicesCanWork();
        }
        return false;
    }

    @Override
    public void resetDevices() {
        if (billing != null) {
            billing.resetDevices();
        }
    }

    @Override
    public void recoverResultCallBack(boolean recoverResult) {
        if (billing != null) {
            billing.recoverResultCallBack(recoverResult);
        }
    }

    @Override
    public void stopReader(boolean needCallBack) {
        if (billing != null) {
            billing.stopReader(needCallBack);
        }
    }

    @Override
    public void openDoor3(int rId) {
        if (billing != null) {
            billing.openDoor3(rId);
        }
    }

    @Override
    public void netIsAlive() {
        view.toRecoverState();
    }
}
