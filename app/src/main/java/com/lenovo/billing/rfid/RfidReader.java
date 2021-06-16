//
// RfidReader is a Java interface that work with vendor's SDK to provide API.
// It supports the following methods:
//      readTid - It reads RFID TIDs from RFID Reader by given a time interval.
//

package com.lenovo.billing.rfid;

import com.lenovo.billing.protocol.*;

public interface RfidReader {

    void register(BillingProcess bp);
    boolean readTidList(int startDelay, int duration);
    boolean isAlive();
    void terminateToRead();
    void stopReader(boolean needCallBack);
}
