/**
 * Created by baohm1 on 2018/3/5.
 */

package cn.lenovo.cwnisface.face.realtime;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import cn.cloudwalk.SdkCodes;
import cn.cloudwalk.SdkManager;
import cn.cloudwalk.SdkResult;
import cn.cloudwalk.sdk.Interface.IRstCode;
import cn.cloudwalk.tool.Covert;
import cn.lenovo.cwnisface.face.Constants;
import cn.lenovo.cwnisface.face.bean.FaceConstants;
import cn.lenovo.cwnisface.face.bean.FaceMsgCallback;
import cn.lenovo.cwnisface.face.bean.FaceRecogMsgCallback;
import cn.lenovo.cwnisface.face.bean.MsgJsonBean;
import cn.lenovo.cwnisface.face.http.FaceCallback;
import cn.lenovo.cwnisface.face.http.HttpFaceAnalyse;
import cn.lenovo.cwnisface.face.utils.FileUtils;
import cn.lenovo.cwnisface.face.utils.MyTime;

/**
 * @author baohm1
 * @email baohm1@lenovo.com
 */
public class FrameProcess implements FaceCallback {
    private static final String TAG = "FrameProcess==";
    private static final int MSG_UPDATE_POST_TIMEOUT = 1;
    private static final int MSG_UPDATE_SKIP_END = 2;
//    private static final int FRAME_PROCESS_STATE_IDLE = 0x10;
    private static final int FRAME_PROCESS_STATE_SKIP = 0x11;
    private static final int FRAME_PROCESS_STATE_WORKING = 0x12;
    private static final int FRAME_PROCESS_STATE_FINISH = 0x13;

    //上报消息的回调函数
    private static FaceMsgCallback msgCallback = null;
    private static FaceRecogMsgCallback msgFaceRecogCallback = null;
    //活体检测SDK
    private SdkManager mSdkManager = SdkManager.getInstance();
    public SdkResult mLastResult = null;
    private volatile boolean mIsSdkOk = false;//SDK初始化返回值

    //帧处理状态
    private int mState = FRAME_PROCESS_STATE_WORKING;
    //上传人脸识别的图片ID
    private static volatile int mTrackID = 0;
    //上传服务器图片返回的识别信息,以trackID为key， MsgJsonBean.DataBean.UserBean为value
    private Map<Integer, MsgJsonBean.DataBean.UserBean> mFaceMap = new HashMap<Integer, MsgJsonBean.DataBean.UserBean>();
    private Map<Integer, String> mFaceMapUrl = new HashMap<Integer, String>();

    private Timer mTimerSkip = null;
    private Timer mTimerPost = null;

    //Camera输出数据参数
    private int mCWFaceFormat = 0;
    private int mWidth, mHeight, mAngle, mMirror;

    public FrameProcess(Context context, int height, int width, int mirror, int angle, int format) {
        mWidth = width;
        mHeight = height;
        mMirror = mirror;
        mAngle = angle;
//        setCwFaceFormat(format);
        mCWFaceFormat = SdkCodes.CW_IMG_BINARY;
        mSdkManager = SdkManager.getInstance();
        InitSDK(context);
        //init timer
        mTimerPost = new Timer();
        mTimerSkip = new Timer();
    }

    //析构函数，释放资源
    @Override
    protected void finalize() throws Throwable {
        Log.d(TAG, "finalize: ");
        super.finalize();
        UnInitSDK();
        mTimerSkip.purge();
        mTimerPost.purge();
        msgCallback = null;
        msgFaceRecogCallback = null;
        mFaceMap.clear();
    }

    public static void registerCallBack(FaceMsgCallback faceMsgCallback) {
        msgCallback = faceMsgCallback;
    }

    public static void registerFaceRecogCallBack(FaceRecogMsgCallback faceCallback) {
        msgFaceRecogCallback = faceCallback;
    }

    public int clearCache() {
        mFaceMap.clear();
        return 0;
    }

    public void startFrameProcess() {
        Log.d(TAG, "startFrameProcess: ");
        mState = FRAME_PROCESS_STATE_WORKING;
    }

    public void stopFrameProcess() {
        Log.d(TAG, "stopFrameProcess: ");
        resetSkipTimerTask();
        resetPostTimerTask();
        mState = FRAME_PROCESS_STATE_FINISH;
    }

    public boolean isSDKInitOK() {
        return mIsSdkOk;
    }

    private void InitSDK(Context context) {
        mSdkManager.unInit();
        Log.d(TAG, "InitSDK: ");
        mSdkManager.init(context, new SdkManager.ISdkInitCallBack() {
            @Override
            public void onFinish(int code) {
                Log.d(TAG, "SDK初始化结果code: " + code);
                mIsSdkOk = SdkCodes.isInitOk(code);
                if (!mIsSdkOk) {
                    Log.e(TAG, "SDK初始化失败 ");
                    return;
                }
                setupSDKParam(Constants.CW_CLARITY, Constants.CW_POSE_YAW,
                        Constants.CW_POSE_PITCH, Constants.CW_POSE_ROLL, Constants.CW_SKIN_SCORE);
                mState = FRAME_PROCESS_STATE_WORKING;
            }
        });
    }

    private void UnInitSDK() {
        mSdkManager.unInit();
    }

    private void setupSDKParam(float clarity, float yaw, float pitch, float roll, float skin) {
        //TODO 设置肤色阈值 范围[0.00,1.00]
        //SdkManager.setSkinScore(mParam_fSkinScore);
		/*//TODO 支持的参数(可选):
		 * "skinThreshold"，肤色阈值，范围[0.00,1.00]，默认0.35；
		 * "yaw"，人脸角度，左转为正，右转为负，范围[-90,90]，绝对值，默认15；
		 * "pitch"，人脸角度，抬头为正，低头为负，范围[-90,90]，绝对值，默认30；
		 * "roll"，人脸角度，顺时针为正，逆时针为负，范围[-90,90]，绝对值，默认30；
		 * "clarity"，清晰度，范围[0.00,1.00]，默认0.30。*/
        Map<String,Float> params = new HashMap<>();
        params.put("skinThreshold",skin);
        params.put("clarity",clarity);
        params.put("yaw",yaw);
        params.put("pitch",pitch);
        params.put("roll",roll);
        SdkManager.getInstance().setParams(params);
    }

    /**
     * 帧处理：人脸检测
     * param byte[]
     * return
     */
    public int onPreviewFrame(byte[] dataVis, byte[] dataNis) {
        return onPreviewFrame(dataVis, dataNis, mWidth, mHeight, mCWFaceFormat, mAngle, mMirror);
    }

    private synchronized int onPreviewFrame(byte[] dataVis, byte[] dataNis, int width, int height, int format, int angle, int mirror) {
        //确认是否处于丢弃数据状态, 是否一轮上传图片数量已经满足，满足一个条件则暂时不进行帧处理
        if (FRAME_PROCESS_STATE_WORKING != mState || mFaceMap.size() > Constants.CW_POST_FACE_NUM) {
            return 0;
        }

        long time = System.currentTimeMillis();

//        Log.d(TAG, "onPreviewFrame: 活体检测 ok angle = " + angle + ", SdkCodes.getAngleCode(angle) = " + SdkCodes.getAngleCode(angle));

        int ret = mSdkManager.putFrame(format, dataVis, width, height,
                                dataNis, width, height,
                                0, 0, width, height,
                                angle, mirror);
        if (ret == 0) {
            //ok
            mLastResult = mSdkManager.getResult();
//            Log.d(TAG, "onPreviewFrame: 活体检测消耗时间= " + (System.currentTimeMillis() - time) + "ms");
//            Log.d(TAG, "onPreviewFrame: 活体检测结果= " + mLastResult.toString());
            ret = doDetectOk(dataVis, dataNis, mLastResult);
        } else {
            //error
            Log.d(TAG, "onPreviewFrame: 活体检测错误..." + ret);
        }
        return ret;
    }

    private int doDetectOk(byte[] curDataVis, byte[] curDataNis, SdkResult result) {
        if (result != null){
            int retCode = result.getCode();
            if (SdkCodes.isDetectOk(retCode)) {//成功
                if (result.getClipedFaceVisWidth() < Constants.CW_FACE_Min_Size) {
                    Log.d(TAG, "doDetectOk: 活体检测成功，人脸太小，忽略此帧. size = "
                            + result.getClipedFaceVisWidth() + "*" + result.getClipedFaceVisHeight());
                    return 1;
                } else {
//                    Log.d(TAG, "doDetectOk: face size = "
//                            + result.getClipedFaceVisWidth() + "*" + result.getClipedFaceVisHeight());
                }
                FileUtils.checkFilePath(Constants.CROP_IMG_PATH);

                long time = System.currentTimeMillis();
                //保存图片：对齐彩色人脸
//                Bitmap bitmapAlignVis = Covert.BGRToBitmap(result.getAlignImg(), result.getAlignWidth(), result.getAlignHeight());
//                String file = Contants.CROP_IMG_PATH + File.separator + MyTime.currentTimeMillis() + ".jpg";
//                boolean saveOK = Covert.saveToJpeg(bitmapAlignVis, file, 80);
//                Covert.RecyleBitmap(bitmapAlignVis);
//                Log.d(TAG, "doDetectOk: 保存对齐图片消耗时间= " + (System.currentTimeMillis() - time) + "ms");

                time = System.currentTimeMillis();
                //保存图片：裁剪彩色人脸
                Bitmap bitmapCropVis = Covert.BGRToBitmap(result.getClipedFaceVisImg(),
                        result.getClipedFaceVisWidth(),
                        result.getClipedFaceVisHeight());
                String fileCrop = Constants.CROP_IMG_PATH + File.separator + "Vis_Crop_" + MyTime.currentTimeMillis() + ".jpg";
                boolean save2OK = Covert.saveToJpeg(bitmapCropVis, fileCrop, 80);
                Covert.RecyleBitmap(bitmapCropVis);
                Log.d(TAG, "doDetectOk: The time for save crop jpg = " + (System.currentTimeMillis() - time) + "ms");

                Log.d(TAG, "doDetectOk: result = " + result.toString());
                //保存图片：红外人脸
//                Bitmap bitmapAlignNis = Covert.BGRToBitmap(result.getAlignNisImg(), result.getAlignNisWidth(), result.getAlignNisHeight());
//                Covert.saveToJpeg(bitmapAlignNis, picRootPath+File.separator+ "alignFaceNis_"+imgSuffix+".jpg", AppConst.MAX_JPG_SAVE_QUALITY);
//                Covert.RecyleBitmap(bitmapAlignNis);
                //保存图片：彩色原图
//                BitmapFactory.Options ops = new BitmapFactory.Options();
//                ops.inPreferredConfig = Bitmap.Config.ARGB_8888;
//                ops.inSampleSize = 1;//为了减少处理时间，若为1则使用原图显示
//                ops.outWidth = CameraManager.getInstance().getPreviewWidth();
//                ops.outHeight = CameraManager.getInstance().getPreviewHeight();
//                Bitmap bitmapCamVis = BitmapFactory.decodeByteArray(curDataVis, 0, curDataVis.length, ops);
//                String fileFull = Contants.CROP_IMG_PATH + File.separator + "Full_" + MyTime.currentTimeMillis() + ".jpg";
//                Bitmap bitmapOrgVis = Covert.rotate(bitmapCamVis, Contants.Camera_Rotate, Contants.Camera_H_Mir, Contants.Camera_V_Mir);
//                Covert.saveToJpeg(bitmapOrgVis, fileFull, 80);
//                Covert.RecyleBitmap(bitmapOrgVis);
                //保存图片：红外原图
//                Bitmap bitmapCamNis = BitmapFactory.decodeByteArray(curDataNis, 0, curDataNis.length, ops);
//                Bitmap bitmapOrgNis = Covert.rotate(bitmapCamNis, mParam_nRotateDegree, mParam_isTrunHor, mParam_isTrunVer);
//                Covert.saveToJpeg(bitmapOrgNis, picRootPath + File.separator+"orgPictureNis_"+imgSuffix+".jpg", AppConst.MAX_JPG_SAVE_QUALITY);
//                Covert.RecyleBitmap(bitmapOrgNis);

                if (save2OK) {
                    //上传图片未返回消息时，mFaceMap添加的信息用null
                    mFaceMap.put(mTrackID, null);
                    mState = FRAME_PROCESS_STATE_SKIP;
                    //已经有足够的上传数量，则进入skip状态
                    if (mFaceMap.size() < Constants.CW_POST_FACE_NUM) {
                        setSkipTimerTask(Constants.CW_POST_FACE_SKIP_TIME);
                    } else {
                        setPostTimerTask(Constants.CW_POST_FACE_POST_TIMEOUT);
                    }
                    mFaceMapUrl.put(mTrackID, fileCrop);
                    Log.d(TAG, "doDetectOk: post mTrackID = " + mTrackID);
                    return HttpFaceAnalyse.FaceRecogImg(this, fileCrop, mTrackID++);
                }
//                mHandler.obtainMessage(AppConst.msg_id_reslut, retCode, 0, null).sendToTarget();
                return 0;
            }

            //人脸距离摄像头距离不佳
            if (retCode == IRstCode.CW_NIS_LIV_DIST_FAILED){
                Log.d(TAG, "doDetectOk: The distance failed,50-100cm is better");
                postListenError(FaceConstants.Err_Code_Distance);
                return 1;
            }

            if (SdkCodes.isDetectAttack(retCode)) {//攻击
                Log.d(TAG, "doDetectOk: Maybe is not real person");
//                mHandler.obtainMessage(AppConst.msg_id_reslut, retCode, 0, null).sendToTarget();
                return 1;
            }
        }
        return 1;
    }


    /**
     * 获取对齐头像并人脸识别
     * param imgData
     * param trackID
     */
//    private synchronized int faceRecog_Img(byte[] imgData, FaceInfo faceInfo, int width, int height, int angle, int mirror) {
//        int trackID = faceInfo.trackId;
//        Log.d(TAG, "faceRecog_Img trackID = " + trackID);
//        int ret = -1;
//        //save IMG
//        String file = FileUtils.saveCropJpg(imgData, width, height, faceInfo.x, faceInfo.y, faceInfo.width, faceInfo.height, angle, mirror);
//        if (file != null) {
//            ret = HttpFaceAnalyse.FaceRecogImg(this, file, trackID);
//        }
//
//        return ret;
//    }

//    private void updateUIFaceTrack() {
//        if (msgCallback != null && Contans.ENABLE_FACE_VIEW) {
//            msgCallback.UpdateFaceMsg(mFaceMap);
//            return;
//        }
//    }

//    private int setCwFaceFormat(int preview_format) {
//        switch (preview_format) {
//            case ImageFormat.YV12:
//                mCWFaceFormat = FaceInterface.cw_img_form_t.CW_IMAGE_NV12;
//                break;
//            case ImageFormat.NV21:
//                mCWFaceFormat = FaceInterface.cw_img_form_t.CW_IMAGE_NV21;
//                break;
//        }
//        return mCWFaceFormat;
//    }

    @Override
    public synchronized int FaceRecognitionCB(MsgJsonBean msgBean) {
        if (msgBean.getData() == null || msgBean.getData().getUser() == null) {
            Log.d(TAG, "recordonResponse = " + msgBean.getStatus() + ", error msg = " + msgBean.getErrMsg());
            return 0;
        }

        int trackID = msgBean.getData().getUser().getTrackID();
        //无效消息，一般是过期的trackID
        if (!mFaceMap.containsKey(trackID)) {
            return 0;
        }
        
        int faceID = msgBean.getData().getUser().getFaceID();
        double similar = msgBean.getData().getUser().getSimilar();

        if(Constants.CW_FACE_IS_OWN.equals("false")){
            if (similar > Constants.CW_FACE_HIGH_SIMILAR) {
                postMsgFaceRecognitionAndFinish(faceID, true, trackID);
                Log.d(TAG, "云-FaceRecognitionCB: trackID = " + trackID + ", faceID = " + faceID + ", similar(" + similar + ") > " + Constants.CW_FACE_HIGH_SIMILAR);
                return faceID;
            }
        }else {
            if (similar <= Constants.CW_FACE_OWN_SIMILAR) {
                postMsgFaceRecognitionAndFinish(faceID, true, trackID);
                Log.d(TAG, "自-FaceRecognitionCB: trackID = " + trackID + ", faceID = " + faceID + ", similar(" + similar + ") > " + Constants.CW_FACE_OWN_SIMILAR);
                return faceID;
            }
        }


        //更新mFaceMap的trackID对应的value
        mFaceMap.put(trackID, msgBean.getData().getUser());
        Log.d(TAG, "mFaceMap.size() = " + mFaceMap.size());
        Log.d(TAG, "trackID = " + trackID + ", faceID = " + faceID + ", similar = " + msgBean.getData().getUser().getSimilar());

        //receive error msg,
        if (msgBean.getStatus() != 0) {
            Log.d(TAG, "recordonResponse = " + msgBean.getStatus() + ", error msg = " + msgBean.getErrMsg());
            mFaceMap.remove(trackID);
            if (msgBean.getStatus() == FaceConstants.CW_FACE_POSE_ERROR) {
                postListenError(FaceConstants.Err_Code_Pose);
            }
            if (msgBean.getStatus() == FaceConstants.CW_FACE_MOG_ERROR) {
                postListenError(FaceConstants.Err_Code_Blur);
            }
        }

        //如果已经上传一轮图片，而且上传图片都已经返回消息
        if (mFaceMap.size() >= Constants.CW_POST_FACE_NUM && isReceiveAllMsg()) {
            return postBestFaceMsg();
        }
        return 0;
    }

    @Override
    public int HttpTimeOutCB() {
        if (msgFaceRecogCallback != null) {
            msgFaceRecogCallback.FaceRecogListenError(FaceConstants.Err_Code_TimeOut);
        }
        return 0;
    }

    private long lastPostErrTime = 0;
    private void postListenError(int errCode) {
        long curTime = System.currentTimeMillis();
        //每个1000ms上报一次错误
        if (msgFaceRecogCallback != null && (curTime - lastPostErrTime) > 1000) {
            msgFaceRecogCallback.FaceRecogListenError(errCode);
            lastPostErrTime = System.currentTimeMillis();
        }
    }

    private void postMsgFaceRecognitionAndFinish(int faceID, boolean isFound, int trackID) {
        if (msgFaceRecogCallback != null) {
            msgFaceRecogCallback.FaceRecogListening(faceID, isFound, mFaceMapUrl.get(trackID));
        }
        mFaceMap.clear();
        mFaceMapUrl.clear();
//        stopFrameProcess();
    }

    private int postBestFaceMsg() {
        //获取相似度最高的faceID
        int bestFaceID = -1;
        int trackID = -1;
        double bestSimilar = 0;
        double ownSimilar = 4;

        if(Constants.CW_FACE_IS_OWN.equals("false")){
            for (MsgJsonBean.DataBean.UserBean userBean: mFaceMap.values()) {
                if (userBean != null && userBean.getSimilar() > bestSimilar) {
                    bestSimilar = userBean.getSimilar();
                    bestFaceID = userBean.getFaceID();
                    trackID = userBean.getTrackID();
                }
            }
            if (bestSimilar > Constants.CW_FACE_SIMILAR) {
                postMsgFaceRecognitionAndFinish(bestFaceID, true, trackID);
                Log.d(TAG, "云-postBestFaceMsg: bestFaceID = " + bestFaceID + "-----Similar:" + bestSimilar);
                return bestFaceID;
            }
        }else {
            for (MsgJsonBean.DataBean.UserBean userBean: mFaceMap.values()) {
                if (userBean != null && userBean.getSimilar() < ownSimilar) {
                    ownSimilar = userBean.getSimilar();
                    bestFaceID = userBean.getFaceID();
                    trackID = userBean.getTrackID();
                }
            }
            if (ownSimilar <= Constants.CW_FACE_OWN_SIMILAR) {
                postMsgFaceRecognitionAndFinish(bestFaceID, true, trackID);
                Log.d(TAG, "自-postBestFaceMsg: bestFaceID = " + bestFaceID + "-----Similar:" + ownSimilar);
                return bestFaceID;
            }
        }
        return -1;
    }

    private boolean isReceiveAllMsg() {
        for (MsgJsonBean.DataBean.UserBean userBean: mFaceMap.values()) {
            if (userBean == null) {
                return false;
            }
        }
        return true;
    }

    private TimerTask mSkipTimerTask = null;
    private TimerTask mPostTimerTask = null;
    private void setSkipTimerTask(int delay) {
        mSkipTimerTask = new TimerTask() {
            @Override
            public void run() {
                if (mFaceMap.size() < Constants.CW_POST_FACE_NUM) {
                    mState = FRAME_PROCESS_STATE_WORKING;
                }
            }
        };
        mTimerSkip.schedule(mSkipTimerTask, delay);
    }

    private void setPostTimerTask(int delay) {
        mPostTimerTask = new TimerTask() {
            @Override
            public void run() {
                int faceID = postBestFaceMsg();
                //如果没有找到faceID，则清空mFaceMap后继续识别
                if (faceID < 0 ) {
                    mFaceMap.clear();
                    mState = FRAME_PROCESS_STATE_WORKING;
                }
            }
        };
        mTimerPost.schedule(mPostTimerTask, delay);
    }

    private void resetSkipTimerTask() {
        if (mSkipTimerTask != null) {
            mSkipTimerTask.cancel();
        }
    }

    private void resetPostTimerTask() {
        if (mPostTimerTask != null) {
            mPostTimerTask.cancel();
        }
    }

    public void setCameraOrientation(int angle, int mirror) {
        mAngle = angle;
        mMirror = mirror;
    }
}
