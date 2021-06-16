package com.lenovo.billing.smartequipment;

import java.util.Timer;
import java.util.TimerTask;

import com.lenovo.billing.smartequipment.StateEvent.StateEventEnum;

/**
 * State010.java
 *
 * @author zhangtc1
 * @date 2018-6-28
 */
public class State010 extends State {
    private Timer mTimeoutTaskTimer = new Timer();
    private TimerTask mTask;

    public State010(StateMachine sm, DoorController dc, SmartEquipmentViewable sev) {
        super(sm, dc, sev);
        mId = State.ID_010;
    }

    //    
    // enterState
    //      recognizeItemsCallBack()
    // timeout >= 5 min
    //      abnormalCallBack()
    //      openDoor2()
    //

    @Override
    public void enterState() {
        super.enterState();
        timeOutTask();

        mSev.seTask("recognizeItemsCallBack()");
        mBp.recognizeItemsCallBack("State010");
    }

    @Override
    public void quitState() {
        super.quitState();
        if (mTask != null) {
            mTask.cancel(); // BUGBUG. Can we sure the timer is canceled?
        }
    }

    //    
    // enterState2
    //      openDoor2()
    //
    // Fix the bug that GUI hagns on preview if the user sits down and stands up.
    // in zone. (101809z2.log)
    //    

    @Override
    public void enterState2() {
        super.enterState2();

        mSev.seTask("openDoor2()");
        mDc.openDoor2();
    }

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
            case BILL_REFRESHED:
                nextId = super.handleBillRefreshed(); // closeDoor3
                break;
            case EVENT_ZONE_NOT_OCC:
                nextId = handleZoneNotOcc();
                break;
            case EVENT_DOOR_2_OPENED:
                nextId = handleDoor2Opened();
                break;
            case EVENT_DOOR_3_OPENED:
                nextId = handleDoor3Opened();
                break;
            case EVENT_BB_PRESSED:
                nextId = handleBbPressed(); // openDoor2, closeDoor3
                break;

            //
            // Cancel timeout of closing door #3 when handling bbPressed or billRefreshed.
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
    // zoneNotOcc
    //      exitDoorNum3CallBack()
    //      nextState = 000        
    //

    public String handleZoneNotOcc() {
        mSev.seReceiveEvent("zoneNotOcc");

        mSev.seTask("exitDoorNum3CallBack()");
        mBp.exitDoorNum3CallBack(false);

        mSev.seTask("nextState = 000");
        return State.ID_000;
    }

    //
    // door2Opened
    //      nextState = 011,2
    //

    @Override
    public String handleDoor2Opened() {
        mSev.seReceiveEvent("door2Opened");
        super.handleDoor2Opened();

        mSev.seTask("nextState = 011,2");
        return State.ID_011 + ",2";
    }

    //
    // door3Opened
    //      nextState = 110
    //

    @Override
    public String handleDoor3Opened() {
        mSev.seReceiveEvent("door3Opened");
        super.handleDoor3Opened();

        mSev.seTask("nextState = 110");
        return State.ID_110;
    }

    //
    // bbPressed
    //      isBbPressed = true
    //      clearBillCallBack()
    //      openDoor2()
    //      closeDoor3()
    //

    public String handleBbPressed() {
        mSev.seReceiveEvent("bbPressed");

        mDc.resetDoors(); // handle bbPressed

        mSev.seTask("isBbPressed = true");
        mDc.setBbPressed(true);

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
                mSev.seReceiveEvent("timeout >= 5 min");

                mSev.seTask("abnormalCallBack()");
                mBp.abnormalCallBack(mId, "timeout");

                mSev.seTask("openDoor2()");
                mDc.openDoor2();
            }
        };

        //
        // schedules the task to be run in a delay time
        //

        mTimeoutTaskTimer.schedule(mTask, 5 * 60 * 1000);
    }
}