package com.lenovo.billing.presenter;

import android.os.Handler;

import com.lenovo.billing.entity.BuyItem;
import com.lenovo.billing.entity.DebtItem;
import com.lenovo.billing.model.Billing;
import com.lenovo.billing.protocol.PayType;

import java.util.ArrayList;

public interface Presenter {
    void setDefaultHandler(Handler defaultHandler);

    void configAndInit();

    boolean isShopping();

    boolean isDebt();

    void isBillChecked();

    void closeAndClear();

    void setFaceId(String faceId);

    void startTimerOfFaceDetection();

    void setItemsScanned(boolean isScanned);

    void clearFlag();

    String getPortrait();

    ArrayList<BuyItem> getItems();

    ArrayList<DebtItem> getDebtItems();

    int getDebtAmount();

    String getName();

    int getAmount();

    int getTotalAmount();

    int getFinalAmount();

    int getCouponAmount();

    int getTotalCount();

    String getQrCodeText();

    String getCustomerId();

    int getOneStepPaymentFalseCode();

    void refreshOrder(String source);

    boolean isPaySuccess();

    boolean isCardPay();

    int getBalance();

    void setPaySuccess(boolean paySuccess);

    void setPayType(PayType payType);

    void setBalance(int balance);

    Billing getBilling();

    void leaveStore(int rId);

    boolean isDevicesCanWork();

    void resetDevices();

    void recoverResultCallBack(boolean recoverResult);

    void stopReader(boolean needCallBack);

    void openDoor3(int rId);
}
