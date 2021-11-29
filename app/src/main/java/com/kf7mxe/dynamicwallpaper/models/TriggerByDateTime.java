package com.kf7mxe.dynamicwallpaper.models;

import androidx.annotation.NonNull;

import java.io.Serializable;

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
}
