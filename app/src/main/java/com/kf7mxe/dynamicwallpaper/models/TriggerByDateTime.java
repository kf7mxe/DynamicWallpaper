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
    private String repeatDayOfWeek ="none";
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
        if(newRepeateDayOfWeek!=null){
            this.repeatDayOfWeek = newRepeateDayOfWeek;
        }
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

    public int getHourToStartTrigger(){
        if(this.timeToTrigger.equals("none")){
            return 0;
        }
        String[] tempSplit = this.timeToTrigger.split(":");
        return Integer.parseInt(tempSplit[0]);
    }

    public long getRepeateIntervalAmount(){
        return Long.parseLong(this.repeatIntervalAmount);
    }

    public String getRepeatIntervalType(){
        return this.repeatIntervalType;
    }

    public long getIntervalTypeAsLong(){
        switch (this.repeatIntervalType){
            case "Minutes":
                return (long)60000;
            case "Hour":
                return (long)3600000;
            case "Day":
                return (long)86400000;
            case "Week":
                return (long)604800000;
            case "Month":
                return (long) 2.6280E+9;
            default:
                return (long) 0;
        }
    }

    public String getRepeatDayOfWeek(){
        return this.repeatDayOfWeek;
    }

    public int getMinuteToStartTrigger(){
        if(this.timeToTrigger.equals("none")){
            return 0;
        }
        String[] tempSplit = this.timeToTrigger.split(":");
        return Integer.parseInt(tempSplit[1]);
    }


}
