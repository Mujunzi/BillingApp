package com.lenovo.billing.protocol;

import android.util.Log;

import org.json.*;

public class BillingConfig {
    private static final String TAG = BillingConfig.class.getSimpleName();

    private static String WEB_API = "http://127.0.0.1:8000/";

    //
    // billingApp.
    //

    public static String BA_IP = "192.168.1.11"; //B506
    public static String BA_APP_ID = "BillingApp";
    public static int BA_LOCATION_ID = 5;
    public static String BA_STORE_ID = "001";
    public static boolean BA_REPORT_STATUS = true;
    public static boolean BA_ENABLE_VOICE = true;
    public static String BA_CONTACT_US = "010-8888888";
    public static boolean BA_AUTO_RESTART_FOR_CRASH = false;
    public static boolean BA_AUTO_RECOVER_FOR_MALFUNCTION = false;
    public static int BA_HTTP_REQUEST_TIMEOUT = 15; // second, 0 : no timeout; value: 1 -> 2147483647

    //
    // camera
    //

    public static int CA_TYPE = 1;
    public static String CA_ID = "ip-cam-1";

    //
    // rfidReader
    //

    public static String RR_TYPE = "invengoSdk";
    //public static String RR_IP_PORT = "192.168.1.12:7086"; // B506
    public static String RR_IP_PORT = "192.168.6.210:7086"; //DEV.
    public static int RR_READ_DURATION = 2000; // ms
    public static int RR_START_DELAY = 500; // ms
    public static String RR_WEB_API = WEB_API + "read-tags.py";
    public static boolean RR_ENABLE = true;
    public static int RR_RECONNECT_COUNT = 10; // reconnectCount. 0: disable it.

    //
    // billingClient
    //

    private static String BC_SERVER = "http://10.110.131.187:8080"; //B506
    //public static String BC_GET_CUSTOMER_INFO_URL = BC_SERVER + "/nowgo-local/api/v1/customer/get-customer-info";
    public static String BC_GET_CUSTOMER_INFO_URL = BC_SERVER + "/lkl-local/api/v2/customer/get-customer-info";
    public static String BC_CUSTOMER_TRACKING = BC_SERVER + "/lkl-local/api/v1/customer/log-customer-tracking";
    public static String BC_GEN_BILL_URL = BC_SERVER + "/lkl-local/api/v1/order/gen-bill";
    public static String BC_IS_CHECKED_URL = BC_SERVER + "/lkl-local/api/v1/order/is-checked";
    public static String BC_DEVICE_STATUS_REPORT_URL = BC_SERVER + "/lkl-local/api/v1/monitor/report-device-status";
    public static String BC_CHECK_BILL_URL = BC_SERVER + "/lkl-local/api/v1/payment/check-bill";
    public static String BC_CUSTOMER_EXIT_URL = BC_SERVER + "/lkl-local/api/v1/payment/customer-exit";

    //
    // smartEquipment.
    //

    public static int SE_TCP_LISTEN_PORT = 6000;
    public static int SE_TYPE = 4;
    //public static String SE_UDP_SEND_ADDR = "192.168.1.14";  // B506
    public static String SE_UDP_SEND_ADDR = "192.168.6.108"; //DEV.
    public static int SE_UDP_SEND_PORT = 5000;
    public static int SE_CLOSE_DOOR3_DELAY = 1000;
    public static int SE_OPEN_DOOR2_DELAY = 4000;
    public static int SE_CLOSE_DOOR2_DELAY = 500;
    public static int SE_WAITE_FOR_BACK = 6000;
    public static int SE_WAITE_FOR_BACK_CLOSE_DOOR = 5000;

    public static int SE_EVENT_REPORTER_RECOVER_TIMEOUT = 10 * 1000; // 0 :disable it.
    public static int SE_EVENT_REPORTER_BAD_TIMEOUT = 20 * 1000; // 0 :disable it.

    public static int SE_TRY_DOOR_COUNT = 3; // 0:disable it.
    public static int SE_TRY_DOOR_PERIOD = 5000;
    public static boolean SE_ENABLE_SELF_RECOVER = false;
    public static boolean SE_NOT_TO_OPEN_DOOR3_IF_NO_SHOPPING = true;
    public static boolean SE_NOT_TO_OPEN_DOOR3_IF_NO_SHOPPING_DISPLAY_OPEN_DOOR_BUTTON = true;
    public static boolean SE_FAREWELL_BUTTON_ANIMATION = false;
    public static boolean SE_FAREWELL_NO_SHOPPING_PLAY_AUDIO = true;


    //
    // remoteManager
    //

    // only legal address can acquire control.
    public static boolean RC_ENABLE = true;
    public static String RC_UDP_SOURCE_ADDR = "10.100.9.146";
    public static int RC_UDP_LISTEN_PORT = 8080;

    //
    // faceDetect
    //

    public static int FD_DETECT_TIMEOUT = 5000;
    public static int FD_OPEN_CAMERA_DELAY = 1000;
    public static boolean FD_STOP_CAMERA_ONLY_FACE_DETECTED = true;

    //
    // employeeCard
    //

    public static boolean EC_ENABLE = true;

    //
    // test
    //

    public static boolean TE_FORCE_QR_CODE_PAY_FAIL = false; // test.forceQrCodePayFail
    public static boolean TE_FORCE_WECHAT_PORTRAIT_FAIL = false; // test.forceWechatPortraitFail
    public static boolean TE_FORCE_CARD_PAY_SUCCESS = false; // test.forceCardPaySuccess
    public static boolean TE_AUTO_RESTART = false; // test.autoRestart
    public static boolean TE_FORCE_UNCAUGHT_EXCEPTION = false; // test.forceUncaughtException

    public static void override(JSONObject configJson) {
        Log.d(TAG, "override()");

        //
        // If the BillingConfig.json doesn't exit, don't override.
        //

        if (configJson == null) {
            return;
        }

        try {

            //
            //Override billingApp.
            //

            if (configJson.has("billingApp")) {
                System.out.println("Override billingApp.");

                JSONObject billingApp = configJson.getJSONObject("billingApp");

                if (billingApp.has("ip")) {
                    System.out.println("Override billingApp.ip");
                    BillingConfig.BA_IP = billingApp.getString("ip");
                }

                if (billingApp.has("appId")) {
                    System.out.println("Override billingApp.appId");
                    BillingConfig.BA_APP_ID = billingApp.getString("appId");
                }

                if (billingApp.has("locationId")) {
                    System.out.println("Override billingApp.locationId");
                    BillingConfig.BA_LOCATION_ID = billingApp.getInt("locationId");
                }

                if (billingApp.has("storeId")) {
                    System.out.println("Override billingApp.storeId");
                    BillingConfig.BA_STORE_ID = billingApp.getString("storeId");
                }

                if (billingApp.has("reportStatus")) {
                    System.out.println("Override billingApp.reportStatus");
                    BillingConfig.BA_REPORT_STATUS = billingApp.getBoolean("reportStatus");
                }

                if (billingApp.has("enableVoice")) {
                    System.out.println("Override billingApp.enableVoice");
                    BillingConfig.BA_ENABLE_VOICE = billingApp.getBoolean("enableVoice");
                }

                if (billingApp.has("contactUs")) {
                    System.out.println("Override billingApp.contactUs");
                    BillingConfig.BA_CONTACT_US = billingApp.getString("contactUs");
                }

                if (billingApp.has("autoRestartForCrash")) {
                    System.out.println("Override billingApp.autoRestartForCrash");
                    BillingConfig.BA_AUTO_RESTART_FOR_CRASH = billingApp.getBoolean("autoRestartForCrash");
                }

                if (billingApp.has("autoRecoverForMalfunction")) {
                    System.out.println("Override billingApp.autoRecoverForMalfunction");
                    BillingConfig.BA_AUTO_RECOVER_FOR_MALFUNCTION = billingApp.getBoolean("autoRecoverForMalfunction");
                }

                if (billingApp.has("httpRequestTimeout")) {
                    System.out.println("Override billingApp.httpRequestTimeout");
                    BillingConfig.BA_HTTP_REQUEST_TIMEOUT = billingApp.getInt("httpRequestTimeout");
                }

            }

            //
            // Override rfidReader.
            //

            if (configJson.has("rfidReader")) {

                JSONObject rfidReader = configJson.getJSONObject("rfidReader");

                if (rfidReader.has("type")) {
                    System.out.println("Override rfidReader.type");
                    BillingConfig.RR_TYPE = rfidReader.getString("type");
                }

                if (rfidReader.has("ipPort")) {
                    System.out.println("Override rfidReader.ipPort");
                    BillingConfig.RR_IP_PORT = rfidReader.getString("ipPort");
                }

                if (rfidReader.has("readDuration")) {
                    System.out.println("Override rfidReader.readDuration");
                    BillingConfig.RR_READ_DURATION = rfidReader.getInt("readDuration");
                }

                if (rfidReader.has("startDelay")) {
                    System.out.println("Override rfidReader.startDelay");
                    BillingConfig.RR_START_DELAY = rfidReader.getInt("startDelay");
                }

                if (rfidReader.has("webApi")) {
                    System.out.println("Override rfidReader.webApi");
                    BillingConfig.RR_WEB_API = rfidReader.getString("webApi");
                }

                if (rfidReader.has("enable")) {
                    System.out.println("Override rfidReader.enable");
                    BillingConfig.RR_ENABLE = rfidReader.getBoolean("enable");
                }

                if (rfidReader.has("reconnectCount")) {
                    System.out.println("Override rfidReader.reconnectCount");
                    BillingConfig.RR_RECONNECT_COUNT = rfidReader.getInt("reconnectCount");
                }
            }

            //
            // Override billingClient
            //

            if (configJson.has("billingClient")) {

                JSONObject billingClient = configJson.getJSONObject("billingClient");

                if (billingClient.has("getCustomerInfoUrl")) {
                    System.out.println("Override billingClient.getCustomerInfoUrl");
                    BillingConfig.BC_GET_CUSTOMER_INFO_URL = billingClient.getString("getCustomerInfoUrl");
                }

                if (billingClient.has("genBillUrl")) {
                    System.out.println("Override billingClient.genBillUrl");
                    BillingConfig.BC_GEN_BILL_URL = billingClient.getString("genBillUrl");
                }

                if (billingClient.has("isCheckedUrl")) {
                    System.out.println("Override billingClient.isCheckedUrl");
                    BillingConfig.BC_IS_CHECKED_URL = billingClient.getString("isCheckedUrl");
                }

                if (billingClient.has("checkBillUrl")) {
                    System.out.println("Override billingClient.checkBillUrl");
                    BillingConfig.BC_CHECK_BILL_URL = billingClient.getString("checkBillUrl");
                }

                if (billingClient.has("customerExitUrl")) {
                    System.out.println("Override billingClient.customerExitUrl");
                    BillingConfig.BC_CUSTOMER_EXIT_URL = billingClient.getString("customerExitUrl");
                }

                if (billingClient.has("deviceStatusReportUrl")) {
                    System.out.println("Override billingClient.deviceStatusReportUrl");
                    BillingConfig.BC_DEVICE_STATUS_REPORT_URL = billingClient.getString("deviceStatusReportUrl");
                }

                if (billingClient.has("customerTrackingUrl")) {
                    System.out.println("Override billingClient.customerTrackingUrl");
                    BillingConfig.BC_CUSTOMER_TRACKING = billingClient.getString("customerTrackingUrl");
                }
            }

            //
            // Override smartEquipment
            //

            if (configJson.has("smartEquipment")) {

                JSONObject smartEquipment = configJson.getJSONObject("smartEquipment");

                if (smartEquipment.has("tcpListenPort")) {
                    System.out.println("Override smartEquipment.tcpListenPort");
                    BillingConfig.SE_TCP_LISTEN_PORT = smartEquipment.getInt("tcpListenPort");
                }

                if (smartEquipment.has("type")) {
                    System.out.println("Override smartEquipment.type");
                    BillingConfig.SE_TYPE = smartEquipment.getInt("type");
                }

                if (smartEquipment.has("udpSendAddr")) {
                    System.out.println("Override smartEquipment.udpSendAddr");
                    BillingConfig.SE_UDP_SEND_ADDR = smartEquipment.getString("udpSendAddr");
                }

                if (smartEquipment.has("udpSendPort")) {
                    System.out.println("Override smartEquipment.udpSendPort");
                    BillingConfig.SE_UDP_SEND_PORT = smartEquipment.getInt("udpSendPort");
                }

                if (smartEquipment.has("delayCloseDoorNum3")) {
                    System.out.println("Override smartEquipment.delayCloseDoorNum3");
                    BillingConfig.SE_CLOSE_DOOR3_DELAY = smartEquipment.getInt("delayCloseDoorNum3");
                }

                if (smartEquipment.has("delayOpenDoorNum2")) {
                    System.out.println("Override smartEquipment.delayOpenDoorNum2");
                    BillingConfig.SE_OPEN_DOOR2_DELAY = smartEquipment.getInt("delayOpenDoorNum2");
                }

                if (smartEquipment.has("delayCloseDoorNum2")) {
                    System.out.println("Override smartEquipment.delayCloseDoorNum2");
                    BillingConfig.SE_CLOSE_DOOR2_DELAY = smartEquipment.getInt("delayCloseDoorNum2");
                }

                if (smartEquipment.has("waitCloseForBack")) {
                    System.out.println("Override smartEquipment.waitCloseForBack");
                    BillingConfig.SE_WAITE_FOR_BACK = smartEquipment.getInt("waitCloseForBack");
                }

                if (smartEquipment.has("waitCloseForBackCloseDoor")) {
                    System.out.println("Override smartEquipment.waitCloseForBackCloseDoor");
                    BillingConfig.SE_WAITE_FOR_BACK_CLOSE_DOOR = smartEquipment.getInt("waitCloseForBackCloseDoor");
                }

                if (smartEquipment.has("eventReporterRecoverTimeout")) {
                    System.out.println("Override smartEquipment.eventReporterRecoverTimeout");
                    BillingConfig.SE_EVENT_REPORTER_RECOVER_TIMEOUT = smartEquipment.getInt("eventReporterRecoverTimeout");
                }

                if (smartEquipment.has("eventReporterBadTimeout")) {
                    System.out.println("Override smartEquipment.eventReporterBadTimeout");
                    BillingConfig.SE_EVENT_REPORTER_BAD_TIMEOUT = smartEquipment.getInt("eventReporterBadTimeout");
                }

                if (smartEquipment.has("tryDoorCount")) {
                    System.out.println("Override smartEquipment.tryDoorCount");
                    BillingConfig.SE_TRY_DOOR_COUNT = smartEquipment.getInt("tryDoorCount");
                }

                if (smartEquipment.has("tryDoorPeriod")) {
                    System.out.println("Override smartEquipment.tryDoorPeriod");
                    BillingConfig.SE_TRY_DOOR_PERIOD = smartEquipment.getInt("tryDoorPeriod");
                }

                if (smartEquipment.has("notToOpenDoorNum3IfNoShopping")) {
                    System.out.println("Override smartEquipment.notToOpenDoorNum3IfNoShopping");
                    BillingConfig.SE_NOT_TO_OPEN_DOOR3_IF_NO_SHOPPING = smartEquipment.getBoolean("notToOpenDoorNum3IfNoShopping");

                }

                if (smartEquipment.has("notToOpenDoorNum3IfNoShoppingDisplayOpenDoorButton")) {
                    System.out.println("Override smartEquipment.notToOpenDoorNum3IfNoShoppingDisplayOpenDoorButton");
                    BillingConfig.SE_NOT_TO_OPEN_DOOR3_IF_NO_SHOPPING_DISPLAY_OPEN_DOOR_BUTTON = smartEquipment.getBoolean("notToOpenDoorNum3IfNoShoppingDisplayOpenDoorButton");

                }

                if (smartEquipment.has("farewellButtonAnimation")) {
                    System.out.println("Override smartEquipment.farewellButtonAnimation");
                    BillingConfig.SE_FAREWELL_BUTTON_ANIMATION = smartEquipment.getBoolean("farewellButtonAnimation");

                }

                if (smartEquipment.has("farewellNoShoppingPlayAudio")) {
                    System.out.println("Override smartEquipment.farewellNoShoppingPlayAudio");
                    BillingConfig.SE_FAREWELL_NO_SHOPPING_PLAY_AUDIO = smartEquipment.getBoolean("farewellNoShoppingPlayAudio");

                }

            }

            //
            // Override remoteManager
            //

            if (configJson.has("remoteManager")) {

                JSONObject remoteManager = configJson.getJSONObject("remoteManager");

                if (remoteManager.has("udpListenPort")) {
                    System.out.println("Override remoteManager.udpListenPort");
                    BillingConfig.RC_UDP_LISTEN_PORT = remoteManager.getInt("udpListenPort");

                }

                if (remoteManager.has("udpSourceAddr")) {
                    System.out.println("Override remoteManager.udpSourceAddr");
                    BillingConfig.RC_UDP_SOURCE_ADDR = remoteManager.getString("udpSourceAddr");

                }

                if (remoteManager.has("enable")) {
                    System.out.println("Override remoteManager.enable");
                    BillingConfig.RC_ENABLE = remoteManager.getBoolean("enable");

                }
            }

            //
            // Override faceDetect
            //

            if (configJson.has("faceDetect")) {

                JSONObject faceDetect = configJson.getJSONObject("faceDetect");

                if (faceDetect.has("detectTimeout")) {
                    System.out.println("Override faceDetect.detectTimeout");
                    BillingConfig.FD_DETECT_TIMEOUT = faceDetect.getInt("detectTimeout");

                }

                if (faceDetect.has("delayOpenCamera")) {
                    System.out.println("Override faceDetect.delayOpenCamera");
                    BillingConfig.FD_OPEN_CAMERA_DELAY = faceDetect.getInt("delayOpenCamera");

                }

                if (faceDetect.has("stopCameraOnlyFaceDetected")) {
                    System.out.println("Override faceDetect.stopCameraOnlyFaceDetected");
                    BillingConfig.FD_STOP_CAMERA_ONLY_FACE_DETECTED = faceDetect.getBoolean("stopCameraOnlyFaceDetected");

                }

            }

            //
            // Override cardPayment
            //

            if (configJson.has("employeeCard")) {

                JSONObject employeeCard = configJson.getJSONObject("employeeCard");

                if (employeeCard.has("enable")) {
                    System.out.println("Override employeeCard.enable");
                    BillingConfig.EC_ENABLE = employeeCard.getBoolean("enable");
                }
            }

            //
            // Override test
            //

            if (configJson.has("test")) {

                JSONObject test = configJson.getJSONObject("test");

                if (test.has("forceQrCodePayFail")) {
                    System.out.println("Override test.forceQrCodePayFail");
                    BillingConfig.TE_FORCE_QR_CODE_PAY_FAIL = test.getBoolean("forceQrCodePayFail");
                }

                if (test.has("forceWechatPortraitFail")) {
                    System.out.println("Override test.forceWechatPortraitFail");
                    BillingConfig.TE_FORCE_WECHAT_PORTRAIT_FAIL = test.getBoolean("forceWechatPortraitFail");
                }

                if (test.has("forceCardPaySuccess")) {
                    System.out.println("Override test.forceCardPaySuccess");
                    BillingConfig.TE_FORCE_CARD_PAY_SUCCESS = test.getBoolean("forceCardPaySuccess");
                }

                if (test.has("autoRestart")) {
                    System.out.println("Override test.autoRestart");
                    BillingConfig.TE_AUTO_RESTART = test.getBoolean("autoRestart");
                }

                if (test.has("forceUncaughtException")) {
                    System.out.println("Override test.forceUncaughtException");
                    BillingConfig.TE_FORCE_UNCAUGHT_EXCEPTION = test.getBoolean("forceUncaughtException");
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void dump() {
        Log.d(TAG, String.format("BA_IP                    = %s\n", BA_IP));
        Log.d(TAG, String.format("BA_APP_ID                = %s\n", BA_APP_ID));
        Log.d(TAG, String.format("BA_LOCATION_ID           = %d\n", BA_LOCATION_ID));
        Log.d(TAG, String.format("BA_STORE_ID              = %s\n", BA_STORE_ID));
        Log.d(TAG, String.format("BA_REPORT_STATUS         = %s\n", BA_REPORT_STATUS));
        Log.d(TAG, String.format("BA_ENABLE_VOICE          = %s\n", BA_ENABLE_VOICE));
        Log.d(TAG, String.format("BA_AUTO_RESTART_FOR_CRASH   = %s\n", BA_AUTO_RESTART_FOR_CRASH));
        Log.d(TAG, String.format("BA_AUTO_RECOVER_FOR_MALFUNCTION   = %s\n", BA_AUTO_RECOVER_FOR_MALFUNCTION));
        Log.d(TAG, String.format("BA_HTTP_REQUEST_TIMEOUT   = %d\n", BA_HTTP_REQUEST_TIMEOUT));

        Log.d(TAG, String.format("CA_TYPE                  = %d\n", CA_TYPE));
        Log.d(TAG, String.format("CA_ID                    = %s\n", CA_ID));

        Log.d(TAG, String.format("RR_TYPE                  = %s\n", RR_TYPE));
        Log.d(TAG, String.format("RR_IP_PORT               = %s\n", RR_IP_PORT));
        Log.d(TAG, String.format("RR_READ_DURATION         = %d\n", RR_READ_DURATION));
        Log.d(TAG, String.format("RR_WEB_API               = %s\n", RR_WEB_API));

        Log.d(TAG, String.format("BC_GET_CUSTOMER_INFO_URL = %s\n", BC_GET_CUSTOMER_INFO_URL));
        Log.d(TAG, String.format("BC_GEN_BILL_URL          = %s\n", BC_GEN_BILL_URL));
        Log.d(TAG, String.format("BC_IS_CHECKED_URL        = %s\n", BC_IS_CHECKED_URL));
        Log.d(TAG, String.format("BC_CHECK_BILL_URL        = %s\n", BC_CHECK_BILL_URL));
        Log.d(TAG, String.format("BC_CUSTOMER_EXIT_URL     = %s\n", BC_CUSTOMER_EXIT_URL));

        Log.d(TAG, String.format("SE_TCP_LISTEN_PORT       = %d\n", SE_TCP_LISTEN_PORT));
        Log.d(TAG, String.format("SE_TYPE                  = %d\n", SE_TYPE));
        Log.d(TAG, String.format("SE_UDP_SEND_ADDR         = %s\n", SE_UDP_SEND_ADDR));
        Log.d(TAG, String.format("SE_UDP_SEND_PORT         = %d\n", SE_UDP_SEND_PORT));
        Log.d(TAG, String.format("SE_CLOSE_DOOR3_DELAY     = %d\n", SE_CLOSE_DOOR3_DELAY));
        Log.d(TAG, String.format("SE_OPEN_DOOR2_DELAY      = %d\n", SE_OPEN_DOOR2_DELAY));
        Log.d(TAG, String.format("SE_CLOSE_DOOR2_DELAY     = %d\n", SE_CLOSE_DOOR2_DELAY));
        Log.d(TAG, String.format("SE_WAITE_FOR_BACK        = %d\n", SE_WAITE_FOR_BACK));

        Log.d(TAG, String.format("SE_EVENT_REPORTER_RECOVER_TIMEOUT = %d\n", SE_EVENT_REPORTER_RECOVER_TIMEOUT));
        Log.d(TAG, String.format("SE_EVENT_REPORTER_BAD_TIMEOUT = %d\n", SE_EVENT_REPORTER_BAD_TIMEOUT));

        Log.d(TAG, String.format("SE_TRY_DOOR_COUNT        = %d\n", SE_TRY_DOOR_COUNT));
        Log.d(TAG, String.format("SE_TRY_DOOR_PERIOD       = %d\n", SE_TRY_DOOR_PERIOD));
        Log.d(TAG, String.format("SE_ENABLE_SELF_RECOVER   = %s\n", SE_ENABLE_SELF_RECOVER));
        Log.d(TAG, String.format("SE_NOT_TO_OPEN_DOOR3_IF_NO_SHOPPING   = %s\n", SE_NOT_TO_OPEN_DOOR3_IF_NO_SHOPPING));
        Log.d(TAG, String.format("SE_NOT_TO_OPEN_DOOR3_IF_NO_SHOPPING_DISPLAY_OPEN_DOOR_BUTTON   = %s\n", SE_NOT_TO_OPEN_DOOR3_IF_NO_SHOPPING_DISPLAY_OPEN_DOOR_BUTTON));
        Log.d(TAG, String.format("SE_FAREWELL_BUTTON_ANIMATION   = %s\n", SE_FAREWELL_BUTTON_ANIMATION));
        Log.d(TAG, String.format("SE_FAREWELL_NO_SHOPPING_PLAY_AUDIO   = %s\n", SE_FAREWELL_NO_SHOPPING_PLAY_AUDIO));

        Log.d(TAG, String.format("RC_ENABLE                = %s\n", RC_ENABLE));
        Log.d(TAG, String.format("RC_UDP_SOURCE_ADDR       = %s\n", RC_UDP_SOURCE_ADDR));
        Log.d(TAG, String.format("RC_UDP_LISTEN_PORT       = %d\n", RC_UDP_LISTEN_PORT));

        Log.d(TAG, String.format("FD_DETECT_TIMEOUT       = %d\n", FD_DETECT_TIMEOUT));
        Log.d(TAG, String.format("FD_OPEN_CAMERA_DELAY       = %d\n", FD_OPEN_CAMERA_DELAY));
        Log.d(TAG, String.format("FD_STOP_CAMERA_ONLY_FACE_DETECTED       = %s\n", FD_STOP_CAMERA_ONLY_FACE_DETECTED));

        Log.d(TAG, String.format("EC_ENABLE                = %s\n", EC_ENABLE));

        Log.d(TAG, String.format("TE_FORCE_QR_CODE_PAY_FAIL = %s\n", TE_FORCE_QR_CODE_PAY_FAIL));
        Log.d(TAG, String.format("TE_FORCE_WECHAT_PORTRAIT_FAIL  = %s\n", TE_FORCE_WECHAT_PORTRAIT_FAIL));
        Log.d(TAG, String.format("TE_AUTO_RESTART  = %s\n", TE_AUTO_RESTART));
        Log.d(TAG, String.format("TE_FORCE_UNCAUGHT_EXCEPTION  = %s\n", TE_FORCE_UNCAUGHT_EXCEPTION));

    }
}
