package com.lenovo.billing.cardpay;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

/**
 * clientSocket server
 * Created by xpw on 18-7-3.
 */
public class PaymentServer {

    private static final String TAG = PaymentServer.class.getSimpleName();

    // server state
    private static int serverState = 0; // S0:normal S1: request charge S2: cancel charge

    private int remainAmount = 0; // card remain amount
    private String money = "0"; // charge money

    private ServerSocket serverSocket;
    private static boolean runSocketTag = false;
    private Socket clientSocket;
    private static final int socketTimeOut = 0; // socket timeout ms
    private static final int PORT = 5000; // socket port

    private static final int CANCEL_CHARGE_RESPONSE_TIME_OUT = 1500; // ms

    private int missHeartBeatTimes = 0; // miss POS send heart beat times, >3；POS timeout
    private long getPosHeartBeatMills = 0;

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case PosConfig.POS_DEVICE_UART_TIMEOUT:
                    cardPaymentHandler.deviceStatusCallBack(PosConfig.POS_DEVICE_UART_TIMEOUT);
                    break;
                case PosConfig.POS_DEVICE_SOCKET_IS_TIMEOUT:
                    cardPaymentHandler.deviceStatusCallBack(PosConfig.POS_DEVICE_SOCKET_IS_TIMEOUT);
                    break;
                case PosConfig.POS_DEVICE_IS_OFFLINE:
                    cardPaymentHandler.deviceStatusCallBack(PosConfig.POS_DEVICE_IS_OFFLINE);
                    break;
                case PosConfig.POS_Client_SOCKET_IS_NULL:
                    cardPaymentHandler.deviceStatusCallBack(PosConfig.POS_Client_SOCKET_IS_NULL);
                    break;
                ////////////
                case PosConfig.CHARGE_FAILURE_REASON_BAD_REQUEST_BODY: // POS: params err
                case PosConfig.CHARGE_FAILURE_REASON_DATABASE_ERROR: // POS: sql err
                case PosConfig.CHARGE_FAILURE_REASON_DESTROY_CARD: // destroy card
                case PosConfig.CHARGE_FAILURE_REASON_EXCEED_AUTO_PAY_TIMES:
                case PosConfig.CHARGE_FAILURE_REASON_EXCEED_DAY_LINIT: //
                case PosConfig.CHARGE_FAILURE_REASON_FULL_BALANCE: //
                case PosConfig.CHARGE_FAILURE_REASON_LESS_BALANCE: //
                case PosConfig.CHARGE_FAILURE_REASON_MINUS_BALANCE: //
                case PosConfig.CHARGE_FAILURE_REASON_MISSED_CARD: //
                case PosConfig.CHARGE_FAILURE_REASON_PERMISSION_DENIED_ON_DEVICE: //
                case PosConfig.CHARGE_FAILURE_REASON_PERMISSION_DENIED_TIME_INTERVAL: //
                case PosConfig.CHARGE_FAILURE_REASON_PERMISSION_DENIED_TIME_NOW: //
                case PosConfig.CHARGE_FAILURE_REASON_POS_DEVICE_CONNECT_TIME_OUT: //
                case PosConfig.CHARGE_FAILURE_REASON_RECORD_EXIST: //
                case PosConfig.CHARGE_FAILURE_REASON_UNKNOWN_CARD: //
                case PosConfig.CHARGE_FAILURE_REASON_SWIPE_FAST: //
                case PosConfig.CHARGE_FAILURE_REASON_OTHER:
                    cardPaymentHandler.chargeResponseCallBack(msg.what, "", -1);
                    break;
                /////////
                case PosConfig.CHARGE_SUCCESS:
                    cardPaymentHandler.chargeResponseCallBack(PosConfig.CHARGE_SUCCESS, "", msg.arg1);
                    break;
                case PosConfig.RECHARGE_REQUEST:
                    cardPaymentHandler.chargeResponseCallBack(PosConfig.RECHARGE_REQUEST, "", -1);
                    break;
                case PosConfig.CANCEL_CHARGE_SUCCESS:
                    cardPaymentHandler.chargeResponseCallBack(PosConfig.CANCEL_CHARGE_SUCCESS, "", -1);
                    break;
                case PosConfig.CANCEL_CHARGE_FAILURE:
                    cardPaymentHandler.chargeResponseCallBack(PosConfig.CANCEL_CHARGE_FAILURE, "", -1);
                    break;
                case PosConfig.CANCEL_CHARGE_RESPONSE_TIME_OUT:
                    cardPaymentHandler.chargeResponseCallBack(PosConfig.CANCEL_CHARGE_RESPONSE_TIME_OUT, "", -1);
                    break;
            }
        }
    };


    private CardPaymentHandler cardPaymentHandler;

    PaymentServer() {
    }

    void registerCallBack(CardPaymentHandler cardPaymentHandler) {
        this.cardPaymentHandler = cardPaymentHandler;
    }

    void openServer() {
        runSocketTag = true;
        init();
    }

    void closeServer() {
        runSocketTag = false;
        try {
            reset();
        } catch (Exception e) {
            Log.d(TAG, "serverSocket close err : " + e.getMessage());
        }
    }

    //
    // Build TCP server socket connected from POS bridge.
    //

    private void init() {
        new Thread(new Runnable() {
            @Override
            public void run() {

                while (runSocketTag) { // BUGBUG. Should add delay otherwise exceptions are quickly sent.
                    try {
                        Thread.sleep(500); // Delay 500 ms.
                        if (serverSocket == null || serverSocket.isClosed()) {
                            serverSocket = new ServerSocket(PORT); // PORT = 5000
                            serverSocket.setSoTimeout(socketTimeOut); // socketTimeOut = always listen
                            getPosHeartBeatMills = System.currentTimeMillis();
                            if (heartBeatThread == null) {
                                heartBeatThread = new HeartBeatCheckThread();
                            }
                        }
                        if (clientSocket == null || (clientSocket != null && !clientSocket.isConnected())) {
                            Log.d(TAG, "server socket accept() ********");
                            clientSocket = serverSocket.accept();
                            clientSocket.setSoTimeout(socketTimeOut);
                            clientSocket.setKeepAlive(true);
                            Log.d(TAG, "client ip is : " + clientSocket.getInetAddress());
                            clear();    // send a clear command to POS bridge.
                            if (readHandlerThread == null) {
                                getClientResponse();
                            }
                        }

                    } catch (Exception e) {
                        Log.d(TAG, "server socket err: " + e.getMessage());
                        // timeout relink
                        reset();
                    }
                }
            }
        }).start();
    }

    private void checkSocketState() {

        while (runSocketTag) {
            try {
                Thread.sleep(5);        // Delay 5 ms.                
                if (serverSocket == null || serverSocket.isClosed()) {
                    serverSocket = new ServerSocket(PORT);
                    serverSocket.setSoTimeout(socketTimeOut);
                }

                if (clientSocket == null || (clientSocket != null && !clientSocket.isConnected())) {
                    Log.d(TAG, " checkSocketState server socket accept() ********");
                    clientSocket = serverSocket.accept();
                    clientSocket.setSoTimeout(socketTimeOut);
                    Log.d(TAG, "client ip is : " + clientSocket.getInetAddress());

                    clear();            // send a clear command to POS bridge.
                } else {
                    break;
                }
            } catch (Exception e) {
                Log.d(TAG, "checkSocketState : err: " + e.getMessage());
            }
        }

    }


    void requestCharge(String money) {
        if (serverSocket == null || clientSocket == null || clientSocket.isClosed()) {
            Log.d(TAG, "socket is disconnected");
            Message msg = handler.obtainMessage();
            msg.what = PosConfig.POS_Client_SOCKET_IS_NULL;
            handler.sendMessage(msg);
            return;
        }

        if ( serverState == 0 ) {
            serverState = 1;
            this.money = money;
            new WriteHandlerThread();
        } else {
            // catch recharge request
            Log.d(TAG, "catch rechargeRequest");
            // failure on return chargeRequest
            Message msg = handler.obtainMessage();
            msg.what = PosConfig.RECHARGE_REQUEST;
            handler.sendMessage(msg);
        }
    }

    void cancelCharge() {

        if (serverSocket == null || clientSocket == null || clientSocket.isClosed()) {
            Log.d(TAG, "socket is disconnected");
            Message msg = handler.obtainMessage();
            msg.what = PosConfig.POS_Client_SOCKET_IS_NULL;
            handler.sendMessage(msg);
            return;
        }

        if ( serverState == 0 || serverState == 1 ) {
            cancelChargeCheckThread = new CancelChargeCheckThread(serverState);
            serverState = 2;
            new WriteHandlerThread();

        } else {
            // catch recancel request
            Log.d(TAG, "catch recancelChargeRequest");
        }
    }

    private void clear() {
        if ( serverState != 2 ) {
            serverState = 2;
            new WriteHandlerThread();
        }
    }

    //----------------------------write data to POS------------------------------------//
    private class WriteHandlerThread implements Runnable {
        WriteHandlerThread() {
            new Thread(this).start();
        }

        public void run() {
            try {
                checkSocketState();
                // send msg to client (pos)
                DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
                byte[] orderBytes = parseMoneyToBytes(serverState == 1, money);
                out.write(orderBytes);
//                out.close();

            } catch (Exception e) {
                Log.d(TAG, "socket write run err: " + e.getClass().getSimpleName());
                reset();
                requestCharge(money);
            }
        }
    }

    /**
     * parse the money to byte[]
     *
     * @param payOrCancel true: is send charge request. false : cancel charge request
     * @param money       money
     * @return money to byte[]
     */
    private byte[] parseMoneyToBytes(boolean payOrCancel, String money) {
        Log.d(TAG, "isRequestCharge: " + payOrCancel + ", money : " + money);
        byte[] bytes;
        if ( payOrCancel ) {
            int srcNum = Integer.parseInt(money);
            //Log.d(TAG, "srcNum = " + srcNum);

            PosConfig.CHARGE_REQUEST_BYTE_ARRAY[3] = (byte) ((srcNum & 0xff000000) >> 24);
            PosConfig.CHARGE_REQUEST_BYTE_ARRAY[4] = (byte) ((srcNum & 0x00ff0000) >> 16);
            PosConfig.CHARGE_REQUEST_BYTE_ARRAY[5] = (byte) ((srcNum & 0x0000ff00) >> 8);
            PosConfig.CHARGE_REQUEST_BYTE_ARRAY[6] = (byte) (srcNum & 0x000000ff);

            bytes = PosConfig.CHARGE_REQUEST_BYTE_ARRAY;
            Log.d(TAG, "CHARGE_REQUEST_BYTE_ARRAY = " + Arrays.toString(bytes));
        } else {
            bytes = PosConfig.CANCEL_CHARGE_REQUEST_BYTE_ARRAY;
            Log.d(TAG, "CANCEL_CHARGE_REQUEST_BYTE_ARRAY = " + Arrays.toString(bytes));
        }

        return bytes;
    }

    /**
     * parse client response
     *
     * @param responseBytes byte[] bytes
     * @param len           bytes's truth data len
     */
    private void parseResponse(byte[] responseBytes, int len) {

        if ( len <= 0 ) {
            return;
        }
        byte[] datas = new byte[len];
        System.arraycopy(responseBytes, 0, datas, 0, len);

        Log.d(TAG, "client send data's len: " + len);
        String clientResponseBytesStr = Arrays.toString(datas);
        // Log.d(TAG, "client send data is : " + clientResponseBytesStr);

        if ( len >= PosConfig.GOT_CARD_BYTE_ARRAY.length
                && PosConfig.GOT_CARD_BYTE_ARRAY[0] == datas[0]
                && PosConfig.GOT_CARD_BYTE_ARRAY[1] == datas[1]
                && PosConfig.GOT_CARD_BYTE_ARRAY[2] == datas[2]
                && PosConfig.GOT_CARD_BYTE_ARRAY[3] == datas[3]
                && PosConfig.GOT_CARD_BYTE_ARRAY[4] == datas[4]
                && PosConfig.GOT_CARD_BYTE_ARRAY[5] == datas[5]
                && PosConfig.GOT_CARD_BYTE_ARRAY[6] == datas[6]
                && PosConfig.GOT_CARD_BYTE_ARRAY[11] == datas[11]
                && PosConfig.GOT_CARD_BYTE_ARRAY[12] == datas[12]
                && PosConfig.GOT_CARD_BYTE_ARRAY[13] == datas[13] ) { // len 14

            remainAmount = (datas[7] << 24 & 0xff000000)
                    + (datas[8] << 16 & 0x00ff0000)
                    + (datas[9] << 8 & 0x0000ff00)
                    + (datas[10] & 0x000000ff);

            Log.d(TAG, "got card :" + clientResponseBytesStr);
            Log.d(TAG, "the remain amount before charging is : " + remainAmount);
        } else if ( Arrays.toString(PosConfig.OFF_LINE_CHARGE_SUCCESS_ARRAY).equalsIgnoreCase(clientResponseBytesStr)
                || (len >= PosConfig.ON_LINE_CHARGE_SUCCESS_ARRAY.length
                && PosConfig.ON_LINE_CHARGE_SUCCESS_ARRAY[0] == datas[0]
                && PosConfig.ON_LINE_CHARGE_SUCCESS_ARRAY[1] == datas[1]
                && PosConfig.ON_LINE_CHARGE_SUCCESS_ARRAY[2] == datas[2]
                && PosConfig.ON_LINE_CHARGE_SUCCESS_ARRAY[3] == datas[3]
                && PosConfig.ON_LINE_CHARGE_SUCCESS_ARRAY[4] == datas[4]
                && PosConfig.ON_LINE_CHARGE_SUCCESS_ARRAY[9] == datas[9]) ) {
            // online charge success || offline charge success

            serverState = 0;

            Log.d(TAG, "charge success -----------" + clientResponseBytesStr);
            // charge success
            if ( len >= PosConfig.ON_LINE_CHARGE_SUCCESS_ARRAY.length ) {
                int amount = remainAmount - Integer.parseInt(money);
                Log.d(TAG, "charge success --------- remain amount(remain - money) = " + amount);
                int posAmount = (datas[5] << 24 & 0xff000000)
                        + (datas[6] << 16 & 0x00ff0000)
                        + (datas[7] << 8 & 0x0000ff00)
                        + (datas[8] & 0x000000ff);
                Log.d(TAG, "charge success --------- remain amount (POS back response) = " + posAmount);
                Message msg = handler.obtainMessage();
                msg.what = PosConfig.CHARGE_SUCCESS;
                msg.arg1 = posAmount;
                handler.sendMessage(msg);
            } else {
                Message msg = handler.obtainMessage();
                msg.what = PosConfig.CHARGE_SUCCESS;
                handler.sendMessage(msg);
            }
            // state 0
            money = "0";
        } else if ( len >= PosConfig.CHARGE_FAILURE_ARRAY.length
                && PosConfig.CHARGE_FAILURE_ARRAY[0] == datas[0]
                && PosConfig.CHARGE_FAILURE_ARRAY[1] == datas[1]
                && PosConfig.CHARGE_FAILURE_ARRAY[2] == datas[2]
                && PosConfig.CHARGE_FAILURE_ARRAY[3] == datas[3]
                && PosConfig.CHARGE_FAILURE_ARRAY[5] == datas[5] ) { // len 6
            serverState = 0;
            Log.d(TAG, "charge failure response is " + clientResponseBytesStr);
            Log.d(TAG, "charge failure, the reason is : " + datas[4]);
            Message msg = handler.obtainMessage();
            switch (datas[4]) {
                case PosConfig.CHARGE_FAILURE_REASON_BAD_REQUEST_BODY: // POS: params err
                case PosConfig.CHARGE_FAILURE_REASON_DATABASE_ERROR: // POS: sql err
                case PosConfig.CHARGE_FAILURE_REASON_DESTROY_CARD: // destroy card
                case PosConfig.CHARGE_FAILURE_REASON_EXCEED_AUTO_PAY_TIMES:
                case PosConfig.CHARGE_FAILURE_REASON_EXCEED_DAY_LINIT: //
                case PosConfig.CHARGE_FAILURE_REASON_FULL_BALANCE: //
                case PosConfig.CHARGE_FAILURE_REASON_LESS_BALANCE: //
                case PosConfig.CHARGE_FAILURE_REASON_MINUS_BALANCE: //
                case PosConfig.CHARGE_FAILURE_REASON_MISSED_CARD: //
                case PosConfig.CHARGE_FAILURE_REASON_PERMISSION_DENIED_ON_DEVICE: //
                case PosConfig.CHARGE_FAILURE_REASON_PERMISSION_DENIED_TIME_INTERVAL: //
                case PosConfig.CHARGE_FAILURE_REASON_PERMISSION_DENIED_TIME_NOW: //
                case PosConfig.CHARGE_FAILURE_REASON_POS_DEVICE_CONNECT_TIME_OUT: //
                case PosConfig.CHARGE_FAILURE_REASON_RECORD_EXIST: //
                case PosConfig.CHARGE_FAILURE_REASON_UNKNOWN_CARD: //
                    msg.what = datas[4];
                    break;
                default:
                    msg.what = PosConfig.CHARGE_FAILURE_REASON_OTHER;
                    break;
            }
            money = "0";
            handler.sendMessage(msg);
        } else if ( len >= PosConfig.POS_HEART_BEAT.length
                && Arrays.toString(PosConfig.POS_HEART_BEAT).equalsIgnoreCase(clientResponseBytesStr) ) {

            Log.d(TAG, "heart beat --- " + clientResponseBytesStr);

            getPosHeartBeatMills = System.currentTimeMillis();
            if ( heartBeatThread == null ) {
                heartBeatThread = new HeartBeatCheckThread();
            }

        } else if ( len >= PosConfig.POS_UART_TIMEOUT_ARRAY.length
                && Arrays.toString(PosConfig.POS_UART_TIMEOUT_ARRAY).equalsIgnoreCase(clientResponseBytesStr) ) {

            Log.d(TAG, "POS_UART_TIMEOUT_ARRAY --- " + clientResponseBytesStr);

            serverState = 0;
            Message msg = handler.obtainMessage();
            msg.what = PosConfig.POS_DEVICE_UART_TIMEOUT;
            handler.sendMessage(msg);
        } else if ( len >= PosConfig.POS_OFFLINE_ARRAY.length
                && clientResponseBytesStr.contains(Arrays.toString(PosConfig.POS_OFFLINE_ARRAY)) ) {

            serverState = 0;
            Log.d(TAG, "POS_DEVICE_IS_OFFLINE --- " + clientResponseBytesStr);
            Message msg = handler.obtainMessage();
            msg.what = PosConfig.POS_DEVICE_IS_OFFLINE;
            handler.sendMessage(msg);
        } else if ( len >= PosConfig.ERROR_COMMAND_ARRAY.length &&
                clientResponseBytesStr.equalsIgnoreCase(Arrays.toString(PosConfig.ERROR_COMMAND_ARRAY)) ) {
            // error command
            Log.d(TAG, "error command ---------" + clientResponseBytesStr);

        } else if ( len >= PosConfig.CHARGE_REQUEST_ACK_ARRAY.length
                && clientResponseBytesStr.contains(Arrays.toString(PosConfig.CHARGE_REQUEST_ACK_ARRAY)) ) {
            // charge request ack

            Log.d(TAG, "charge request ACK ---------" + clientResponseBytesStr);

        } else if ( len >= PosConfig.CANCEL_SUCCESS_BYTE_ARRAY.length
                && clientResponseBytesStr.contains(Arrays.toString(PosConfig.CANCEL_SUCCESS_BYTE_ARRAY)) ) {

            serverState = 0;
            Log.d(TAG, "cancel request success ------------ " + clientResponseBytesStr);

            Message msg = handler.obtainMessage();
            msg.what = PosConfig.CANCEL_CHARGE_SUCCESS;
            handler.sendMessage(msg);

            money = "0";

        } else if ( len >= PosConfig.CANCEL_FAILURE_BYTE_ARRAY.length
                && Arrays.toString(PosConfig.CANCEL_FAILURE_BYTE_ARRAY).equalsIgnoreCase(clientResponseBytesStr) ) {

            serverState = 0;
            Log.d(TAG, "cancel request failure ------------" + clientResponseBytesStr);
            Message msg = handler.obtainMessage();
            msg.what = PosConfig.CANCEL_CHARGE_FAILURE;
            handler.sendMessage(msg);

            money = "0";
        } else {
            // unknown response
            Log.d(TAG, "unknown response : " + clientResponseBytesStr);
        }

    }

    // *******************************************************************************//
    private ReadHandlerThread readHandlerThread;

    private void getClientResponse() {
        if ( readHandlerThread == null ) {
            readHandlerThread = new ReadHandlerThread();
        }
    }

    private class ReadHandlerThread implements Runnable {

        DataInputStream input;

        ReadHandlerThread() {
            new Thread(this).start();
        }

        public void run() {
            while (true) {
                try {
                    checkSocketState();

                    // read msg from client socket
                    input = new DataInputStream(clientSocket.getInputStream());
                    byte[] bytes = new byte[14];
                    int len = input.read(bytes);
                    parseResponse(bytes, len);

                    //input.close();
                } catch (Exception e) {
                    Log.d(TAG, "read msg from client socket err: " + e.getClass().getSimpleName());
                    reset();
                    getClientResponse();
                    break;
                }
            }
        }
    }

    private HeartBeatCheckThread heartBeatThread;

    private class HeartBeatCheckThread implements Runnable {

        HeartBeatCheckThread() {
            new Thread(this).start();
        }

        public void run() {
            while (true) {
                try {
                    Thread.sleep(4000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if ( System.currentTimeMillis() - getPosHeartBeatMills > 30000 ) {
                    getPosHeartBeatMills = System.currentTimeMillis();
                    missHeartBeatTimes++;
                }

                if ( missHeartBeatTimes > 1 ) {
                    // TIME OUT
                    Log.d(TAG, "pos timeout -------");
                    Message msg = handler.obtainMessage();
                    msg.what = PosConfig.POS_DEVICE_SOCKET_IS_TIMEOUT;
                    handler.sendMessage(msg);
                    reset();
                    missHeartBeatTimes = 0;
                    getPosHeartBeatMills = System.currentTimeMillis();
                }
            }
        }
    }


    /**
     * cancel response time out
     */
    private CancelChargeCheckThread cancelChargeCheckThread;

    private class CancelChargeCheckThread implements Runnable {

        private int preServerState;

        public CancelChargeCheckThread(int serverState) {
            preServerState = serverState;
            new Thread(this).start();
        }

        public void run() {
            try {
                Thread.sleep(CANCEL_CHARGE_RESPONSE_TIME_OUT);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if ( serverState != 0 ) {
                // TIME OUT
                Log.d(TAG, "cancel response timeout -------");
                Message msg = handler.obtainMessage();
                msg.what = PosConfig.CANCEL_CHARGE_RESPONSE_TIME_OUT;
                handler.sendMessage(msg);
                serverState = preServerState;
            }
        }
    }

    public void reset() {
        try {
            if ( clientSocket != null ) {
                clientSocket.close();
            }
            if ( serverSocket != null ) {
                serverSocket.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            clientSocket = null;
            serverSocket = null;
        }

        readHandlerThread = null;
        serverState = 0;
    }
}
