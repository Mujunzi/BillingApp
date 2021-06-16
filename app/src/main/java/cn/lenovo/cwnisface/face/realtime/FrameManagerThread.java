package cn.lenovo.cwnisface.face.realtime;

import android.content.Context;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

import cn.cloudwalk.sdk.Interface.IColorType;
import cn.lenovo.cwnisface.face.Constants;

/**
 * Created by baohm1 on 2018/3/5.
 */

public class FrameManagerThread {
    private static final String TAG = "FrameManagerThread==";
    private FrameManager mFManager = null;
    public FrameProcess mFrameProcess = null;

    private boolean isFrameGrabbing = false;
    private boolean isFrameDetecting = false;

    private Timer mTimerFaceDetect = null;

    private int mLengthVis = 0;
    private int mLengthNis = 0;
    private boolean isInit = false;

    private static FrameManagerThread sFrameManagerThread = null;

    public static FrameManagerThread getInstance() {
        if (null == sFrameManagerThread) {
            sFrameManagerThread = new FrameManagerThread();
        }
        return sFrameManagerThread;
    }

    public void init(Context context, int w, int h, int format, int angle, int mirror) {
        if (isInit || w == 0 || h == 0)
            return;

        isInit = true;
        mLengthVis = (int)(w * h * SIZE_FORMAT(format));
        mLengthNis = (int)(w * h * SIZE_FORMAT(format));
        mFrameProcess = new FrameProcess(context, w, h, format, angle, mirror);
        initFaceFrameManager(mLengthVis, mLengthNis);
        Log.d(TAG, "FrameManagerThread init ok, angle = " + angle);
        Log.d(TAG, "FrameManagerThread init ...");
    }

    public void start() {
        Log.d(TAG, "start: ");
        startFaceDetect();
        mFrameProcess.startFrameProcess();
    }

    public void stop() {
        Log.d(TAG, "stop: ");
        stopFaceDetect();
        mFrameProcess.stopFrameProcess();
        while(isFrameGrabbing || isFrameDetecting) {
            try {
                Log.d(TAG, "Wait frame handle...");
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        mFrameProcess.clearCache();
        mFManager.resetFrameBuffer();
    }

    public int pushFrame(byte[] dataVis, byte[] dataNis) {
        if (isFrameGrabbing || dataVis == null || dataNis == null)
            return 0;

        isFrameGrabbing = true;
        FrameManager.FrameBuffer fBuffer = mFManager.queueBuffer();
        if (fBuffer != null) {
//			System.out.println("=========Write Buffer:" + fBuffer.index + ", counter = " + fBuffer.counter);
//            Log.d(TAG, "putFrameData: 0");
            System.arraycopy(dataVis, 0, fBuffer.dataVis, 0, dataVis.length);
            System.arraycopy(dataNis, 0, fBuffer.dataNis, 0, dataNis.length);
//            Log.d(TAG, "putFrameData: 1");
            fBuffer.isBusy = false;
            fBuffer.isVaild = true;
        }
        isFrameGrabbing = false;
        return mLengthVis;
    }

    public void setCameraOrientation(int angle, int mirror) {
        mFrameProcess.setCameraOrientation(angle, mirror);
    }

    private void initFaceFrameManager(int lengthVis, int lengthNis) {
        mFManager = new FrameManager();
        for (int i=0; i<FrameManager.FRAME_BUFFER_NUM; i++) {
            mFManager.frameBuffers[i].dataVis = new byte[lengthVis];
            mFManager.frameBuffers[i].lengthVis = lengthVis;
            mFManager.frameBuffers[i].dataNis = new byte[lengthNis];
            mFManager.frameBuffers[i].lengthNis = lengthNis;
        }
        Log.d(TAG, "initFaceFrameManager: Frame lengthVis = " + lengthVis + ", lengthNis = " + lengthNis);
    }

    private void startFaceDetect() {
        TimerTask faceDetect = new TimerTask() {
            @Override
            public void run() {
                isFrameDetecting = true;
                FrameManager.FrameBuffer fBuffer = mFManager.dequeueBuffer();
                if (fBuffer != null) {
//					System.out.println("=========Read Buffer:" + fBuffer.index + ", counter = " + fBuffer.counter);
                    mFrameProcess.onPreviewFrame(fBuffer.dataVis, fBuffer.dataNis);
//					int faceNum = mFrameProcess.onPreviewFrame(fBuffer.frame);
//					if (faceNum > 0) {
//						ImgUtils.saveImage(fBuffer.frame);
//					}
                    fBuffer.isBusy = false;
                    fBuffer.isVaild = false;
//					mFps.fps();
//					saveImage(fBuffer.frame);
                }
                isFrameDetecting = false;
            }
        };
        mTimerFaceDetect = new Timer();
        mTimerFaceDetect.schedule(faceDetect, 0, Constants.FrameDetectPeriod);
    }

    private void stopFaceDetect() {
        if (mTimerFaceDetect != null) {
            mTimerFaceDetect.cancel();
            mTimerFaceDetect = null;
        }
    }

    private float SIZE_FORMAT(int format) {
        float ret = 2;
        switch (format) {
            case IColorType.CW_IMG_YV12:
                break;
            case IColorType.CW_IMG_NV21:
                ret = 1.5f;
                break;
            case IColorType.CW_IMG_BINARY:
                ret = 3;
                break;
        }
        return ret;
    }
}
