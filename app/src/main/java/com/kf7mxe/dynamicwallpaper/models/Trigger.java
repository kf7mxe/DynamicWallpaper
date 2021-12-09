package com.kf7mxe.dynamicwallpaper.models;

import java.io.Serializable;
import java.util.ArrayList;


public class Trigger implements Serializable {
    String triggerType;

    public Trigger(){

    }

    public String getTriggerType() {
        return triggerType;
    }

    public String getDisplayType(){
        return triggerType;
    }

    public Trigger(String fromString){

    }

    public int getHourToStartTrigger(){
        return 0;
    }
    public int getMinuteToStartTrigger() {
        return 0;
    }

    public long getRepeateIntervalAmount() {
        return (long)0;
    }
    public long getIntervalTypeAsLong() {
        return (long)0;
    }

    public String getRepeatIntervalType() {
        return "";
    }
    public String getRepeatDayOfWeek() {
        return"";
    }
        public String myToString() {
        return "";
    }

    public int getSeasonsSize(){
        return 0;
    }
    public ArrayList<TriggerBySeason.Season> getSeasons(){
        return new ArrayList<>();
    }

    }
