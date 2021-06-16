package cn.lenovo.cwnisface.face.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/** 
* @Date：2018年2月5日 下午3:55:57  
* @author baohm1
*/

public class MyTime {
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd_HH.mm.ss");
	private static SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy.MM.dd_HH.mm.ss_SSS");
	
	public static String currentTimeSec() {
		return String.valueOf(System.currentTimeMillis()).substring(0, 10);
	}
	
	public static String currentTime() {
		Date date = new Date();
		return sdf.format(date);
	}
	
	public static String currentTimeMillis() {
		Date date = new Date();
		return sdf2.format(date);
	}
	
	public static String timestamp2localTime(long timestamp) {
		Date date = new Date(timestamp);
		return sdf.format(date);
	}
	
	/**
	 * 判断请求头中的时间戳，与当前时间计算，大于5分钟为无效请求
	 * @param timeStamp
	 * @return
	 */
	public static Boolean isOutOfTime(String timeStamp) {
		String curSeconds = String.valueOf(System.currentTimeMillis()).substring(0, 10);
		Integer diffTime = Integer.parseInt(curSeconds) - Integer.parseInt(timeStamp);
		System.out.println(diffTime);
		if (diffTime > 300 || diffTime < -300) {
			return true;
		}
		return false;
	}
}
