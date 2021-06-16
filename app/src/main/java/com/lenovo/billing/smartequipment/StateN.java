package com.lenovo.billing.smartequipment;

import com.lenovo.billing.smartequipment.StateEvent.StateEventEnum;

/**
 * FMS - StateN -loseControlState
 *
 * @author zhangtc1
 * @date 2018-6-27
 */
public class StateN extends State {

    public StateN(StateMachine sm, DoorController dc, SmartEquipmentViewable sev) {
        super(sm, dc, sev);
        mId = State.ID_N;
    }

    @Override
    public void enterState() {
        super.enterState();
    }

    public String nextState(String event) {
        String nextId = mId;
        StateEventEnum typeEnum = StateEventEnum.fromTypeName(event);
        if (typeEnum == null) {
            return mId;
        }
        switch (typeEnum) {
            case CONTROL_RELEARSED: // BUGBUG. Don't need to handle it.
                nextId = handleControlReleased();
                break;
            case DEVICE_BAD:
                nextId = handleDeviceBad();  // BUGBUG. Don't need to handle it.
                break;
            default:
                nextId = super.nextState(event);
        }

        return nextId;
    }

}