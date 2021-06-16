package com.lenovo.billing.smartequipment;

import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

import com.lenovo.billing.protocol.BillingConfig;
import com.lenovo.billing.smartequipment.StateEvent.StateEventEnum;
import com.lenovo.billing.common.Util;

/**
 * State011
 *
 * @author zhangtc1
 * @date 2018-6-29
 */
public class State011 extends State {
    private Timer mTimeoutTaskTimer = new Timer();
    private TimerTask mTask;

    public State011(StateMachine sm, DoorController dc, SmartEquipmentViewable sev) {
        super(sm, dc, sev);
        mId = State.ID_011;
    }

    @Override
    public void quitState() {
        super.quitState();
        if (mTask != null)
            mTask.cancel();
    }

    //
    // enterState
    //      isBbPressed = false
    //      clearBillCallBack()
    //      identifyUserCallBack()
    //      delayCloseDoor2()
    //

    public void enterState() {
        super.enterState();

        mSev.seTask("isBbPressed = false");
        mDc.setBbPressed(false);

        mSev.seTask("clearBillCallBack()");
        mBp.clearBillCallBack();

        mSev.seTask("identifyUserCallBack(0)");
        mBp.identifyUserCallBack(0); // 0 means no voice

        mSev.seTask("delayCloseDoor2()");
        delayCloseDoor2();
    }

    //
    // enterState2
    //      clearBillCallBack()
    //      if isBbPressed
    //          wait(waitForBack)
    //          identifyUserCallBack(0)
    //          closeDoor2()
    //      else
    //          identifyUserCallBack(1)
    //          closeDoor2()
    //    

    public void enterState2() {
        super.enterState2();

        mSev.seTask("clearBillCallBack()");
        mBp.clearBillCallBack();

        if (mDc.isBbPressed()) {

            mSev.seTask("isBbPressed is true");

            mSev.seTask("isBbPressed = false");
            mDc.setBbPressed(false);

            timeOutTask(BillingConfig.SE_WAITE_FOR_BACK, BillingConfig.SE_WAITE_FOR_BACK_CLOSE_DOOR);

        } else {

            mSev.seTask("isBbPressed = false");
            mDc.setBbPressed(false);

            mSev.seTask("identifyUserCallBack(0)");
            mBp.identifyUserCallBack(0); // 0 means no voice

            mSev.seTask("delayCloseDoor2()");
            delayCloseDoor2();
        }
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
                nextId = super.handleBillVerified(); // openDoor3()
                break;
            case EVENT_ZONE_NOT_OCC:
                nextId = handleZoneNotOcc();
                break;
            case EVENT_DOOR_2_CLOSED:
                nextId = handleDoor2Closed();
                break;
            case EVENT_DOOR_3_OPENED:
                nextId = handleDoor3Opened();
                break;
            case EVENT_BB_PRESSED:
                nextId = handleBbPressed(); // openDoor2(), wait, closeDoor2()
                break;               
                
            default:
                nextId = super.nextState(event);
        }
        return nextId;
    }

    //
    // door2Closed
    //      nextState = 010
    //

    @Override
    public String handleDoor2Closed() {
        mSev.seReceiveEvent("door2Closed");
        super.handleDoor2Closed();

        mSev.seTask("nextState = 010");
        return State.ID_010;
    }

    //
    // door3Opened
    //      invalidEventCallBack()
    //      nextState = 111
    //

    @Override
    public String handleDoor3Opened() {
        mSev.seReceiveEvent("door3Opened");
        super.handleDoor3Opened();

        mSev.seTask("invalidEventCallBack()");
        mBp.invalidEventCallBack(
                StateEventEnum.EVENT_DOOR_3_OPENED.getTypeName() + "@" + mId);

        mSev.seTask("nextState = 111");
        return State.ID_111;
    }

    //
    // zoneNotOcc
    //      abnormalCallBack()
    //      nextState = 001
    //

    @Override
    public String handleZoneNotOcc() {
        mSev.seReceiveEvent("zoneNotOcc");

        mSev.seTask("abnormalCallBack()");
        mBp.abnormalCallBack(mId, "zoneNotOcc");

        mSev.seTask("nextState = 001");
        return State.ID_001;
    }

    //
    // bbPressed
    //      openDoor2()
    //      abnormalCallBack()
    //      clearBillCallBack()
    //      isBbPressed = false
    //      wait(waitForBack)
    //      identifyUserCallBack(0)
    //      wait(5 sec)
    //      closeDoor2()
    //

    @Override
    public String handleBbPressed() { // BUGBUG. It should be reviewed.
        mSev.seReceiveEvent("bbPressed");
        
        mDc.resetDoors(); // handle bbPressed

        mSev.seTask("openDoor2()");
        mDc.openDoor2();

        mSev.seTask("abnormalCallBack()");
        mBp.abnormalCallBack(mId, "bbPressed");

        mSev.seTask("clearCallBack()");
        mBp.clearBillCallBack();

        mSev.seTask("isBbPressed = false");
        mDc.setBbPressed(false);

        timeOutTask(BillingConfig.SE_WAITE_FOR_BACK, BillingConfig.SE_WAITE_FOR_BACK_CLOSE_DOOR); // Voice spends and then close door #2.

        return mId;
    }

    /**
     * Timeout task (action when timeout)
     */
    private void timeOutTask(int delay, final int delay2) {
        Log.d("FSM", "timeOutTask, delay = " + delay + ", delay2 = " + delay2);
        System.out.format("timeOutTask --1-- in state %s\n", mId);
        if (mTask != null)
            mTask.cancel();

        mTask = new TimerTask() {

            //
            // task to run goes here
            //

            @Override
            public void run() {
                mSev.seTask("wait(sec)");

                mSev.seTask("identifyUserCallBack(1)");
                mBp.identifyUserCallBack(1); // for voice 1
                
                Util.delay(delay2);

                mSev.seTask("closeDoor2()");
                mDc.closeDoor2();
            }
        };
        // schedules the task to be run in a delay time
        mTimeoutTaskTimer.schedule(mTask, delay);
    }

    private void delayCloseDoor2() {

        mTask = new TimerTask() {
            @Override
            public void run() {
                mDc.closeDoor2();
            }
        };

        mTimeoutTaskTimer.schedule(mTask, BillingConfig.SE_CLOSE_DOOR2_DELAY);
    }
}