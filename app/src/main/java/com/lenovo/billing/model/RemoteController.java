//
// A UDP socket that receives events from a server.
//

package com.lenovo.billing.model;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.lang.Thread;

import android.util.Log;

import com.lenovo.billing.protocol.*;
import com.lenovo.billing.common.*;

public class RemoteController extends Thread {

    private static final String TAG = RemoteController.class.getSimpleName();

    private DatagramSocket dsForServer;
    private boolean isInterrupt;        // Default is false. Set it true when closing socket before app exits.

    //
    // Call-back functions.
    //

    private BillingProcess bp;

    public void register(BillingProcess bp) {
        this.bp = bp;
    }

    @Override
    public void run() {
        while (true) {
            Util.delay(10);       // delay by 10 ms.
            if (!isInterrupt) {
                receiveEventsFromServer();
            }
        }
    }

    private void receiveEventsFromServer() {
        try {

            if (dsForServer == null) {

                dsForServer = new DatagramSocket(BillingConfig.RC_UDP_LISTEN_PORT);
                dsForServer.setReuseAddress(true);
            }

            byte[] inBuf = new byte[1024];
            DatagramPacket inPacket = new DatagramPacket(inBuf, inBuf.length);
            dsForServer.receive(inPacket);

            String hostAddress = inPacket.getAddress().getHostAddress();
            if (!hostAddress.equals(BillingConfig.RC_UDP_SOURCE_ADDR)) {
                Log.d(TAG, "Event from invalid host:" + hostAddress);
                return;
            }

            String event = new String(inPacket.getData(), 0, inPacket.getLength());
            Log.d(TAG, "Event from NowGo Server:" + event);

            bp.remoteControlCallBack(event);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void closeSocket() {

        isInterrupt = true;

        if (dsForServer != null) {
            dsForServer.close();
        }
    }
}
