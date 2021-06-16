package com.lenovo.billing.rfid;

public interface RfidReaderViewable {
    public void vSendMessage (int msgType, String msgText);
    public static final int MSG_GET_EPC = 0;
    public static final int MSG_READ_STOPPED = 1;
    public static final int MSG_GET_TID = 2;
    public static final int MSG_DISCONNECT = 3;
}
