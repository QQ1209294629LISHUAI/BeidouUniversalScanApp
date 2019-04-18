package com.beidouspatial.universalscanapp.db.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.beidouspatial.universalscanapp.db.entity.Pic;

import java.util.List;


@Dao
public interface PicDao {

    @Query("SELECT * FROM pic")
    List<Pic> getAll();

    @Query("SELECT * FROM pic WHERE uid IN (:userIds)")
    List<Pic> loadAllByIds(int[] userIds);

    @Query("SELECT * FROM pic WHERE folder_name LIKE :first AND " + "create_time LIKE :last LIMIT 1")
    Pic findByName(String first, String last);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(Pic... users);

    @Delete
    void delete(Pic user);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void insertPics(List<Pic> pics);

    @Update
    public void updateUsers(Pic... pics);

    @Delete
    public void deleteUsers(Pic... pics);

}
