package com.lenovo.billing.smartequipment;

import com.lenovo.billing.common.EventHandler;
import com.lenovo.billing.protocol.*;

public abstract class SmartEquipment
        implements EventHandler {

    public static final String EVENT_ZONE_NOT_OCC   = "0100 ";
    public static final String EVENT_ZONE_OCC       = "0101 ";
    public static final String EVENT_DOOR_2_CLOSED  = "0220 ";
    public static final String EVENT_DOOR_2_OPENED  = "0221 ";
    public static final String EVENT_DOOR_3_CLOSED  = "0230 ";
    public static final String EVENT_DOOR_3_OPENED  = "0231 ";
    public static final String EVENT_BB_PRESSED     = "0301 ";
    public static final String EVENT_BB_RELEASED    = "0302 ";
    public static final String EVENT_IR_LINE_1      = "0411 ";
    public static final String EVENT_IR_LINE_2      = "0421 ";
    public static final String EVENT_IR_LINE_3      = "0431 ";

    //
    // Maintain the following statuses in the app.
    //

    public boolean isOccupied = false; // status of the zone.

    //
    // Methods.
    //

    public abstract void start();

    public abstract void stop();

    public abstract void register(BillingProcess bp);

    public abstract boolean isAlive();

    public abstract boolean isDoor3Closed();

    public abstract void resetDoors();
}