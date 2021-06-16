package cn.lenovo.cwnisface.face.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;


import com.lenovo.billing.R;

import cn.lenovo.cwnisface.face.camera.CameraPreview;
import cn.lenovo.cwnisface.face.utils.JsonUtil;

/**
 * Created by baohm1 on 2018/3/14.
 */

public class MyCameraPreview extends RelativeLayout {
    private static final String TAG = "MyCameraPreview";
//    private static final boolean WAITING = false;
//    private static final boolean WORKING = true;
    private Context context;

    private FrameLayout faceRectContainer;
    private CameraPreview cameraPreview;
    private int previewWidth = 0;
    private int previewHeight = 0;

//    private int workingCount = 0;

//    private SparseBooleanArray workerList = new SparseBooleanArray();
//    private SparseArray<FaceView> faceViewList = new SparseArray<>(5);

    public MyCameraPreview(Context context) {
        super(context);
        init(context);
    }

    public MyCameraPreview(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public MyCameraPreview(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        this.context = context;
        LayoutInflater.from(context).inflate(R.layout.view_my_camera_preview, this, true);
        cameraPreview = (CameraPreview) findViewById(R.id.camera_preview);
//        faceRectContainer = (FrameLayout) findViewById(R.id.face_container);
//        txtErrorView = (TextView) findViewById(R.id.errorMessage);

        JsonUtil.updateConfigFromSdcard();
    }


    public boolean startCamera() {
        return cameraPreview.xfStartCamera();
    }

    public void stopCamera() {
        cameraPreview.xfStopCamera();
        previewWidth = 0;
        previewHeight = 0;
    }

//    public void refresh(List<FaceDetInfo> faceInfoList) {
//        if (!cameraPreview.isPreview() || !Contans.ENABLE_FACE_VIEW) {
//            return;
//        }
//        if (previewWidth == 0 || previewHeight == 0) {
//            Camera.Size size = cameraPreview.getPreviewSize();
//            if (CameraUtils.isPortrait(context)) {
//                previewWidth = size.height;
//                previewHeight = size.width;
//            } else {
//                previewWidth = size.width;
//                previewHeight = size.height;
//            }
//        }
//        refresh(faceInfoList, previewWidth, previewHeight);
//    }
//
//    private void refresh(List<FaceDetInfo> faceInfoList, int imageWidth, int imageHeight) {
//        faceRectContainer.removeAllViews();
//        if (null == faceInfoList || 0 == faceInfoList.size()) {
//            return;
//        }
//
//        resetViewWaiting();
//        for (int i = 0; i < (faceInfoList.size() - getWaitingCount()) && 5 > faceViewList.size(); i++) {
//            FaceView faceView = new FaceView(context, cameraPreview.getMeasuredWidth(), cameraPreview.getMeasuredHeight());
//            int id = faceViewList.size() + 1;
//            faceViewList.append(id, faceView);
//            workerList.append(id, WAITING); // 新加入一个View,处于等待状态
//        }
//
//        for (FaceDetInfo faceInfo : faceInfoList) {
//            FaceView faceView;
//            int viewId = getWorkerId();
//            faceView = faceViewList.get(viewId);
//            if (null == faceView) {
//                return;
//            }
//            workerList.put(viewId, WORKING);
//            workingCount++;
//            faceView.onRefresh(faceInfo, imageWidth, imageHeight);
//            faceRectContainer.addView(faceView);
//        }
//
//        invalidate();
//    }

//    private void resetViewWaiting() {
//        for (int i = 1; i < workerList.size() + 1; i++) {
//            workerList.put(i, WAITING);
//        }
//        workingCount = 0;
//    }

//    private int getWaitingCount() {
//        return workerList.size() - workingCount;
//    }
//
//    private int getWorkerId() {
//        for (int i = 1; i < workerList.size() + 1; i++) {
//            if (!workerList.get(i)) { // 没有工作
//                return i;
//            }
//        }
//        return 0;
//    }

    public CameraPreview getCameraPreview() {
        return cameraPreview;
    }
}
