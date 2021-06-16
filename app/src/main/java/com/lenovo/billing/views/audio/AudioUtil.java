package com.lenovo.billing.views.audio;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.lenovo.billing.R;
import com.lenovo.billing.protocol.BillingConfig;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AudioUtil {

    private static final String TAG = "AudioUtil";

    private static SoundPool soundPool;
    private static List<Integer> soundArrEnable = new ArrayList<Integer>();
    private static int prePlaySoundId = 0;

    public static final int LOOK_AT_SCREEN_NOT_COVER_ICONS = 1;
    public static final int LOOK_AT_CAMERA = 2;
    public static final int DOOR_WILL_CLOSE = 3;

    public static final int IN_SCANNING = 4;
    public static final int PLEASE_CHECK_YOUR_DEBT_BILL = 5;
    public static final int PLEASE_CHECK_YOUR_BILL = 6;

    public static final int CARD_PAY_SUCCESS = 7;
    public static final int PAY_SUCCESS_SEE_YOU = 8;
    public static final int SEE_YOU = 9;

    public static final int NOSHOPPING_PLEASE_CLICK_RE_RECOGNIZE = 10;
    public static final int PLEASE_CLICK_RE_RECOGNIZE = 11;

    public static final int PLEASE_CONFIRM_ORDER = 12;
    public static final int IF_NOTHING_RECOGNIZED_PUT_GOODS_ON_THE_TABLE_01 = 13;
    public static final int IF_NOTHING_RECOGNIZED_PUT_GOODS_ON_THE_TABLE_02 = 14;

    //
    // rawId : raw duration
    //

    @SuppressLint("UseSparseArrays")
    private static Map<Integer, Integer> rawInfos = new HashMap<>();

    //
    // soundId : rawId
    //

    @SuppressLint("UseSparseArrays")
    private static Map<Integer, Integer> soundMap = new HashMap<>();

    private static boolean isPlaying = false;

    @SuppressLint("HandlerLeak")
    private static Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            isPlaying = false;
        }
    };

    public static void loadAudio(Context context) {

        if (soundPool == null) {
            soundPool = new SoundPool.Builder().setMaxStreams(1).build();
        }

        soundPool.load(context, R.raw.look_at_screen_not_cover_icons, 1);
        soundPool.load(context, R.raw.look_at_camera, 1);
        soundPool.load(context, R.raw.door_will_close, 1);

        soundPool.load(context, R.raw.in_scanning, 1);
        soundPool.load(context, R.raw.please_check_your_debt_bill, 1);
        soundPool.load(context, R.raw.please_check_your_bill, 1);

        soundPool.load(context, R.raw.card_pay_success, 1);
        soundPool.load(context, R.raw.pay_success_see_you, 1);
        soundPool.load(context, R.raw.see_you, 1);

        soundPool.load(context, R.raw.noshopping_please_click_re_recognize, 1);
        soundPool.load(context, R.raw.please_click_re_recognize, 1);

        soundPool.load(context, R.raw.please_confirm_the_order, 1);
        soundPool.load(context, R.raw.if_nothing_recognized_put_goods_on_the_table_01, 1);
        soundPool.load(context, R.raw.if_nothing_recognized_put_goods_on_the_table_02, 1);


        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                // finish loading
            }
        });

        initSoundMap();
        getWavLength(context);
    }

    public static void enableAudio(int soundId) {
        if (!soundArrEnable.contains(soundId)) {
            soundArrEnable.add(soundId);
        }
    }

    public static void disableAudio() {
        soundArrEnable.clear();
    }

    public static void playAudio(int soundId) {

        if (isPlaying) {
            Log.d(TAG, "audio is playing : soundId is " + prePlaySoundId);
            return;
        }

        if (prePlaySoundId==soundId && soundId == PLEASE_CONFIRM_ORDER) {
            Log.d(TAG, "PLEASE_CONFIRM_ORDER can't be played twice series.");
            return;
        }

//        if (prePlaySoundId != soundId && prePlaySoundId != 0 && soundArrEnable.contains(soundId)) {
//            Log.d(TAG, "stop audio : soundId is " + prePlaySoundId);
//            soundPool.stop(prePlaySoundId);
//        }

        if (BillingConfig.BA_ENABLE_VOICE && soundArrEnable.contains(soundId)) {
            prePlaySoundId = soundId;
            Log.d(TAG, "play audio : soundId is " + soundId);
            soundPool.play(soundId, 1, 1, 1, 0, 1);

            isPlaying = true;
            int rawId = soundMap.get(soundId);
            int duration = rawInfos.get(rawId);

            handler.sendEmptyMessageDelayed(0, duration);

        } else {
            Log.d(TAG, "No voice remind.");
        }
    }

    private static void initSoundMap() {
        soundMap.put(LOOK_AT_SCREEN_NOT_COVER_ICONS, R.raw.look_at_screen_not_cover_icons);
        soundMap.put(LOOK_AT_CAMERA, R.raw.look_at_camera);
        soundMap.put(DOOR_WILL_CLOSE, R.raw.door_will_close);
        soundMap.put(IN_SCANNING, R.raw.in_scanning);
        soundMap.put(PLEASE_CHECK_YOUR_DEBT_BILL, R.raw.please_check_your_debt_bill);
        soundMap.put(PLEASE_CHECK_YOUR_BILL, R.raw.please_check_your_bill);
        soundMap.put(CARD_PAY_SUCCESS, R.raw.card_pay_success);
        soundMap.put(PAY_SUCCESS_SEE_YOU, R.raw.pay_success_see_you);
        soundMap.put(SEE_YOU, R.raw.see_you);
        soundMap.put(NOSHOPPING_PLEASE_CLICK_RE_RECOGNIZE, R.raw.noshopping_please_click_re_recognize);
        soundMap.put(PLEASE_CLICK_RE_RECOGNIZE, R.raw.please_click_re_recognize);
        soundMap.put(PLEASE_CONFIRM_ORDER, R.raw.please_confirm_the_order);
        soundMap.put(IF_NOTHING_RECOGNIZED_PUT_GOODS_ON_THE_TABLE_01, R.raw.if_nothing_recognized_put_goods_on_the_table_01);
        soundMap.put(IF_NOTHING_RECOGNIZED_PUT_GOODS_ON_THE_TABLE_02, R.raw.if_nothing_recognized_put_goods_on_the_table_02);
    }

    private static void getWavLength(Context context) {
        Field[] fields = R.raw.class.getDeclaredFields();
        int rawId;
        for (Field field : fields) {
            try {
                rawId = field.getInt(R.raw.class);
                Log.i(TAG, "-----------rawId=" + rawId + "----------" + R.raw.card_pay_success);
                Uri uri = Uri.parse("android.resource://" + context.getPackageName() + "/" + rawId);
                MediaPlayer mediaPlayer = new MediaPlayer();
                mediaPlayer.setDataSource(context, uri);
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mediaPlayer.prepare();
                int duration = mediaPlayer.getDuration();
                Log.i(TAG, "-----------duration =" + duration + "----------");
                rawInfos.put(rawId, duration);

                mediaPlayer.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
