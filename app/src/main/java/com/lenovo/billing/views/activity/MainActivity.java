package com.lenovo.billing.views.activity;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.lenovo.billing.R;
import com.lenovo.billing.presenter.ActPresenterImpl;
import com.lenovo.billing.protocol.RecoverCode;
import com.lenovo.billing.views.protocol.IView;
import com.lenovo.billing.presenter.Presenter;
import com.lenovo.billing.protocol.BillingConfig;
import com.lenovo.billing.views.audio.AudioUtil;
import com.lenovo.billing.protocol.BillingViewable;
import com.lenovo.billing.views.customized.NetworkChangeReceiver;
import com.lenovo.billing.views.fragment.FarewellFragment;
import com.lenovo.billing.views.fragment.MalfunctionFragment;
import com.lenovo.billing.views.fragment.WelcomeFragment;
import com.lenovo.billing.views.fragment.PaymentFragment;
import com.lenovo.billing.views.fragment.RecognitionFragment;

import java.lang.ref.WeakReference;

import cn.lenovo.cwnisface.face.alarm.AlarmSet;
import cn.lenovo.cwnisface.utils.PermissionUtils;

public class MainActivity
        extends
        AppCompatActivity
        implements
        IView, View.OnClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    //
    // App language flag
    // -- true : Chinese
    // -- false : english
    //

    public boolean languageFlag = true;

    //
    // Is shown malfunction fragment.
    // if true don't show other fragments and do't play audios.
    //

    private boolean isShownMalfunction = false;

    private boolean isShownPayment = false;

    private WelcomeFragment welcomeFragment;
    private RecognitionFragment recognitionFragment;
    private PaymentFragment paymentFragment;
    private FarewellFragment farewellFragment;
    private MalfunctionFragment malfunctionFragment;

    public static DefaultHandler handler;

    private FragmentManager manager;
    private FragmentTransaction transaction;

    private boolean aNewFlow;
    private boolean onBackground;       // check if the app is run on background.   

    private Presenter presenter;

    private IntentFilter intentFilter;
    private NetworkChangeReceiver networkChangeReceiver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_default);


        this.presenter = new ActPresenterImpl();

        ImageView ivFinishBilling = findViewById(R.id.iv_finish_billing);
        ImageView ivStartConfig = findViewById(R.id.iv_start_config);
        ivFinishBilling.setOnClickListener(this);
        ivStartConfig.setOnClickListener(this);

        //
        // enable audios will be playing
        //

        AudioUtil.loadAudio(this);
        AudioUtil.enableAudio(AudioUtil.LOOK_AT_SCREEN_NOT_COVER_ICONS);
        AudioUtil.enableAudio(AudioUtil.DOOR_WILL_CLOSE);
        AudioUtil.enableAudio(AudioUtil.CARD_PAY_SUCCESS);
        AudioUtil.enableAudio(AudioUtil.PLEASE_CLICK_RE_RECOGNIZE);
        AudioUtil.enableAudio(AudioUtil.PLEASE_CONFIRM_ORDER);
        AudioUtil.enableAudio(AudioUtil.IN_SCANNING);

        aNewFlow = true;
        manager = getFragmentManager();
        handler = new DefaultHandler(this);
        presenter.setDefaultHandler(handler);
        setDefaultFragment();

        AlarmSet.SetRepeatingClearJpg(MainActivity.this);

        // ask for permissions
        if (!PermissionUtils.hasPermission(this)) {
            PermissionUtils.requestPermission(this);
        } else {
            presenter.configAndInit();

            intentFilter = new IntentFilter();
            intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
            networkChangeReceiver = new NetworkChangeReceiver();
            networkChangeReceiver.setPresenter(presenter);
            registerReceiver(networkChangeReceiver, intentFilter);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "MainActivity:onResume()");

        onBackground = false;

    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause()");

        onBackground = true;

        Log.d(TAG, "remove PAYMENTRESULT");
        handler.removeMessages(BillingViewable.TORECOGNITION);

        Log.d(TAG, "remove RECOGNITIONYOU");
        handler.removeMessages(BillingViewable.RECOGNITIONYOU);

        Log.d(TAG, "remove REFRESHISPRESSED");
        handler.removeMessages(BillingViewable.REFRESHISPRESSED);

        Log.d(TAG, "remove SEEYOU");
        handler.removeMessages(BillingViewable.SEEYOU);

        Log.d(TAG, "remove NO_SHOPPING");
        handler.removeMessages(BillingViewable.NO_SHOPPING);

        Log.d(TAG, "remove TODEFAULT");
        handler.removeMessages(BillingViewable.TODEFAULT);
    }

    @Override
    public void setDefaultFragment() {

        if (isShownMalfunction) {
            Log.d(TAG, "now is shown malfunction fragment...");
            return;
        }

        Log.d(TAG, "remove PAYMENTRESULT");
        handler.removeMessages(BillingViewable.PAYMENTRESULT); // stop polling when ???

        Log.d(TAG, "setDefaultFragment()");

        transaction = manager.beginTransaction();

        if (null == welcomeFragment) {
            welcomeFragment = WelcomeFragment.newInstance();
            welcomeFragment.setPresenter(presenter);
            transaction.add(R.id.content_fl, welcomeFragment, "welcomeFragment");
        }
        transaction.show(welcomeFragment);
        transaction.commit();
    }

    @Override
    public void showRecognitionFragment() {

        if (isShownMalfunction) {
            Log.d(TAG, "now is shown malfunction fragment...");
            return;
        }

        Log.d(TAG, "remove PAYMENTRESULT");
        handler.removeMessages(BillingViewable.PAYMENTRESULT); // stop polling when showRecognitionFragment()

        Log.d(TAG, "showRecognitionFragment(),aNewFlow = " + aNewFlow);

        transaction = manager.beginTransaction();

        if (null == recognitionFragment) {
            recognitionFragment = RecognitionFragment.newInstance();
            recognitionFragment.setPresenter(presenter);
            transaction.add(R.id.content_fl, recognitionFragment, "recognitionFragment");
        }

        //
        // Fix bug4 and bug5.
        //

        if (aNewFlow) {
            transaction.show(recognitionFragment).hide(welcomeFragment);
            aNewFlow = false;
        } else {
            if (farewellFragment != null) {
                transaction.show(recognitionFragment).hide(farewellFragment);
            }
        }

        transaction.commit();
    }

    @Override
    public void refreshRecognitionFragment(String source) {

        if (isShownMalfunction) {
            Log.d(TAG, "now is shown malfunction fragment...");
            return;
        }

        Log.d(TAG, "remove PAYMENTRESULT");
        handler.removeMessages(BillingViewable.PAYMENTRESULT); // stop polling when refreshRecognitionFragment()

        Log.d(TAG, "refreshRecognitionFragment()");

        transaction = manager.beginTransaction();
        recognitionFragment = (RecognitionFragment) manager.findFragmentByTag("recognitionFragment");
        recognitionFragment.setRefresh(true);

        if (source.equals("refresh") || isShownPayment) {
            transaction.show(recognitionFragment).hide(paymentFragment);
        } else if (source.equals("noShopping")) {
            transaction.show(recognitionFragment).hide(farewellFragment);
        }
        transaction.commit();
    }

    @Override
    public void showPaymentFragment() {

        if (isShownMalfunction) {
            Log.d(TAG, "now is shown malfunction fragment...");
            return;
        }

        Log.d(TAG, "showPaymentFragment()");

        isShownPayment = true;

        transaction = manager.beginTransaction();

        if (null == paymentFragment) {
            paymentFragment = PaymentFragment.newInstance();
            paymentFragment.setPresenter(presenter);
            transaction.add(R.id.content_fl, paymentFragment, "paymentFragment");
        } else {
            paymentFragment = (PaymentFragment) manager.findFragmentByTag("paymentFragment");
        }
        transaction.addSharedElement(recognitionFragment.getIvSharePortrait(),
                ViewCompat.getTransitionName(recognitionFragment.getIvSharePortrait()))
                .show(paymentFragment).hide(recognitionFragment);
        transaction.commit();
    }

    @Override
    public void showFarewellFragment() {

        if (isShownMalfunction) {
            Log.d(TAG, "now is shown malfunction fragment...");
            return;
        }

        Log.d(TAG, "showFarewellFragment()");

        transaction = manager.beginTransaction();

        if (null == farewellFragment) {
            farewellFragment = FarewellFragment.newInstance();
            farewellFragment.setPresenter(presenter);
            transaction.add(R.id.content_fl, farewellFragment, "farewellFragment");
        } else {
            farewellFragment = (FarewellFragment) manager.findFragmentByTag("farewellFragment");
        }

        if (!presenter.isShopping() && !presenter.isDebt()) {
            transaction.show(farewellFragment).hide(recognitionFragment);
        } else {
            Log.d(TAG, "show(farewellFragment).hide(paymentFragment)");
            transaction.show(farewellFragment).hide(paymentFragment);
        }

        transaction.commit();
    }

    @Override
    public void toWelcomeFragment() {

        if (isShownMalfunction) {
            Log.d(TAG, "now is shown malfunction fragment...");
            return;
        }

        Log.d(TAG, "toWelcomeFragment()");

        transaction = manager.beginTransaction();

        welcomeFragment = (WelcomeFragment) manager.findFragmentByTag("welcomeFragment");
        recognitionFragment = (RecognitionFragment) manager.findFragmentByTag("recognitionFragment");
        paymentFragment = (PaymentFragment) manager.findFragmentByTag("paymentFragment");
        farewellFragment = (FarewellFragment) manager.findFragmentByTag("farewellFragment");

        if (recognitionFragment != null) {
            transaction.hide(recognitionFragment);
        }
        if (paymentFragment != null) {
            transaction.hide(paymentFragment);
        }
        if (farewellFragment != null) {
            transaction.hide(farewellFragment);
        }


        if (welcomeFragment != null) {
            transaction.show(welcomeFragment);
        }

        transaction.commit();
    }

    @Override
    public synchronized void toMalfunctionFragment(int errorCode) {
        Log.d(TAG, "toMalfunctionFragment()");

        transaction = manager.beginTransaction();

        malfunctionFragment = (MalfunctionFragment) manager.findFragmentByTag("malfunctionFragment");

        if (null == malfunctionFragment) {
            malfunctionFragment = MalfunctionFragment.newInstance();
            malfunctionFragment.setPresenter(presenter);
            transaction.add(R.id.content_fl, malfunctionFragment, "malfunctionFragment");
        }
        welcomeFragment = (WelcomeFragment) manager.findFragmentByTag("welcomeFragment");
        recognitionFragment = (RecognitionFragment) manager.findFragmentByTag("recognitionFragment");
        paymentFragment = (PaymentFragment) manager.findFragmentByTag("paymentFragment");
        farewellFragment = (FarewellFragment) manager.findFragmentByTag("farewellFragment");

        if (recognitionFragment != null) {
            transaction.hide(recognitionFragment);
        }
        if (paymentFragment != null) {
            transaction.hide(paymentFragment);
        }
        if (farewellFragment != null) {
            transaction.hide(farewellFragment);
        }
        if (welcomeFragment != null) {
            transaction.hide(welcomeFragment);
        }


        if (malfunctionFragment == null || malfunctionFragment.isVisible()) {
            return;
        } else {
            malfunctionFragment.setErrorCode(errorCode);
        }

        transaction.show(malfunctionFragment);
        transaction.commit();

        //
        // set shown malfunction flag = true
        //

        isShownMalfunction = true;

        //
        // disable play audios
        //

        AudioUtil.disableAudio();

    }

    @Override
    public synchronized void toRecoverState() {
        Log.d(TAG, "toRecoverState()");

        transaction = manager.beginTransaction();

        malfunctionFragment = (MalfunctionFragment) manager.findFragmentByTag("malfunctionFragment");

        if (null == malfunctionFragment) {
            malfunctionFragment = MalfunctionFragment.newInstance();
            malfunctionFragment.setPresenter(presenter);
            transaction.add(R.id.content_fl, malfunctionFragment, "malfunctionFragment");
        }

        int showCode = malfunctionFragment.getErrorCode();

        if (showCode == -1) {
            Log.d(TAG, "Normal, need not to recover state");
            return;
        }

        Log.d(TAG, "Try to recover");

        if (RecoverCode.RECOVER_CODES_L_LIST.contains(showCode)) {
            if (presenter.isDevicesCanWork()) {
                transaction.hide(malfunctionFragment);
                welcomeFragment = (WelcomeFragment) manager.findFragmentByTag("welcomeFragment");
                transaction.show(welcomeFragment);
                transaction.commit();

                malfunctionFragment.setErrorCode(-1);

                if (presenter != null) {
                    presenter.resetDevices();
                }

                Log.d(TAG, "recover state for errCode := " + showCode);
                Log.d(TAG, "call recoverResultCallBack() := recover success");
                presenter.recoverResultCallBack(true);

                //
                // set shown malfunction flag = false
                //

                isShownMalfunction = false;

                //
                // enable play audios
                //

                AudioUtil.enableAudio(AudioUtil.LOOK_AT_SCREEN_NOT_COVER_ICONS);
                AudioUtil.enableAudio(AudioUtil.DOOR_WILL_CLOSE);
                AudioUtil.enableAudio(AudioUtil.CARD_PAY_SUCCESS);
                AudioUtil.enableAudio(AudioUtil.PLEASE_CLICK_RE_RECOGNIZE);
                AudioUtil.enableAudio(AudioUtil.PLEASE_CONFIRM_ORDER);
                AudioUtil.enableAudio(AudioUtil.IN_SCANNING);
                return;
            }
            Log.d(TAG, "Device state still wronging, Can not recover state for errCode := " + showCode);
            Log.d(TAG, "call recoverResultCallBack() := recover fail");
            presenter.recoverResultCallBack(false);
            return;
        }
        Log.d(TAG, "Can not recover state for errCode := " + showCode);
        Log.d(TAG, "call recoverResultCallBack() := recover fail");
        presenter.recoverResultCallBack(false);
    }

    int finish = 0, start = 0;

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (!BillingConfig.TE_AUTO_RESTART) {
            return;
        }

        switch (id) {
            case R.id.iv_finish_billing:
                finish++;

                if (finish >= 5) {

                    Log.d(TAG, "restart() -- restart Billing App");
                    restart();

                    finish = 0;
                }

                break;
            case R.id.iv_start_config:
                start++;

                if (start >= 5) {

                    // start config, reset zero
                    PackageManager packageManager = getPackageManager();
                    Intent i;
                    i = packageManager.getLaunchIntentForPackage("lenovo.com.unifiedconfig");
                    assert i != null;
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(i);

                    start = 0;
                }

                break;
        }
    }

    public static class DefaultHandler extends Handler {
        private final WeakReference<Activity> mActivityReference;

        DefaultHandler(Activity activity) {
            this.mActivityReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            MainActivity activity = (MainActivity) mActivityReference.get();

            //
            // If the app on background, it won't change ui
            //

            if (activity.onBackground) {
                Log.d(TAG, "MainActivity now is background, receive the handleMessage:" + msg.what);
                return;
            }

            switch (msg.what) {
                case BillingViewable.TORECOGNITION:

                    int arg1 = msg.arg1;
                    int arg2 = msg.arg2;
                    if (arg1 == BillingViewable.DETECTZONEOCCUPIED) {

                        if (arg2 == 0) {

                            AudioUtil.playAudio(AudioUtil.LOOK_AT_SCREEN_NOT_COVER_ICONS);

                        } else if (arg2 == 1) {

                            AudioUtil.playAudio(AudioUtil.DOOR_WILL_CLOSE); // door will close

                        }

                    }

                    activity.showRecognitionFragment();
                    activity.isShownPayment = false;

                    break;
                case BillingViewable.NO_SHOPPING:
                case BillingViewable.RECOGNITIONYOU:
                    Log.d(TAG, msg.what == BillingViewable.NO_SHOPPING ? "BillingViewable.NO_SHOPPING" : "BillingViewable.RECOGNITIONYOU");

                    if (activity.isShownPayment) {
                        activity.paymentFragment.handleView(true);
                    } else {
                        activity.showPaymentFragment();
                    }

                    break;
                case BillingViewable.REFRESHISPRESSED:

                    Bundle bundle = msg.getData();
                    String source = bundle.getString("source");

                    activity.refreshRecognitionFragment(source);
                    activity.isShownPayment = false;

                    break;
                case BillingViewable.SEEYOU:

                    activity.showFarewellFragment();
                    activity.isShownPayment = false;

                    break;
                case BillingViewable.TODEFAULT:

                    activity.aNewFlow = true;
                    activity.languageFlag = true;  // set language : Chinese
                    activity.toWelcomeFragment();
                    activity.isShownPayment = false;
                    if (activity.paymentFragment != null) {
                        activity.paymentFragment.changeDelayOpenDoor3Flag();
                    }

                    break;
                case BillingViewable.PAYMENTRESULT:
                    Log.d(TAG, "PAYMENT RESULT");
                    activity.presenter.isBillChecked();

                    break;
                case BillingViewable.MALFUNCTION:

                    int errorCode = msg.arg1;
                    if (errorCode == RecoverCode.RECOVER_CODE.getCode()) {
                        activity.toRecoverState();
                    } else {
                        activity.toMalfunctionFragment(errorCode);
                    }

                    activity.isShownPayment = false;

                    break;
                case BillingViewable.PORTRAITISREADY:

                    if (activity.recognitionFragment != null && !activity.recognitionFragment.isHidden()) {
                        activity.recognitionFragment.showPortrait();
                    }

                    activity.isShownPayment = false;

                    break;
                case BillingViewable.CARDPAYFAILURE:

                    int returnCode = msg.arg1;

                    if (activity.paymentFragment != null) {
                        activity.paymentFragment.setCardPayResult(returnCode);
                    }

                    break;
                case BillingViewable.POSBREAKDOWN:

                    if (activity.paymentFragment != null) {
                        activity.paymentFragment.posCantUse();
                    }

                    break;
                case BillingViewable.CARDPAYSUCCESS:

                    AudioUtil.playAudio(AudioUtil.CARD_PAY_SUCCESS);

                    break;
                case BillingViewable.AUTORESTART:

                    activity.restart();

                    break;
                case BillingViewable.STOPFACEDETECT:

                    if (activity.recognitionFragment != null) {
                        activity.recognitionFragment.stopFaceDetect();
                    }

                    break;
                case BillingViewable.STOPCAMERA:

                    if (activity.recognitionFragment != null) {
                        activity.recognitionFragment.stopCamera();
                    }

                    break;
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void
    onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PermissionUtils.PERMISSIONS_REQUEST) {
            boolean granted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED)
                    granted = false;
            }
            if (!granted) {
                if (!shouldShowRequestPermissionRationale(PermissionUtils.PERMISSION_CAMERA)
                        || !shouldShowRequestPermissionRationale(PermissionUtils.PERMISSION_STORAGE)
                        || !shouldShowRequestPermissionRationale(PermissionUtils.PERMISSION_INTERNET)
                        || !shouldShowRequestPermissionRationale(PermissionUtils.PERMISSION_ACCESS_NETWORK_STATE)) {


                    Toast.makeText(
                            getApplicationContext(),
                            "Camera,Internet,Sdcard are needed.",
                            Toast.LENGTH_SHORT).show();
                }
            } else {
                presenter.configAndInit();

                intentFilter = new IntentFilter();
                intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
                networkChangeReceiver = new NetworkChangeReceiver();
                networkChangeReceiver.setPresenter(presenter);
                registerReceiver(networkChangeReceiver, intentFilter);

            }
        }
    }

    private void restart() throws NullPointerException {

        Intent intent = new Intent();
        intent.setAction("com.lenovo.billing.restart");
        intent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        sendBroadcast(intent);

        // close billing, reset zero
        android.os.Process.killProcess(android.os.Process.myPid());
        //presenter.closeAndClear();
        //finish();

        /*Intent intent = null;
        sendBroadcast(intent);*/

    }

    //
    // Called when pressing the left button.
    //

    /*@Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed()");
        //presenter.closeAndClear();
        //unregisterReceiver(networkChangeReceiver);
        super.onBackPressed();
    }*/

    //
    // Called when exiting the App.
    //

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy()");
        presenter.closeAndClear();
        unregisterReceiver(networkChangeReceiver);
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return keyCode == KeyEvent.KEYCODE_BACK || super.onKeyDown(keyCode, event);
    }

    /**
     * hide navigation bar, full screen
     * not use now.
     */
    protected void hideNavigationBarMenu() {
        if (Build.VERSION.SDK_INT < 19) { // lower api
            View v = this.getWindow().getDecorView();
            v.setSystemUiVisibility(View.GONE);
        } else if (Build.VERSION.SDK_INT >= 19) {
            //for new api versions.
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        }
    }
}
