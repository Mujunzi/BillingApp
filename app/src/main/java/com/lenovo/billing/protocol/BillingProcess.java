//
// BillingProcess is a Java class that manages the process of billing.
//

package com.lenovo.billing.protocol;

import org.json.*;

public interface BillingProcess {
    void enableBillCallBack();
    void disableBillCallBack();
    void identifyUserCallBack(int code);
    void recognizeItemsCallBack(String caller);
    void clearBillCallBack();

    void tidListReadCallBack(JSONObject tidList);
    void faceDetectedCallBack(String faceId);
    void faceDetectedErrorCallBack(int errCode);
    void getCustomerInfoCallBack(String customerInfo);

    void generateBillCallBack(String from);
    void billGeneratedCallBack(String bill);

    void billCheckedCallBack(String result);
    void exitDoorNum3CallBack (boolean tracking);
    void customerExitCallback(String result);
    void recognizeFaceCallback(int faceId);
    void remoteControlCallBack(String event);

    void stateCodeCallBack(StateCode stateCode);               // device bad event CallBack
    void invalidStateCallBack(String state); // invalid State CallBack
    void invalidEventCallBack(String event); // invalid Event CallBack
    void abnormalCallBack(String stateId, String event);            // abnormal CallBack

    void reportDevicesStateCallBack(boolean isSuccess); // report devices CallBack
}