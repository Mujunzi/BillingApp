package com.lenovo.billing.rfid;

import android.util.Log;

import com.lenovo.billing.protocol.BillingConfig;
import com.lenovo.billing.common.Util;
import com.lenovo.billing.protocol.*;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.json.JSONException;
import org.json.JSONObject;

public class RfidReaderInvengoSdk
        implements RfidReader, RfidReaderViewable {

    private static final String TAG = RfidReaderInvengoSdk.class.getSimpleName();

    private static final String READER_STATE_INIT = "INIT";
    private static final String READER_STATE_IDLE = "IDLE";
    private static final String READER_STATE_CHECK = "CHECK";
    private static final String READER_STATE_READ = "READ";
    private static final String READER_STATE_RECOVER = "RECOVER";

    //
    // reader State:
    // -- init     reader init. default value
    // -- idle     reader is health, can work
    // -- check    reader is checking state
    // -- read     reader is reading tid
    // -- recover  reader try to reconnect
    //

    private String readerState = READER_STATE_IDLE;

    //
    // RFID reader get antenna fail count
    // -- > 3 mean the reader is disconnect,and report the malfunction to UI.
    //

    private int getAntennaFailTimes = 0;

    //
    // Reconnect count
    //

    private int reconnectCount = 0;

    private InvengoRfidReader reader;

    private ExecutorService service;

    public RfidReaderInvengoSdk() {

        //
        // Create the RFID reader object.
        //

        reader = new InvengoRfidReader(this);

        //
        // Connect to the RFID reader.
        //

        int processors = Runtime.getRuntime().availableProcessors();
        service = Executors.newFixedThreadPool(processors);

        service.execute(new Runnable() {
            @Override
            public void run() {
                reader.connect(BillingConfig.RR_IP_PORT);
            }
        });

        //
        // TODO. Below code can trigger uncaughtException. It is used to verify
        // the print of exception in uncaughtException().
        //

        if (BillingConfig.TE_FORCE_UNCAUGHT_EXCEPTION) {
            bp.stateCodeCallBack(Breakdown.CONNECT_RFID_READER_FAILED);
        }

        Log.d(TAG, "InvengoSdk init.");

        //
        // Check reader state thread, if reader is disconnected to reconnect it.
        //

        new Thread(new Runnable() {
            @Override
            public void run() {


                while (true) {
                    try {

                        Thread.sleep(5033);
                        if (reader == null) {
                            Log.d(TAG, "loop start, reader == null");
                            continue;
                        }

                        Log.d(TAG, "loop start, readerState := " + readerState);

                        //
                        // if reader is reading tid list. skip this check.
                        //

                        if (READER_STATE_READ.equals(readerState)) {
                            Log.d(TAG, "reader is reading tid list now, skip this check");
                            continue;
                        }

                        //
                        // if reader is recover state, try to recover reader
                        //

                        if (READER_STATE_RECOVER.equals(readerState)) {
                            if (recoverReader()) {
                                readerState = READER_STATE_IDLE;
                                Log.d(TAG, "reconnect success, readerState := READER_STATE_IDLE");
                            }
                            continue;
                        }


                        Log.d(TAG, "readerState := READER_STATE_CHECK");
                        readerState = READER_STATE_CHECK;

                        if (checkReader()) {

                            readerState = READER_STATE_IDLE;
                            Log.d(TAG, "getAntenna success, readerState := READER_STATE_IDLE");

                            getAntennaFailTimes = 0;
                            reconnectCount = 0;

                            if (BillingConfig.BA_AUTO_RECOVER_FOR_MALFUNCTION) {
                                sendAutoRecoverMsg();
                            }

                        } else {
                            getAntennaFailTimes += 1;
                            if (getAntennaFailTimes >= 1) {
                                readerState = READER_STATE_RECOVER;
                                Log.d(TAG, "getAntenna fail, getAntennaFailTimes = " + getAntennaFailTimes
                                        + ", readerState := READER_STATE_RECOVER");
                            }
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private synchronized boolean checkReader() {
        boolean readerIsConnected = reader.getAntenna();
        Log.d(TAG, "checkReader() readerIsConnected := " + readerIsConnected);

        return readerIsConnected;
    }

    private synchronized void sendAutoRecoverMsg() {
        if (bp != null) {
            Log.d(TAG, "sendAutoRecoverMsg := RecoverCode.RECOVER_CODE");
            bp.stateCodeCallBack(RecoverCode.RECOVER_CODE);

        }
    }

    private synchronized boolean recoverReader() {

        Log.d(TAG, "recoverReader(): getAntennaFailTimes = " + getAntennaFailTimes);

        boolean reconnectResult = false;

        getAntennaFailTimes += 1;
        if (getAntennaFailTimes >= 4) {

            //
            // Display 'errCode 10012' mailfunction GUI.
            //

            Log.d(TAG, "Send RFID reader disconnect msg to update UI.");
            vSendMessage(RfidReaderViewable.MSG_DISCONNECT, "");
        }

        //
        // Reconnect the RFID reader when miss twice antenna value.
        //

        if (getAntennaFailTimes >= 1) {
            reconnectCount += 1;
            Log.d(TAG, "Try to reconnect RFID READER ----- " + reconnectCount);
            reconnectResult = reader.reconnect();
        }

        return reconnectResult;
    }

    //
    // Implement RfidReader.
    //

    private BillingProcess bp;

    @Override
    public void register(BillingProcess bp) {
        this.bp = bp;
    }

    @Override
    public synchronized boolean readTidList(final int delay, final int duration) {

        if (!READER_STATE_IDLE.equals(readerState)) {
            return false;
        }

        service.execute(new Runnable() {
            @Override
            public void run() {
                //
                // If the RFID reader is connected, read RFID tags within duration.
                //

                readerState = READER_STATE_READ;
                Log.d(TAG, "read tid begin, readerState := READER_STATE_READ");

                reader.readWithin(delay, duration);

                readerState = READER_STATE_IDLE;
                Log.d(TAG, "read tid over, readerState := READER_STATE_IDLE");
            }
        });

        return true;
    }

    @Override
    public boolean isAlive() {
        return reader.isAlive();
    }

    @Override
    public void terminateToRead() throws NullPointerException {

        //
        // Throws NullPointException.
        // Fixed bug 129: RFID SDK NullPointException.
        //

//        reader.readTerminate();
        reader.readStop(false);
    }

    @Override
    public void stopReader(boolean needCallBack) {
        reader.readStop(needCallBack);
    }

    @Override
    public void netIsAlive() {
        reader.netIsAlive();
    }

    //
    // Implement RfidReaderViewable.
    //

    @Override
    public void vSendMessage(int msgType, String msgText) {

        if (msgType == RfidReaderViewable.MSG_READ_STOPPED) {

            this.bp.generateBillCallBack("RecognizeItems");

        } else if (msgType == RfidReaderViewable.MSG_GET_TID) {

            getAntennaFailTimes = 0;

            int readCount = this.reader.tagList.size();
            Log.d(TAG, "vSendMessage() readCount = " + readCount);

            int readTagCount = this.reader.tagSet.size();
            Log.d(TAG, "vSendMessage() readTagCount = " + readTagCount);

            String tidList = Util.generateTidListJson(this.reader.tagSet);
            JSONObject tidListJson = null;
            try {
                tidListJson = new JSONObject(tidList);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            Log.d(TAG, "vSendMessage() tidListJson = " + tidListJson);

            this.bp.tidListReadCallBack(tidListJson);

        } else if (msgType == RfidReaderViewable.MSG_DISCONNECT) {
            bp.stateCodeCallBack(Breakdown.RFID_READER_DISCONNECT);
        }
    }
}
