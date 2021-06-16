package com.lenovo.billing.smartequipment;

import com.lenovo.billing.protocol.Breakdown;
import com.lenovo.billing.smartequipment.StateEvent.StateEventEnum;
import com.lenovo.billing.protocol.BillingConfig;

/**
 * StateF.java
 * 
 * @author zhangtc1
 * @date 2018-6-29
 */
public class StateF extends State {

    public StateF(StateMachine sm, DoorController dc, SmartEquipmentViewable sev) {
        super(sm, dc, sev);
        mId = State.ID_F;
    }

    //
    // enterState
    //      openDoor2()
    //      closeDoor3()
    //      stateCodeCallBack()
    //

    @Override
    public void enterState() {
        super.enterState();
        
        mSev.seTask("openDoor2()");
        mDc.openDoor2Directly();
        
        mSev.seTask("closeDoor3()");
        mDc.closeDoor3Directly();
        
        mSev.seTask ("stateCodeCallBack()");
        mBp.stateCodeCallBack(Breakdown.DOOR_IS_BAD);
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
            case EVENT_DOOR_2_CLOSED:
            case EVENT_DOOR_2_OPENED:
            case EVENT_DOOR_3_CLOSED:
            case EVENT_DOOR_3_OPENED:
                if (BillingConfig.SE_ENABLE_SELF_RECOVER) {
                    nextId = handleDoor(typeEnum);
                }
                break;
            default:
                ;
        }
        return nextId;
    }
    
    //
    // appStarted
    //      nextState = I
    //

    @Override
    public String handleAppStarted() {
        mSev.seReceiveEvent ("appStarted");
        
        mSev.seTask ("nextState = I");
        return State.ID_I;
    }

    //
    // Try to recovery by itself.
    //

    public String handleDoor(StateEventEnum typeNum) {
        mSev.seReceiveEvent ("handleDoor: " + typeNum);
        String badDoorEvent = mDc.getBadDoorEvent();
        mSev.seTask ("badDoorEvent = " + badDoorEvent );
        String nextId = mId;

        if (typeNum == StateEventEnum.EVENT_DOOR_2_CLOSED) {

            if (badDoorEvent == DoorController.OVER_COUNT_CLOSE_DOOR_2) {
                nextId = State.ID_I;
            }

        } else if (typeNum == StateEventEnum.EVENT_DOOR_2_OPENED) {

            if (badDoorEvent == DoorController.OVER_COUNT_OPEN_DOOR_2) {
                nextId = State.ID_I;
            }

        } else if (typeNum == StateEventEnum.EVENT_DOOR_3_CLOSED) {

            if (badDoorEvent == DoorController.OVER_COUNT_CLOSE_DOOR_3) {
                nextId = State.ID_I;
            }

        } else if (typeNum == StateEventEnum.EVENT_DOOR_3_OPENED) {

            if (badDoorEvent == DoorController.OVER_COUNT_OPEN_DOOR_3) {
                nextId = State.ID_I;
            }
        }

        return nextId;
    }
}