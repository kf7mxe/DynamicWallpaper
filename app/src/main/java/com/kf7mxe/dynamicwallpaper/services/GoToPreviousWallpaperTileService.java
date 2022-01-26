package com.kf7mxe.dynamicwallpaper.services;

import android.content.Context;
import android.content.SharedPreferences;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.widget.Toast;

import com.kf7mxe.dynamicwallpaper.database.RoomDB;
import com.kf7mxe.dynamicwallpaper.models.Collection;

public class GoToPreviousWallpaperTileService extends TileService {
    private RoomDB database;
    private SharedPreferences sharedPreferences;

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onTileAdded() {
        super.onTileAdded();
    }

    @Override
    public void onTileRemoved() {
        super.onTileRemoved();
    }

    @Override
    public void onStartListening() {
        super.onStartListening();
       Tile tile = getQsTile();
       tile.setState(Tile.STATE_ACTIVE);
    }

    @Override
    public void onStopListening() {
        super.onStopListening();
    }

    @Override
    public void onClick() {
        super.onClick();
        database = RoomDB.getInstance(getApplication().getApplicationContext());
        sharedPreferences = getApplication().getSharedPreferences("sharedPrefrences", Context.MODE_PRIVATE);
        String selectedCollectionString = sharedPreferences.getString("selectedCollection","none");
        if(selectedCollectionString.equals("none")){
            Toast.makeText(getApplicationContext(), "No Selected Collection", Toast.LENGTH_LONG).show();
            return;
        }
        Long collectionId = Long.parseLong(selectedCollectionString);
        Collection selectedCollection = database.mainDao().getCollectionById(collectionId);
        selectedCollection.goToPreviousWallpaper(getApplicationContext());
        database.mainDao().updateCollection(selectedCollection);
        Toast.makeText(getApplicationContext(), "Wallpaper Changed", Toast.LENGTH_SHORT).show();
    }
}
