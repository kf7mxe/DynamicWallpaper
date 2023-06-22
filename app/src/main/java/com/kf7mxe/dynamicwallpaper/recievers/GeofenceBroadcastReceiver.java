package com.kf7mxe.dynamicwallpaper.recievers;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.kf7mxe.dynamicwallpaper.MainActivity;
import com.kf7mxe.dynamicwallpaper.database.RoomDB;
import com.kf7mxe.dynamicwallpaper.models.Collection;
import com.kf7mxe.dynamicwallpaper.models.Trigger;

public class GeofenceBroadcastReceiver extends BroadcastReceiver {
    private RoomDB database;

    @Override
    public void onReceive(Context context, Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (!geofencingEvent.hasError()) {
            int geofenceTransition = geofencingEvent.getGeofenceTransition();
            if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER || geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {

                Toast.makeText(context, "In reciever", Toast.LENGTH_SHORT).show();
                database = RoomDB.getInstance(context.getApplicationContext());
                if(intent.getLongExtra("selectedCollection", (long) 0.0)==0){
                    return;
                }
                Collection selectedCollection = database.mainDao().getCollectionById(intent.getLongExtra("selectedCollection", (long) 0.0));
//                int test = intent.getIntExtra("actionIndex",20);
//                Trigger trigger = selectedCollection.getSpecificRule(intent.getIntExtra("actionIndex",0)).getTrigger();

                if (selectedCollection == null) {
                    return;
                }

                selectedCollection.runAction(intent.getIntExtra("actionIndex",0),context);
                database.mainDao().updateCollection(selectedCollection);

            }
        } else {
Toast.makeText(context, "Error in geofence", Toast.LENGTH_SHORT).show();

        }
    }
}
