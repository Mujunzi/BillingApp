package com.lenovo.billing.views.customized;

import android.app.Application;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        MyCrashHandler.getInstance().initCrashHandler(this);

    }
}
