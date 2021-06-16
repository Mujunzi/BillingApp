package cn.lenovo.cwnisface.face.http;

import android.os.Environment;
import android.util.Log;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import cn.lenovo.cwnisface.face.Constants;
import cn.lenovo.cwnisface.face.bean.MsgJsonBean;
import cn.lenovo.cwnisface.face.utils.FileUtils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by baohm1 on 2018/3/12.
 */

public class HttpFaceAnalyse {
    private static final String TAG = "HttpFaceAnalyse==";
    private static final String url = Constants.url_server;

    private static HttpFaceAnalyse mHttpFaceAnalyse = null;
    private static Gson mGson = new Gson();
    private static FaceCallback mFaceCallback = null;

    private HttpFaceAnalyse() {
    }

    public static HttpFaceAnalyse getInstance() {
        if (mHttpFaceAnalyse == null) {
            mHttpFaceAnalyse = new HttpFaceAnalyse();
        }
        return mHttpFaceAnalyse;
    }

    Gson gson = new Gson();
    public static int FaceRecogImg(final FaceCallback callback, String fileUrl, int trackID) {
        mFaceCallback = callback;

        File file = new File(fileUrl);
        String fileName = fileUrl.substring(fileUrl.lastIndexOf("/")+1);
        OkHttp3Utils.postPic(url, file, fileName, trackID, getPadID(), HttpCB);

        //remove tmp file
//        FileUtils.deleteFile(fileUrl);
        return 0;
    }

    //TODO temp for test
    public static int uploadJpg(String fileUrl) {
        String url_upload = Constants.url_img_server;
        File file = new File(fileUrl);
        String fileName = fileUrl.substring(fileUrl.lastIndexOf("/")+1);
        OkHttp3Utils.uploadPic(url_upload, file, fileName, 0, "Test", new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG, "onFailure: =========error");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //上传成功回调 目前不需要处理
                Log.d(TAG, "onResponse: ===========doUpload()===========OK");
            }
        });
        return 0;
    }

    private static Callback HttpCB = new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
            Log.d(TAG, "onFailure: =========error");
            if (mFaceCallback != null) {
                mFaceCallback.HttpTimeOutCB();
            }
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            String msg = response.body().string();
            if (msg == null || response.code() != 200 || msg.indexOf("data") == -1 || msg.indexOf("user") == -1) {
                return;
            }
//            Log.d(TAG, "onResponse: msg = "+ msg);
            MsgJsonBean msgBean = mGson.fromJson(msg, MsgJsonBean.class);
            //ret != 0, error， for test
//            if (msgBean.getStatus() != 0) {
//                Log.d(TAG, "recordonResponse: " + msg);
//                return;
//            }

            if (mFaceCallback != null && msgBean != null) {
                mFaceCallback.FaceRecognitionCB(msgBean);
            }
        }
    };

    private static String saveFaceData2Bin(byte[] data, int w, int h, int channels) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd_HH.mm.ss");
        String time = sdf.format(new Date());

        String fileName = "IMG_" + time + ".bin";  //jpeg文件名定义
        String filePath = Environment.getExternalStorageDirectory().toString() + "/tmpFace123";    //系统路径
        FileUtils.checkFilePath(filePath);
        String fileUrl = filePath + "/" + fileName;
        try {
            FileOutputStream fos = new FileOutputStream(filePath + "/" + fileName);
            Log.d(TAG, "saveRawJpg: " + fileName + "====>data.length = " + data.length);
            fos.write(w);
            fos.write(h);
            fos.write(channels);
            fos.write(data);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileUrl;
    }

    private static String getPadID() {
        if (Constants.PAD_ID != null) {
            return Constants.PAD_ID;
        }

        String ipv4 = getLocalIpAddress();
        if (ipv4 == null || ipv4.isEmpty()) {
            return "Default";
        }
//        Log.d(TAG, "getIP: " + ipv4);
        return ipv4.substring(ipv4.lastIndexOf(".")+1);
    }

    private static String getLocalIpAddress() {
        try {
            String ipv4;
            List<NetworkInterface> nilist = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface ni: nilist) {
                List<InetAddress>  ialist = Collections.list(ni.getInetAddresses());
                for (InetAddress address: ialist){
                    if (!address.isLoopbackAddress() && (address instanceof Inet4Address)) {
                        return address.getHostAddress().toString();
                    }
                }
            }
        } catch (SocketException ex) {
            Log.e(TAG, ex.toString());
        }
        return null;
    }
}
