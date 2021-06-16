package cn.lenovo.cwnisface.face.camera;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import cn.cloudwalk.CameraManager;
import cn.cloudwalk.SdkCodes;
import cn.cloudwalk.SdkManager;
import cn.cloudwalk.tool.Covert;
import cn.lenovo.cwnisface.face.Constants;
import cn.lenovo.cwnisface.face.realtime.FrameManagerThread;
import cn.lenovo.cwnisface.face.utils.Utils;

/**
 * Created by baohm1 on 2018/5/8.
 */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback{
    private static final String TAG = "CameraPreview==";
    private FrameManagerThread mFrameManagerThread = FrameManagerThread.getInstance();
    private SurfaceHolder mHolder; // 用于控制SurfaceView
    private CameraUtils mCameraUtils = null;
    private Context mContext = null;

    //线程
    private Thread mThreadPreview = null;				//Preview线程
    private Thread mThreadFrameGrab = null;			//Camera数据获取线程
    private volatile boolean mIsPreviewing = false;	//线程运行控制
    private volatile boolean mIsFrameGrabbing = false;	//Camera数据获取控制
    private volatile boolean mIsDisplay = true;		//预览显示控制

    //缓存数据
    private volatile byte[] m_FrameDataVis = null;
    private volatile byte[] m_FrameDataNis = null;
    private volatile boolean mIsCheckingVis = false;  //可见光相机重连接控制
    private volatile boolean mIsCheckingNis = false;  //红外相机重连接控制
    //跳过开始时间
    private long mPreviewSkipTime = 0;

    //TODO,下面为临时方案
    //根据图像是否灰度判断交换相机
    private boolean mIsOnceShowSwapped = false;
    private boolean mIsNeedDataSwapped = false;

    private boolean mIsDet = false;

    public CameraPreview(Context context) {
        super(context);
        initCameraPreview(context);
    }

    public CameraPreview(Context context, AttributeSet attrs) {
        super(context, attrs);
        initCameraPreview(context);
    }

    public CameraPreview(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initCameraPreview(context);
    }

    private void initCameraPreview(Context context) {
        mContext = context;
        mHolder = getHolder(); // 获得SurfaceHolder对象
        mCameraUtils = new CameraUtils();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d(TAG, "surfaceCreated: ");
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.d(TAG, "surfaceChanged: width * = height" + width + " * " + height);
        if (holder.getSurface() == null) {
            return;
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d(TAG, "surfaceDestroyed: ");
//        stopThread();
    }

    /**
     * 打开摄像头开始预览，但是并未开始识别
     */
    public boolean xfStartCamera() {
        mPreviewSkipTime = 0;
        boolean isOpen = false;
        for (int i=0; i<5; i++){
            isOpen = CameraManager.getInstance().CamerasOpen(Constants.default_begin_cam_index, Constants.default_max_cam_count);
            if (!isOpen) {
                Utils.msleep(20);
                CameraManager.getInstance().CamerasClose();
            } else {
                break;
            }
            Utils.msleep(20);
        }
        if (!isOpen) {
//            ShowMsg("打开相机失败！");
            Log.e(TAG, "xfStartCamera() open camera error...");
            return false;
        } else {
            boolean ret = mCameraUtils.setPreviewSize(Constants.PREVIEW_W, Constants.PREVIEW_H);
            if (!ret) {
                Log.e(TAG, "xfStartCamera() set preview size fail...");
                return false;
            } else {
                return xfStartCameraPreview();
            }
        }
    }

    /**
     * 关闭摄像头预览，并且隐藏扫描框
     */
    public void xfStopCamera() {
        xfStopCameraPreview();
        CameraManager.getInstance().CamerasClose();
        m_FrameDataVis = null;
        m_FrameDataNis = null;
    }

    public boolean xfStartCameraPreview() {
        //开启预览流
        boolean ret = CameraManager.getInstance().startPreview();
        if (ret) {
            Log.d(TAG, "xfStartCameraPreview() start camera preview OK...");
            startThread();
            return true;
        } else {
            Log.e(TAG, "xfStartCameraPreview() start camera preview fail...");
            return false;
        }
    }

    public void xfStopCameraPreview() {
        stopThread();
        CameraManager.getInstance().stopPreview();
        Canvas canvas = mHolder.lockCanvas(); // 获得画布对象，开始对画布画画
        if (canvas != null) {
            Log.d(TAG, "xfStopCameraPreview: ");
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            mHolder.unlockCanvasAndPost(canvas); // 完成画画，把画布显示在屏幕上
        }
    }

    //线程打开
    private void startThread() {
        //preview
        if (null == mThreadPreview) {
            mThreadPreview = new Thread(mRunnablePreview);
            mThreadPreview.start();
        }
        //frame grab
        if (null == mThreadFrameGrab) {
            mThreadFrameGrab = new Thread(mRunnableFrameGrab);
            mThreadFrameGrab.start();
        }
    }

    //线程关闭
    private void stopThread() {
        if (null != mThreadPreview) {
            mIsPreviewing = false;
            Utils.msleep(100);
            mThreadPreview = null;
        }
        if (null != mThreadFrameGrab) {
            mIsFrameGrabbing = false;
            Utils.msleep(100);
            mThreadFrameGrab = null;
        }
    }

    //Camera帧数据获取线程
    Runnable mRunnableFrameGrab = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "mRunnableFrameGrab: start...");
            mIsFrameGrabbing = true;
            while (mIsFrameGrabbing) {
                Utils.msleep(1);
                /*|| !mIsDetVisOk || (!mIsDetectOk && !mIsRecogOk)*/
                if (!mIsDisplay)
                    continue;

                int ret = processFrameGrab();
                if (1 == ret) {
                    break;
                } else if (0 == ret) {
                    ;//fps.update();
                } else {
                    continue;
                }
            }
            mIsFrameGrabbing = false;
            Log.d(TAG, "mRunnableFrameGrab: stop...");
        }
    };

    //预览线程执行
    private Runnable mRunnablePreview = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "mRunnablePreview: start");
            //FpsUtil fps = new FpsUtil("预览帧率测试", 3);
            mFrameManagerThread.init(mContext, CameraManager.getInstance().getPreviewWidth(),
                    CameraManager.getInstance().getPreviewHeight(),
                    SdkCodes.CW_IMG_BINARY, SdkCodes.getAngleCode(Constants.Camera_Rotate),
                    SdkCodes.getMirrorCode(Constants.Camera_H_Mir, Constants.Camera_V_Mir));
            mFrameManagerThread.start();

            mIsPreviewing = true;
            mIsDet = true;
            while (mIsPreviewing) {
                Utils.msleep(1);
                /*|| !mIsDetVisOk || (!mIsDetectOk && !mIsRecogOk)*/
                if (!mIsDisplay || !mFrameManagerThread.mFrameProcess.isSDKInitOK())
                    continue;
                try {
                    int ret = processOneFrameForPreview();
                    if (1 == ret) {
                        break;
                    } else if (0 == ret) {
                        ;//fps.update();
                    } else {
                        continue;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            mIsPreviewing = false;
            mIsDet = false;
            mFrameManagerThread.stop();
            Log.d(TAG, "mRunnablePreview: stop");
        }
    };

    //获取帧图像数据
    private int processFrameGrab() {
        int ret = 0;
        //红外相机采集
        if (CameraManager.getInstance().isOpenedNis()) {
            if (mIsCheckingNis) {
                CameraManager.getInstance().stopPreviewNis();
                checkCameraNis();
                ret = 1;
            } else {
                if (!CameraManager.getInstance().isPreviewingNis()) {
                    boolean bRet = mCameraUtils.setPreviewSizeNis(Constants.PREVIEW_W, Constants.PREVIEW_H);
                    if (!bRet) {
                        Log.e(TAG, "processFrameGrab()1 set preview size fail...");
                    } else {
                        //开启预览流
                        CameraManager.getInstance().startPreviewNis();
                        //等待一帧数据的时间
                        Utils.msleep(30);
                    }
                }

                //读取图像帧
                long time = System.currentTimeMillis();
                m_FrameDataNis = CameraManager.getInstance().getNisVideoData();
                //8ms
//                Log.d(TAG, "processFrameGrab: 获取Nis图像帧耗时 = " + (System.currentTimeMillis() - time) + "ms");
//                Log.d(TAG, "processFrameGrab: Nis图像帧大小 = " + m_FrameDataNis.length);

                if (null==m_FrameDataNis && ! mIsCheckingNis){
                    mIsCheckingNis = true;
                    CameraManager.getInstance().stopPreviewNis();
                    checkCameraNis();
                    ret = 1;
                }
            }
        }

        //彩色相机采集
        if (CameraManager.getInstance().isOpenedVis()) {
            if (mIsCheckingVis) {
                checkCameraVis();
                ret = 1;
            } else {
                if (!CameraManager.getInstance().isPreviewingVis()) {
                    boolean bRet = mCameraUtils.setPreviewSizeVis(Constants.PREVIEW_W, Constants.PREVIEW_H);
                    if (!bRet) {
                        Log.e(TAG, "processFrameGrab()2 set preview size fail...");
                    } else {
                        //开启预览流
                        CameraManager.getInstance().startPreviewVis();
                        //等待一帧数据的时间
                        Utils.msleep(30);
                    }
                }

                //读取图像帧
                long time = System.currentTimeMillis();
                m_FrameDataVis = CameraManager.getInstance().getVisVideoData();
                //45ms
//                Log.d(TAG, "processFrameGrab: 获取Vis图像帧耗时 = " + (System.currentTimeMillis() - time) + "ms");
//                Log.d(TAG, "processFrameGrab: Vis图像帧大小 = " + m_FrameDataVis.length + ", w*h = "
//                        + CameraManager.getInstance().getPreviewWidth() + " * "
//                        + CameraManager.getInstance().getPreviewHeight());


                if (null==m_FrameDataVis && ! mIsCheckingVis){
                    mIsCheckingVis = true;
                    CameraManager.getInstance().stopPreviewVis();
                    checkCameraVis();
                    ret = 1;
                }
            }
        }
        return ret;
    }

    private boolean test_first_flag = true;
    //Preview
    private int processOneFrameForPreview() {
        //图像数据判断
        if (null==m_FrameDataVis || null==m_FrameDataNis) {
            mPreviewSkipTime = 0;

            if (mIsOnceShowSwapped) {
                mIsOnceShowSwapped = false;//重置，预览可能需要交换相机
                mIsNeedDataSwapped = false;//重置，算法可能需要交换相机
            }
            return -1;
        }

        //打开相机后忽略一定时间，跳过相机成像初始阶段
        if (0 == mPreviewSkipTime) {
            mPreviewSkipTime = System.currentTimeMillis();
            return -1;
        }
        if (System.currentTimeMillis() - mPreviewSkipTime < Constants.MAX_PREVIEW_SKIP_TIME_MS) {
            return -1;
        }
        long time = System.currentTimeMillis();

        byte[] dataVis = m_FrameDataVis;
        byte[] dataNis = m_FrameDataNis;

        //图像解码
        BitmapFactory.Options ops = new BitmapFactory.Options();
        ops.inPreferredConfig = Bitmap.Config.ARGB_8888;
        ops.inSampleSize = 1;//为了减少处理时间，若为1则使用原图显示
        ops.outWidth = CameraManager.getInstance().getPreviewWidth();
        ops.outHeight = CameraManager.getInstance().getPreviewHeight();
        Bitmap bitmapVis = BitmapFactory.decodeByteArray(dataVis, 0, dataVis.length, ops);
        Bitmap bitmapNis = BitmapFactory.decodeByteArray(dataNis, 0, dataNis.length, ops);

        //23ms
//        Log.d(TAG, "processOneFrameForPreview: 图像格式转换消耗时间= " + (System.currentTimeMillis() - time) + "ms");

        if (! mIsOnceShowSwapped) {
            time = System.currentTimeMillis();
            mIsOnceShowSwapped = true;
            //根据图像颜色判断是否需要交换
            int nFlagNis = SdkManager.getInstance().isPictureGray(SdkCodes.CW_IMG_BINARY, dataNis, CameraManager.getInstance().getPreviewWidth(), CameraManager.getInstance().getPreviewHeight());
            Log.d(TAG, "step.cameraOpened");
            Log.d(TAG, "processOneFrameForPreview: 根据图像颜色判断是否需要交换耗时 = " + (System.currentTimeMillis() - time) + "ms");
            if (1 != nFlagNis) {//红外判断为彩色图时，需要交换
                mIsNeedDataSwapped = true;
                Log.d(TAG, "processOneFrameForPreview: 需要交换数据===============");
            }
        }
        time = System.currentTimeMillis();
        //数据推送给活体检测算法线程
        if(mIsDet){
            if (mIsNeedDataSwapped) {
                mFrameManagerThread.pushFrame(m_FrameDataNis, m_FrameDataVis);
            } else {
                mFrameManagerThread.pushFrame(m_FrameDataVis, m_FrameDataNis);
            }
        }

        //2ms
//        Log.d(TAG, "processOneFrameForPreview: 推送数据消耗时间= " + (System.currentTimeMillis() - time) + "ms");

        time = System.currentTimeMillis();
        //显示的图像处理
        Bitmap bmpDest = null;
        if (Constants.IsShowVis) {//交换相机显示，默认显示彩色，不使能显示红外
            bmpDest = mIsNeedDataSwapped ? bitmapNis : bitmapVis;
        } else {
            bmpDest = mIsNeedDataSwapped ? bitmapVis : bitmapNis;
        }
        bmpDest = Covert.rotate(bmpDest, Constants.Camera_Rotate, Constants.Camera_H_Mir, Constants.Camera_V_Mir);
        Matrix m = new Matrix();
        m.postScale(Constants.PREVIEW_SCALE, Constants.PREVIEW_SCALE);
        bmpDest = Bitmap.createBitmap(bmpDest, 0, 0, bmpDest.getWidth(), bmpDest.getHeight(), m, true);
        if (null == bmpDest)
            return -1;
        //8ms
//        Log.d(TAG, "processOneFrameForPreview: 图像处理= " + (System.currentTimeMillis() - time) + "ms");

        //绘制图像显示
//        time = System.currentTimeMillis();
        Canvas canvas = mHolder.lockCanvas(); // 获得画布对象，开始对画布画画
        if (canvas != null) {
//            Log.d(TAG, "processOneFrameForPreview: w * h = " + bmpDest.getWidth() + " * " + bmpDest.getHeight());
            canvas.drawBitmap(bmpDest, 0, 0, null);
            mHolder.unlockCanvasAndPost(canvas); // 完成画画，把画布显示在屏幕上
        }
        //4ms
//        Log.d(TAG, "processOneFrameForPreview: 图像绘制= " + (System.currentTimeMillis() - time) + "ms");

        //宽高重置
//        int picWidth = bmpDest.getWidth();
//        int picHeight = bmpDest.getHeight();
        //头像框坐标转换（屏幕->图像）
//        m_rcHeadInViewToBmp = surfaceView.mapHeadFromViewToBmp(
//                picWidth * ops.inSampleSize, picHeight * ops.inSampleSize
//                , new Rect(imgHead.getLeft(),imgHead.getTop(),imgHead.getLeft()+imgHead.getWidth(),imgHead.getTop()+imgHead.getHeight())
//        );
//        Log.d(TAG, "processOneFrameForPreview: 图像显示= " + (System.currentTimeMillis() - time) + "ms");
        //回收资源
        Covert.RecyleBitmap(bitmapVis);
        Covert.RecyleBitmap(bitmapNis);
        Covert.RecyleBitmap(bmpDest);
        return 0;
    }


    //红外Camera重连检查
    private boolean checkCameraNis() {
        CameraManager.getInstance().CameraCloseNis();
        Utils.msleep(5);
        boolean isOpen = CameraManager.getInstance().CameraOpenNis();
        if (isOpen) {
            mIsCheckingNis = false;
            Log.d(TAG, "checkCameraNis: Retry Open Camera OK");
        } else {
            Log.e(TAG, "checkCameraNis: Retry Open Camera NG...");
        }
        return isOpen;
    }

    //彩色Camera重连检查
    private boolean checkCameraVis() {
        CameraManager.getInstance().CameraCloseVis();
        Utils.msleep(5);
        boolean isOpen = CameraManager.getInstance().CameraOpenVis();
        if (isOpen) {
            mIsCheckingVis = false;
            Log.d(TAG, "checkCameraVis: Retry Open Camera OK");
        } else {
            Log.e(TAG, "checkCameraVis: Retry Open Camera NG...");
        }
        return isOpen;
    }

    public void setDet(boolean det) {
        mIsDet = det;
    }
}
