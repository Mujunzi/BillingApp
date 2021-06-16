package cn.lenovo.cwnisface.face.utils;

import android.os.Environment;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import cn.lenovo.cwnisface.face.Constants;


public class JsonUtil {
    private static final String TAG = "JsonUtil==";
    private static final String JsonConfigFile = "cwNisFaceConfig.json";

    public static void updateConfigFromSdcard() {
        String configStr = readConfig();
        if (configStr.isEmpty()) {
            return;
        }
        try {
            JSONObject configJson = new JSONObject(configStr);
            updateConstants(configJson);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private static String readConfig(){
        if(!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            return "";
        }

        File dataFile = new File(Environment.getExternalStorageDirectory(), JsonConfigFile);
        if (!dataFile.exists()) {
            Log.d (TAG, "/sdcard/"+ JsonConfigFile +" is not exist");
            dataFile = new File(Environment.getExternalStorageDirectory() + File.separator + "CWModels", JsonConfigFile);
            if (!dataFile.exists()) {
                return "";
            }
        }
        Log.d (TAG, "JsonConfigFile = " + dataFile);

        StringBuilder stringBuilder = new StringBuilder();
        try {
            FileInputStream inputStream = new FileInputStream(dataFile);
            BufferedReader bf = new BufferedReader(new InputStreamReader(
                    inputStream));

            String line;
            while ((line = bf.readLine()) != null) {
                stringBuilder.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }

    private static final String _PAD_PARAM = "padParam";
    private static final String _PAD_PARAM_PAD_ID = "padID";
    private static final String _PAD_PARAM_PREVIEW_W = "preview_w";
    private static final String _PAD_PARAM_PREVIEW_H = "preview_h";

    private static final String _CW_FACE = "cw_face";
    private static final String _CW_FACE_URL = "url_server";

    private static final String _CW_PARAM = "cw_param";
    private static final String _CW_PARAM_MIN_FACE_SIZE = "min_face_size";
    private static final String _CW_PARAM_HIGH_SIMILAR = "high_similar";
    private static final String _CW_PARAM_LOW_SIMILAR = "low_similar";
    private static final String _CW_PARAM_OWN_SIMILAR = "own_similar";
    private static final String _CW_PARAM_IS_OWN = "is_own";
    private static final String _CW_PARAM_SKIN = "skin";
    private static final String _CW_PARAM_BLUR = "blur";
    private static final String _CW_PARAM_YAW = "pose_yaw";
    private static final String _CW_PARAM_PITCH = "pose_pitch";
    private static final String _CW_PARAM_ROLL = "pose_roll";

    private static void updateConstants(JSONObject json) throws JSONException {
        if (json == null)
            return;

        if (json.has (_PAD_PARAM)) {
            Log.d (TAG,"Override " + _PAD_PARAM);
            JSONObject padConfigJson = json.getJSONObject(_PAD_PARAM);
            if (padConfigJson.has (_PAD_PARAM_PAD_ID)) {
                Constants.PAD_ID = padConfigJson.getString(_PAD_PARAM_PAD_ID);
                Log.d (TAG,"Override Constants.PAD_ID = " + Constants.PAD_ID);
            }
            if (padConfigJson.has (_PAD_PARAM_PREVIEW_W)) {
                Constants.PREVIEW_W = padConfigJson.getInt(_PAD_PARAM_PREVIEW_W);
                Log.d (TAG,"Override Constants.PREVIEW_W = " + Constants.PREVIEW_W);
            }
            if (padConfigJson.has (_PAD_PARAM_PREVIEW_H)) {
                Constants.PREVIEW_H = padConfigJson.getInt(_PAD_PARAM_PREVIEW_H);
                Log.d (TAG,"Override Constants.PREVIEW_H = " + Constants.PREVIEW_H);
            }
        }

        if (json.has (_CW_FACE)) {
            Log.d (TAG,"Override " + _CW_FACE);
            JSONObject cwConfigJson = json.getJSONObject(_CW_FACE);
            if (cwConfigJson.has (_CW_FACE_URL)) {
                Constants.url_server = cwConfigJson.getString(_CW_FACE_URL);
                Log.d (TAG,"Override Constants.url_server = " + Constants.url_server);
            }
        }

        if (json.has (_CW_PARAM)) {
            Log.d (TAG,"Override " + _CW_PARAM);
            JSONObject cwConfigJson = json.getJSONObject(_CW_PARAM);
            if (cwConfigJson.has (_CW_PARAM_MIN_FACE_SIZE)) {
                Constants.CW_FACE_Min_Size = cwConfigJson.getInt(_CW_PARAM_MIN_FACE_SIZE);
                Log.d (TAG,"Override Constants.CW_FACE_Min_Size = " + Constants.CW_FACE_Min_Size);
            }
            if (cwConfigJson.has (_CW_PARAM_HIGH_SIMILAR)) {
                Constants.CW_FACE_HIGH_SIMILAR = (float)cwConfigJson.getDouble(_CW_PARAM_HIGH_SIMILAR);
                Log.d (TAG,"Override Constants.CW_FACE_HIGH_SIMILAR = " + Constants.CW_FACE_HIGH_SIMILAR);
            }
            if (cwConfigJson.has (_CW_PARAM_LOW_SIMILAR)) {
                Constants.CW_FACE_SIMILAR = (float)cwConfigJson.getDouble(_CW_PARAM_LOW_SIMILAR);
                Log.d (TAG,"Override Constants.CW_FACE_SIMILAR = " + Constants.CW_FACE_SIMILAR);
            }
            if (cwConfigJson.has (_CW_PARAM_OWN_SIMILAR)) {
                Constants.CW_FACE_OWN_SIMILAR = (float)cwConfigJson.getDouble(_CW_PARAM_OWN_SIMILAR);
                Log.d (TAG,"Override Constants._CW_PARAM_OWN_SIMILAR = " + Constants.CW_FACE_OWN_SIMILAR);
            }
            if (cwConfigJson.has (_CW_PARAM_IS_OWN)) {
                Constants.CW_FACE_IS_OWN = cwConfigJson.getString(_CW_PARAM_IS_OWN);
                Log.d (TAG,"Override Constants._CW_PARAM_IS_OWN = " + Constants.CW_FACE_IS_OWN);
            }
            if (cwConfigJson.has (_CW_PARAM_SKIN)) {
                Constants.CW_SKIN_SCORE = (float)cwConfigJson.getDouble(_CW_PARAM_SKIN);
                Log.d (TAG,"Override Constants.CW_SKIN_SCORE = " + Constants.CW_SKIN_SCORE);
            }
            if (cwConfigJson.has (_CW_PARAM_BLUR)) {
                Constants.CW_CLARITY = (float)cwConfigJson.getDouble(_CW_PARAM_BLUR);
                Log.d (TAG,"Override Constants.CW_CLARITY = " + Constants.CW_CLARITY);
            }
            if (cwConfigJson.has (_CW_PARAM_YAW)) {
                Constants.CW_POSE_YAW = cwConfigJson.getInt(_CW_PARAM_YAW);
                Log.d (TAG,"Override Constants.CW_POSE_YAW = " + Constants.CW_POSE_YAW);
            }
            if (cwConfigJson.has (_CW_PARAM_PITCH)) {
                Constants.CW_POSE_PITCH = cwConfigJson.getInt(_CW_PARAM_PITCH);
                Log.d (TAG,"Override Constants.CW_POSE_PITCH = " + Constants.CW_POSE_PITCH);
            }
            if (cwConfigJson.has (_CW_PARAM_ROLL)) {
                Constants.CW_POSE_ROLL = cwConfigJson.getInt(_CW_PARAM_ROLL);
                Log.d (TAG,"Override Constants.CW_POSE_ROLL = " + Constants.CW_POSE_ROLL);
            }
        }
    }
}
