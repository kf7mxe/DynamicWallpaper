package com.kf7mxe.dynamicwallpaper.models;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.Calendar;

public class TriggerByDateTime extends Trigger implements Serializable {
    private String repeatIntervalAmount;
    private String repeatIntervalType;
    private String timeToTrigger;
    private String repeatDayOfWeek;
    private String triggerType="triggerByDateTime";
    public TriggerByDateTime(){

    }

    @Override
    public String getTriggerType() {
        return triggerType;
    }
    @Override
    public String getDisplayType(){
        return "Trigger By Date and Time \n Repeat Every"+repeatIntervalType +" "+repeatIntervalType + "\n Change at "+timeToTrigger+"\n Repeat Day Of week"+repeatDayOfWeek;
    }

    public TriggerByDateTime(String newRepeatIntervalAmount, String newRepeatIntervalType, String newTimeToTrigger, String newRepeateDayOfWeek){
        this.repeatIntervalType = newRepeatIntervalType;
        this.repeatIntervalAmount = newRepeatIntervalAmount;
        this.timeToTrigger = newTimeToTrigger;
        this.repeatDayOfWeek = newRepeateDayOfWeek;
    }

    public TriggerByDateTime(String splitString){
        String[] triggerDateTimeSplit = splitString.split("~triggerDateTime~");
        this.triggerType = triggerDateTimeSplit[0];
        this.repeatIntervalAmount = triggerDateTimeSplit[1];
        this.repeatIntervalType = triggerDateTimeSplit[2];
        this.timeToTrigger = triggerDateTimeSplit[3];
        this.repeatDayOfWeek = triggerDateTimeSplit[4];
    }

    public String myToString() {
        return this.triggerType +"~triggerTypeDeliminator~" + this.triggerType+"~triggerDateTime~"+ this.repeatIntervalAmount+"~triggerDateTime~"+this.repeatIntervalType
                +"~triggerDateTime~"+timeToTrigger+"~triggerDateTime~"+repeatDayOfWeek;
    }

    public void runTrigger(Context context){
//        Calendar calendar = Calendar.getInstance();
//
//        calendar.set(Calendar.HOUR_OF_DAY, 13); // For 1 PM or 2 PM
//        calendar.set(Calendar.MINUTE, 0);
//        calendar.set(Calendar.SECOND, 0);
//        PendingIntent pi = PendingIntent.getService(context, 0,
//                new Intent(context, MyClass.class),PendingIntent.FLAG_UPDATE_CURRENT);
//        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
//        am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
//                AlarmManager.INTERVAL_DAY, pi);
    }
}
