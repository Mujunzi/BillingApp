package com.lenovo.billing.smartequipment;

import com.lenovo.billing.smartequipment.StateEvent.StateEventEnum;

import java.util.Timer;
import java.util.TimerTask;

/**
 * State000.java
 *
 * @author zhangtc1
 * @date 2018-6-28
 */
public class State000 extends State {

    private Timer mTimeoutTaskTimer = new Timer();
    private TimerTask mTask;

    public State000(StateMachine sm, DoorController dc, SmartEquipmentViewable sev) {
        super(sm, dc, sev);
        mId = State.ID_000;
    }

    //    
    // timeout >= 5 sec
    //      invalidStateCallBack()
    //      openDoor2()
    //      closeDoor3()
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
            case EVENT_ZONE_OCC:
                nextId = handleZoneOcc();
                break;
            case EVENT_DOOR_2_OPENED:
                nextId = handleDoor2Opened();
                break;
            case EVENT_DOOR_3_OPENED:
                nextId = handleDoor3Opened();
                break;
            default:
                nextId = super.nextState(event);
        }
        return nextId;
    }

    //
    // door2Opened
    //      nextState = 001
    //

    @Override
    public String handleDoor2Opened() {
        mSev.seReceiveEvent("door2Opened");

        mSev.seTask("nextState = 001");
        return ID_001;
    }

    //
    // door3Opened
    //      invalidEventCallBack()
    //      nextState = 100
    //

    @Override
    public String handleDoor3Opened() {
        mSev.seReceiveEvent("door3Opened");

        mSev.seTask("invalidEventCallBack()");
        mBp.invalidEventCallBack(SmartEquipment.EVENT_DOOR_3_OPENED + "@ " + mId);

        mSev.seTask("nextState = 100");
        return ID_100;
    }

    //
    // zoneOcc
    //      abnormalCallBack()
    //      nextState = 010,2
    //

    public String handleZoneOcc() {
        mSev.seReceiveEvent("zoneOcc");

        mSev.seTask("abnormalCallBack()");
        mBp.abnormalCallBack(mId, "zoneOcc");

        mSev.seTask("nextState = 010,2");
        return ID_010 + ",2";
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

                mSev.seTask("invalidStateCallBack(), " + mId);
                mBp.invalidStateCallBack(mId);

                mSev.seTask("openDoor2()");
                mDc.openDoor2();

                mSev.seTask("closeDoor3()");
                mDc.closeDoor3();
            }
        };

        //
        // Wait a moment to open door #2 and close door #3 when the state is 000.
        //

        mTimeoutTaskTimer.schedule(mTask, 5 * 1000); // wait 5 sec.
    }
}