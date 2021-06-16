package com.lenovo.billing.smartequipment;

public interface DoorControllerHandler {

    void overCountCallBack(String eventOverCount);

    void networkUnreachableCallBack();
}