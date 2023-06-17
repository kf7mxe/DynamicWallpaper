package com.kf7mxe.dynamicwallpaper.recievers;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.widget.Toast;


import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationChannelCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.kf7mxe.dynamicwallpaper.database.RoomDB;
import com.kf7mxe.dynamicwallpaper.models.Action;
import com.kf7mxe.dynamicwallpaper.models.Collection;
import com.kf7mxe.dynamicwallpaper.models.Trigger;
import com.kf7mxe.dynamicwallpaper.models.TriggerByWeather;
import com.kf7mxe.dynamicwallpaper.viewmodels.CollectionViewModel;

public class AlarmActionReciever extends BroadcastReceiver {
    private RoomDB database;

    @Override
    public void onReceive(Context context, Intent intent) {

        Toast.makeText(context, "In reciever", Toast.LENGTH_SHORT).show();




        // Build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "channel_id")
                .setSmallIcon(android.R.drawable.ic_menu_help)
                .setContentTitle("My Notification")
                .setContentText("This is a sample notification")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        // Show the notification
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
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
        notificationManager.notify(0, builder.build());

        database = RoomDB.getInstance(context.getApplicationContext());
        if(intent.getLongExtra("selectedCollection", (long) 0.0)==0){
            return;
        }
        Collection selectedCollection = database.mainDao().getCollectionById(intent.getLongExtra("selectedCollection", (long) 0.0));
        int test = intent.getIntExtra("actionIndex",20);
        Trigger trigger = selectedCollection.getSpecificRule(intent.getIntExtra("actionIndex",0)).getTrigger();
        if (trigger.getTriggerType()=="triggerByWeather"){
            TriggerByWeather triggerByWeather = (TriggerByWeather) trigger;
            triggerByWeather.removeWeatherTriggersToUpdate(context,intent.getIntExtra("actionIndex",0),(int)intent.getLongExtra("selectedCollection", (long) 0.0));
            triggerByWeather.setWeatherTriggersToUpdate(context,intent.getIntExtra("actionIndex",0),(int)intent.getLongExtra("selectedCollection", (long) 0.0));

            database.mainDao().updateCollection(selectedCollection); // might not work if the triger is not updated by referenced
        } else {
            selectedCollection.runAction(intent.getIntExtra("actionIndex",0),context);
            database.mainDao().updateCollection(selectedCollection);
        }

    }
}
