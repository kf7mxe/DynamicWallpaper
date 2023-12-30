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
    public String getTriggerTypeAsHumanReadableString(){
        // split the string on camel case
        if (triggerType == null) {
            return "";
        }
        String[] words = triggerType.split("(?=[A-Z])");
        String humanReadableString = "";
        for (String word : words) {
            humanReadableString += word + " ";
        }

        return humanReadableString;
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

    public int getM_date() {
        return -1;
    }

    public int getM_Month() {
        return -1;
    }

    public boolean isExact(){
        return false;
    }
}
