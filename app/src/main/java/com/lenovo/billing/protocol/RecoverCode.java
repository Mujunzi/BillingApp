package com.lenovo.billing.protocol;

//
// Define recover-events & the recover-items.
//

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum RecoverCode implements StateCode {

    //
    // Recover breakdown
    //

    RECOVER_CODE(90000, "state recover breakdown");

    RecoverCode(int code, String describe) {
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

    /**
     * 10000, "Failure in getCustomerInfo()",
     * 10001, "Failure in genBill()",
     * 10002, "Failure in customerExit()",
     * 10003, "Failure in isChecked()",
     * 10004, "Failure in reportDeviceStatus()",
     * <p>
     * 10008, "NullPointerException happens in billGeneratedCallBack()",
     * 10009, "NullPointerException happens in billCheckedCallback()",
     * 10010, "NullPointerException happens in getCustomerInfoCallback()",
     * 10011, "NullPointerException happens in tryToGenBill()",
     * 10012, "RFID Reader is disconnected",
     * 10013, "Network is unavailable",
     * 10014, "Customer info status failure",
     * 10015, "Failure in checkBill()",
     * <p>
     * 10017, "rfidReader can't be connected",
     * <p>
     * 10050, "Illegal state exception",
     * 10051, "Door is bad",
     * 10052, "Event reporter is bad",
     * 10053, "The connection of the door controller is broken",
     * <p>
     * 10060, "undeclared exception",
     * 10092, "emergency close store"
     */
    public static final List<Integer> RECOVER_CODES_H_LSIT = new ArrayList<>(Arrays.asList(
            10000, 10001, 10002, 10003, 10004, 10008, 10009,
            10010, 10011, 10012, 10013, 10014, 10015, 10017,
            10050, 10051, 10052, 10053, 10060, 10092));

    /**
     * 10012, "RFID Reader is disconnected",
     * 10013, "Network is unavailable",
     * 10017, "rfidReader can't be connected",
     * 10051, "Door is bad",
     * 10052, "Event reporter is bad",
     * 10053, "The connection of the door controller is broken"
     */
    public static final List<Integer> RECOVER_CODES_M_LIST = new ArrayList<>(Arrays.asList(
            10012, 10013, 10017,
            10051, 10052, 10053));

    /**
     * 10012, "RFID Reader is disconnected",
     * 10013, "Network is unavailable",
     * 10017, "rfidReader can't be connected",
     * 10052, "Event reporter is bad",
     * 10053, "The connection of the door controller is broken"
     */
    public static final List<Integer> RECOVER_CODES_L_LIST = new ArrayList<>(Arrays.asList(
            10012, 10013, 10017,
            10052, 10053));

    /**
     * 10012, "RFID Reader is disconnected",
     * 10013, "Network is unavailable",
     * 10052, "Event reporter is bad",
     * 10053, "The connection of the door controller is broken"
     */
    public static final List<Integer> RECOVER_CODES_S_LIST = new ArrayList<>(Arrays.asList(
            10012, 10013,
            10052, 10053));
}
