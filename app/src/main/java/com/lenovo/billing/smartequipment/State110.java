package com.lenovo.billing.smartequipment;

import java.util.Timer;
import java.util.TimerTask;

import com.lenovo.billing.smartequipment.StateEvent.StateEventEnum;

/**
 * State110.java
 *
 * @author zhangtc1
 * @date 2018-6-29
 */
public class State110 extends State {
    private Timer mTimeoutTaskTimer = new Timer();
    private TimerTask mTask;

    public State110(StateMachine sm, DoorController dc, SmartEquipmentViewable sev) {
        super(sm, dc, sev);
        mId = State.ID_110;
    }

    //    
    // timeout >= 1 min
    //      abnormalCallBack()
    //

    @Override
    public void enterState() {
        super.enterState();

        timeOutTask();
    }

    @Override
    public void quitState() {
        super.quitState();
        if (mTask != null)
            mTask.cancel();
    }

    @Override
    public String nextState(String event) {
        String nextId = mId;
        StateEventEnum typeEnum = StateEventEnum.fromTypeName(event);
        if (typeEnum == null) {
            return mId;
        }
        switch (typeEnum) {
            case BILL_VERIFIED:
                nextId = super.handleBillVerified(); // openDoor3
                break;
            case EVENT_ZONE_NOT_OCC:
                nextId = handleZoneNotOcc();
                break;
            case EVENT_DOOR_2_OPENED:
                nextId = handleDoor2Opened();
                break;
            case EVENT_DOOR_3_CLOSED:
                nextId = handleDoor3Closed();
                break;
            case EVENT_BB_PRESSED:
                nextId = handleBbPressed(); // openDoor2, closeDoor3
                break;
            case BILL_REFRESHED:
                nextId = super.handleBillRefreshed(); // closeDoor3
                break;

            //
            // Cancel timeout of opening door #3 when handling billVerified.
            //

            case EVENT_DOOR_3_OPENED:
                nextId = super.handleDoor3Opened();
                break;

            default:
                nextId = super.nextState(event);
        }
        return nextId;
    }

    //
    // zoneNotOcc
    //      exitDoorNum3CallBack()
    //      nextState = 100
    //

    @Override
    public String handleZoneNotOcc() {
        mSev.seReceiveEvent("zoneNotOcc");

        mSev.seTask("exitDoorNum3CallBack()");
        mBp.exitDoorNum3CallBack(true);

        mSev.seTask("nextState = 100");
        return State.ID_100;
    }

    //
    // door2Opened
    //      nextState = 111
    //

    @Override
    public String handleDoor2Opened() {
        mSev.seReceiveEvent("door2Opened");
        super.handleDoor2Opened();

        mSev.seTask("nextState = 111");
        return State.ID_111;
    }

    //
    // door3Closed
    //      nextState = 010
    //

    @Override
    public String handleDoor3Closed() {
        mSev.seReceiveEvent("door3Closed");
        super.handleDoor3Closed();

        mSev.seTask("nextState = 010");
        return State.ID_010;
    }

    //
    // bbPressed
    //      isBbPressed = True
    //      abnormalCallBack()
    //      clearBillCallBack()
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

        mSev.seTask("clearBillCallBack()");
        mBp.clearBillCallBack();

        mSev.seTask("openDoor2()");
        mDc.openDoor2();

        mSev.seTask("closeDoor3()");
        mDc.closeDoor3();

        return mId;
    }

    /**
     * Timeout task (action when timeout)
     */
    private void timeOutTask() {
        System.out.format("timeOutTask --1-- in state %s\n", mId);
        if (mTask != null)
            mTask.cancel();
        mTask = new TimerTask() {

            //
            // task to run goes here
            //

            @Override
            public void run() {
                mSev.seReceiveEvent("timeout >= 1 min");

                mSev.seTask("abnormalCallBack()");
                mBp.abnormalCallBack(mId, "timeout");
            }
        };
        // schedules the task to be run in a delay time
        mTimeoutTaskTimer.schedule(mTask, 60 * 1000);
    }
}