package com.lenovo.billing.smartequipment;

import com.lenovo.billing.smartequipment.StateEvent.StateEventEnum;

/**
 * State111.java
 * 
 * @author zhangtc1
 * @date 2018-6-29
 */
public class State111 extends State {

    public State111(StateMachine sm, DoorController dc, SmartEquipmentViewable sev) {
        super(sm, dc, sev);
        mId = State.ID_111;
    }
    
    //
    // enterState
    //      openDoor2()
    //      closeDoor3()
    //

    @Override
    public void enterState() {       
        super.enterState();    
        
        mSev.seTask ("openDoor2()");
        mDc.openDoor2();
        
        mSev.seTask ("closeDoor3()");
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
        case EVENT_ZONE_NOT_OCC:
            nextId = handleZoneNotOcc();
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
    // zoneNotOcc
    //      nextState = 101
    //
    
    @Override
    public String handleZoneNotOcc() {
        mSev.seReceiveEvent ("zoneNotOcc");
        
        mSev.seTask ("nextState = 101");
        return State.ID_101;
    }
    
    // 
    // door2Closed
    //      nextState = 110
    //
    
    @Override
    public String handleDoor2Closed() {
        mSev.seReceiveEvent ("door2Closed");
        super.handleDoor2Closed();
        
        mSev.seTask ("nextState = 110");
        return State.ID_110;
    }
    
    // 
    // door3Closed
    //    nextState = 011,2
    //
    
    @Override
    public String handleDoor3Closed() {
        mSev.seReceiveEvent ("door3Closed");
        super.handleDoor3Closed();
        
        mSev.seTask ("nextState = 011,2");
        return State.ID_011 + ",2";
    }
    
}