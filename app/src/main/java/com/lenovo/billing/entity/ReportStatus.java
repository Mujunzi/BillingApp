package com.lenovo.billing.entity;

public class ReportStatus implements Report{


    private String deviceId;

    //
    // 100... offline breakdown
    // 200... error breakdown
    //

    private int errorCode;

    private String errorMessage;

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
