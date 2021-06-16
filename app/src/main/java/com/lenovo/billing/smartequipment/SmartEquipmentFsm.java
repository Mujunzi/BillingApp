//
// SmartEquipment is a Java class that works with Smart Equipment device.
// It receives events from the smart equpment device and sends command to the device. 
//

package com.lenovo.billing.smartequipment;

import java.io.*;

import android.util.Log;

import com.lenovo.billing.common.*;
import com.lenovo.billing.protocol.*;

public class SmartEquipmentFsm
        extends SmartEquipment
        implements EventHandler {

    public static final String TAG = "SMART";

    private SmartEquipmentViewable mSev; // for displaying
    private StateMachine mSm;           // for finite state machine.
    private DoorController mDc;         // for door controller.
    private EventFilter mEf;            // for event filter
    private TcpServer mServer;          // server for event reporter

    public SmartEquipmentFsm(SmartEquipmentViewable sev) {

        //
        // Init the statuses.
        //

        this.isOccupied = false;        // The zone is defaulted to non-occupied.

        //
        // Build a door controller.
        //

        mDc = new DoorController(sev);

        //
        // Build a state machine and the current state is i (init).
        //

        mSm = new StateMachine(mDc, sev);
        mDc.setStateMachine(mSm);

        //
        // Build an event filter 
        //

        mEf = new EventFilter(mSm, sev);

        //
        // Build TCP server.
        //

        mServer = new TcpServer(
                BillingConfig.SE_TCP_LISTEN_PORT,
                BillingConfig.SE_EVENT_REPORTER_RECOVER_TIMEOUT);
        mServer.registerReactive(mEf);

        //
        // Link viewable.
        //

        this.mSev = sev;

    }

    //
    // Register call-back functions of billing process in the state machine.
    //

    public void register(BillingProcess bp) {
        mSm.register(bp);
        mEf.register(bp);
        mDc.register(bp);
    }

    //
    // Implement SmartEquipment
    //

    @Override
    public void start() {
        mServer.start();                // start to recive events.
        mServer.openSocket();
        try {
            mDc.openUdpSocket();
        } catch (IOException e) {
            e.printStackTrace();        // BUGBUG. Should report error log.
        }
    }

    @Override
    public void stop() {
        mServer.closeSocket();
        mServer.interrupt();
    }

    //
    // Implement EventHandler
    //

    @Override
    public void handleEvent(String event) {

        //
        // If the zone is not occupied.
        //

        if (event.equals(SmartEquipment.EVENT_ZONE_NOT_OCC)) {
            this.isOccupied = false;

            //
            // If the zone is occupied.
            //

        } else if (event.equals(SmartEquipment.EVENT_ZONE_OCC)) {
            this.isOccupied = true;
        }

        //
        // Dispatch the event to state machine.
        //

        Util.dispatchEvent(mSm, event);
    }

    @Override
    public boolean isAlive() {
        return mEf.isAlive();
    }

    @Override
    public boolean isDoor3Closed() {
        return mDc.isDoor3Closed();
    }

    @Override
    public void resetDoors() {
        if (mDc != null) {
            Log.d(TAG, "Reset door#3");
            mDc.closeDoor3();
            Log.d(TAG, "Reset door#2");
            mDc.openDoor2();
        }
    }
}
