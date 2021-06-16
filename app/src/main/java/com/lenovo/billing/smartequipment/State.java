package com.lenovo.billing.smartequipment;

import android.util.Log;

import com.lenovo.billing.common.Util;
import com.lenovo.billing.protocol.*;
import com.lenovo.billing.smartequipment.StateEvent.StateEventEnum;

/**
 * FMS - abstract class State
 * 
 * @author zhangtc1
 * @date 2018-6-27
 */
public abstract class State {
    protected String mId;
    protected DoorController mDc; // close/close door number 2/3
    protected SmartEquipmentViewable mSev;
    protected BillingProcess mBp; // callback of billing process.
    protected StateMachine mSM;

    // All States
    public static final String ID_000 = "000";
    public static final String ID_001 = "001";
    public static final String ID_010 = "010";
    public static final String ID_011 = "011";
    public static final String ID_100 = "100";
    public static final String ID_101 = "101";
    public static final String ID_110 = "110";
    public static final String ID_111 = "111";
    public static final String ID_N = "n";
    public static final String ID_I = "i";
    public static final String ID_F = "f";

    public State(StateMachine sm, DoorController dc, SmartEquipmentViewable sev) {
        mDc = dc;
        mSM = sm;
        mSev = sev;
    }

    public void register(BillingProcess bp) {
        mBp = bp;
    }

    public String getId() {
        return mId;
    }

    public void enterState() {
        
        mSev.seCurrentState(mId);       // display the current state.
    }
    
    public void enterState2() {
        
        mSev.seCurrentState (mId+",2"); // Display the current state.
    }

    /**
     * quit state cancel task and 
     */
    public void quitState() {
        Log.d("FSM", "quitState(), " + mId);
        mDc.resetDoors();
    }
    
    public String nextState(String event) {
        String nextId = mId;
        StateEventEnum typeEnum = StateEventEnum.fromTypeName(event);
        if (typeEnum == null) {
            return mId;
        }
        // Same processing logic
        switch (typeEnum) {
            case APP_STARTED:
                nextId = handleAppStarted();
                break;
            case CONTROL_ACQUIRED:
                nextId = handleControlAquired();
                break;
            case CONTROL_RELEARSED:
                nextId = handleControlReleased();
                break;
            case DEVICE_BAD:
                nextId = handleDeviceBad();
                break;
            default:
                ;
                //System.out.format(
                //        "Error! Cannot handle the event %s in the state %s\n",
                //        event, mId);
        }
        return nextId;
    }

    //
    // deviceBad
    //      nextState = F
    //
     
    public String handleDeviceBad() {
        mSev.seReceiveEvent ("deviceBad");
        
        mSev.seTask ("nextState = F");
        return ID_F;
    }

    //
    // controlReleased
    //      nextState = I
    //

    public String handleControlReleased() {
        mSev.seReceiveEvent ("controlReleased");
        
        mSev.seTask ("nextState = I");
        return ID_I;
    }

    //
    // controlAcquired
    //      nextState = N
    //
    
    public String handleControlAquired() {
        mSev.seReceiveEvent ("controlAcquired");
        
        mSev.seTask ("nextState = N");
        return ID_N;
    }

    //
    // appStarted
    //      openDoor2()
    //      closeDoor3()
    //
    
    public String handleAppStarted() {
        mSev.seReceiveEvent ("appStarted");
        
        mSev.seTask ("openDoor2()");
        mDc.openDoor2();
        
        mSev.seTask ("closeDoor3()");
        mDc.closeDoor3();
        
        mSev.seTask ("nextState = I");
        return ID_I;
    }
    
    //
    // billVerified
    //      openDoor3()
    //

    public String handleBillVerified() {
        mSev.seReceiveEvent ("billVerified");

        mDc.resetDoor3();
        mSev.seTask ("openDoor3()");
        mDc.openDoor3();
        return mId;
    }
    
    //
    // billRefreshed
    //      abnormalCallBack()
    //      closeDoor3()
    //

    public String handleBillRefreshed() {
        mSev.seReceiveEvent ("billRefreshed");
        
        mSev.seTask ("closeDoor3()");
        mDc.closeDoor3();
        
        return mId;
    }
    
    //
    // bbPressed
    //      openDoor2()
    //      closeDoor3()
    //

    public String handleBbPressed() {
        mSev.seReceiveEvent ("bbPressed");
        
        mDc.resetDoors(); // handle bbPressed
        
        mSev.seTask ("openDoor2()");
        mDc.openDoor2();
        
        mSev.seTask ("closeDoor3()");
        mDc.closeDoor3();
        
        return mId;
    }

    public String handleDoor2Opened() {
        Util.dispatchEvent(mDc, SmartEquipment.EVENT_DOOR_2_OPENED);
        return mId;
    }

    public String handleDoor2Closed() {
        Util.dispatchEvent(mDc, SmartEquipment.EVENT_DOOR_2_CLOSED);
        return mId;
    }

    public String handleDoor3Opened() {
        Util.dispatchEvent(mDc, SmartEquipment.EVENT_DOOR_3_OPENED);
        return mId;
    }

    public String handleDoor3Closed() {
        Util.dispatchEvent(mDc, SmartEquipment.EVENT_DOOR_3_CLOSED);
        return mId;
    }

    public String handleZoneNotOcc() {
        return mId;
    }

    public String handleZoneOcc() {
        return mId;
    }
}