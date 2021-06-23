package com.lenovo.billing.views.customized;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

import com.lenovo.billing.presenter.Presenter;
import com.lenovo.billing.protocol.Breakdown;

public class NetworkChangeReceiver extends BroadcastReceiver {

    private Presenter presenter;

    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = null;
        if (connectivityManager != null) {
            networkInfo = connectivityManager.getActiveNetworkInfo();
        }
        if (networkInfo != null && networkInfo.isAvailable()) {
            Toast.makeText(context, "Network is available.", Toast.LENGTH_SHORT).show();

            presenter.netIsAlive();
        } else {
            Toast.makeText(context, "Network is unavailable.", Toast.LENGTH_SHORT).show();

            presenter.getBilling().sendErrorMsg(Breakdown.NETWORK_IS_UNAVAILABLE);
        }
    }

    public void setPresenter(Presenter presenter){
        this.presenter = presenter;
    }
}
