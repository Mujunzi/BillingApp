package com.lenovo.billing.cardpay;

/**
 * card payment base interface
 *
 * Created by xpw on 18-7-3.
 */
public interface CardPaymentHandler {

    /**
     * charge callback
     *
     * @param returnCode   0:the charge is success,
     *                     1:the card is not detected,
     *                     2:the charge is failure,
     *                     3:the devices is bad.
     * @param cardNumber   card NO.
     * @param remainAmount remaining amount after charging
     */
    void chargeResponseCallBack(int returnCode, String cardNumber, int remainAmount);

    /**
     * pos device status callback
     *
     * @param statusCode 0:the device is health,
     *                   1:the Serial-to-RJ45 is bad,
     *                   2:the card reader is bad
     */
    void deviceStatusCallBack(int statusCode);
}
