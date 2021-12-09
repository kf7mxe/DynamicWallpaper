package com.kf7mxe.dynamicwallpaper.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TriggerBySeason implements Serializable {
    private String triggerType="triggerBySeason";
    private ArrayList<Season> seasons = new ArrayList<>();
    public TriggerBySeason(){

    }

    public void addSeason(String season,String date,String month,String endDate,String endMonth){
        seasons.add(new Season(season,month,date,endMonth,endDate));
    }

    public int getSeasonsSize(){
        return seasons.size();
    }

    public ArrayList<Season> getSeasons(){
        return seasons;
    }

    public class Season {
        private String m_season ="none";
        private String m_month = "none";
        private String m_date = "none";
        private String m_endMonth = "none";
        private String m_endDate = "none";
        private int calMonth = -1;
        private int calEndMonth = -1;
        Season(String season, String month,String date,String endMonth,String endDate){
            m_season = season;
            m_date =date;
            m_month = month;
            m_endMonth = endMonth;
            m_endDate = endDate;
            setCalMonth(month,false);
            setCalMonth(endMonth,true);

        }

        public int getCalMonth(){
            return calMonth;
        }

        public int getEndMCalMonthInt(){
            return calEndMonth;
        }

        public int getDay(){
            return Integer.parseInt(this.m_date);
        }

        public int getEndDayInt(){
            return Integer.parseInt(this.m_endDate);
        }

        private void setCalMonth(String month,boolean isEndMonth){
            switch (month){
                case "Jan":
                    if(isEndMonth){
                        this.calEndMonth = 0;
                    } else {
                        this.calMonth = 0;
                    }
                    break;
                case "Feb":
                    if(isEndMonth){
                        this.calEndMonth =1;
                    } else {
                        this.calMonth =1;
                    }
                            break;
                case "Mar":
                    if(isEndMonth){
                        this.calEndMonth =2;
                    } else {
                        this.calMonth =2;
                    }
                    break;
                case "Apr":
                    if(isEndMonth){
                        this.calEndMonth =3;
                    } else {
                        this.calMonth =3;
                    }
                    break;
                case "May":
                    if(isEndMonth){
                        this.calEndMonth =4;
                    } else {
                        this.calMonth =4;
                    }
                    break;
                case "Jun":
                    this.calMonth = 5;
                    break;
                case "Jul":
                    if(isEndMonth){
                        this.calEndMonth =6;
                    } else {
                        this.calMonth =6;
                    }
                    break;
                case "Aug":
                    if(isEndMonth){
                        this.calEndMonth =7;
                    } else {
                        this.calMonth =7;
                    }
                    break;
                case "Sep":
                    if(isEndMonth){
                        this.calEndMonth =8;
                    } else {
                        this.calMonth =8;
                    }
                    break;
                case "Oct":
                    if(isEndMonth){
                        this.calEndMonth =9;
                    } else {
                        this.calMonth =9;
                    }
                    break;
                case "Nov":
                    if(isEndMonth){
                        this.calEndMonth =10;
                    } else {
                        this.calMonth =10;
                    }
                    break;
                case "Dec":
                    if(isEndMonth){
                        this.calEndMonth =11;
                    } else {
                        this.calMonth =11;
                    }
                    break;
            }
        }
    }

}
