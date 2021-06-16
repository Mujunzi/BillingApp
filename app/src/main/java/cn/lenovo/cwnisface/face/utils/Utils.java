package cn.lenovo.cwnisface.face.utils;

/**
 * Created by baohm1 on 2018/5/8.
 */

public class Utils {

    public static void msleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
