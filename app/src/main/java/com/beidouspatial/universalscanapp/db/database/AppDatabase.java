package com.beidouspatial.universalscanapp.db.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.beidouspatial.universalscanapp.db.dao.PicDao;
import com.beidouspatial.universalscanapp.db.entity.Pic;


@Database(entities = {Pic.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract PicDao picDao();
}
