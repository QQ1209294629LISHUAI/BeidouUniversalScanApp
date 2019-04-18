package com.beidouspatial.universalscanapp;

import android.graphics.Bitmap;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class MyBitmapUtils {

    /**
     * 将bitmap保存在本地
     *
     * @param bmp
     * @param path 文件全路径
     */
    public static void saveBitmapToLocal(Bitmap bmp, String path) {
        File file = new File(path);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
