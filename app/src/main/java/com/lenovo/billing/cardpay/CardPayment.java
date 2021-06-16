package com.lenovo.billing.cardpay;

/**
 * base of card payment class
 * <p>
 * Created by xpw on 18-7-3.
 */
abstract class CardPayment {

    /**
     * Register a handler of card payment with callback functions.
     *
     * @param handler charging status call back
     */
    abstract void registerCallBack(CardPaymentHandler handler);


    /**
     * Send the charge request to the card reader with amount to prepare for charging.
     *
     * @param chargeAmount amount of the charge limit to CNY 0.01
     */
    abstract void requestCharge(int chargeAmount);

    /**
     * Send the cancel request to the card reader to cancel the charging.
     */
    abstract void sendCancelRequest();
}
