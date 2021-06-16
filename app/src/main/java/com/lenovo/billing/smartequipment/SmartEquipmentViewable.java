package com.lenovo.billing.smartequipment;

public interface SmartEquipmentViewable {

    public void seUpdateStatus();

    public void seReceiveEvents(String events);

    public void seHandleEvent(String event);

    public void seSendCommand(byte[] cmd);

    public void seCurrentState(String stateId);

    public void seReceiveEvent(String event);

    public void seTask(String task);
}