package com.kf7mxe.dynamicwallpaper.models;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class TriggerByDateTime extends Trigger implements Serializable {
    private String repeatIntervalAmount;
    private String repeatIntervalType;
    private String timeToTrigger;
    private String repeatDayOfWeek;
    public TriggerByDateTime(){

    }

    public TriggerByDateTime(String newRepeatIntervalAmount,String newRepeatIntervalType,String newTimeToTrigger,String newRepeateDayOfWeek){
        this.repeatIntervalType = newRepeatIntervalType;
        this.repeatIntervalAmount = newRepeatIntervalAmount;
        this.timeToTrigger = newTimeToTrigger;
        this.repeatDayOfWeek = newRepeateDayOfWeek;
    }

    public TriggerByDateTime(String splitString){
        String[] triggerDateTimeSplit = splitString.split("~triggerDateTime~");
        this.repeatIntervalAmount = triggerDateTimeSplit[0];
        this.repeatIntervalType = triggerDateTimeSplit[1];
        this.timeToTrigger = triggerDateTimeSplit[2];
        this.repeatDayOfWeek = triggerDateTimeSplit[3];
    }

    public String myToString() {
        return this.repeatIntervalAmount+"~triggerDateTime~"+this.repeatIntervalType
                +"~triggerDateTime~"+timeToTrigger+"~triggerDateTime~"+repeatDayOfWeek;
    }
}
