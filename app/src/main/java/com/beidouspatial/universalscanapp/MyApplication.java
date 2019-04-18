package com.beidouspatial.universalscanapp;

import android.app.Application;
import androidx.room.Room;
import android.graphics.Bitmap;

import com.beidouspatial.universalscanapp.db.database.AppDatabase;

import net.doo.snap.ScanbotSDKInitializer;


public class MyApplication extends Application {
    public static Bitmap cameraPhoto;
    public static Bitmap cutCameraPhoto;
    public static AppDatabase db;

    @Override
    public void onCreate() {
        super.onCreate();
        //初始化Scanbot SDK
        new ScanbotSDKInitializer().initialize(this);
        db = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "database-name").build();
    }
}
