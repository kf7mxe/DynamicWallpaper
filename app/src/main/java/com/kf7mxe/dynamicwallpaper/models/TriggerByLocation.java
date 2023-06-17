package com.kf7mxe.dynamicwallpaper.models;

import com.google.android.gms.location.Geofence;

import java.io.Serializable;

public class TriggerByLocation extends Trigger implements Serializable {
    private String triggerType="triggerByLocation";
    private String latitude;
    private String longitude;
    private String radius;

    private String endEnterTrigger;

    @Override
    public String getTriggerType() {
        return triggerType;
    }

    @Override
    public String getDisplayType(){
        return "Location By Latitude: "+latitude+" Longitude: "+longitude+" Radius: "+radius+" End Trigger: "+endEnterTrigger;
    }
    public TriggerByLocation(){

    }



    public TriggerByLocation(String newLatitude, String newLongitude, String newRadius){

        this.latitude = newLatitude;
        this.longitude = newLongitude;
        this.radius = newRadius;
        this.endEnterTrigger = "enter";
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
        String[] triggerDateTimeSplit = tempString.split("~triggerByLocation~");
        if (triggerDateTimeSplit.length>1) {
            this.latitude = triggerDateTimeSplit[0];
        }
        if (triggerDateTimeSplit.length>2) {
            this.longitude = triggerDateTimeSplit[1];
        }
        if (triggerDateTimeSplit.length>2) {
            this.radius = triggerDateTimeSplit[2];
        }
        if (triggerDateTimeSplit.length>3) {
            this.endEnterTrigger = triggerDateTimeSplit[3];
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

    public void setEndEnterTrigger(String endEnterTrigger) {
        this.endEnterTrigger = endEnterTrigger;
    }

    public int getEndEnterTrigger() {
        if (endEnterTrigger.equals("enter")) {
            return Geofence.GEOFENCE_TRANSITION_ENTER;
        } else {
            return Geofence.GEOFENCE_TRANSITION_EXIT;
        }
    }

    public String myToString(){
        return this.triggerType +"~triggerTypeDeliminator~" +  this.latitude+"~triggerByLocation~"
                +this.longitude+"~triggerByLocation~"
                +this.radius+"~triggerByLocation~"+this.endEnterTrigger;

    }
}
