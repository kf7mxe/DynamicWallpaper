package com.kf7mxe.dynamicwallpaper.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.kf7mxe.dynamicwallpaper.models.Collection;

@Database(entities = {Collection.class},version=1,exportSchema = false)
public abstract class CacheRoomDB extends RoomDatabase {
    private static CacheRoomDB database;

    private static String DATABASE_NAME = "collections-cache";

    public synchronized static CacheRoomDB getInstance(Context context){
        if(database ==null){
            database = Room.databaseBuilder(context.getApplicationContext(), CacheRoomDB.class,DATABASE_NAME)
                    .allowMainThreadQueries()
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return database;
    }

    public abstract MainDao mainDao();
}
