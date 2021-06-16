//
// SmartEquipment is a Java class that works with Smart Equipment device.
// It receives events from the smart equpment device and sends command to the device. 
//

package com.lenovo.billing.smartequipment;

import java.util.*;

import android.util.Log;

import com.lenovo.billing.protocol.*;
import com.lenovo.billing.common.*;

public class EventFilter implements Reactive {

    public static final String TAG = EventFilter.class.getSimpleName();

    private SmartEquipmentViewable mSev; // for displaying
    private EventHandler mTop;           // for finite state machine.
    private BillingProcess mBp;
    private DoorController mDc;         // for door controller.

    public EventFilter(EventHandler top, SmartEquipmentViewable sev) {
        mTop = top;
        mSev = sev;
        if (BillingConfig.SE_EVENT_REPORTER_BAD_TIMEOUT > 0) {
            timeoutTask();
        }
    }

    public void register(BillingProcess bp) {
        mBp = bp;
    }

    //
    // For eventReporterBadTimeout
    //

    private Timer mTimer;
    private TimerTask mTimerTask;

    //
    // For recovering from malfunction if is alive
    //

    private boolean mIsAlive = false;

    public boolean isAlive() {
        return mIsAlive;
    }

    //
    // Timeout task that is actioned when an event is not received in period.
    //

    private synchronized void timeoutTask() {
        Log.d(TAG, "timeoutTask() mIsAlive = " + mIsAlive);

        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
            mIsAlive = true;
        }

        if (mTimerTask != null) {
            mTimerTask.cancel();
            mTimerTask = null;
            mIsAlive = true;
        }


        mTimer = new Timer();

        mTimerTask = new TimerTask() {

            //
            // task to run goes here
            //

            @Override
            public void run() {
                Log.d(TAG, "timeOutTask() run() Event Report BAD Timeout");
                mBp.stateCodeCallBack(Breakdown.EVENT_REPORTER_IS_BAD);
                mIsAlive = false;
            }
        };

        //
        // schedules the task to be run in a delay time
        //

        mTimer.schedule(mTimerTask, BillingConfig.SE_EVENT_REPORTER_BAD_TIMEOUT);
    }

    //
    // implements Reactive
    //

    @Override
    public void reactiveHandle(byte bytes[], int length) {
        String events = new String(bytes, 0, length);
        if (mSev != null) {
            mSev.seReceiveEvents(events);
        }

        //
        // Seperate the events into events to solve the problem of combined
        // events.
        //

        StringTokenizer st = new StringTokenizer(events, " "); // true is to keep the delimeter.
        while (st.hasMoreTokens()) {
            String event = st.nextToken() + " ";

            //
            // Display the received event.
            //

            if (mSev != null) {
                mSev.seHandleEvent(event);
            }

            //
            // Handle the event.
            //

            handleEvent(event);

            //
            // Display updated status.
            //

            if (mSev != null) {
                mSev.seUpdateStatus();
            }
        }

        if (BillingConfig.SE_EVENT_REPORTER_BAD_TIMEOUT > 0) {
            timeoutTask();              // reset the timeout for eventReporterBadTimeout.
        }
    }

    public void handleEvent(String event) {
        Util.dispatchEvent(mTop, event);
    }
}
