package cn.lenovo.cwnisface.face.camera;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import cn.cloudwalk.CameraManager;

/**
 * Created by baohm1 on 2018/5/8.
 */

public class CameraUtils {
    private static final String TAG = "CameraUtils==";

    public boolean setPreviewSize(int w, int h) {
        CameraManager.Size size = getOptimalPreviewSize(CameraManager.getInstance().getSupportedSize(), w, h);
        Log.d(TAG, "setPreviewSize: w * h = " + w + " * " + h);
        return CameraManager.getInstance().setPreviewSize(size.getWidth(), size.getHeight());
    }

    public boolean setPreviewSizeNis(int w, int h) {
        CameraManager.Size size = getOptimalPreviewSize(CameraManager.getInstance().getSupportedSize(), w, h);
        Log.d(TAG, "setPreviewSizeNis: w * h = " + w + " * " + h);
        return CameraManager.getInstance().setPreviewSizeNis(size.getWidth(), size.getHeight());
    }

    public boolean setPreviewSizeVis(int w, int h) {
        CameraManager.Size size = getOptimalPreviewSize(CameraManager.getInstance().getSupportedSize(), w, h);
        Log.d(TAG, "setPreviewSizeVis: w * h = " + w + " * " + h);
        return CameraManager.getInstance().setPreviewSizeVis(size.getWidth(), size.getHeight());
    }

    /**
     * getOptimalPreviewSize:获取最接近预览分辨率
     */
    private CameraManager.Size getOptimalPreviewSize(List<CameraManager.Size> localList, int w, int h) {
        CameraManager.Size optimalSize = null;
        try {
            ArrayList<CameraManager.Size> localArrayList = new ArrayList<CameraManager.Size>();
            Iterator<CameraManager.Size> localIterator = localList.iterator();
            while (localIterator.hasNext()) {
                CameraManager.Size localSize = localIterator.next();
                if (localSize.getWidth() > localSize.getHeight()) {
                    localArrayList.add(localSize);
                }
                Log.d(TAG, "getOptimalPreviewSize: localSize = " + localSize.getWidth() + "*" + localSize.getHeight());
            }
            Collections.sort(localArrayList, new PreviewComparator(w, h));
            optimalSize = localArrayList.get(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return optimalSize;
    }

    class PreviewComparator implements Comparator<CameraManager.Size> {
        int w, h;
        public PreviewComparator(int w, int h) {
            this.w = w;
            this.h = h;
        }

        @Override
        public int compare(CameraManager.Size paramSize1, CameraManager.Size paramSize2) {
            return Math.abs(paramSize1.getWidth() * paramSize1.getHeight() - this.w * this.h)
                    - Math.abs(paramSize2.getWidth() * paramSize2.getHeight() - this.w * this.h);
        }
    }
}
