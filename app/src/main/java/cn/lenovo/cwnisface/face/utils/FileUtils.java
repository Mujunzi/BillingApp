package cn.lenovo.cwnisface.face.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;

import cn.lenovo.cwnisface.face.Constants;

/**
 * Created by baohm1 on 2018/3/12.
 */

public class FileUtils {
    private static final String TAG = "FileUtils";
    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd_HH:mm:ss");

    /**
     * 确认指定目录是否存在，不存在则创建(只识别一级目录)
     * @param filePath
     */
    public static void checkFilePath(String filePath) {
        File file = new File(filePath);
        if (!file.exists() && !file.isDirectory()) {
            Log.d("FileUtils", filePath + " is not exist");
            file.mkdir();
        }
    }

    /**
     * 删除单个文件
     *
     * @param fileName
     *            要删除的文件的文件名
     * @return 单个文件删除成功返回true，否则返回false
     */
    public static boolean deleteFile(String fileName) {
        File file = new File(fileName);
        // 如果文件路径所对应的文件存在，并且是一个文件，则直接删除
        if (file.exists() && file.isFile()) {
            if (file.delete()) {
                System.out.println("删除单个文件" + fileName + "成功！");
                return true;
            } else {
                System.out.println("删除单个文件" + fileName + "失败！");
                return false;
            }
        } else {
            System.out.println("删除单个文件失败：" + fileName + "不存在！");
            return false;
        }
    }

    //获取文件夹内文件数量
    public static int getFileNumber(final String pPath) {
        File dir = new File(pPath);
        if (!dir.exists() || !dir.isDirectory())
            return 0;
        return dir.listFiles().length;
    }

    //重命名文件夹或文件
    public static void renameFile(final String oldPath, final String newPath) {
        File oleFile = new File(oldPath);
        File newFile = new File(newPath);
        //执行重命名
        oleFile.renameTo(newFile);
    }

    //删除文件夹和文件夹里面的文件
    public static void deleteDir(final String pPath) {
        File dir = new File(pPath);
        deleteDirWithFile(dir);
    }

    private static void deleteDirWithFile(File dir) {
        if (dir == null || !dir.exists() || !dir.isDirectory())
            return;
        for (File file : dir.listFiles()) {
            if (file.isFile())
                file.delete(); // 删除所有文件
            else if (file.isDirectory())
                deleteDirWithFile(file); // 递规的方式删除文件夹
        }
        dir.delete();// 删除目录本身
    }

    public static void saveJpg(byte[] yuv_data, int frameW, int frameH) {
        if (Constants.CROP_IMG_PATH.isEmpty())
            return;
        checkFilePath(Constants.CROP_IMG_PATH);

        final YuvImage image = new YuvImage(yuv_data, ImageFormat.NV21, frameW, frameH, null);
        ByteArrayOutputStream os = new ByteArrayOutputStream(yuv_data.length*2);
        if (!image.compressToJpeg(new Rect(0, 0, frameW, frameH), 100, os)) {
            return;
        }
        try {
            String file = Constants.CROP_IMG_PATH + File.separator + MyTime.currentTimeMillis() + ".jpg";
            FileOutputStream fos = new FileOutputStream(new File(file));
            try {
                os.writeTo(fos);
                os.flush();
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static String saveCropJpg(byte[] yuv_data, int frameW, int frameH, int cropX, int cropY, int cropW, int cropH, int angle, int mirror) {
        System.out.println("================================saveCropJpg() IN===========");
        if (Constants.CROP_IMG_PATH.isEmpty())
            return null;
        checkFilePath(Constants.CROP_IMG_PATH);

        Rect rect = setCropEdge(frameW, frameH, cropX, cropY, cropW, cropH, angle, mirror, 100);
        final YuvImage yuvImage = new YuvImage(yuv_data, ImageFormat.NV21, frameW, frameH, null);
        ByteArrayOutputStream ostream = new ByteArrayOutputStream(yuv_data.length*2);
        yuvImage.compressToJpeg(rect, 100, ostream);
        Bitmap bitmap = BitmapFactory.decodeByteArray(ostream.toByteArray(), 0, ostream.size());
        try {
            ostream.flush();
            ostream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Matrix matrix = new Matrix();
        matrix.setRotate(angle*90);
        matrix.postScale((mirror==0)?1:-1, 1);
        Bitmap bmp = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

        String file = Constants.CROP_IMG_PATH + File.separator + "crop_" + MyTime.currentTimeMillis() + ".jpg";
        try {
            FileOutputStream fos = new FileOutputStream(new File(file));

            bmp.compress(Bitmap.CompressFormat.JPEG, 80, fos);
            fos.flush();
            fos.close();
            System.out.println("================================saveCropJpg() out===========");
            return file;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static Rect setCropEdge(int frameW, int frameH, int cropX, int cropY, int cropW, int cropH,
                                    int angle, int mirror, int edgeValue) {
        Rect rect = new Rect();
        int switchXY = (angle + mirror*2)%4;
        switch(switchXY) {
            case 0:
                rect = new Rect(cropX, cropY, cropX + cropW, cropY + cropH);
                break;
            case 3:
                rect = new Rect(cropY, cropX, cropY + cropH, cropX + cropW);
                break;
            case 2:
                rect = new Rect(cropX, frameH - (cropY + cropH), cropX + cropW, frameH - cropY);
                break;
            case 1:
                rect = new Rect(frameW - (cropY + cropH), frameH - (cropX + cropW), frameW - cropY, frameH - cropX);
                break;
        }
//        android.util.Log.d(TAG, "saveCropJpg: [angle,mirror] = " + angle + ", " + mirror);
//        android.util.Log.d(TAG, "saveCropJpg: [frameW,frameH] = " + frameW + ", " + frameH);
//        android.util.Log.d(TAG, "saveCropJpg: [x,y,w,h] = " + cropX + ", " + cropY + ", " + cropW + ", " + cropH);
//        android.util.Log.d(TAG, "saveCropJpg: rect[x,y,w,h] = " + rect.left + ", " + rect.top + ", " + rect.right + ", " + rect.bottom);
        rect.left = rect.left - edgeValue;
        rect.top = rect.top - edgeValue;
        rect.right = rect.right + edgeValue;
        rect.bottom = rect.bottom + edgeValue;

        rect.left = rect.left < 0 ? 0:rect.left;
        rect.top = rect.top < 0 ? 0:rect.top;
        rect.right = (frameW < rect.right) ? frameW:rect.right;
        rect.bottom = (frameH < rect.bottom) ? frameH:rect.bottom;

        return rect;
    }
}
