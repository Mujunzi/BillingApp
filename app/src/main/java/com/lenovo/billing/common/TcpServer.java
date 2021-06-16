package com.lenovo.billing.common;

import java.io.*;
import java.net.*;

import android.util.Log;

public class TcpServer extends Thread {

    public static final String TAG = TcpServer.class.getSimpleName();

    private int mPort;
    private int mRecoverTimeout;

    public TcpServer(int port, int recoverTimeout) {
        mPort = port;
        mRecoverTimeout = recoverTimeout;
    }

    //
    // For socket.
    //

    private ServerSocket ss;
    private boolean willOpen = false;
    private Socket server = null;

    public void openSocket() {
        this.willOpen = true;
    }

    private synchronized void doOpenSocket() throws IOException {
        //
        // close old socket before open server socket
        //

        closeSocket();

        Log.d(TAG, "doOpenSocket()");

        this.ss = new ServerSocket(mPort);
        Log.d(TAG, "Server is created. Waiting for connection...");

        //ss.setSoTimeout (5*1000);   // Set connection timed out.

        this.server = this.ss.accept();

        if (mRecoverTimeout > 0) {
            Log.d(TAG, "Set timeout " + mRecoverTimeout);
            this.server.setSoTimeout(mRecoverTimeout);
        }

        this.server.setReuseAddress(true);
        Log.d(TAG, "Client is connected, IP: " + this.server.getInetAddress());

        this.willOpen = false;

    }

    public synchronized void closeSocket() {
        Log.d(TAG, "closeSocket()");

        try {

            if (this.ss != null) {
                this.ss.close();
            }

            if (this.server != null) { // server is null if connection is broken.
                this.server.close();
            }

            this.ss = null;
            this.server = null;
            this.willOpen = true;

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //
    // extends Thread
    //

    @Override
    public void run() {
        while (true) {

            Util.delay(500);           // delay by 500 ms.

            try {

                if (this.willOpen) {
                    doOpenSocket();
                }

                if (this.server == null || this.ss == null) {
                    continue;
                }

                receiveEvents();

            } catch (IOException e) { // contains SocketException
                e.printStackTrace();
                this.willOpen = true;
            }
        }
    }

    private void receiveEvents() throws IOException {
        BufferedInputStream in = new BufferedInputStream(this.server.getInputStream());
        byte[] b = new byte[1024];
        int length;

        Util.delay(10);            // delay by 10 ms.
        length = in.read(b);       // It maybe timeout if the client is disconnected.
        if (length <= 0) {

            //
            // length <=0 happened when the "in" stream take over.
            //

            // closeSocket ();         // close the server socket.
            return;
        }
        Log.d(TAG, "socket read data, length := " + length);
        mReactive.reactiveHandle(b, length);
    }

    private Reactive mReactive;

    public void registerReactive(Reactive reactive) {
        mReactive = reactive;
    }

    //
    // The main method for testing.
    //

    public static void main(String[] args) {
        TcpServer server = new TcpServer(6000, 10 * 1000);
        server.start();
        server.openSocket();
    }
}
