package cn.lenovo.cwnisface.face.bean;

/**
* @Date：2018年2月2日 下午2:28:32  
* @author baohm1
*/

public class MsgBean {
	public static final String TYPE_RECOG = "recognized";
	public static final String TYPE_UNRECOG = "unrecognized";
	public static final String TYPE_GONE = "gone";
	public static final String TYPE_NONE = "none";
	public static final String TYPE_ERROR = "error";
	
	public static final String keyRect = "rect";
	public static final String keyFaceID = "faceID";
	public static final String keyFaceName = "uID";
	public static final String keyGender = "gender";
	
	private String type = TYPE_NONE;
	private FaceRect rect;
	private int faceID;
	private String uID;
	private int gender = 0;//UserBean.GENDER_NONE;
	private long timestamp;
	private String localTime;
	
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}

	public FaceRect getRect() {
		return rect;
	}

	public void setRect(FaceRect rect) {
		this.rect = rect;
	}

	public int getFaceID() {
		return faceID;
	}

	public void setFaceID(int faceID) {
		this.faceID = faceID;
	}

	public String getuID() {
		return uID;
	}

	public void setuID(String uID) {
		this.uID = uID;
	}

	public int getGender() {
		return gender;
	}

	public void setGender(int gender) {
		this.gender = gender;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public String getLocalTime() {
		return localTime;
	}

	public void setLocalTime(String localTime) {
		this.localTime = localTime;
	}
}
