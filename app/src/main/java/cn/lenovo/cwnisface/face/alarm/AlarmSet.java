package cn.lenovo.cwnisface.face.alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

import java.util.Calendar;
import java.util.TimeZone;

import cn.lenovo.cwnisface.face.Constants;

import static android.content.Context.ALARM_SERVICE;

/**
 * Created by baohm1 on 2018/5/22.
 */

public class AlarmSet {
    private static final String TAG = "AlarmSet";
    private static final int REQUEST_CODE_CLEAR_JPG = 100;
    public static final String ACTION_CLEAR_CACHE_JPG = "action.clear.cache.jpg";

    public static void SetRepeatingClearJpg(Context context) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.setAction(ACTION_CLEAR_CACHE_JPG);
        PendingIntent sender = PendingIntent.getBroadcast(context, REQUEST_CODE_CLEAR_JPG, intent, 0);

        long firstTime = SystemClock.elapsedRealtime(); // 开机之后到现在的运行时间(包括睡眠时间)
        long systemTime = System.currentTimeMillis();

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        // 这里时区需要设置一下，不然会有8个小时的时间差
        calendar.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        // 选择的定时时间
        long selectTime = calendar.getTimeInMillis();
        // 如果当前时间大于设置的时间，那么就从第二天的设定时间开始
        if(systemTime > selectTime) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            selectTime = calendar.getTimeInMillis();
        }
        // 计算现在时间到设定时间的时间差
        long time = selectTime - systemTime;
        firstTime += time;

        AlarmManager manager = (AlarmManager)context.getSystemService(ALARM_SERVICE);
        manager.cancel(sender);
        manager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                firstTime, Constants.REPEAT_TIME_CHECK_CACHE, sender);
        Log.d(TAG, "SetRepeatingClearJpg: OK...");
    }
}
