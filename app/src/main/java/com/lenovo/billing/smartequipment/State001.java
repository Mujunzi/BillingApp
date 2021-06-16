package com.lenovo.billing.smartequipment;

import com.lenovo.billing.smartequipment.StateEvent.StateEventEnum;

/**
 * State001.java
 *
 * @author zhangtc1
 * @date 2018-6-28
 */
public class State001 extends State {

    public State001(StateMachine sm, DoorController dc, SmartEquipmentViewable sev) {
        super(sm, dc, sev);
        mId = State.ID_001;
    }

    //
    // enterState
    //      clearBillCallBack
    //

    @Override
    public void enterState() {
        super.enterState();

        mSev.seTask("clearBillCallBack()");
        mBp.clearBillCallBack();
    }

    @Override
    public String nextState(String event) {
        String nextId = mId;

        StateEventEnum typeEnum = StateEventEnum.fromTypeName(event);
        if (typeEnum == null) {
            return mId;
        }

        switch (typeEnum) {
            case EVENT_ZONE_OCC:
                nextId = handleZoneOcc();
                break;
            case EVENT_ZONE_NOT_OCC:            // BUGBUG. Don't need to handle it.
                nextId = handleZoneNotOcc();
                break;
            case EVENT_DOOR_2_CLOSED:
                nextId = handleDoor2Closed();
                break;
            case EVENT_DOOR_3_OPENED:
                nextId = handleDoor3Opened();
                break;
            case EVENT_BB_PRESSED:
                nextId = handleBbPressed(); // openDoor2, closeDoor3
                break;

            //
            // Cancel timeout of opening door #2 when handling bbPressed.
            //       

            case EVENT_DOOR_2_OPENED:
                nextId = super.handleDoor2Opened();
                break;

            //
            // Cancel timeout of closing door #3 when handling bbPressed.
            //       

            case EVENT_DOOR_3_CLOSED:
                nextId = super.handleDoor3Closed();
                break;

            default:
                nextId = super.nextState(event);
        }

        return nextId;
    }

    //
    // door2Closed
    //      invalidEventCallBack()
    //      nextState = 000
    //

    @Override
    public String handleDoor2Closed() {
        mSev.seReceiveEvent("door2Closed");
        super.handleDoor2Closed();

        mSev.seTask("invalidEventCallBack()");
        mBp.invalidEventCallBack(SmartEquipment.EVENT_DOOR_2_CLOSED + "@ " + mId);

        mSev.seTask("nextState = 000");
        return State.ID_000;
    }

    //
    // door3Opened
    //      invalidEventCallBack()
    //      nextState = 101
    //

    @Override
    public String handleDoor3Opened() {
        mSev.seReceiveEvent("door3Opened");
        super.handleDoor3Closed();

        mSev.seTask("invalidEventCallBack");
        mBp.invalidEventCallBack(SmartEquipment.EVENT_DOOR_3_OPENED + "@ " + mId);

        mSev.seTask("nextState = 101");
        return State.ID_101;
    }

    //
    // zoneOcc
    //      nextState = 011
    //

    @Override
    public String handleZoneOcc() {
        mSev.seReceiveEvent("zoneOcc");

        mSev.seTask("nextState = 011");
        return State.ID_011;
    }

    //
    // bbPressed
    //      abnormalCallBack()
    //      isBbPressed = True
    //      openDoor2()
    //      closeDoor3()
    //

    @Override
    public String handleBbPressed() {
        mSev.seReceiveEvent("bbPressed");

        mDc.resetDoors(); // handle bbPressed

        mSev.seTask("isBbPressed = true");
        mDc.setBbPressed(true);

        mSev.seTask("abnormalCallBack()");
        mBp.abnormalCallBack(mId, "bbPressed");

        mSev.seTask("openDoor2()");
        mDc.openDoor2();

        mSev.seTask("closeDoor3()");
        mDc.closeDoor3();

        return mId;
    }
}