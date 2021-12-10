package com.kf7mxe.dynamicwallpaper.database;

import static androidx.room.OnConflictStrategy.REPLACE;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.kf7mxe.dynamicwallpaper.models.Collection;

import java.util.ArrayList;
import java.util.List;

@Dao
public interface MainDao {
    @Insert(onConflict = REPLACE)
    Long insert(Collection collection);

    @Delete
    void delete(Collection collection);
    //Delete all querys
    @Delete
    void reset(List<Collection> collections);

    @Update
    void updateCollection(Collection collection);

    @Query("SELECT * FROM collection WHERE id=:id")
    Collection getCollectionById(long id);

    @Query("SELECT * FROM collection")
    List<Collection> getAll();
}
