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

                // send notification
                NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                        .setSmallIcon(android.R.drawable.ic_dialog_info)
                        .setContentTitle("Geofence Triggered")
                        .setContentText("Geofence Triggered")
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);

                NotificationManagerCompat notifcationManager =
                        NotificationManagerCompat.from(context);
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                notifcationManager.notify(200, builder.build());



                Toast.makeText(context, "In reciever", Toast.LENGTH_SHORT).show();
                database = RoomDB.getInstance(context.getApplicationContext());
                if(intent.getLongExtra("selectedCollection", (long) 0.0)==0){
                    return;
                }
                Collection selectedCollection = database.mainDao().getCollectionById(intent.getLongExtra("selectedCollection", (long) 0.0));
                int test = intent.getIntExtra("actionIndex",20);
                Trigger trigger = selectedCollection.getSpecificRule(intent.getIntExtra("actionIndex",0)).getTrigger();
                selectedCollection.runAction(intent.getIntExtra("actionIndex",0),context);
                database.mainDao().updateCollection(selectedCollection);

            }
        }
    }
}
