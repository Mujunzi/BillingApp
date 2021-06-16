package com.lenovo.billing.smartequipment;

import com.lenovo.billing.protocol.BillingConfig;
import com.lenovo.billing.smartequipment.StateEvent.StateEventEnum;

import java.util.Timer;
import java.util.TimerTask;

/**
 * State100.java
 * @author zhangtc1
 * @date 2018-6-28
 */
public class State100 extends State {

    private Timer mTimeoutTaskTimer = new Timer();
    private TimerTask mTaskCloseDoor3;
    private TimerTask mTaskOpenDoor2;

    public State100(StateMachine sm, DoorController dc, SmartEquipmentViewable sev) {
        super(sm, dc, sev);
        mId = State.ID_100;
    }
    
    //
    // enterState
    //      delayCloseDoor3()
    //      delay(4 sec)
    //      openDoor2()
    //

    @Override
    public void enterState() {
        super.enterState();
        
        mSev.seTask ("delayCloseDoor3()");
        delayCloseDoor3();
        
        //mSev.seTask ("delay(4 sec)");   
        //Util.delay (4000);         // BUGBUG. For B5. It should be in config.
        //
        //mSev.seTask ("openDoor2()");   
        //openDoor2();
        
        mSev.seTask ("delayOpenDoor2()");
        delayOpenDoor2();        
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
        case EVENT_DOOR_3_CLOSED:
            nextId = handleDoor3Closed();
            break;
        case EVENT_DOOR_2_OPENED:
            nextId = handleDoor2Opened();
            break;
        case EVENT_BB_PRESSED:
            nextId = handleBbPressed(); // openDoor2, closeDoor3
            break;
        default:
            nextId = super.nextState(event);
        }
        return nextId;
    }
    
    //
    // zoneOcc
    //      abnormalCallBack()
    //      nextState = 110
    //

    @Override
    public String handleZoneOcc() {
        mSev.seReceiveEvent ("zoneOcc");
        
        mSev.seTask ("abnormalCallBack()");
        mBp.abnormalCallBack(mId, "zoneOcc");
        
        mSev.seTask ("nextState = 110");
        return State.ID_110;
    }
    
    //
    // door2Opened
    //      nextState = 101
    //

    @Override
    public String handleDoor2Opened() {
        mSev.seReceiveEvent ("door2Opened");
        super.handleDoor2Opened();
        
        mSev.seTask ("nextState = 101");
        return State.ID_101;
    }
    
    //
    // door3Closed
    //      nextState = 000
    //

    @Override
    public String handleDoor3Closed() {
        mSev.seReceiveEvent ("door3Closed");
        super.handleDoor3Closed();
        
        mSev.seTask ("nextState = 000");
        return State.ID_000;
    }

    //
    //  bbPressed
    //      isBbPressed = true
    //      invalidEventCallBack()
    //      openDoor2()
    //      closeDoor3()
    //
    
    @Override
    public String handleBbPressed() {
        mSev.seReceiveEvent ("bbPressed");
        
        mDc.resetDoors(); // handle bbPressed
        
        mSev.seTask ("isBbPressed = true");
        mDc.setBbPressed (true);
        
        mSev.seTask ("invalidEventCallBack()");
        mBp.invalidEventCallBack(
            StateEventEnum.EVENT_BB_PRESSED.getTypeName() + "@" + mId);
            
        mSev.seTask ("openDoor2()");
        mDc.openDoor2();
        
        mSev.seTask ("closeDoor3()");
        mDc.closeDoor3();
        return mId;
    }

    private void delayCloseDoor3(){

        mTaskCloseDoor3 = new TimerTask() {
            @Override
            public void run() {
                mDc.closeDoor3();
            }
        };

        mTimeoutTaskTimer.schedule(mTaskCloseDoor3, BillingConfig.SE_CLOSE_DOOR3_DELAY);
    }
    
    private void delayOpenDoor2(){

        mTaskOpenDoor2 = new TimerTask() {
            @Override
            public void run() {
                mDc.openDoor2();
            }
        };

        mTimeoutTaskTimer.schedule(mTaskOpenDoor2, BillingConfig.SE_OPEN_DOOR2_DELAY);
    }    
}