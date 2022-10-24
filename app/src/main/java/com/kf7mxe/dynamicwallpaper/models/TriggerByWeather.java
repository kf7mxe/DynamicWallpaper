package com.kf7mxe.dynamicwallpaper.models;

import java.io.Serializable;

public class TriggerByWeather extends Trigger implements Serializable {
    private String triggerType="triggerByWeather";
    private String whenTempretureIs;
    private String whenTempretureIsLessThan;
    private String whenTempretureIsGreaterThan;
    private String betweenLowEndTempreture;
    private String betweenHighEndTempreture;
    private String weatherCondition;

    public TriggerByWeather(){

    }

    public TriggerByWeather(String newWhenTempretureIs, String newWhenTempretureIsLessThan, String newWhenTempretureIsGreaterThan, String newBetweenLowEndTempreture, String newBetweenHighEndTempreture, String newWeatherCondition){
        this.whenTempretureIs = newWhenTempretureIs;
        this.whenTempretureIsLessThan = newWhenTempretureIsLessThan;
        this.whenTempretureIsGreaterThan = newWhenTempretureIsGreaterThan;
        this.betweenLowEndTempreture = newBetweenLowEndTempreture;
        this.betweenHighEndTempreture = newBetweenHighEndTempreture;
        this.weatherCondition = newWeatherCondition;
    }

    public TriggerByWeather(String splitString){
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
            this.whenTempretureIs = triggerDateTimeSplit[0];
        }
        if (triggerDateTimeSplit.length>2) {
            this.whenTempretureIsLessThan = triggerDateTimeSplit[1];
        }
        if (triggerDateTimeSplit.length>2) {
            this.whenTempretureIsGreaterThan = triggerDateTimeSplit[2];
        }
        if (triggerDateTimeSplit.length>2) {
            this.betweenLowEndTempreture = triggerDateTimeSplit[3];
        }
        if (triggerDateTimeSplit.length>2) {
            this.betweenHighEndTempreture = triggerDateTimeSplit[4];
        }
        if (triggerDateTimeSplit.length>2) {
            this.weatherCondition = triggerDateTimeSplit[5];
        }
    }

    public String getWhenTempretureIs() {
        return whenTempretureIs;
    }

    public void setWhenTempretureIs(String whenTempretureIs) {
        this.whenTempretureIs = whenTempretureIs;
    }

    public void setWhenTempretureIs(double whenTempretureIs) {
        this.whenTempretureIs = String.valueOf(whenTempretureIs);
    }

    public String getWhenTempretureIsLessThan() {
        return whenTempretureIsLessThan;
    }

    public void setWhenTempretureIsLessThan(String whenTempretureIsLessThan) {
        this.whenTempretureIsLessThan = whenTempretureIsLessThan;
    }

    public void setWhenTempretureIsLessThan(double whenTempretureIsLessThan) {
        this.whenTempretureIsLessThan = String.valueOf(whenTempretureIsLessThan);
    }

    public String getWhenTempretureIsGreaterThan() {
        return whenTempretureIsGreaterThan;
    }

    public void setWhenTempretureIsGreaterThan(String whenTempretureIsGreaterThan) {
        this.whenTempretureIsGreaterThan = whenTempretureIsGreaterThan;
    }

    public void setWhenTempretureIsGreaterThan(double whenTempretureIsGreaterThan) {
        this.whenTempretureIsGreaterThan = String.valueOf(whenTempretureIsGreaterThan);
    }

    public String getBetweenLowEndTempreture() {
        return betweenLowEndTempreture;
    }

    public void setBetweenLowEndTempreture(String betweenLowEndTempreture) {
        this.betweenLowEndTempreture = betweenLowEndTempreture;
    }

    public void setBetweenLowEndTempreture(double betweenLowEndTempreture) {
        this.betweenLowEndTempreture = String.valueOf(betweenLowEndTempreture);
    }

    public String getBetweenHighEndTempreture() {
        return betweenHighEndTempreture;
    }

    public void setBetweenHighEndTempreture(String betweenHighEndTempreture) {
        this.betweenHighEndTempreture = betweenHighEndTempreture;
    }

    public void setBetweenHighEndTempreture(double betweenHighEndTempreture) {
        this.betweenHighEndTempreture = String.valueOf(betweenHighEndTempreture);
    }

    public String getWeatherCondition() {
        return weatherCondition;
    }

    public void setWeatherCondition(String weatherCondition) {
        this.weatherCondition = weatherCondition;
    }

    public String myToString(){
        String returnString = "";
        if (this.whenTempretureIs != null) {
            returnString = returnString + this.whenTempretureIs;
        }
        returnString = returnString + "~triggerDateTime~";
        if (this.whenTempretureIsLessThan != null) {
            returnString = returnString + this.whenTempretureIsLessThan;
        }
        returnString = returnString + "~triggerDateTime~";
        if (this.whenTempretureIsGreaterThan != null) {
            returnString = returnString + this.whenTempretureIsGreaterThan;
        }
        returnString = returnString + "~triggerDateTime~";
        if (this.betweenLowEndTempreture != null) {
            returnString = returnString + this.betweenLowEndTempreture;
        }
        returnString = returnString + "~triggerDateTime~";
        if (this.betweenHighEndTempreture != null) {
            returnString = returnString + this.betweenHighEndTempreture;
        }
        returnString = returnString + "~triggerDateTime~";
        if (this.weatherCondition != null) {
            returnString = returnString + this.weatherCondition;
        }
        returnString = returnString + "~triggerDateTime~";
        return returnString;
    }
}
