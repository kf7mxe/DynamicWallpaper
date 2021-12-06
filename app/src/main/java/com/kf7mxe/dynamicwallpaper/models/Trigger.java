package com.kf7mxe.dynamicwallpaper.models;

import java.io.Serializable;

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

    public String myToString() {
        return "";
    }
}
