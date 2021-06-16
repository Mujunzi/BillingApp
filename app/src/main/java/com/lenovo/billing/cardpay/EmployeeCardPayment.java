package com.lenovo.billing.cardpay;

import android.util.Log;

/**
 * Employee card payment class
 * <p>
 * Created by xpw on 18-7-3.
 */
public class EmployeeCardPayment extends CardPayment {

    private static final String TAG = EmployeeCardPayment.class.getSimpleName();


    private PaymentServer paymentServer;

    public EmployeeCardPayment() {
        paymentServer = new PaymentServer();
        paymentServer.openServer();
    }

    @Override
    public void registerCallBack(CardPaymentHandler handler) {
        paymentServer.registerCallBack(handler);
    }

    @Override
    public void requestCharge(int chargeAmount) {

        // send chargeAmount to pos
        String money = String.valueOf(chargeAmount);
        if ( money.length() > 8 ) {
            Log.e(TAG, "err: money length > 8");
            return;
        }
        if ( paymentServer != null ) {
            paymentServer.requestCharge(money);
        }
    }

    public void close(){
        if(paymentServer != null){
            paymentServer.reset();
        }
    }

    @Override
    public void sendCancelRequest() {
        paymentServer.cancelCharge();
    }

    public void clostPaymentServer() {
        paymentServer.closeServer();
    }

}
