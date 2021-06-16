//
// Door is a Java class that is an emulator of a door.
//

package com.lenovo.billing.smartequipment;

import java.util.*;
import java.net.*;

import android.util.Log;

import com.lenovo.billing.common.Util;
import com.lenovo.billing.protocol.*;

public class Door {

    public static final String TAG = "Door";

    private boolean isClosed;

    private String mName;
    private byte[] mCmdOpen;
    private byte[] mCmdClose;
    String mEventOverCountOpen;
    String mEventOverCountClose;

    private DoorControllerHandler mHandler;
    protected SmartEquipmentViewable mSev;
    private BillingProcess mBp;

    public Door(
            String name,
            byte[] cmdOpen,
            byte[] cmdClose,
            String eventOverCountOpen,
            String eventOverCountClose,
            DoorControllerHandler handler,
            SmartEquipmentViewable sev) {
        mName = name;
        mCmdOpen = cmdOpen;
        mCmdClose = cmdClose;
        mEventOverCountOpen = eventOverCountOpen;
        mEventOverCountClose = eventOverCountClose;
        mHandler = handler;
        mSev = sev;
    }

    public void register(BillingProcess bp) {
        mBp = bp;
    }

    private DatagramSocket mDs;

    public void setDs(DatagramSocket ds) {
        mDs = ds;
    }

    public void open() {
        if (BillingConfig.SE_TRY_DOOR_COUNT > 0) {
            Log.d(TAG, mName + ": openTask()");
            openTask();
        } else {
            Log.d(TAG, mName + ": mCmdOpen");
            sendCommand(mCmdOpen);
        }
    }

    public void close() {
        if (BillingConfig.SE_TRY_DOOR_COUNT > 0) {
            Log.d(TAG, mName + ": closeTask()");
            closeTask();
        } else {
            Log.d(TAG, mName + ": mCmdClose()");
            sendCommand(mCmdClose);
        }
    }

    public void openDirectly() {
        Log.d(TAG, mName + ": mCmdOpen");
        sendCommand(mCmdOpen);
    }

    public void closeDirectly() {
        Log.d(TAG, mName + ": mCmdClose()");
        sendCommand(mCmdClose);
    }

    //
    // For commands
    //

    private void sendCommand(byte[] data) {
        try {

            //
            // Send the event to door controller.
            //

            DatagramPacket dp = new DatagramPacket(data, data.length);

            //
            // 0816. We must wait that UDP connection is built.
            //

            while (true) {
                Util.delay(100);       // delay by 100 ms.
                if (mDs != null) {
                    mDs.send(dp);
                    break;
                }
            }

            Util.delay(100);           // delay by 100 ms.

        } catch (Exception e) {
            Log.d(TAG, mName + ": sendCommand(): Error.");
            e.printStackTrace();        // maybe "Network is unreachable"

            //
            // Try to reconnect doorControler.(UDP SOCKET)
            //

            if (mTryCountClose < BillingConfig.SE_TRY_DOOR_COUNT
                    &&
                    mTryCountOpen < BillingConfig.SE_TRY_DOOR_COUNT) {
                mHandler.networkUnreachableCallBack();
                return;
            }
            if (mBp != null) {
                mBp.stateCodeCallBack(Breakdown.DOOR_CONTROLLER_IS_BAD);
            }
        }
    }

    long mTaskDelay = BillingConfig.SE_TRY_DOOR_PERIOD;
    long mTaskPeriod = BillingConfig.SE_TRY_DOOR_PERIOD; // milliseconds    

    private boolean mOpenedFlag = false;  // a flag of receiving the opened event.
    private boolean mClosedFlag = false;  // a flag of receiving the closed event.   

    Timer mTimer = new Timer();
    TimerTask mOpenTimerTask;
    TimerTask mCloseTimerTask;
    private int mTryCountClose = 0;
    private int mTryCountOpen = 0;

    //
    // Open door (retry 3 times when timeout)
    //

    private void openTask() {

        if (mOpenTimerTask != null) {
            Log.d(TAG, mName + ": Call mOpenTimerTask.cancel() - 1");
            mOpenTimerTask.cancel();
        }

        sendCommand(mCmdOpen);

        mOpenedFlag = false;
        Log.d(TAG, mName + ": mOpenedFlag := " + mOpenedFlag);

        mTryCountOpen = 0;
        Log.d(TAG, mName + ": mTryCountOpen := " + mTryCountOpen);

        mOpenTimerTask = new TimerTask() {

            @Override
            public void run() {
                Log.d(TAG, mName + ": in run()");

                if (mOpenedFlag) {
                    Log.d(TAG, mName + ": Call mOpenTimerTask.cancel() - 2");
                    mOpenTimerTask.cancel();
                    return;

                } else {
                    Log.d(TAG, mName + ": Call timeoutOpened()");
                    timeoutOpened();
                }
            }
        };
        // schedules the task to be run in an interval period
        mTimer.scheduleAtFixedRate(mOpenTimerTask, mTaskDelay, mTaskPeriod);
    }

    //
    // timeoutDoor?Opened
    //      openDoor?()
    //      tryCountOpenDoor?++
    //    

    private void timeoutOpened() {
        Log.d(TAG, mName + ": timeoutOpened");

        Log.d(TAG, mName + ": open()");
        sendCommand(mCmdOpen);

        Log.d(TAG, "mTryCountOpen++");
        mTryCountOpen++;
        Log.d(TAG, mName + ": mTryCountOpen = " + mTryCountOpen);

        if (mTryCountOpen > BillingConfig.SE_TRY_DOOR_COUNT) {
            Log.d(TAG, mName + ": mOpenDoorTimerTask.cancel()");
            mOpenTimerTask.cancel();
            mHandler.overCountCallBack(mEventOverCountOpen);
        }
    }

    //
    // Close door (retry 3 times when timeout)
    //

    private void closeTask() {

        if (mCloseTimerTask != null) {
            Log.d(TAG, mName + ": Call mCloseDoorTimerTask.cancel() - 1");
            mCloseTimerTask.cancel();
        }

        sendCommand(mCmdClose);

        mClosedFlag = false;
        Log.d(TAG, mName + ": mClosedFlag := " + mClosedFlag);

        mTryCountClose = 0;
        Log.d(TAG, mName + ": mTryCountClose := " + mTryCountClose);

        mCloseTimerTask = new TimerTask() {

            @Override
            public void run() {
                Log.d(TAG, mName + ": in run()");

                if (mClosedFlag) {
                    Log.d(TAG, mName + ": Call mCloseTimerTask.cancel() - 2");
                    mCloseTimerTask.cancel();
                    return;

                } else {
                    Log.d(TAG, mName + ": Call timeoutClosed()");
                    timeoutClosed();
                }
            }
        };
        // schedules the task to be run in an interval period
        mTimer.scheduleAtFixedRate(mCloseTimerTask, mTaskDelay, mTaskPeriod);
    }

    //
    // timeoutDoor?Closed
    //      closeDoor?()
    //      tryCountCloseDoor?++
    //    

    private void timeoutClosed() {
        mSev.seReceiveEvent(mName + ": timeoutClosed");

        mSev.seTask(mName + ": close()");
        sendCommand(mCmdClose);

        mSev.seTask("tryCountClose++");
        mTryCountClose++;
        Log.d(TAG, mName + ": mTryCountClose = " + mTryCountClose);

        if (mTryCountClose > BillingConfig.SE_TRY_DOOR_COUNT) {
            Log.d(TAG, mName + ": mCloseTimerTask.cancel()");
            mCloseTimerTask.cancel();
            mHandler.overCountCallBack(mEventOverCountClose);
        }
    }

    public void clearFlags() {
        Log.d(TAG, mName + ": clearFlags()");
        this.mOpenedFlag = false;
        this.mClosedFlag = false;
    }

    public void setOpenedFlag() {
        if (!mOpenedFlag) {
            mOpenedFlag = true;         // cancel the timer task.
            Log.d(TAG, mName + ": mOpenedFlag := " + mOpenedFlag);
        }
    }

    public void setClosedFlag() {
        if (!mClosedFlag) {
            mClosedFlag = true;         // cancel the timer task.
            Log.d(TAG, mName + ": mClosedFlag := " + mClosedFlag);
        }
    }

    public void reset() {
        Log.d(TAG, mName + ": reset()");
        mTimer.cancel();
        mTimer = new Timer();

        mTryCountOpen = 0;
        mTryCountClose = 0;

        this.mOpenedFlag = true;         // hope to promptly cancel the mOpenTimerTask
        this.mClosedFlag = true;         // hope to promptly cancel the mCloseTimerTask

        if (mOpenTimerTask != null) {
            mOpenTimerTask.cancel();
        }

        if (mCloseTimerTask != null) {
            mCloseTimerTask.cancel();
        }
    }

    public boolean isOpened() {
        return mOpenedFlag;
    }

    public boolean isClosed() {
        return mClosedFlag;
    }
}