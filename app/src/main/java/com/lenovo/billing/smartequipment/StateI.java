package com.lenovo.billing.smartequipment;

import com.lenovo.billing.smartequipment.StateEvent.StateEventEnum;

/**
 * StateI Initial State
 *
 * @author zhangtc1
 * @date 2018-6-27
 */
public class StateI extends State {

    public StateI(StateMachine sm, DoorController dc, SmartEquipmentViewable sev) {
        super(sm, dc, sev);
        mId = State.ID_I;
    }

    public String nextState(String event) {
        String nextId = mId;
        StateEventEnum typeEnum = StateEventEnum.fromTypeName(event);
        if (typeEnum == null) {
            return mId;
        }
        switch (typeEnum) {
            case EVENT_DOOR_2_OPENED:
                nextId = handleDoor2Opened();
                break;
            case EVENT_DOOR_3_CLOSED:
                nextId = handleDoor3Closed();
                break;
            default:
                nextId = super.nextState(event);
        }

        return nextId;
    }

    private boolean mStep1 = false;

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

        mStep1 = true;
        String msg = String.format(
                "%s: handleAppStarted(): mStep1=%s, isDoor2Opened=%s, isDoor3Closed=%s\n",
                mId, mStep1, mDc.isDoor2Opened(), mDc.isDoor3Closed());
        mSev.seTask(msg);
    }

    //
    // appStarted
    //      openDoor2()
    //      closeDoor3()
    //

    @Override
    public String handleAppStarted() {
        mSev.seReceiveEvent("appStarted");

        mSev.seTask("openDoor2()");
        mDc.openDoor2();

        mSev.seTask("closeDoor3()");
        mDc.closeDoor3();

        mStep1 = true;

        String msg = String.format(
                "%s: handleAppStarted(): mStep1=%s, isDoor2Opened=%s, isDoor3Closed=%s\n",
                mId, mStep1, mDc.isDoor2Opened(), mDc.isDoor3Closed());
        mSev.seTask(msg);

        return mId;
    }

    //
    // door2Opened
    //      waitCheck (door2Opened)
    // door3Closed
    //      waitCheck (door3Closed)
    // door2Opened & door3Closed
    //      nextState = 001
    //

    @Override
    public String handleDoor2Opened() {
        mSev.seReceiveEvent("door2Opened");
        super.handleDoor2Opened();

        System.out.format(
                "%s: handleDoor2Opened(): mStep1=%s, isDoor2Opened=%s, isDoor3Closed=%s\n",
                mId, mStep1, mDc.isDoor2Opened(), mDc.isDoor3Closed());
        mSev.seTask("waitCheck(door2Opened)");

        if (mStep1 && mDc.isDoor2Opened() && mDc.isDoor3Closed()) {
            mSev.seReceiveEvent("door2Opened & door3Closed");
            clear();

            mSev.seTask("nextState = 001");
            return State.ID_001;
        }
        return mId;
    }

    @Override
    public String handleDoor3Closed() {
        mSev.seReceiveEvent("door3Closed");
        super.handleDoor3Closed();

        System.out.format(
                "%s: handleDoor3Closed(): mStep1=%s, isDoor2Opened=%s, isDoor3Closed=%s\n",
                mId, mStep1, mDc.isDoor2Opened(), mDc.isDoor3Closed());
        mSev.seTask("waitCheck(door3Closed)");

        if (mStep1 && mDc.isDoor2Opened() && mDc.isDoor3Closed()) {
            mSev.seReceiveEvent("door2Opened & door3Closed");
            clear();

            mSev.seTask("nextState = 001");
            return State.ID_001;
        }
        return mId;
    }

    private void clear() {
        mStep1 = false;
        mDc.clearDoorsFlags();
    }

}