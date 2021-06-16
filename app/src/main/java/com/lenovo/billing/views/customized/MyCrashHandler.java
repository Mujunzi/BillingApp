package com.lenovo.billing.views.customized;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.lenovo.billing.protocol.BillingConfig;
import com.lenovo.billing.views.activity.MainActivity;

public class MyCrashHandler implements Thread.UncaughtExceptionHandler {

    private static MyCrashHandler mAppCrashHandler;

    //private Thread.UncaughtExceptionHandler mDefaultHandler;

    private MyApplication mAppContext;

    public void initCrashHandler(MyApplication application) {

        this.mAppContext = application;
        //mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    public static MyCrashHandler getInstance() {

        if (mAppCrashHandler == null) {
            mAppCrashHandler = new MyCrashHandler();
        }
        return mAppCrashHandler;
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {

        Log.d("MyCrashHandler", "uncaughtException: " + ex.getMessage());
        Log.d("MyCrashHandler", "uncaughtException LocalizedMessage is: " + ex.getLocalizedMessage());

        if (BillingConfig.BA_AUTO_RESTART_FOR_CRASH) {

            AlarmManager mgr = (AlarmManager) mAppContext.getSystemService(Context.ALARM_SERVICE);

            Intent intent = new Intent(mAppContext, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("crash", true);
            PendingIntent restartIntent = PendingIntent.getActivity(mAppContext, 0, intent, PendingIntent.FLAG_ONE_SHOT);
            assert mgr != null;
            mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 1000, restartIntent); // restart after one second
        }

        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
        System.gc();

    }

}
