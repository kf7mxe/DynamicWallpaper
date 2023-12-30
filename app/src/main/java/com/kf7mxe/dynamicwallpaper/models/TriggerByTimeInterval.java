package com.kf7mxe.dynamicwallpaper.models;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.Calendar;

public class TriggerByTimeInterval extends Trigger implements Serializable {
    private String repeatIntervalAmount;
    private String repeatIntervalType;
    private String timeToTrigger;
    private String repeatDayOfWeek ="none";
    private final String triggerType="triggerByTimeInterval";
    private String exactTime="false";
    public TriggerByTimeInterval(){

    }

    @Override
    public String getTriggerType() {
        return triggerType;
    }
    @Override
    public String getDisplayType(){
        return "Trigger By Date and Time \n Repeat Every"+repeatIntervalType +" "+repeatIntervalType + "\n Change at "+timeToTrigger+"\n Repeat Day Of week"+repeatDayOfWeek;
    }

    public TriggerByTimeInterval(String newRepeatIntervalAmount, String newRepeatIntervalType, String newTimeToTrigger, String newRepeateDayOfWeek,String exactTime){
        this.repeatIntervalType = newRepeatIntervalType;
        this.repeatIntervalAmount = newRepeatIntervalAmount;
        this.timeToTrigger = newTimeToTrigger;
        this.exactTime = exactTime;
        if(newRepeateDayOfWeek!=null){
            this.repeatDayOfWeek = newRepeateDayOfWeek;
        }
    }

    public TriggerByTimeInterval(String splitString){
        String[] splitSeasons;
        String tempString=splitString;
        if(splitString.contains("~triggerTypeDeliminator~")){
            String[] temp = splitString.split("~triggerTypeDeliminator~");
            if(temp.length==1){tempString=temp[0];} else{
                tempString = temp[1];
            }
        }
        String[] triggerDateTimeSplit = tempString.split("~triggerDateTime~");
        if (triggerDateTimeSplit.length>1) {
            this.repeatIntervalAmount = triggerDateTimeSplit[0];
        }
        if (triggerDateTimeSplit.length>2) {
            this.repeatIntervalType = triggerDateTimeSplit[1];
        }
        if (triggerDateTimeSplit.length>2) {
            this.timeToTrigger = triggerDateTimeSplit[2];
        }
        if (triggerDateTimeSplit.length>3) {
            this.repeatDayOfWeek = triggerDateTimeSplit[3];
        }
        if(triggerDateTimeSplit.length>4){
            this.exactTime = triggerDateTimeSplit[4];
        }
    }

    public String myToString() {
        return this.triggerType +"~triggerTypeDeliminator~"+ this.repeatIntervalAmount+"~triggerDateTime~"+this.repeatIntervalType
                +"~triggerDateTime~"+timeToTrigger+"~triggerDateTime~"+repeatDayOfWeek+"~triggerDateTime~"+exactTime;
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

    public boolean isExact(){
        return Boolean.parseBoolean(this.exactTime);
    }

@Override
    public String getTriggerTypeAsHumanReadableString(){
        // split the string on camel case
        String[] words = triggerType.split("(?=[A-Z])");
    // remove the first word if it is "trigger"
    if (words[0].equals("trigger")) {
        String[] temp = new String[words.length - 1];
        for (int i = 1; i < words.length; i++) {
            temp[i - 1] = words[i];
        }
        words = temp;
    }
    String humanReadableString = "";
        for (String word : words) {
            humanReadableString += word + " ";
        }

        return humanReadableString;
    }

}
