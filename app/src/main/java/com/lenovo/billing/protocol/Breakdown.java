package com.lenovo.billing.protocol;

//
// Define abnormal events that report to NowGo server.
//

public enum Breakdown implements StateCode {

    GET_CUSTOMER_INFO_F      (10000, "Failure in getCustomerInfo()"),
    GEN_BILL_F               (10001, "Failure in genBill()"),
    CUSTOMER_EXIT_F          (10002, "Failure in customerExit()"),
    IS_CHECKED_F             (10003, "Failure in isChecked()"),
    REPORT_STATUS_F          (10004, "Failure in reportDeviceStatus()"),
    BILL_EMPTY               (10005, "Bill is empty"),
    BILL_NOT_SUCCESS         (10006, "Bill status is not success"),
    BILL_NOT_ZERO            (10007, "Bill code is not zero"),
    GEN_BILL_CB_NPE          (10008, "NullPointerException happens in billGeneratedCallBack()"),
    IS_CHECKED_CB_NPE        (10009, "NullPointerException happens in billCheckedCallback()"),
    GET_CUSTOMER_INFO_CB_NPE (10010, "NullPointerException happens in getCustomerInfoCallback()"),
    TRY_GEN_BILL_NPE         (10011, "NullPointerException happens in tryToGenBill()"),
    RFID_READER_DISCONNECT   (10012, "RFID Reader is disconnected"),
    NETWORK_IS_UNAVAILABLE   (10013, "Network is unavailable"),
    CUSTOMER_STATUS_FAILURE  (10014, "Customer info status failure"),
    CHECK_BILL_F             (10015, "Failure in checkBill()"),
    CUSTOMER_TRACKING_F      (10016, "Customer tracking failure"),
    CONNECT_RFID_READER_FAILED(10017, "rfidReader can't be connected"),
    ILLEGAL_STATE_EXCEPTION  (10050, "Illegal state exception"),
    DOOR_IS_BAD              (10051, "Door is bad"),
    EVENT_REPORTER_IS_BAD    (10052, "Event reporter is bad"),
    DOOR_CONTROLLER_IS_BAD   (10053, "The connection of the door controller is broken"),
    UNDECLARED_NPE           (10060, "undeclared exception"),
    EMERGENCY_CLOSE_STORE    (10092, "emergency close store");

    Breakdown(int code, String describe) {
        this.code = code;
        this.describe = describe;
    }

    public int getCode() {
        return code;
    }

    public String getDescribe() {
        return describe;
    }

    private int code;
    private String describe;
}
