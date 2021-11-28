package com.kf7mxe.dynamicwallpaper.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableArrayList;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.kf7mxe.dynamicwallpaper.database.RoomDB;
import com.kf7mxe.dynamicwallpaper.models.Collection;

import java.util.List;

public class CollectionViewModel extends AndroidViewModel {
    private RoomDB database;
    LiveData<List<Collection> >collections;
    public CollectionViewModel(@NonNull Application application) {
        super(application);
        database = RoomDB.getInstance(application.getApplicationContext());
        queryAllFromDatabase();
    }

    public void queryAllFromDatabase(){
        collections = database.mainDao().getAll();
    }

    public Long saveCollection(Collection collection){
        return database.mainDao().insert(collection);
    }

    public List<Collection> getAllCollections(){
        return collections.getValue();
    }

    public Collection getSpecificCollection(Long id){
        return database.mainDao().getCollectionById(id);
    }

}
