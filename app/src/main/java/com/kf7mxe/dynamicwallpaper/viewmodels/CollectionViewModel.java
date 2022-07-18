package com.kf7mxe.dynamicwallpaper.viewmodels;

import static android.content.Intent.ACTION_OPEN_DOCUMENT;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableArrayList;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.kf7mxe.dynamicwallpaper.database.CacheRoomDB;
import com.kf7mxe.dynamicwallpaper.database.RoomDB;
import com.kf7mxe.dynamicwallpaper.models.Collection;

import java.io.File;
import java.util.List;

public class CollectionViewModel extends AndroidViewModel {
    private RoomDB database;
    private CacheRoomDB cacheDatabase;
    private Context m_context;
    List<Collection> collections;
    List <Collection> cacheCollections;
    public CollectionViewModel(@NonNull Application application, Context context) {
        super(application);
        database = RoomDB.getInstance(application.getApplicationContext());
        cacheDatabase = CacheRoomDB.getInstance(application.getApplicationContext());
        m_context = context;
        queryAllFromDatabase();
    }


    public void queryAllFromDatabase(){
        collections = database.mainDao().getAll();
        cacheCollections = cacheDatabase.mainDao().getAll();
    }

    public Long saveCollection(Collection collection){
        cacheDatabase.mainDao().delete(collection);
        return database.mainDao().insert(collection);
    }

    public Long saveCollectionToCache(Collection collection){
        return cacheDatabase.mainDao().insert(collection);
    }

    public void deleteFromDatabase(Collection collection){
         database.mainDao().delete(collection);
    }

    public boolean isEmpty(){
        List<Collection> test = collections;
        if(this.collections==null){
            return true;
        }
        return this.collections.size()==0;
    }

    public List<Collection> getAllCollections(){
        if(RoomDB.getInstance(m_context).isOpen()){
            return collections;
        } else {
            RoomDB.getInstance(m_context);
        }
        return collections;
    }

    public List<Collection> getAllCacheCollections(){
        if(RoomDB.getInstance(m_context).isOpen()){
            return cacheCollections;
        } else {
            RoomDB.getInstance(m_context);
        }
        return cacheCollections;
    }


    public Collection getSpecificCollection(Long id){
        return database.mainDao().getCollectionById(id);
    }

    public Collection getSpecificCachCollection(Long id){
        return cacheDatabase.mainDao().getCollectionById(id);
    }


    public int deleteAllItemsInCache(){
        queryAllFromDatabase();
        for(int i=0;i<cacheCollections.size();i++){
            cacheCollections.get(i).getName();
            boolean inCollection = false;
            for(int j=0;j<collections.size();j++){
                if(cacheCollections.get(i).getName().equals(collections.get(j).getName())){
                    inCollection = true;
                }
            }
            if(!inCollection){
                if (cacheCollections.get(i).getPhotoNames().size() != 0 && cacheCollections.get(i).getName().length() != 0) {
                    deleteRecursive(new File(m_context.getExternalFilesDir(ACTION_OPEN_DOCUMENT).getAbsolutePath(), cacheCollections.get(i).getName()));
                }
            }
        }
        return cacheDatabase.mainDao().deleteAll();
    }

    public void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            for (File child : fileOrDirectory.listFiles()) {
                deleteRecursive(child);
            }
        }

        fileOrDirectory.delete();
    }
}
