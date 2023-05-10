package com.kf7mxe.dynamicwallpaper.recievers;

import android.content.BroadcastReceiver;

import com.kf7mxe.dynamicwallpaper.database.RoomDB;
import com.kf7mxe.dynamicwallpaper.models.Collection;
import com.kf7mxe.dynamicwallpaper.models.TriggerByWeather;

public class AlarmUpdateWeatherActionReciever extends BroadcastReceiver {
    private RoomDB database;

    @Override
    public void onReceive(android.content.Context context, android.content.Intent intent) {
        database = RoomDB.getInstance(context.getApplicationContext());
        if(intent.getLongExtra("selectedCollection", (long) 0.0)==0){
            return;
        }
        Collection selectedCollection = database.mainDao().getCollectionById(intent.getLongExtra("selectedCollection", (long) 0.0));



    }
}
