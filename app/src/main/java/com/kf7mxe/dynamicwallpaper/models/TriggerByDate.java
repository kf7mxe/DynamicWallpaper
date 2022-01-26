package com.kf7mxe.dynamicwallpaper.models;

import java.io.Serializable;

public class TriggerByDate extends Trigger implements Serializable {
    private String triggerType = "triggerByDate";
    private String m_month = "none";
    private String m_date = "none";

    public TriggerByDate(){

    }
    public TriggerByDate(String splitString){
        if(splitString.contains("~triggerTypeDeliminator~")){
            String[] temp = splitString.split("~triggerTypeDeliminator~");
            if(temp.length==2){
                String[] tempMonthDate = temp[1].split("~triggerDate~");
                if(tempMonthDate.length==2){
                    this.m_date = tempMonthDate[0];
                    this.m_month = tempMonthDate[1];
                }
            }
        }
    }

    public void setM_month(String m_month) {
        this.m_month = m_month;
    }

    public void setM_date(String m_date) {
        this.m_date = m_date;
    }

    public int getM_Month(){
        return getMonthInt(this.m_month);
    }

    public int getM_date(){
        return Integer.parseInt(m_date);
    }

    public String myToString(){
        return this.triggerType + "~triggerTypeDeliminator~"+this.m_date+"~triggerDate~"+this.m_month;
    }


    private int getMonthInt(String month) {
        switch (month) {
            case "Jan":
                return 0;
            case "Feb":
                return 1;
            case "Mar":
                return 2;
            case "Apr":
                return 3;
            case "May":
                return 4;
            case "Jun":
                return 5;
            case "Jul":
                return 6;
            case "Aug":
                return 7;
            case "Sep":
                return 8;
            case "Oct":
                return 9;
            case "Nov":
                return 10;
            case "Dec":
                return 11;
        }
        return -1;
    }
}
