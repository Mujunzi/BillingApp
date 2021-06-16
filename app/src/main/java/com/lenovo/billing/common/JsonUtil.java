package com.lenovo.billing.common;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;
import android.util.Log;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;

/**
 * Json util.
 */
public class JsonUtil {

    private static final String TAG = "JsonUtil";

    private static final String FILE_NAME = "BillingConfig.json";

    public static String readConfig(){

        File dataFile = new File(Environment.getExternalStorageDirectory(),FILE_NAME);
        Log.d (TAG, "dataFile = "+dataFile);

        if(!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            return "";
        }

        StringBuilder stringBuilder = new StringBuilder();
        try {
            FileInputStream inputStream = new FileInputStream(dataFile);

            BufferedReader bf = new BufferedReader(new InputStreamReader(
                    inputStream));

            String line;
            while ((line = bf.readLine()) != null) {
                stringBuilder.append(line);
            }

            bf.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return stringBuilder.toString();
    }

    public static String readAssetJson(String fileName,Context context) {
        //json to string
        StringBuilder stringBuilder = new StringBuilder();
        try {

            AssetManager assetManager = context.getAssets();
            BufferedReader bf = new BufferedReader(new InputStreamReader(
                    assetManager.open(fileName)));
            String line;
            while ((line = bf.readLine()) != null) {
                stringBuilder.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }

    static Object deserialize(String str, Type type){

        return new Gson().fromJson(str,type);
    }
}
