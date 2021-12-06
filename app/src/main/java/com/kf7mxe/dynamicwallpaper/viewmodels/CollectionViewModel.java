package com.kf7mxe.dynamicwallpaper.viewmodels;

import android.app.Application;
import android.content.Context;

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
    private Context m_context;
    List<Collection> collections;
    public CollectionViewModel(@NonNull Application application, Context context) {
        super(application);
        database = RoomDB.getInstance(application.getApplicationContext());
        m_context = context;
        queryAllFromDatabase();
    }


    public void queryAllFromDatabase(){
        collections = database.mainDao().getAll();
    }

    public Long saveCollection(Collection collection){
        return database.mainDao().insert(collection);
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

    public Collection getSpecificCollection(Long id){
        return database.mainDao().getCollectionById(id);
    }

}
