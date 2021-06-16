package com.lenovo.billing.model;


import com.lenovo.billing.protocol.BillingProcess;

import cn.lenovo.cwnisface.face.bean.FaceRecogMsgCallback;
import cn.lenovo.cwnisface.face.realtime.FrameProcess;

public class FaceDetection implements FaceRecogMsgCallback {

    private BillingProcess bp;

    public FaceDetection() {
        FrameProcess.registerFaceRecogCallBack(this);
    }

    public void register(BillingProcess bp) {
        this.bp = bp;
    }

    @Override
    public void FaceRecogListening(int faceID, boolean isFound, String url) {

        //
        // isFound && faceID > 0
        //  -- true: register
        //  -- false: stranger
        //

        if (!isFound || faceID < 0) {
            return;
        }

        bp.recognizeFaceCallback(faceID);
    }

    @Override
    public void FaceRecogListenError(int errCode) {

        if (bp != null) {
            bp.faceDetectedErrorCallBack(errCode);
        }
    }
}
