package com.kf7mxe.dynamicwallpaper.recievers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.kf7mxe.dynamicwallpaper.database.RoomDB;
import com.kf7mxe.dynamicwallpaper.models.Collection;

public class BootCompleteIntentReciever extends BroadcastReceiver {
    private RoomDB database;
    private SharedPreferences sharedPreferences;
    @Override
    public void onReceive(Context context, Intent intent) {

        if("android.intent.action.BOOT_COMPLETED".equals(intent.getAction()) || "android.intent.action.QUICKBOOT_POWERON".equals(intent.getAction())) {
            sharedPreferences = context.getApplicationContext().getSharedPreferences("sharedPrefrences", Context.MODE_PRIVATE);
            String selectedCollectionString = sharedPreferences.getString("selectedCollection","none");
            if(selectedCollectionString.equals("none")){
                Toast.makeText(context, "No Selected Collection", Toast.LENGTH_LONG).show();
                return;
            }
            database = RoomDB.getInstance(context.getApplicationContext());
            Long collectionId = Long.parseLong(selectedCollectionString);
            Collection selectedCollection = database.mainDao().getCollectionById(collectionId);
            selectedCollection.startTriggers(context);
        }
    }
}
