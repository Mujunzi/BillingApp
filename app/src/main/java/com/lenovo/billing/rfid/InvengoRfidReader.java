package com.lenovo.billing.rfid;

import android.util.Log;

import invengo.javaapi.core.BaseReader;
import invengo.javaapi.core.IMessageNotification;
import invengo.javaapi.handle.IMessageNotificationReceivedHandle;
import invengo.javaapi.protocol.IRP1.*;
import invengo.javaapi.protocol.IRP1.ReadTag.*;
import invengo.javaapi.protocol.receivedInfo.SysQuery800ReceivedInfo;

import java.util.*;

//
// The class connects RFID reader to read TID or EPC.
// The reader is a server and the sample is a client.
//

public class InvengoRfidReader
        implements IMessageNotificationReceivedHandle {
    private static final String TAG = InvengoRfidReader.class.getSimpleName();

    private RfidReaderViewable viewer = null;
    private Reader mReader = null;
    private final Object lock = new Object();
    private boolean mConnected = false;
    private boolean mScanning = false;
    private boolean startScanLoop = false;

    private boolean needCallBackWhenPowerOff = true;

    Vector tagList = new Vector();
    Set tagSet = new HashSet();


    InvengoRfidReader(RfidReaderViewable viewer) {
        this.viewer = viewer;
    }

    private String mIpAndPort;

    boolean connect(String ipAndPort) {
        Log.d(TAG, "connect()");
        mIpAndPort = ipAndPort;
        mReader = new Reader("Reader", "TCPIP_Client", ipAndPort);

        //
        // Start to connect.
        //

        Log.d(TAG, "Call mReader.connect()");
        mConnected = mReader.connect();

        if (mConnected) {   // connect the RFID reader.

            //
            // Register the class as a callback function in the reader.
            // The class must implements IMessageNotificationReceivedHandle to
            // receive asynchronized messages.
            //

            mReader.onMessageNotificationReceived.add(this);

        } else {
            Log.d(TAG, "connect() Error. Cannot connect the RFID reader.");
        }

        return mConnected;
    } // connect

    boolean reconnect() {
        Log.d(TAG, "reconnect()");

        //
        // Need to disConnect.
        // Else the RFID reader will be connect fail.
        //

        if (mReader != null) {
            mReader.disConnect();
        }

        mConnected = connect(mIpAndPort);
        Log.d(TAG, String.format("mConnected = %s", mConnected));

        return mConnected;
    }

    private boolean readStart(int duration) {

        Log.d(TAG, "readStart()");
        if (mReader == null) {
            Log.d(TAG, "readStart() Error. the mReader is null.");
            return false;
        }

        if (!mScanning) {

            //
            // Add try catch for RFID mReader.send() throws NullPointException.
            // Fixed bug 129: RFID SDK NullPointException.
            //

            try {
                Thread.sleep(500);

                //
                // send the asynchronized command of reading tags.
                //

                ReadTag readTag = new ReadTag(ReadMemoryBank.TID_6C);
                readTag.setAntenna((byte) 0x8F); // enable multiple antennas.

                boolean result = mReader.send(readTag);
                mScanning = result;
                Log.d(TAG, "readStart(), mReader.send(TID_6C), result := " + result);

                //
                // duration to send power off
                //

                Thread.sleep(duration);

                result = mReader.send(new PowerOff());

                Log.d(TAG, "readStart(), mReader.send(PowerOff), result := " + result);
                if (result) {
                    mScanning = false;
                }

            } catch (NullPointerException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        } else {

            //
            // Add try catch for RFID mReader.send() throws NullPointException.
            // Fixed bug 129: RFID SDK NullPointException.
            //

            try {
                Thread.sleep(500);
                boolean result = mReader.send(new PowerOff());

                Log.d(TAG, "readStart(), scanning now, mReader.send(PowerOff), result := " + result);
                if (result) {
                    mScanning = false;
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

        return true;
    }

    boolean readStop(boolean needCallBack) {
        Log.d(TAG, "readStop()");
        Log.d("cpb", "InvengoRfidReader: readStop()停止读取");

        if (mReader == null) {
            Log.d(TAG, "readStop() Error. the mReader is null.");
            return false;
        }

        startScanLoop = false;

        needCallBackWhenPowerOff = needCallBack;

        return true;

//        //
//        // Sending a command of PowerOff.
//        //
//
//        boolean result = false;
//
//        //
//        // Add try catch for RFID mReader.send() throws NullPointException.
//        // Fixed bug 129: RFID SDK NullPointException.
//        //
//
//        try {
//            result = mReader.send(new PowerOff());
//            Log.d(TAG, "readStop(), mReader.send(PowerOff), result := " + result);
//            if (result) {
//                mScanning = false;
//            }
//        } catch (NullPointerException e) {
//            e.printStackTrace();
//        }
//
//        //
//        // Send messages to callback.
//        //
//
//        if (needCallBack) {
//            if (result) {
//                vSendMessage(RfidReaderViewable.MSG_READ_STOPPED, "true");
//            } else {
//                vSendMessage(RfidReaderViewable.MSG_READ_STOPPED, "false");
//            }
//        }
//
//        return result;
    }

    synchronized void readWithin(final int delay, final int duration) {
        Log.d(TAG, "readWithin()");

        if (mReader == null) {
            Log.d(TAG, "readWithin() Error. the mReader is null.");
            return;
        }

        if (startScanLoop) {
            Log.d(TAG, "readWithin() the mReader loop is started...");
            return;
        }

        Log.d(TAG, "Start the thread. Scan looping ... start");

        //
        // Clear tagList.
        //

        tagList.clear();
        tagSet.clear();

        startScanLoop = true;
        while (startScanLoop) {
            readStart(duration);
            Log.d("cpb", "while (" + startScanLoop + ")");
            if (startScanLoop) {
                vSendMessage(RfidReaderViewable.MSG_GET_TID, "");
                Log.d("cpb", "vSendMessage(RfidReaderViewable.MSG_GET_TID, \"\"); ");
            }
        }

        if (needCallBackWhenPowerOff) {
            vSendMessage(RfidReaderViewable.MSG_READ_STOPPED, "true");
            Log.d("cpb", "vSendMessage(RfidReaderViewable.MSG_READ_STOPPED, \"true\");");
        }

        Log.d(TAG, "Stop the thread. Scan looping ... end");

    }

    private void vSendMessage(int msgType, String msgText) {
        if (this.viewer != null) {
            this.viewer.vSendMessage(msgType, msgText);
        }
    }

    private boolean mIsAlive = false;

    public boolean isAlive() {
        return mIsAlive;
    }

    @Override
    public void
    messageNotificationReceivedHandle(
            BaseReader reader,
            IMessageNotification msg) {

        //Log.d(TAG, "messageNotificationReceivedHandle(): IMessageNotification = " + msg.getErrInfo());

        if (msg instanceof RXD_TagData) {
            synchronized (lock) {
                if (startScanLoop) {
                    RXD_TagData fromTagData = (RXD_TagData) msg;
                    String tid = convertByteArrayToHexString(fromTagData.getReceivedMessage().getTID());
                    Log.d(TAG, String.format("messageNotificationReceivedHandle() tid = [%s]", tid));
                    this.tagList.addElement(tid);
                    this.tagSet.add(tid);
                }
            }
        }

        //
        // BUGBUG. We don't know how to disable heartbeat after enabling it.
        //

        if (msg.getMessageType().equals("Keepalive")) { // msg.getMessageID()=229,msg.getMessageType()=Keepalive
            Log.d(TAG, "messageNotificationReceivedHandle(): Keepalive");
            mIsAlive = true;
        }
    }

    private static String convertByteArrayToHexString(byte[] byte_array) {
        String s = "";
        if (byte_array == null)
            return s;

        for (byte aByte_array : byte_array) {
            String hex = Integer.toHexString(aByte_array & 0xff);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            s = s + hex;
        }
        return s.toUpperCase();
    }

    /**
     * Check the RFID reader state
     */
    public boolean getAntenna() {

        if (mReader == null) {
            Log.d(TAG, "getAntenna(): mReader is null");
            mConnected = false;
            return false;
        }

        if (!mConnected) {
            Log.d(TAG, "getAntenna(): mConnected is false");
            return false;
        }

        //
        // Handle normal case
        //

        //
        // Query the antenna power, four bytes correspond to four antenna power values (hexadecimal 0-36)
        //

        SysQuery_800 msg = new SysQuery_800((byte) 0x65);// 0x21
        boolean rst;

        //
        // Add try catch for RFID mReader.send() throws NullPointException.
        // Fixed bug 129: RFID SDK NullPointException.
        //

        try {
            rst = mReader.send(msg);
            Log.d(TAG, "getAntenna(): rst = " + rst);
        } catch (NullPointerException e) {
            e.printStackTrace();
            rst = false;
        }

        if (!rst) {
            Log.d(TAG, "rst is false");
            mConnected = false;
            return false;
        }

        Log.d(TAG, "---getAntenna----SysQuery_800 ---->msg.getStatusCode()=" + msg.getStatusCode());
        SysQuery800ReceivedInfo receivedInfo = msg.getReceivedMessage();

        if (receivedInfo == null) {
            Log.d(TAG, "receivedInfo is null");
            mConnected = false;
            return false;
        }

        mConnected = true;

        return true;
    }

}
