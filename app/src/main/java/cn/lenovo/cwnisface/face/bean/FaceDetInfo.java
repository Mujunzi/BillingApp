package cn.lenovo.cwnisface.face.bean;

import android.graphics.RectF;

public class FaceDetInfo {
	public int faceID = -1;
	public int trackID = -1;
	public int missNum = 0;
	public boolean isUsed = false;
	public boolean isValid = false;

	public RectF rectf;
}
