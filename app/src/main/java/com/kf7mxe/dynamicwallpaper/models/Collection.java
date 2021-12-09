package com.kf7mxe.dynamicwallpaper.models;

import static android.content.Intent.ACTION_OPEN_DOCUMENT;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.Toast;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.kf7mxe.dynamicwallpaper.recievers.AlarmActionReciever;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;

@Entity
public class Collection implements Serializable{

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name="id")
    private long id;
    @ColumnInfo(name="name")
    private String name;
    @ColumnInfo(name="sub_collections")
    @TypeConverters(com.kf7mxe.dynamicwallpaper.utilis.SubcollectionTypeConverter.class)
    private ArrayList<SubCollection> subCollectionArray;
    @ColumnInfo(name="sub_collections_index")
    private int selectedSubCollectionArrayIndex =-1;
    @ColumnInfo(name="sub_collection_selected_image_index")
    private int subCollectionSelectedImageIndex = -1;
    @ColumnInfo(name="rules")
    @TypeConverters(com.kf7mxe.dynamicwallpaper.utilis.RulesTypeConverter.class)
    private ArrayList<Rule> rules;
    @ColumnInfo(name="selected_image_index")
    private int selectedImageIndex;
    @ColumnInfo(name="photo_names")
    @TypeConverters(com.kf7mxe.dynamicwallpaper.utilis.PhotoNamesTypeConverter.class)
    private ArrayList<String> photoNames ;

    public Collection(){
        this.name="";
        this.subCollectionArray = new ArrayList<>();
        this.rules = new ArrayList<>();
        this.photoNames  = new ArrayList<>();
    }

    public void setName(String name) {
        this.name = name;
    }
    public void addRule(Rule newRule){
        this.rules.add(newRule);
    }

    public void addSubCollection(SubCollection newSubCollection){
        this.subCollectionArray.add(newSubCollection);
    }

    public void addImage(String fileName){
        photoNames.add(fileName);
    }

    public long getId() {
        return id;
    }
    public int getIdAsInt(){
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

    public void removeTriggersBroadcastRecievers(Context context){
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        for(int i=0;i<this.getRules().size();i++){
            Intent intent = new Intent(context.getApplicationContext(), AlarmActionReciever.class);
            PendingIntent pi=null;
                intent.putExtra("selectedCollection", getId());
                //intent.putExtra("actionToRun",action)
                pi = PendingIntent.getBroadcast(context,getIdAsInt()+1001+i, intent
                        , PendingIntent.FLAG_MUTABLE);

            if(pi!=null){
                am.cancel(pi);
            }
        }
    }

    public void startTriggers(Context context){
        for(int i=0;i<this.getRules().size();i++){
            Trigger trigger = this.getRules().get(i).getTrigger();
            switch (trigger.getTriggerType()){
                case "triggerByDateTime":
                    createAlarmForByDateTime(context,trigger,i);
                    break;
                case "triggerBySeason":
                    createAlarmForSeasons(context,trigger,i);
                    break;
                case "":

            }


        }
    }



    public void runAction(int actionIndex,Context context){
        this.rules.get(actionIndex).getAction();
        switch (this.rules.get(actionIndex).getAction().getType()){
            case "selectActionNextInCollection":
                goToNextWallpaper(context);
                break;
            case "selectActionSwitchToDiffSubColRadio":
                int subcollection = Integer.parseInt(this.rules.get(actionIndex).getAction().changeToSubCollection);
                goToSpecificSubCollection(context,subcollection);
                break;
            case "selectActionRandomInCollSubRadio":
                goToRandWallpaper(context);
                //type="Go to Random Wallpaper in Collection or subcollection";
                break;
            case "selectActionSpecificWallpaperRadio":
                goToSpecificWallpaper(context,this.rules.get(actionIndex).getAction().changeToSecificImage);
                // type="Go to Specific Wallpaper \n Selected Wallpaper:"+changeToSecificImage;
                break;
        }
    }

    public void goToNextWallpaper(Context context){
        File file;

        ArrayList<String> images;
        int nextIndex;
        if(this.selectedSubCollectionArrayIndex!=-1) {
            images = subCollectionArray.get(this.selectedSubCollectionArrayIndex).getFileNames();
            nextIndex = this.subCollectionSelectedImageIndex;
        } else {
            images = this.getPhotoNames();
            nextIndex = this.getSelectedImageIndex();
        }
        if(nextIndex<images.size()-1){
            file = new File(context.getExternalFilesDir(ACTION_OPEN_DOCUMENT).getAbsolutePath()+"/"+this.getName()+"/"+images.get(nextIndex));
            int temp = this.getSelectedImageIndex()+1;
            this.setSelectedImageIndex(temp);
        } else {
            file = new File(context.getExternalFilesDir(ACTION_OPEN_DOCUMENT).getAbsolutePath()+"/"+this.getName()+"/"+images.get(0));
            this.setSelectedImageIndex(0);
        }
        setWallpaper(context,file);
    }
    public void goToRandWallpaper(Context context){
        File file;
        ArrayList<String> photos;
        if(selectedSubCollectionArrayIndex!=-1){
            photos = this.subCollectionArray.get(selectedSubCollectionArrayIndex).getFileNames();
        } else {
            photos = this.getPhotoNames();
        }
        Random rand = new Random();
        int index = rand.nextInt(this.getPhotoNames().size());

            file = new File(context.getExternalFilesDir(ACTION_OPEN_DOCUMENT).getAbsolutePath()+"/"+this.getName()+"/"+photos.get(index));
            int temp = this.getSelectedImageIndex()+1;
            this.setSelectedImageIndex(temp);
        setWallpaper(context,file);
    }

    public void goToSpecificWallpaper(Context context, String specificWallpaper){
        File file;
        file = new File(context.getExternalFilesDir(ACTION_OPEN_DOCUMENT).getAbsolutePath()+"/"+this.getName()+"/"+specificWallpaper);
        int temp = this.getSelectedImageIndex()+1;
        this.setSelectedImageIndex(temp);
        setWallpaper(context,file);
    }

    public void goToSpecificSubCollection(Context context, int subCollection){
        File file;
        this.selectedSubCollectionArrayIndex = subCollection;
        ArrayList<String> images;
        int nextIndex;
        if(this.selectedSubCollectionArrayIndex!=-1) {
            images = subCollectionArray.get(this.selectedSubCollectionArrayIndex).getFileNames();
            if(this.subCollectionSelectedImageIndex!=-1){
                nextIndex = this.subCollectionSelectedImageIndex;
            } else {
                nextIndex = 0;
            }
        } else {
            images = this.getPhotoNames();
            nextIndex = this.getSelectedImageIndex();
        }
        if(nextIndex<images.size()-1){
            file = new File(context.getExternalFilesDir(ACTION_OPEN_DOCUMENT).getAbsolutePath()+"/"+this.getName()+"/"+images.get(nextIndex));
            int temp = this.getSelectedImageIndex()+1;
            this.setSelectedImageIndex(temp);
        } else {
            file = new File(context.getExternalFilesDir(ACTION_OPEN_DOCUMENT).getAbsolutePath()+"/"+this.getName()+"/"+images.get(0));
            this.setSelectedImageIndex(0);
        }
        setWallpaper(context,file);
    }

    public void setWallpaper(Context context,File file){
        WallpaperManager wallpaperManager = WallpaperManager.getInstance(context);
        //File file = new File(resultUri.getPath());
        //Bitmap testBitmap = BitmapFactory.decodeFile( resultUri.getPath());
        try {
            InputStream inputStream = new FileInputStream(file);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            if(bitmap!=null){
                wallpaperManager.setBitmap(bitmap);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void createAlarmForByDateTime(Context context,Trigger trigger,int triggerIndex){
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context.getApplicationContext(), AlarmActionReciever.class);
        PendingIntent pi=null;
        intent = new Intent(context.getApplicationContext(), AlarmActionReciever.class);
        intent.putExtra("selectedCollection",id);
        pi = PendingIntent.getBroadcast(context, getIdAsInt()+1001+triggerIndex,intent
                ,PendingIntent.FLAG_MUTABLE);
        Calendar startTime = Calendar.getInstance();
        startTime.set(Calendar.HOUR_OF_DAY,trigger.getHourToStartTrigger());
        startTime.set(Calendar.MINUTE,trigger.getMinuteToStartTrigger());
        if(trigger.getRepeatIntervalType().equals("Week")){
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

        long interval = trigger.getRepeateIntervalAmount()*trigger.getIntervalTypeAsLong();

        am.setInexactRepeating(AlarmManager.RTC_WAKEUP,startTime.getTimeInMillis(),
                interval, pi);
        Toast.makeText(context, "in Set alarm", Toast.LENGTH_SHORT).show();
    }

    public void createAlarmForSeasons(Context context,Trigger trigger,int triggerIndex){
        for(int i=0;i<trigger.getSeasonsSize();i++){
            AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(context.getApplicationContext(), AlarmActionReciever.class);
            PendingIntent pi=null;
            intent = new Intent(context.getApplicationContext(), AlarmActionReciever.class);
            intent.putExtra("selectedCollection",id);
            pi = PendingIntent.getBroadcast(context, getIdAsInt()+1001+triggerIndex,intent
                    ,PendingIntent.FLAG_MUTABLE);
            Calendar startTime = Calendar.getInstance();
            Calendar endTime = Calendar.getInstance();
            startTime.set(Calendar.MONTH,trigger.getSeasons().get(i).getCalMonth());
            startTime.set(Calendar.DAY_OF_MONTH,trigger.getSeasons().get(i).getDay());
            endTime.set(Calendar.MONTH,trigger.getSeasons().get(i).getEndMCalMonthInt());
            endTime.set(Calendar.DAY_OF_MONTH,trigger.getSeasons().get(i).getEndDayInt());
            long interval = endTime.getTimeInMillis()- startTime.getTimeInMillis();
            am.setInexactRepeating(AlarmManager.RTC_WAKEUP,startTime.getTimeInMillis(),
                    interval, pi);
            Toast.makeText(context, "in Set alarm", Toast.LENGTH_SHORT).show();
        }
    }


}
