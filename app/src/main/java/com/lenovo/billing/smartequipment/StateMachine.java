package com.lenovo.billing.smartequipment;

import java.util.*;

import com.lenovo.billing.common.EventHandler;
import com.lenovo.billing.protocol.BillingProcess;

/**
 * Finite-state machine, State Machine Manager
 * 
 * @author zhangtc1
 * @date 2018-6-27
 */
public class StateMachine implements EventHandler {

    private static StateMachine mStateMachine;
    private Vector<State> mStates;

    private State mCurrentState;
    private State mLastState;

    public StateMachine(DoorController dc, SmartEquipmentViewable sev) {
        mStates = new Vector<State>();

        // Build the init state and set the current state to it.
        StateI si = new StateI(this, dc, sev);
        mCurrentState = si;
        mLastState = si;
        // Build a list to store 8+3 states.
        mStates.addElement(new State000(this, dc, sev));
        mStates.addElement(new State001(this, dc, sev));
        mStates.addElement(new State010(this, dc, sev));
        mStates.addElement(new State011(this, dc, sev));
        mStates.addElement(new State100(this, dc, sev));
        mStates.addElement(new State101(this, dc, sev));
        mStates.addElement(new State110(this, dc, sev));
        mStates.addElement(new State111(this, dc, sev));
        mStates.addElement(si);
        mStates.addElement(new StateF(this, dc, sev));
        mStates.addElement(new StateN(this, dc, sev));
        System.out.format("--StateMachine()--mStates.size()=%d\n", mStates.size());
    }

//
//    public static StateMachine getStateMachine() {
//        if(mStateMachine == null) {
//            mStateMachine = new StateMachine();
//        }
//        return mStateMachine;
//    }
    
    public void register(BillingProcess bp) {
        System.out.format("--register(mBp)--mStates.size()=%d\n", mStates.size());
        for (int i = 0; i < mStates.size(); i++) {
            State s = (State) mStates.elementAt(i);
            System.out.format("--register(mBp)--i=%d, State.id=%s\n", i, s.getId());
            s.register(bp);
        }
    }
    
    //
    // Implement EventHandler.
    //
    
    @Override
    public synchronized void handleEvent(String event) {        
        String nextStateId = mCurrentState.nextState(event);
        String[] values = nextStateId.split(",");
        String nextStateEntry = "1";
        if (values.length == 2) {
            nextStateId = values[0];
            nextStateEntry = values[1];
        }

        //System.out.format("next state = %s, %s\n", nextStateId, nextStateEntry);

        //
        // If the state is transferred, set it to the current state.
        //
        
        boolean found = false;
        if (!nextStateId.equals(mCurrentState.getId())) {
            for (int i = 0; i < mStates.size(); i++) {
                State s = (State) mStates.elementAt(i);
                if (nextStateId.equals(s.getId())) {
                    mLastState = mCurrentState;
                    mCurrentState = s;
                    found = true;
                    break;
                }
            }
            if (!found) {
                System.out.format("The state %s is not found.\n", nextStateId);
            }
        }

        //
        // If the state is transition, promptly enter the state. Otherwise the
        // state machine will be pending because no event occurs.
        //
        
        if (found) {
            mLastState.quitState();     // quit the last state.
            if (nextStateEntry.equals("1")) {
                mCurrentState.enterState(); // enter the next state.
            } else if (nextStateEntry.equals("2")) {
                mCurrentState.enterState2(); // enter the next state of entry 2.
            } else {
                System.out.format ("Error!!! entry %s.\n", nextStateEntry);
            }
        }
    }
}