package cn.lenovo.cwnisface.face;

/**
 * Created by baohm1 on 2018/5/7.
 */

public class Constants {
    //人脸识别服务器的url
    public static String url_server = "http://10.100.207.229:50001/pad/recognize";
    //保存临时识别照片的url
    public static String url_img_server = "http://10.100.207.229:50001/pad/upload";
    
	public static String PAD_ID = "IN_001";
	
	//保存临时文件的地址
    public static String CROP_IMG_PATH = "/sdcard/faces";
	public static int MAX_NUMBER_CROP_IMG = 10000;
    public static int REPEAT_TIME_CHECK_CACHE = 24 * 3600 * 1000; //ms

    /**
     * 人脸识别策略，
     */
    public static int CW_POST_FACE_NUM = 2;            //一轮上传的图片数量
    public static int CW_POST_FACE_SKIP_TIME = 50;      //上传图片后skip时长，单位ms
    public static int CW_POST_FACE_POST_TIMEOUT = 1000;      //上传图片后skip时长，单位ms
    /**
     * 人脸尺寸和相似度阈值
     */
    public static int CW_FACE_Min_Size = 100;       // 人脸检测最小人脸
    public static float CW_FACE_SIMILAR = 0.84f;    //人脸相似度阈值，默认为0.84，范围[0.82, 0.92]
    public static float CW_FACE_HIGH_SIMILAR = 0.88f; //人脸相似度高阈值，默认为0.88，范围[0.82, 0.92]
    public static float CW_FACE_OWN_SIMILAR = 1.15f; //自研阈值
    public static String CW_FACE_IS_OWN = ""; //云丛 or 自研
    /**
     * Face param，人脸质量阈值
     */
    public static float CW_SKIN_SCORE = 0.35f;    //默认肤色阈值，范围[0.00,1.00]
    public static float CW_CLARITY = 0.60f;       //默认清晰度0.30f，范围[0.00,1.00]
    public static int CW_POSE_YAW = 15;           //人脸角度，左转为正，右转为负，范围[-90,90]，绝对值
    public static int CW_POSE_PITCH = 30;         //人脸角度，抬头为正，低头为负，范围[-90,90]，绝对值
    public static int CW_POSE_ROLL = 30;          //人脸角度，顺时针为正，逆时针为负，范围[-90,90]，绝对值

    /**
     * Camera Param,根据硬件调整
     */
    public static float PREVIEW_SCALE = 1.05f;   //preview视频显示的放大倍率
    public static int PREVIEW_W = 640;//1280;
    public static int PREVIEW_H = 480;//720;
    public static int Camera_Rotate = 0;         //默认旋转角度, 0/90/180/270
    public static boolean Camera_H_Mir = true; //默认是否水平镜像
    public static boolean Camera_V_Mir = false; //默认是否垂直镜像
    public static int default_begin_cam_index = 0;//相机匹配时的开始索引
    public static int default_max_cam_count = 3;//相机匹配时的最大数量
    public static int MAX_PREVIEW_SKIP_TIME_MS = 100;//预览初始忽略时间ms
    public static int FrameDetectPeriod = 40;
    public static boolean IsShowVis = true;       //显示可见光
}
