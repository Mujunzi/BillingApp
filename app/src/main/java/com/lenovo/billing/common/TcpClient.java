package com.lenovo.billing.common;

import java.net.*;
import java.io.*;

import android.util.Log;

public class TcpClient extends Thread implements Reactive {

    public static String TAG = "TcpClient";

    private String mServerIp;
    private int mServerPort = 5000;

    private Socket mSocket;

    private boolean willConnect = false;

    private boolean isTerminated = false;

    public TcpClient(String serverIp, int serverPort) {
        this.mServerIp = serverIp;
        this.mServerPort = serverPort;
    }

    private Reactive mReactive;

    public void registerReactive(Reactive reactive) {
        mReactive = reactive;
    }

    @Override
    public void run() {

        while (true) {

            try {

                Util.delay(500);

                if (this.willConnect) {
                    doConnect();
                }

                if (this.isTerminated) {
                    break;
                }

                if (mSocket == null) {
                    continue;
                }

                receiveCommands();

            } catch (SocketException e) {
                e.printStackTrace();
                willReconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void receiveCommands() throws IOException {
        BufferedInputStream in = new BufferedInputStream(mSocket.getInputStream());
        BufferedReader inStream = new BufferedReader(new InputStreamReader(in));
        byte[] bytes = new byte[1024];
        int length;

        Util.delay(10);                 // delay by 10 ms.

        if (!inStream.ready()) {
            return;
        }

        length = in.read(bytes);
        if (length <= 0) {
            willReconnect();            // disconnect the client from the server socket and will reconnect.
            return;
        }

        mReactive.reactiveHandle(bytes, length);
    }

    //
    // The function sets the willConnect flag to true. Why not directly create socket here?
    // The reason is for Android, the socket functions cannot be called by main thread.
    //

    public void connect() {
        this.willConnect = true;
    }

    //
    // The function create a client socket. It is called by the emulator thread not by the main thread.
    // We use the way to avoid app crashed when creating client socket in main thread.
    //

    private void doConnect() throws IOException {
        System.out.println("doConnect()");

        //
        // Don't connect twice
        //

        if (mSocket != null) {
            System.out.println("Don't connect twice.");
            return;
        }

        mSocket = new Socket(mServerIp, mServerPort);
        //mSocket.setSoTimeout(5 * 1000);  // set read timed out.
        this.willConnect = false;

        //this.viewer.updateStatus ();
    }

    public void willReconnect() {
        Log.d(TAG, "willReconnect()");

        disconnect();
        this.willConnect = true;
    }

    public void disconnect() {
        System.out.println("disconnect()");

        try {
            if (mSocket != null) {
                mSocket.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        mSocket = null;
    }

    @Override
    public void reactiveHandle(byte bytes[], int length) {
        try {
            Util.sendBytes(mSocket, bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
    