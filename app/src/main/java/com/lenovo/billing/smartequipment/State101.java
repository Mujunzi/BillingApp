package com.lenovo.billing.smartequipment;

import com.lenovo.billing.smartequipment.StateEvent.StateEventEnum;

/**
 * State101.java
 *
 * @author zhangtc1
 * @date 2018-6-29
 */
public class State101 extends State {

    public State101(StateMachine sm, DoorController dc, SmartEquipmentViewable sev) {
        super(sm, dc, sev);
        mId = State.ID_101;
    }

    //
    // enterState
    //      openDoor2()
    //      closeDoor3()
    //

    @Override
    public void enterState() {
        super.enterState();

        mSev.seTask("openDoor2()");
        mDc.openDoor2();

        mSev.seTask("closeDoor3()");
        mDc.closeDoor3();
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
            case EVENT_DOOR_2_CLOSED:
                nextId = handleDoor2Closed();
                break;
            case EVENT_DOOR_3_CLOSED:
                nextId = handleDoor3Closed();
                break;
            default:
                nextId = super.nextState(event);
        }
        return nextId;
    }

    //
    // zoneOcc
    //      nextState = 111
    //

    @Override
    public String handleZoneOcc() {
        mSev.seReceiveEvent("zoneOcc");

        mSev.seTask("nextState = 111");
        return State.ID_111;
    }

    //
    // door3Closed
    //      nextState = 001
    //

    @Override
    public String handleDoor3Closed() {
        mSev.seReceiveEvent("door3Closed");
        super.handleDoor3Closed();

        mSev.seTask("nextState = 001");
        return State.ID_001;
    }

    //
    // door2Closed
    //      invalidEventCallBack()
    //      nextState = 100
    //

    @Override
    public String handleDoor2Closed() {
        mSev.seReceiveEvent("door2Closed");
        super.handleDoor2Closed();

        mSev.seTask("invalidEventCallBack()");
        mBp.invalidEventCallBack(SmartEquipment.EVENT_DOOR_2_CLOSED + "@ " + mId);

        mSev.seTask("nextState = 100");
        return State.ID_100;
    }
}