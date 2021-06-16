package com.lenovo.billing.views.customized;

import android.util.Log;
import android.view.View;


//
// single click listener
//

public abstract class OnMultiClickListener implements View.OnClickListener {
    private static final int MIN_CLICK_DELAY_TIME = 1000;
    private long lastClickTime = 0;

    public abstract void onMultiClick(View v);

    @Override
    public void onClick(View v) {

        long curClickTime = System.currentTimeMillis();
        if ((curClickTime - lastClickTime) >= MIN_CLICK_DELAY_TIME) {
            lastClickTime = curClickTime;
            Log.d("OnMultiClickListener", "onMultiClick()......");
            onMultiClick(v);
        }
    }
}
