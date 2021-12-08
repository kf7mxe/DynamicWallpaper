package com.kf7mxe.dynamicwallpaper.recievers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;


import com.kf7mxe.dynamicwallpaper.database.RoomDB;
import com.kf7mxe.dynamicwallpaper.models.Action;
import com.kf7mxe.dynamicwallpaper.models.Collection;
import com.kf7mxe.dynamicwallpaper.viewmodels.CollectionViewModel;

public class AlarmActionReciever extends BroadcastReceiver {
    private RoomDB database;

    @Override
    public void onReceive(Context context, Intent intent) {

        Toast.makeText(context, "In reciever", Toast.LENGTH_SHORT).show();
        database = RoomDB.getInstance(context.getApplicationContext());
        Long test = intent.getLongExtra("selectedCollection", (long) 0.0);
        if(intent.getLongExtra("selectedCollection", (long) 0.0)==0){
            return;
        }
        Collection selectedCollection = database.mainDao().getCollectionById(intent.getLongExtra("selectedCollection", (long) 0.0));
        selectedCollection.runAction(intent.getIntExtra("actionIndex",0),context);
        database.mainDao().updateCollection(selectedCollection);
    }
}
