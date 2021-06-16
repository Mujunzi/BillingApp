package com.lenovo.billing.smartequipment;

import java.io.*;
import java.net.*;

import android.util.Log;

import com.lenovo.billing.common.EventHandler;
import com.lenovo.billing.protocol.*;

public class DoorController
        implements
        DoorControllerHandler,
        EventHandler {

    public static final String TAG = "DoorController";

    private static final byte[] CMD_CLOSE_DOOR_2
            = {(byte) 0xfe, (byte) 0x05, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xd9, (byte) 0xc5};
    private static final byte[] CMD_OPEN_DOOR_2
            = {(byte) 0xfe, (byte) 0x05, (byte) 0x00, (byte) 0x00, (byte) 0xff, (byte) 0x00, (byte) 0x98, (byte) 0x35};
    private static final byte[] CMD_CLOSE_DOOR_3
            = {(byte) 0xfe, (byte) 0x05, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x88, (byte) 0x05};
    private static final byte[] CMD_OPEN_DOOR_3
            = {(byte) 0xfe, (byte) 0x05, (byte) 0x00, (byte) 0x01, (byte) 0xff, (byte) 0x00, (byte) 0xc9, (byte) 0xf5};

    static final String OVER_COUNT_OPEN_DOOR_2 = "overCountOpenDoor2 ";
    static final String OVER_COUNT_OPEN_DOOR_3 = "overCountOpenDoor3 ";
    static final String OVER_COUNT_CLOSE_DOOR_2 = "overCountCloseDoor2 ";
    static final String OVER_COUNT_CLOSE_DOOR_3 = "overCountCloseDoor3 ";

    private SmartEquipmentViewable mSev;
    private Door mDoor2;
    private Door mDoor3;

    public DoorController(SmartEquipmentViewable sev) {
        mSev = sev;
        mDoor2 = new Door(
                "mDoor2",
                DoorController.CMD_OPEN_DOOR_2,
                DoorController.CMD_CLOSE_DOOR_2,
                DoorController.OVER_COUNT_OPEN_DOOR_2,
                DoorController.OVER_COUNT_CLOSE_DOOR_2,
                this,
                sev);
        mDoor3 = new Door(
                "mDoor3",
                DoorController.CMD_OPEN_DOOR_3,
                DoorController.CMD_CLOSE_DOOR_3,
                DoorController.OVER_COUNT_OPEN_DOOR_3,
                DoorController.OVER_COUNT_CLOSE_DOOR_3,
                this,
                sev);
    }

    public void register(BillingProcess bp) {
        mDoor2.register(bp);
        mDoor3.register(bp);
    }

    private StateMachine mSm;

    void setStateMachine(StateMachine sm) {
        mSm = sm;
    }

    //
    // A UDP socket sends commands.
    //

    private DatagramSocket mDs;

    void openUdpSocket() throws IOException {
        Log.d(TAG, "openUdpSocket()");

        new Thread(new Runnable() {

            public void run() {

                if (mDs == null || mDs.isClosed()) {
                    try {
                        InetAddress iAddress = InetAddress.getByName(BillingConfig.SE_UDP_SEND_ADDR);
                        mDs = new DatagramSocket();
                        mDs.connect(iAddress, BillingConfig.SE_UDP_SEND_PORT);
                        mDoor2.setDs(mDs);
                        mDoor3.setDs(mDs);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    //
    // back button status.
    //

    private boolean mIsBbPressed = false;

    boolean isBbPressed() {
        return mIsBbPressed;
    }

    void setBbPressed(boolean flag) {
        mIsBbPressed = flag;
    }

    private String mBadDoorEvent;

    String getBadDoorEvent() {
        return mBadDoorEvent;
    }

    //
    // Implement DoorControllerHandler.
    //
    // overCount ---> deviceBad
    //    

    @Override
    public void overCountCallBack(String eventOverCount) {
        Log.d(TAG, "overCount: " + eventOverCount);
        mBadDoorEvent = eventOverCount;
        mSm.handleEvent(StateEvent.DEVICE_BAD);
    }

    //
    // Implement DoorControllerHandler.
    // network unreachable ---> reset mDs
    //

    @Override
    public void networkUnreachableCallBack() {
        try {
            if (mDs != null) {
                mDs.disconnect();
                mDs = null;
            }
            openUdpSocket();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //
    // Implement EventHandler.
    //

    @Override
    public void handleEvent(String event) {

        switch (event) {
            case SmartEquipment.EVENT_DOOR_2_OPENED:
                this.mDoor2.setOpenedFlag();

                break;
            case SmartEquipment.EVENT_DOOR_2_CLOSED:
                this.mDoor2.setClosedFlag();

                break;
            case SmartEquipment.EVENT_DOOR_3_OPENED:
                this.mDoor3.setOpenedFlag();

                break;
            case SmartEquipment.EVENT_DOOR_3_CLOSED:
                this.mDoor3.setClosedFlag();
                break;
        }
    }

    //
    // Control doors.
    //

    public void resetDoors() {
        mDoor2.reset();
        mDoor3.reset();
    }

    void resetDoor3() {
        mDoor3.reset();
    }

    public void openDoor2() {
        mDoor2.open();
    }

    void closeDoor2() {
        mDoor2.close();
    }

    void openDoor3() {
        mDoor3.open();
    }

    public void closeDoor3() {
        mDoor3.close();
    }

    void openDoor2Directly() {
        mDoor2.openDirectly();
    }

    void closeDoor3Directly() {
        mDoor3.closeDirectly();
    }

    boolean isDoor2Opened() {
        return mDoor2.isOpened();
    }

    boolean isDoor3Closed() {
        return mDoor3.isClosed();
    }

    void clearDoorsFlags() {
        mDoor2.clearFlags();
        mDoor3.clearFlags();
    }

}