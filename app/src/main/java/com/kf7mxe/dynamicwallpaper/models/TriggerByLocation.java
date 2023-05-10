package com.kf7mxe.dynamicwallpaper.models;

import java.io.Serializable;

public class TriggerByLocation extends Trigger implements Serializable {
    private String triggerType="triggerByLocation";
    private String latitude;
    private String longitude;
    private String radius;

    public TriggerByLocation(){

    }

    public TriggerByLocation(String newLatitude, String newLongitude, String newRadius){
        this.latitude = newLatitude;
        this.longitude = newLongitude;
        this.radius = newRadius;
    }

    public TriggerByLocation(String splitString){
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
            this.latitude = triggerDateTimeSplit[0];
        }
        if (triggerDateTimeSplit.length>2) {
            this.longitude = triggerDateTimeSplit[1];
        }
        if (triggerDateTimeSplit.length>2) {
            this.radius = triggerDateTimeSplit[2];
        }
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = String.valueOf(latitude);
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = String.valueOf(longitude);
    }

    public String getRadius() {
        return radius;
    }

    public void setRadius(String radius) {
        this.radius = radius;
    }


    public String myToString(){
        return this.latitude+"~triggerDateTime~"+this.longitude+"~triggerDateTime~"+this.radius+"~triggerTypeDeliminator~"+this.triggerType;
    }
}