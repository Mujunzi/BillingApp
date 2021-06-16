package cn.lenovo.cwnisface.face.alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import cn.lenovo.cwnisface.face.Constants;
import cn.lenovo.cwnisface.face.utils.FileUtils;

/**
 * Created by baohm1 on 2018/5/22.
 */

public class AlarmReceiver extends BroadcastReceiver {
    private static final String TAG = "AlarmReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(AlarmSet.ACTION_CLEAR_CACHE_JPG)) {
            Log.d(TAG, "onReceive: =====>Clear old jpg...");

            mThread.start();
        }
    }

    private Thread mThread = new Thread(new Runnable() {
        @Override
        public void run() {
            ClearCacheFile();
        }
    });

    private void ClearCacheFile() {
        Log.d(TAG, "ClearCacheFile: ");
        //finish about 2s when file number is 10000
        int number = FileUtils.getFileNumber(Constants.CROP_IMG_PATH);
        if (number > Constants.MAX_NUMBER_CROP_IMG) {
            String oldFolderPath = Constants.CROP_IMG_PATH + "_old";
            Log.d(TAG, "ClearCacheFile: 1, number = " + number);
            FileUtils.deleteDir(oldFolderPath);
            Log.d(TAG, "ClearCacheFile: 2");
            FileUtils.renameFile(Constants.CROP_IMG_PATH, oldFolderPath);
            Log.d(TAG, "ClearCacheFile: 3");
            FileUtils.checkFilePath(Constants.CROP_IMG_PATH);
        } else {
            Log.d(TAG, "ClearCacheFile: " + Constants.CROP_IMG_PATH + " size = " + number);
        }
    }
}
