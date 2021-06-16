package com.lenovo.billing.common;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.gson.internal.$Gson$Types;
import com.lenovo.billing.protocol.BillingConfig;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.ParametersAreNonnullByDefault;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * http request class use okhttp3.
 */
public class OkHttpUtil {

    private static final String TAG = OkHttpUtil.class.getSimpleName();

    private static OkHttpUtil mInstance;
    private OkHttpClient mOkHttpClient;
    private Handler mHandler;

    private OkHttpUtil() {

        mOkHttpClient = new OkHttpClient.Builder()
                .connectTimeout(BillingConfig.BA_HTTP_REQUEST_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(BillingConfig.BA_HTTP_REQUEST_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(BillingConfig.BA_HTTP_REQUEST_TIMEOUT, TimeUnit.SECONDS)
                .build();

        mHandler = new Handler(Looper.getMainLooper());
    }


    private synchronized static OkHttpUtil getmInstance() {
        if (mInstance == null) {
            mInstance = new OkHttpUtil();
        }
        return mInstance;
    }


    private void getRequest(String url, final ResultCallback callback) {
        final Request request = new Request.Builder().url(url).build();
        deliveryResult(callback, request);
    }


    private void postRequest(String url, final ResultCallback callback, List<Param> params) {
        Request request = buildPostRequest(url, params);
        deliveryResult(callback, request);
    }

    private void postRequest(String url, final ResultCallback callback, String jsonParams) {
        Request request = buildPostRequest(url, jsonParams);
        deliveryResult(callback, request);
    }

    @ParametersAreNonnullByDefault
    private void deliveryResult(final ResultCallback callback, Request request) {

        mOkHttpClient.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, final IOException e) {
                sendFailCallback(callback, e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String str = response.body() != null ? response.body().string() : "";
                    if (callback.mType == String.class) {
                        sendSuccessCallBack(callback, str);
                    } else {
                        Object object = JsonUtil.deserialize(str, callback.mType);
                        sendSuccessCallBack(callback, object);
                    }
                } catch (final Exception e) {
                    Log.e(TAG, "convert json failure", e);
                    sendFailCallback(callback, e);
                }

            }
        });
    }


    private void sendFailCallback(final ResultCallback callback, final Exception e) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (callback != null) {
                    callback.onFailure(e);
                }
            }
        });
    }

    private void sendSuccessCallBack(final ResultCallback callback, final Object obj) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (callback != null) {
                    try {
                        callback.onSuccess(obj);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }


    private Request buildPostRequest(String url, List<Param> params) {
        Log.d(TAG, "url = " + url);
        //FormBody.Builder builder = new FormBody.Builder();
        JSONObject jsonObject = new JSONObject();
        for (Param param : params) {
            try {
                Log.d(TAG, "param.value = " + param.value);
                jsonObject.put(param.key, param.value);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            //builder.add(param.key, param.value);
        }
        //RequestBody requestBody = builder.build();
        Log.d(TAG, "params = " + jsonObject.toString());
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonObject.toString());
        return new Request.Builder().url(url).post(requestBody).addHeader("Content-Type", "application/json").build();
    }

    private Request buildPostRequest(String url, String jsonParams) {
        Log.d(TAG, "jsonParams = " + jsonParams);
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonParams);
        return new Request.Builder().url(url).post(requestBody).addHeader("Content-Type", "application/json").build();
    }


    public static void get(String url, ResultCallback callback) {
        getmInstance().getRequest(url, callback);
    }


    public static void post(String url, final ResultCallback callback, List<Param> params) {
        getmInstance().postRequest(url, callback, params);
    }

    public static void post(String url, final ResultCallback callback, String jsonParms) {
        getmInstance().postRequest(url, callback, jsonParms);
    }

    public static abstract class ResultCallback<T> {

        Type mType;

        protected ResultCallback() {
            mType = getSuperclassTypeParameter(getClass());
        }

        static Type getSuperclassTypeParameter(Class<?> subclass) {
            Type superclass = subclass.getGenericSuperclass();
            if (superclass instanceof Class) {
                throw new RuntimeException("Missing type parameter.");
            }
            ParameterizedType parameterized = (ParameterizedType) superclass;

            assert parameterized != null;  // if null throw NullPointerException
            return $Gson$Types.canonicalize(parameterized.getActualTypeArguments()[0]);
        }


        public abstract void onSuccess(T response) throws JSONException;


        public abstract void onFailure(Exception e);
    }

    public static class Param {

        String key;
        String value;

        public Param() {
        }

        public Param(String key, String value) {
            this.key = key;
            this.value = value;
        }

    }

}
