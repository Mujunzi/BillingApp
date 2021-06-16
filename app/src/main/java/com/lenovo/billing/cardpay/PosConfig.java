package com.lenovo.billing.cardpay;

/**
 * Created by xpw on 18-7-12.
 */

public class PosConfig {

    //
    // ------------------
    // POS RESPONSE
    //

    // charge request bytes
    static byte[] CHARGE_REQUEST_BYTE_ARRAY = new byte[]{
            (byte) 0x02, (byte) 0x43, (byte) 0x57,
            (byte) 0x0a, (byte) 0x0b, (byte) 0x0c, (byte) 0x0d, // remain amount: 0x0a*1000000 + 0x0b*10000 + 0x0c*100 + 0x0d
            (byte) 0x30, (byte) 0x30, (byte) 0x03, (byte) 0x37};

    // have card response
    static byte[] GOT_CARD_BYTE_ARRAY = new byte[]{
            (byte) 0x02, (byte) 0x63, (byte) 0x72,
            (byte) 0x00, (byte) 0x40, (byte) 0x00, (byte) 0x00,
            (byte) 0x0a, (byte) 0x0b, (byte) 0x0c, (byte) 0x0d, // remain amount: 0x0a*1000000 + 0x0b*10000 + 0x0c*100 + 0x0d
            (byte) 0x30, (byte) 0x03, (byte) 0x72}; // len 14

    // charge request ACK response
    static final byte[] CHARGE_REQUEST_ACK_ARRAY =
            new byte[]{(byte) 0x02, (byte) 0x53, (byte) 0x4C, (byte) 0x30, (byte) 0x03, (byte) 0x2C};

    // offline charge success response
    static final byte[] OFF_LINE_CHARGE_SUCCESS_ARRAY = new byte[]{
            (byte) 0x02, (byte) 0x50, (byte) 0x57,
            (byte) 0x30, (byte) 0xFF, (byte) 0x34}; // len 6

    // online charge success response
    static byte[] ON_LINE_CHARGE_SUCCESS_ARRAY = new byte[]{
            (byte) 0x02, (byte) 0x50, (byte) 0x57, (byte) 0x30,
            (byte) 0x00,
            (byte) 0x0a, (byte) 0x0b, (byte) 0x0c, (byte) 0x0d, // remain amount: 0x0a*1000000 + 0x0b*10000 + 0x0c*100 + 0x0d
            (byte) 0x34}; // len 10

    // charge failure response
    static byte[] CHARGE_FAILURE_ARRAY = new byte[]{
            (byte) 0x02, (byte) 0x50, (byte) 0x44, (byte) 0x30,
            (byte) 0x0a,
            (byte) 0x27
    }; // len 6

    // cancel charge request bytes
    static final byte[] CANCEL_CHARGE_REQUEST_BYTE_ARRAY =
            new byte[]{(byte) 0x02, (byte) 0x4C, (byte) 0x52, (byte) 0x03, (byte) 0x1D};

    // cancel charge success response
    static final byte[] CANCEL_SUCCESS_BYTE_ARRAY =
            new byte[]{(byte) 0x02, (byte) 0x4C, (byte) 0x52, (byte) 0x41, (byte) 0x03, (byte) 0x5C};
    // cancel charge failure response
    static final byte[] CANCEL_FAILURE_BYTE_ARRAY = new byte[]{(byte) 0x6A, (byte) 0x93, (byte) 0xBB, (byte) 0x7E, (byte) 0x2, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0x0F}; // len 4

    // POS heart beat
    static final byte[] POS_HEART_BEAT = new byte[]{(byte) 0x6A, (byte) 0x93, (byte) 0xBB, (byte) 0x7E, (byte) 0x6, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0x0F};

    // POS uart timeout err
    static final byte[] POS_UART_TIMEOUT_ARRAY = new byte[]{(byte) 0x6A, (byte) 0x93, (byte) 0xBB, (byte) 0x7E, (byte) 0x5, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0x0F};

    // POS state offline
    static final byte[] POS_OFFLINE_ARRAY = new byte[]{(byte) 0x02, (byte) 0x50, (byte) 0x57, (byte) 0x30, (byte) 0xEE, (byte) 0x34};

    // Error command
    static final byte[] ERROR_COMMAND_ARRAY = new byte[]{(byte) 0x6A, (byte) 0x93, (byte) 0xBB, (byte) 0x7E, (byte) 0x8, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0x0F};



    //
    // ------------------------------------------
    // Server socket handler callback return code
    //
    public static final int CHARGE_SUCCESS = 100001; // charge success code

    public static final int RECHARGE_REQUEST = 100002; // recharge

    public static final int CANCEL_CHARGE_SUCCESS = 10007; // cancel success
    public static final int CANCEL_CHARGE_FAILURE = 10008; // cancel failure
    public static final int CANCEL_CHARGE_RESPONSE_TIME_OUT = 10009; // cancel request timeout

    public static final int POS_Client_SOCKET_IS_NULL = 2000;
    public static final int POS_DEVICE_SOCKET_IS_TIMEOUT = 2001; // POS device connect timeout
    public static final int POS_DEVICE_UART_TIMEOUT = 2002; // POS device uart timeout
    public static final int POS_DEVICE_IS_BUSY = 2003;
    public static final int POS_DEVICE_IS_OFFLINE = 2004; // POS device state offline



    //
    // -------------------------------
    // charge failure code
    //

    public static final byte CHARGE_FAILURE_REASON_PERMISSION_DENIED_ON_DEVICE = (byte) 0x03;
    public static final byte CHARGE_FAILURE_REASON_PERMISSION_DENIED_TIME_NOW = (byte) 0x04;
    public static final byte CHARGE_FAILURE_REASON_PERMISSION_DENIED_TIME_INTERVAL = (byte) 0x05;
    public static final byte CHARGE_FAILURE_REASON_EXCEED_AUTO_PAY_TIMES = (byte) 0x06;
    public static final byte CHARGE_FAILURE_REASON_EXCEED_DAY_LINIT = (byte) 0x07;
    public static final byte CHARGE_FAILURE_REASON_LESS_BALANCE = (byte) 0x08;
    public static final byte CHARGE_FAILURE_REASON_FULL_BALANCE = (byte) 0x09;
    public static final byte CHARGE_FAILURE_REASON_RECORD_EXIST = (byte) 0x0a;
    public static final byte CHARGE_FAILURE_REASON_UNKNOWN_CARD = (byte) 0x0b;
    public static final byte CHARGE_FAILURE_REASON_MISSED_CARD = (byte) 0x0c;
    public static final byte CHARGE_FAILURE_REASON_DESTROY_CARD = (byte) 0x0d; // destroy card
    public static final byte CHARGE_FAILURE_REASON_MINUS_BALANCE = (byte) 0x0e;
    public static final byte CHARGE_FAILURE_REASON_DATABASE_ERROR = (byte) 0x0f; // POS: sql err
    public static final byte CHARGE_FAILURE_REASON_BAD_REQUEST_BODY = (byte) 0x11; // POS: params err
    public static final byte CHARGE_FAILURE_REASON_POS_DEVICE_CONNECT_TIME_OUT = (byte) 0x12;
    public static final byte CHARGE_FAILURE_REASON_SWIPE_FAST = (byte) 0x80;
    public static final byte CHARGE_FAILURE_REASON_OTHER = (byte) 0x13;

}
