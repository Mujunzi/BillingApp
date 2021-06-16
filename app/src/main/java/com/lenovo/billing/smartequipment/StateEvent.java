package com.lenovo.billing.smartequipment;

public class StateEvent {
    public static final String APP_STARTED = "appStarted ";
    public static final String BILL_VERIFIED = "billVerified ";
    public static final String BILL_REFRESHED = "billRefreshed ";
    public static final String CONTROL_ACQUIRED = "controlAcquired ";
    public static final String CONTROL_RELEARSED = "controlReleased ";
    public static final String DEVICE_BAD = "deviceBad ";
    

    public static enum StateEventEnum {
        APP_STARTED("appStarted "),
        BILL_VERIFIED("billVerified "),
        BILL_REFRESHED("billRefreshed "),
        CONTROL_ACQUIRED("controlAcquired "),
        CONTROL_RELEARSED("controlReleased "),
        EVENT_ZONE_NOT_OCC("0100 "),
        EVENT_ZONE_OCC("0101 "),
        EVENT_DOOR_2_CLOSED("0220 "),
        EVENT_DOOR_2_OPENED("0221 "),
        EVENT_DOOR_3_CLOSED("0230 "),
        EVENT_DOOR_3_OPENED("0231 "),
        EVENT_BB_PRESSED("0301 "),
        EVENT_BB_RELEASED("0302 "),
        EVENT_IR_LINE_1("0411 "),
        EVENT_IR_LINE_2("0421 "),
        EVENT_IR_LINE_3("0431 "),
        DEVICE_BAD("deviceBad ");
        
        private String typeName;
 
        StateEventEnum(String typeName) {
            this.typeName = typeName;
        }
 
        public static StateEventEnum fromTypeName(String typeName) {
            for (StateEventEnum type : StateEventEnum.values()) {
                if (type.getTypeName().equals(typeName)) {
                    return type;
                }
            }
            return null;
        }
 
        public String getTypeName() {
            return this.typeName;
        }
    }
    
}