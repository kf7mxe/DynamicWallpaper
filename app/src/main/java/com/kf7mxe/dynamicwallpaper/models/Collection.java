package com.kf7mxe.dynamicwallpaper.models;

import static android.content.Intent.ACTION_OPEN_DOCUMENT;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Debug;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;

import androidx.core.app.ActivityCompat;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.kf7mxe.dynamicwallpaper.recievers.AlarmActionReciever;
import com.kf7mxe.dynamicwallpaper.recievers.AlarmUpdateWeatherActionReciever;
import com.kf7mxe.dynamicwallpaper.recievers.GeofenceBroadcastReceiver;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;

@Entity
public class Collection implements Serializable {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private long id;
    @ColumnInfo(name = "name")
    private String name;
    @ColumnInfo(name = "sub_collections")
    @TypeConverters(com.kf7mxe.dynamicwallpaper.utilis.SubcollectionTypeConverter.class)
    private ArrayList<SubCollection> subCollectionArray;
    @ColumnInfo(name = "sub_collections_index")
    private int selectedSubCollectionArrayIndex = -1;
    @ColumnInfo(name = "sub_collection_selected_image_index")
    private int subCollectionSelectedImageIndex = -1;
    @ColumnInfo(name = "rules")
    @TypeConverters(com.kf7mxe.dynamicwallpaper.utilis.RulesTypeConverter.class)
    private ArrayList<Rule> rules;
    @ColumnInfo(name = "selected_image_index")
    private int selectedImageIndex;
    @ColumnInfo(name = "photo_names")
    @TypeConverters(com.kf7mxe.dynamicwallpaper.utilis.PhotoNamesTypeConverter.class)
    private ArrayList<String> photoNames;


    public Collection() {
        this.name = "";
        this.subCollectionArray = new ArrayList<>();
        this.rules = new ArrayList<>();
        this.photoNames = new ArrayList<>();
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addRule(Rule newRule) {
        this.rules.add(newRule);
    }

    public void addSubCollection(SubCollection newSubCollection) {
        this.subCollectionArray.add(newSubCollection);
    }

    public void addImage(String fileName) {
        photoNames.add(fileName);
    }

    public long getId() {
        return id;
    }

    public int getIdAsInt() {
        Long temp = getId();
        return Integer.parseInt(temp.toString());
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public ArrayList<String> getPhotoNames() {
        return photoNames;
    }

    public void setPhotoNames(ArrayList<String> photoNames) {
        this.photoNames = photoNames;
    }

    public ArrayList<SubCollection> getSubCollectionArray() {
        return subCollectionArray;
    }

    public ArrayList<Rule> getRules() {
        return rules;
    }

    public Rule getSpecificRule(int index) {
        return rules.get(index);
    }

    public int getSelectedImageIndex() {
        return selectedImageIndex;
    }

    public int getSelectedSubCollectionArrayIndex() {
        return selectedSubCollectionArrayIndex;
    }

    public void setRules(ArrayList<Rule> rules) {
        this.rules = rules;
    }

    public void setSelectedImageIndex(int selectedImageIndex) {
        this.selectedImageIndex = selectedImageIndex;
    }

    public void setSelectedSubCollectionArrayIndex(int selectedSubCollectionArrayIndex) {
        this.selectedSubCollectionArrayIndex = selectedSubCollectionArrayIndex;
    }

    public void setSubCollectionArray(ArrayList<SubCollection> subCollectionArray) {
        this.subCollectionArray = subCollectionArray;
    }

    public void setSubCollectionSelectedImageIndex(int subCollectionSelectedImageIndex) {
        this.subCollectionSelectedImageIndex = subCollectionSelectedImageIndex;
    }

    public int getSubCollectionSelectedImageIndex() {
        return subCollectionSelectedImageIndex;
    }

    public void removeTriggersBroadcastRecievers(Context context) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        for (int i = 0; i < this.getRules().size(); i++) {

            if (this.getSpecificRule(i).getTrigger().getTriggerType() == "triggerByWeather") {
                TriggerByWeather triggerByWeather = (TriggerByWeather) this.getSpecificRule(i).getTrigger();
                triggerByWeather.removeWeatherTriggersToUpdate(context, i,getIdAsInt());
            }

            Intent intent = new Intent(context.getApplicationContext(), AlarmActionReciever.class);
            PendingIntent pi = null;
            intent.putExtra("selectedCollection", getId());
            intent.putExtra("actionIndex", i);
            pi = PendingIntent.getBroadcast(context, getIdAsInt() + 1001 + i, intent
                    , PendingIntent.FLAG_MUTABLE);

            if (pi != null) {
                am.cancel(pi);
            }
        }
    }

    public void startTriggers(Context context) {
        for (int i = 0; i < this.getRules().size(); i++) {
            Trigger trigger = this.getRules().get(i).getTrigger();
            switch (trigger.getTriggerType()) {
                case "triggerByTimeInterval":
                    if (trigger.isExact()) {
                        createExactAlarmForByDateTime(context, trigger, i);
                    }
                    createAlarmForByDateTime(context, trigger, i);
                    break;
                case "triggerBySeason":
                    createAlarmForSeasons(context, trigger, i);
                    break;
                case "triggerByDate":
                    createAlarmForDateTrigger(context, trigger, i);
                    break;
                case "triggerByWeather":
                    createAlarmForWeatherTrigger(context, trigger, i);
                    break;
                case "triggerByLocation":
                    createTriggersForLocation(context, trigger, i);
                default:
                    break;
            }


        }
    }


    public void runAction(int actionIndex, Context context) {
        this.rules.get(actionIndex).getAction();
        switch (this.rules.get(actionIndex).getAction().getType()) {
            case "selectActionNextInCollection":
                goToNextWallpaper(context);
                break;
            case "selectActionSwitchToDiffSubColRadio":
                String subCollectionName = this.rules.get(actionIndex).getAction().changeToSubCollection.replace("Selected Subcollection To change to: ", "");
                goToSpecificSubCollection(context, subCollectionName);
                break;
            case "selectActionRandomInCollSubRadio":
                goToRandWallpaper(context);
                //type="Go to Random Wallpaper in Collection or subcollection";
                break;
            case "selectActionSpecificWallpaperRadio":
                goToSpecificWallpaper(context, this.rules.get(actionIndex).getAction().changeToSecificImage);
                // type="Go to Specific Wallpaper \n Selected Wallpaper:"+changeToSecificImage;
                break;
        }
    }

    public void goToNextWallpaper(Context context) {
        File file;

        ArrayList<String> images;
        int nextIndex;
        if (this.selectedSubCollectionArrayIndex != -1) {
            images = subCollectionArray.get(this.selectedSubCollectionArrayIndex).getFileNames();
            nextIndex = this.subCollectionSelectedImageIndex;

        } else {
            images = this.getPhotoNames();
            nextIndex = this.getSelectedImageIndex();
        }
        if (nextIndex < images.size() - 1) {
            file = new File(context.getExternalFilesDir(ACTION_OPEN_DOCUMENT).getAbsolutePath() + "/" + this.getName() + "/" + images.get(nextIndex));
            if (this.selectedSubCollectionArrayIndex != -1) {
                this.subCollectionSelectedImageIndex = this.selectedSubCollectionArrayIndex + 1;
            } else {
                int temp = this.getSelectedImageIndex() + 1;
                this.setSelectedImageIndex(temp);
            }
        } else {
            file = new File(context.getExternalFilesDir(ACTION_OPEN_DOCUMENT).getAbsolutePath() + "/" + this.getName() + "/" + images.get(0));
            if (this.selectedSubCollectionArrayIndex != -1) {
                this.selectedSubCollectionArrayIndex = 0;
            } else {
                this.setSelectedImageIndex(0);
            }
        }
        setWallpaper(context, file);
    }

    public void goToPreviousWallpaper(Context context) {
        File file;

        ArrayList<String> images;
        int nextIndex;
        if (this.selectedSubCollectionArrayIndex != -1) {
            images = subCollectionArray.get(this.selectedSubCollectionArrayIndex).getFileNames();
            nextIndex = this.subCollectionSelectedImageIndex;

        } else {
            images = this.getPhotoNames();
            nextIndex = this.getSelectedImageIndex();
        }
        if (nextIndex < images.size() - 1 && nextIndex > -1) {
            file = new File(context.getExternalFilesDir(ACTION_OPEN_DOCUMENT).getAbsolutePath() + "/" + this.getName() + "/" + images.get(nextIndex));
            if (this.selectedSubCollectionArrayIndex != -1) {
                this.subCollectionSelectedImageIndex = this.selectedSubCollectionArrayIndex - 1;
            } else {
                int temp = this.getSelectedImageIndex() - 1;
                this.setSelectedImageIndex(temp);
            }
        } else {
            file = new File(context.getExternalFilesDir(ACTION_OPEN_DOCUMENT).getAbsolutePath() + "/" + this.getName() + "/" + images.get(0));
            if (this.selectedSubCollectionArrayIndex != -1) {
                this.selectedSubCollectionArrayIndex = 0;
            } else {
                this.setSelectedImageIndex(0);
            }
        }
        setWallpaper(context, file);
    }

    public void goToRandWallpaper(Context context) {
        File file;
        ArrayList<String> photos;
        if (selectedSubCollectionArrayIndex != -1) {
            photos = this.subCollectionArray.get(selectedSubCollectionArrayIndex).getFileNames();
        } else {
            photos = this.getPhotoNames();
        }
        Random rand = new Random();
        int index = rand.nextInt(this.getPhotoNames().size());

        file = new File(context.getExternalFilesDir(ACTION_OPEN_DOCUMENT).getAbsolutePath() + "/" + this.getName() + "/" + photos.get(index));
        int temp = this.getSelectedImageIndex() + 1;
        this.setSelectedImageIndex(temp);
        setWallpaper(context, file);
    }

    public void goToSpecificWallpaper(Context context, String specificWallpaperUnEdited) {
        String specificWallpaper = specificWallpaperUnEdited.replace("Selected Specific Wallpaper:", "");
        File file;
        file = new File(context.getExternalFilesDir(ACTION_OPEN_DOCUMENT).getAbsolutePath() + "/" + this.getName() + "/" + specificWallpaper);
        int temp = this.getSelectedImageIndex() + 1;
        this.setSelectedImageIndex(temp);
        setWallpaper(context, file);
    }

    public void goToSpecificSubCollection(Context context, String subCollection) {
        int subcollectionIndex = -1;
        for (int i = 0; i < this.getSubCollectionArray().size(); i++) {
            if (this.getSubCollectionArray().get(i).getName().equals(subCollection)) {
                subcollectionIndex = i;
            }
        }
        File file;
        this.selectedSubCollectionArrayIndex = subcollectionIndex;
        ArrayList<String> images;
        int nextIndex;
        if (this.selectedSubCollectionArrayIndex != -1) {
            images = subCollectionArray.get(this.selectedSubCollectionArrayIndex).getFileNames();
            if (this.subCollectionSelectedImageIndex != -1) {
                nextIndex = this.subCollectionSelectedImageIndex;
                this.subCollectionSelectedImageIndex = this.subCollectionSelectedImageIndex + 1;
            } else {
                nextIndex = 0;
                this.subCollectionSelectedImageIndex = 0;
            }
        } else {
            images = this.getPhotoNames();
            nextIndex = this.getSelectedImageIndex();
        }
        if (nextIndex < images.size() - 1) {
            file = new File(context.getExternalFilesDir(ACTION_OPEN_DOCUMENT).getAbsolutePath() + "/" + this.getName() + "/" + images.get(nextIndex));
            int temp = this.getSelectedImageIndex() + 1;
            this.setSelectedImageIndex(temp);
        } else {
            file = new File(context.getExternalFilesDir(ACTION_OPEN_DOCUMENT).getAbsolutePath() + "/" + this.getName() + "/" + images.get(0));
            this.setSelectedImageIndex(0);
        }
        setWallpaper(context, file);
    }

    public void setWallpaper(Context context, File file) {
        WallpaperManager wallpaperManager = WallpaperManager.getInstance(context);
        //File file = new File(resultUri.getPath());
        //Bitmap testBitmap = BitmapFactory.decodeFile( resultUri.getPath());
        try {
            InputStream inputStream = new FileInputStream(file);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            if (bitmap != null) {
                wallpaperManager.setBitmap(bitmap);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void createAlarmForByDateTime(Context context, Trigger trigger, int triggerIndex) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context.getApplicationContext(), AlarmActionReciever.class);
        PendingIntent pi = null;
        intent = new Intent(context.getApplicationContext(), AlarmActionReciever.class);
        intent.putExtra("selectedCollection", id);
        intent.putExtra("actionIndex", triggerIndex);
        pi = PendingIntent.getBroadcast(context, getIdAsInt() + 1001 + triggerIndex, intent
                , PendingIntent.FLAG_MUTABLE);
        Calendar startTime = Calendar.getInstance();
        startTime.set(Calendar.HOUR_OF_DAY, trigger.getHourToStartTrigger());
        startTime.set(Calendar.MINUTE, trigger.getMinuteToStartTrigger());
        if (trigger.getRepeatIntervalType().equals("Week")) {
//            trigger.getRepeatDayOfWeek().contains("monday");
//
//            startTime.set(Calendar.DAY_OF_WEEK,Calendar.MONDAY);
//            startTime.set(Calendar.DAY_OF_WEEK,Calendar.TUESDAY);
//            startTime.set(Calendar.DAY_OF_WEEK,Calendar.WEDNESDAY);
//            startTime.set(Calendar.DAY_OF_WEEK,Calendar.THURSDAY);
//            startTime.set(Calendar.DAY_OF_WEEK,Calendar.FRIDAY);
//            startTime.set(Calendar.DAY_OF_WEEK,Calendar.SATURDAY);
//            startTime.set(Calendar.DAY_OF_WEEK,Calendar.SUNDAY);

        }

        long interval = trigger.getRepeateIntervalAmount() * trigger.getIntervalTypeAsLong();

        am.setInexactRepeating(AlarmManager.RTC_WAKEUP, startTime.getTimeInMillis(),
                interval, pi);
        Toast.makeText(context, "in Set alarm", Toast.LENGTH_SHORT).show();
    }

    public void createExactAlarmForByDateTime(Context context, Trigger trigger, int triggerIndex) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context.getApplicationContext(), AlarmActionReciever.class);
        PendingIntent pi = null;
        intent = new Intent(context.getApplicationContext(), AlarmActionReciever.class);
        intent.putExtra("selectedCollection", id);
        intent.putExtra("actionIndex", triggerIndex);
        pi = PendingIntent.getBroadcast(context, getIdAsInt() + 1001 + triggerIndex, intent
                , PendingIntent.FLAG_MUTABLE);
        Calendar startTime = Calendar.getInstance();
        startTime.set(Calendar.HOUR_OF_DAY, trigger.getHourToStartTrigger());
        startTime.set(Calendar.MINUTE, trigger.getMinuteToStartTrigger());
        if (trigger.getRepeatIntervalType().equals("Week")) {
//            trigger.getRepeatDayOfWeek().contains("monday");
//            startTime.set(Calendar.DAY_OF_WEEK,Calendar.MONDAY);
//            startTime.set(Calendar.DAY_OF_WEEK,Calendar.TUESDAY);
//            startTime.set(Calendar.DAY_OF_WEEK,Calendar.WEDNESDAY);
//            startTime.set(Calendar.DAY_OF_WEEK,Calendar.THURSDAY);
//            startTime.set(Calendar.DAY_OF_WEEK,Calendar.FRIDAY);
//            startTime.set(Calendar.DAY_OF_WEEK,Calendar.SATURDAY);
//            startTime.set(Calendar.DAY_OF_WEEK,Calendar.SUNDAY);

        }

        long interval = trigger.getRepeateIntervalAmount() * trigger.getIntervalTypeAsLong();

        am.setRepeating(AlarmManager.RTC_WAKEUP, startTime.getTimeInMillis(),
                interval, pi);
    }

    public void createAlarmForSeasons(Context context, Trigger trigger, int triggerIndex) {
        for (int i = 0; i < trigger.getSeasonsSize(); i++) {
            AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(context.getApplicationContext(), AlarmActionReciever.class);
            PendingIntent pi = null;
            intent = new Intent(context.getApplicationContext(), AlarmActionReciever.class);
            intent.putExtra("selectedCollection", id);
            intent.putExtra("actionIndex", triggerIndex);
            pi = PendingIntent.getBroadcast(context, getIdAsInt() + 1001 + triggerIndex, intent
                    , PendingIntent.FLAG_MUTABLE);
            Calendar startTime = Calendar.getInstance();
            Calendar endTime = Calendar.getInstance();
            startTime.set(Calendar.MONTH, trigger.getSeasons().get(i).getCalMonth());
            startTime.set(Calendar.DAY_OF_MONTH, trigger.getSeasons().get(i).getDay());
            endTime.set(Calendar.MONTH, trigger.getSeasons().get(i).getEndMCalMonthInt());
            endTime.set(Calendar.DAY_OF_MONTH, trigger.getSeasons().get(i).getEndDayInt());
            long interval = endTime.getTimeInMillis() - startTime.getTimeInMillis();
            am.setInexactRepeating(AlarmManager.RTC_WAKEUP, startTime.getTimeInMillis(),
                    interval, pi);
        }
    }

    public void createAlarmForDateTrigger(Context context, Trigger trigger, int triggerIndex) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context.getApplicationContext(), AlarmActionReciever.class);
        PendingIntent pi = null;
        intent = new Intent(context.getApplicationContext(), AlarmActionReciever.class);
        intent.putExtra("selectedCollection", id);
        intent.putExtra("actionIndex", triggerIndex);
        pi = PendingIntent.getBroadcast(context, getIdAsInt() + 1001 + triggerIndex, intent
                , PendingIntent.FLAG_MUTABLE);
        Calendar startTime = Calendar.getInstance();
        Calendar endTime = Calendar.getInstance();
        endTime.set(Calendar.MONTH, trigger.getM_Month());
        endTime.set(Calendar.DAY_OF_MONTH, trigger.getM_date());
        long interval = endTime.getTimeInMillis() - startTime.getTimeInMillis();
        am.setInexactRepeating(AlarmManager.RTC_WAKEUP, startTime.getTimeInMillis(),
                interval, pi);
    }

    public void createExactAlarmForDateTrigger(Context context, Trigger trigger, int triggerIndex) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context.getApplicationContext(), AlarmActionReciever.class);
        PendingIntent pi = null;
        intent = new Intent(context.getApplicationContext(), AlarmActionReciever.class);
        intent.putExtra("selectedCollection", id);
        intent.putExtra("actionIndex", triggerIndex);
        pi = PendingIntent.getBroadcast(context, getIdAsInt() + 1001 + triggerIndex, intent
                , PendingIntent.FLAG_MUTABLE);
        Calendar startTime = Calendar.getInstance();
        Calendar endTime = Calendar.getInstance();
        endTime.set(Calendar.MONTH, trigger.getM_Month());
        endTime.set(Calendar.DAY_OF_MONTH, trigger.getM_date());
        long interval = endTime.getTimeInMillis() - startTime.getTimeInMillis();
        am.setRepeating(AlarmManager.RTC_WAKEUP, startTime.getTimeInMillis(),
                interval, pi);
    }

    public void createAlarmForWeatherTrigger(Context context, Trigger trigger, int triggerIndex) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context.getApplicationContext(), AlarmUpdateWeatherActionReciever.class);
        PendingIntent pi = null;
        intent.putExtra("selectedCollection", id);
        intent.putExtra("actionIndex", triggerIndex);

        pi = PendingIntent.getBroadcast(context, getIdAsInt() + 1001 + triggerIndex, intent
                , PendingIntent.FLAG_MUTABLE);
        TriggerByWeather triggerByWeather = TriggerByWeather.class.cast(trigger);

        String updateEvery = triggerByWeather.getUpdateForcastEvery();
        switch (updateEvery) {
            case "Hourly":
                am.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),
                        AlarmManager.INTERVAL_HOUR, pi);
                break;
            case "6 Hours":
                am.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),
                        AlarmManager.INTERVAL_DAY / 4, pi);
                break;
            case "12 Hours":
                am.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),
                        AlarmManager.INTERVAL_DAY / 2, pi);
                break;
            case "24 Hours":
                am.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),
                        AlarmManager.INTERVAL_DAY, pi);
                break;

            case "2 Days":

                am.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),
                        AlarmManager.INTERVAL_DAY * 2, pi);
                break;
            default:
                am.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),
                        AlarmManager.INTERVAL_DAY, pi);
                break;
        }
    }

    public void createTriggersForLocation(Context context, Trigger trigger, int triggerIndex) {

        TriggerByLocation triggerByLocation = (TriggerByLocation) trigger;
        float radius = 10;
        double latitude = 1;
        double longitude = 1;
        try {
            radius = Float.parseFloat(triggerByLocation.getRadius());
            latitude = Double.parseDouble(triggerByLocation.getLatitude());
            longitude = Double.parseDouble(triggerByLocation.getLongitude());
        } catch (NumberFormatException e) {
            Log.e("Error", "Radius is not a number");
        }

        GeofencingClient geofencingClient;
        geofencingClient = LocationServices.getGeofencingClient(context);
        Geofence geofence = new Geofence.Builder()
                .setRequestId(Integer.toString(triggerIndex))
                .setCircularRegion(latitude, longitude, radius)
                .setTransitionTypes(triggerByLocation.getEndEnterTrigger())
                .build();


        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofence(geofence);
        GeofencingRequest geofencingRequest = builder.build();

        Intent intent = new Intent(context.getApplicationContext(), GeofenceBroadcastReceiver.class);
        intent.putExtra("selectedCollection", id);
        intent.putExtra("actionIndex", triggerIndex);
        PendingIntent pi = PendingIntent.getBroadcast(context, getIdAsInt() + 1001 + triggerIndex, intent
                , PendingIntent.FLAG_MUTABLE);


        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        geofencingClient.addGeofences(geofencingRequest, pi);
    }

}
