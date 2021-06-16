package com.lenovo.billing.views.fragment;

import android.app.Fragment;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.lenovo.billing.GlideApp;
import com.lenovo.billing.R;
import com.lenovo.billing.presenter.Presenter;
import com.lenovo.billing.protocol.BillingConfig;
import com.lenovo.billing.views.activity.MainActivity;
import com.lenovo.billing.views.audio.AudioUtil;
import com.lenovo.billing.views.customized.BaseFragment;

import java.lang.ref.WeakReference;
import java.util.Timer;
import java.util.TimerTask;

import cn.lenovo.cwnisface.face.Constants;
import cn.lenovo.cwnisface.face.camera.CameraPreview;
import cn.lenovo.cwnisface.face.utils.JsonUtil;

/**
 * Recognition fragment.
 */
public class RecognitionFragment extends BaseFragment {

    private static final String TAG = RecognitionFragment.class.getSimpleName();

    private static final int DELAY_START_ANIM = 1;
    private static final int DELAY_PLAY_AUDIO = 2;

    private boolean refresh;
    private boolean isOpenCamera;
    private boolean recHidden;  // whether the app is in background

    private CameraPreview myCameraPreview;
    private LinearLayout llRecognitionRefresh;
    private LinearLayout llRecognition;
    private ConstraintLayout llRecognitionPreview;
    private LinearLayout llWelcomeDelay;

    private ImageView ivLogoPlus;
    private RotateAnimation rotate;

    private ImageView ivLecooGif;
    private TextView tvRefreshTipsTop, tvRefreshTipsBottom;

    private LinearLayout llSharePortrait;
    private ImageView ivSharePortrait;

    private Presenter presenter;
    private MyHandler handler;

    private RequestListener<Drawable> listener;
    private View wait;
    private Timer timer;
    private int a;

    public RecognitionFragment() {
        // Required empty public constructor
    }

    public static RecognitionFragment newInstance() {
        JsonUtil.updateConfigFromSdcard();
        return new RecognitionFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate()");

        refresh = false;
        Log.d(TAG, "onCreate() refresh := false");
        isOpenCamera = false;
        Log.d(TAG, "onCreate() isOpenCamera := false");
        presenter.setFaceId("");
        handler = new MyHandler(this);

        rotate = new RotateAnimation(0f,
                90f,
                Animation.RELATIVE_TO_SELF,
                0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f);
        LinearInterpolator lin = new LinearInterpolator();
        rotate.setInterpolator(lin);
        rotate.setDuration(500);
        rotate.setRepeatCount(-1);
        rotate.setFillAfter(true);
        rotate.setStartOffset(200);

        listener = new RequestListener<Drawable>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                Log.d(TAG, "onException, " + e.toString() +
                        "  model:" + model + " isFirstResource: " + isFirstResource);
                return false;
            }

            @Override
            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                Log.d(TAG, "onResourceReady, " + " model:" + model + " isFirstResource: " + isFirstResource);
                return false;
            }
        };
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume(), recHidden = " + recHidden);

        //
        // If the app is run in background, skip face detection.
        //

        if (recHidden) {
            return;
        }

        handler.sendEmptyMessageDelayed(DELAY_PLAY_AUDIO, 500);

        if (!isOpenCamera) {

            handler.sendEmptyMessageDelayed(DELAY_START_ANIM, BillingConfig.FD_OPEN_CAMERA_DELAY);

            myCameraPreview.xfStartCamera();
            isOpenCamera = true;
            Log.d(TAG, "onResume() isOpenCamera := true");

            presenter.setFaceId("");
            presenter.startTimerOfFaceDetection();

            //
            // The camera is enabled before enabling RFID reader.
            // Therefore set the flag of itemsScanned.
            //

            // presenter.setItemsScanned(false);
        }

        a = 0;
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                a++;
                if (a == 5) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            wait.setVisibility(View.VISIBLE);
                        }
                    });
                }
                if (a == 5) {
                    AudioUtil.playAudio(AudioUtil.IN_SCANNING);
                }
            }
        }, 1000, 1000);
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause()");

        if (!BillingConfig.FD_STOP_CAMERA_ONLY_FACE_DETECTED) {

            if (isOpenCamera) {
                myCameraPreview.xfStopCamera();
                isOpenCamera = false;
                Log.d(TAG, "onPause() isOpenCamera := false");
            }
        }

        recHidden = true;
        Log.d(TAG, "onPause() recHidden := true");
    }

    @Override
    public void onStop() {
        super.onStop();
        recHidden = true;
        Log.d(TAG, "onStop() recHidden := true");
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        Log.d(TAG, "onHiddenChanged() hidden = " + hidden + ", refresh = " + refresh + ", isOpenCamera = " + isOpenCamera);

        //
        // If the app is changed to foreground.
        //

        if (!hidden) {

            a = 0;
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    a++;
                    if (a == 5) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                wait.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                    if (a == 5) {
                        AudioUtil.playAudio(AudioUtil.IN_SCANNING);
                    }
                }
            }, 1000, 1000);

            //
            // If refresh, show Refresh GUI.
            //

            if (refresh) {

                AudioUtil.playAudio(AudioUtil.IN_SCANNING);

                llRecognition.setVisibility(View.GONE); // close Preview GUI.
                llRecognitionRefresh.setVisibility(View.VISIBLE); // open Refresh GUI.
                llRecognitionPreview.setVisibility(View.GONE); // close Preview GUI.

                Log.d(TAG, "startAnimation,");
                ivLogoPlus.startAnimation(rotate);

                //
                // If mainActivity.languageFlag  true : Chinese
                //

                if (((MainActivity) getActivity()).languageFlag) {
                    tvRefreshTipsTop.setVisibility(View.VISIBLE);
                    tvRefreshTipsBottom.setVisibility(View.VISIBLE);

                    //
                    // If mainActivity.languageFlag  false : English
                    //

                } else {
                    tvRefreshTipsTop.setVisibility(View.GONE);
                    tvRefreshTipsBottom.setVisibility(View.GONE);
                }

                //
                // Else show Preview GUI.
                //

            } else {

                //AudioUtil.playAudio(soundId_1);
                AudioUtil.playAudio(AudioUtil.LOOK_AT_CAMERA);

                llRecognition.setVisibility(View.VISIBLE); // open Preview GUI.
                llRecognitionRefresh.setVisibility(View.GONE); // close Refresh GUI.
                llRecognitionPreview.setVisibility(View.VISIBLE); // open Preview GUI.
            }

            //
            // If the camera is opened or bill is refreshed, don't open the camera.
            //            

            if (isOpenCamera || refresh) {
                Log.d(TAG, "Don't open camera."); // Don't open camera.

                if (!refresh && BillingConfig.FD_STOP_CAMERA_ONLY_FACE_DETECTED) {

                    //
                    // Handle the case that the user enters the billing zone
                    // to start to detect face and to scan items.
                    //
                    // refresh is false:
                    //     It means that the user MUST enter the billing zone.
                    //
                    // isOpenCamera is true:
                    //     It means that the camera is opened because the last 
                    //     user exited the zone and the bill was not generated.
                    //

                    Log.d(TAG, "Start to detect face and to scan items");

                    //
                    // Start timer to detect face.
                    //

                    presenter.setFaceId("");
                    presenter.startTimerOfFaceDetection();

                    //
                    // The camera is enabled before enabling RFID reader.
                    // Therefore set the flag of itemsScanned.
                    //

                    presenter.setItemsScanned(false);
                }

                //
                // Else open the camera and start to detect face.
                //

            } else {

                llWelcomeDelay.setVisibility(View.VISIBLE);
                handler.sendEmptyMessageDelayed(DELAY_START_ANIM, BillingConfig.FD_OPEN_CAMERA_DELAY);

                //
                // Open the camera
                //

                Log.d(TAG, "step.openCamera");
                myCameraPreview.xfStartCamera();
                isOpenCamera = true;
                Log.d(TAG, "onHiddenChanged() isOpenCamera := true");

                //
                // Start timer to detect face.
                //

                presenter.setFaceId("");
                presenter.startTimerOfFaceDetection();

                //
                // The camera is enabled before enabling RFID reader.
                // Therefore set the flag of itemsScanned.
                //

                presenter.setItemsScanned(false);
            }

            recHidden = false;
            Log.d(TAG, "onHiddenChanged() recHidden := false");

        } else {

            wait.setVisibility(View.INVISIBLE);
            timer.cancel();

            llSharePortrait.setVisibility(View.GONE);

            //
            // If the app is changed to background.
            //
            recHidden = true;
            Log.d(TAG, "onHiddenChanged() recHidden := true");
            if (!BillingConfig.FD_STOP_CAMERA_ONLY_FACE_DETECTED) {
                if (isOpenCamera) {
                    myCameraPreview.xfStopCamera();
                    presenter.clearFlag();
                    isOpenCamera = false;
                    Log.d(TAG, "onHiddenChanged() isOpenCamera := false");
                }
            }

            if (refresh) {
                ivLogoPlus.clearAnimation();
            }
        }

        //
        // Always set refresh false otherwise Refresh GUI may happen when a user enters the zone.
        //

        refresh = false;
        Log.d(TAG, "onHiddenChanged() refresh := false");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_recognition, container, false);
        myCameraPreview = view.findViewById(R.id.mcp_rec);

        ViewGroup.LayoutParams layoutParams = myCameraPreview.getLayoutParams();
        layoutParams.width = (int) (Constants.PREVIEW_W * Constants.PREVIEW_SCALE);
        layoutParams.height = (int) (Constants.PREVIEW_H * Constants.PREVIEW_SCALE);
        myCameraPreview.setLayoutParams(layoutParams);

        llRecognition = view.findViewById(R.id.ll_recognition_layout);
        llRecognitionRefresh = view.findViewById(R.id.ll_recognition_refresh_layout);
        llRecognitionPreview = view.findViewById(R.id.cl_recognition_preview);
        llWelcomeDelay = view.findViewById(R.id.ll_welcome_delay);

        ivLogoPlus = view.findViewById(R.id.iv_logo_plus);

        tvRefreshTipsTop = view.findViewById(R.id.tv_refresh_tips_top);
        tvRefreshTipsBottom = view.findViewById(R.id.tv_refresh_tips_bottom);

        //
        // Loading animation for gif pic,but there are too many sawtooth.
        // So we set visibility is GONE FOR the ivLecooGif view in the layout file.
        //

        ivLecooGif = view.findViewById(R.id.iv_lecoo_gif);
        GlideApp.with(this)
                .asGif()
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .centerCrop()
                .load(R.mipmap.lecoo_loading)
                .into(ivLecooGif);

        llSharePortrait = view.findViewById(R.id.ll_share_portrait);
        ivSharePortrait = view.findViewById(R.id.civ_portrait_share);

        wait = view.findViewById(R.id.pb_wait);
        return view;
    }

    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }

    public void setRefresh(boolean re) {
        refresh = re;
        Log.d(TAG, "setRefresh() refresh := " + re);
    }

    public static class MyHandler extends Handler {
        private final WeakReference<Fragment> fragmentReference;

        MyHandler(Fragment recognitionFragment) {
            this.fragmentReference = new WeakReference<>(recognitionFragment);
        }

        @Override
        public void handleMessage(Message msg) {
            RecognitionFragment fragment = (RecognitionFragment) fragmentReference.get();
            switch (msg.what) {
                case DELAY_PLAY_AUDIO:

                    AudioUtil.playAudio(AudioUtil.LOOK_AT_CAMERA);

                    break;
                case DELAY_START_ANIM:

                    //fragment.transY.start();

                    fragment.llWelcomeDelay.setVisibility(View.GONE);

                    break;
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public ImageView getIvSharePortrait() {
        return ivSharePortrait;
    }

    public void showPortrait() {

        llSharePortrait.setVisibility(View.VISIBLE);

        GlideApp.with(this)
                .asDrawable()
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .listener(listener)
                .load(presenter.getPortrait())
                .into(ivSharePortrait);
    }

    public void stopFaceDetect() {
        Log.d(TAG, "stopFaceDetect");
        this.myCameraPreview.setDet(false);
    }

    public void stopCamera() {
        if (BillingConfig.FD_STOP_CAMERA_ONLY_FACE_DETECTED) {
            if (myCameraPreview != null) {
                myCameraPreview.xfStopCamera();
                presenter.clearFlag();
                isOpenCamera = false;
                Log.d(TAG, "stopCamera() isOpenCamera := false");
            }
        }
    }
}
