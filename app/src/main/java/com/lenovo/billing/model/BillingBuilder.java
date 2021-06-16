package com.lenovo.billing.model;

import com.lenovo.billing.smartequipment.*;
import com.lenovo.billing.cardpay.EmployeeCardPayment;
import com.lenovo.billing.protocol.BillingConfig;
import com.lenovo.billing.rfid.*;

class BillingBuilder {

    private static final String TAG = BillingBuilder.class.getSimpleName();

    BillingBuilder() {
        ;
    }

    RfidReader buildRfidReader() {
        RfidReader reader = null;

        if (BillingConfig.RR_TYPE.equals("invengoSdk")) {
            reader = new RfidReaderInvengoSdk();
        }

        return reader;
    }

    FaceDetection buildFaceDetection() {
        return new FaceDetection();
    }

    BillingClient buildBillingClient() {
        return new BillingClient();
    }

    SmartEquipment buildSmartEquipment(SmartEquipmentViewable viewer) {
        SmartEquipment se = null;
        switch (BillingConfig.SE_TYPE) {
            case 1:
                //se = new SmartEquipmentTcp (viewer);
                break;
            case 2:
                //se = new SmartEquipmentUdp (viewer);
                break;
            case 3:
                //se = new SmartEquipmentUdp2 (viewer);
                break;
            case 4:
                System.out.println("--> new SmartEquipmentFsm (viewer)");
                se = new SmartEquipmentFsm(viewer);
                break;
        }
        return se;
    }

    RemoteController buildRemoteController() {
        return new RemoteController();
    }

    EmployeeCardPayment buildEmployeeCardPayment() {
        return new EmployeeCardPayment();
    }

}
