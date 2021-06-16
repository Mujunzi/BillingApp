package cn.lenovo.cwnisface.face.bean;

import java.util.Map;

/**
* @Date：2018年2月5日 下午3:24:53  
* @author baohm1
*/

public interface FaceMsgCallback {
	public void UpdateFaceMsg(Map<Integer, FaceDetInfo> map);
}
