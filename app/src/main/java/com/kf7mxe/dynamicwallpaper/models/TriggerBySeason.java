package com.kf7mxe.dynamicwallpaper.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TriggerBySeason extends Trigger implements Serializable {
    private final String triggerType="triggerBySeason";
    private ArrayList<Season> seasons = new ArrayList<>();
    public TriggerBySeason(){

    }
    public TriggerBySeason(String splitString){
        String[] splitSeasons;
        String tempString=splitString;
        if(splitString.contains("~triggerTypeDeliminator~")){
            String[] temp = splitString.split("~triggerTypeDeliminator~");
            if(temp.length==1){tempString=temp[0];} else{
                tempString = temp[1];
            }
        }
        splitSeasons = tempString.split("~triggerSeasonDeliminator~");
        for(int i=0;i<splitSeasons.length;i++){
            seasons.add(new Season(splitSeasons[i]));
        }

    }

//    protected TriggerBySeason(Parcel in) {
//        triggerType = in.readString();
//    }

//    public static final Creator<TriggerBySeason> CREATOR = new Creator<TriggerBySeason>() {
//        @Override
//        public TriggerBySeason createFromParcel(Parcel in) {
//            return new TriggerBySeason(in);
//        }
//
//        @Override
//        public TriggerBySeason[] newArray(int size) {
//            return new TriggerBySeason[size];
//        }
//    };

    public void addSeason(String season, String date, String month, String endDate, String endMonth){
        seasons.add(new Season(season,month,date,endMonth,endDate));
    }

    public int getSeasonsSize(){
        return seasons.size();
    }

    public ArrayList<Season> getSeasons(){
        return seasons;
    }

    @Override
    public String getTriggerType() {
        return triggerType;
    }

    @Override
    public String getDisplayType(){
        String temp =  "Trigger By Change in Season";
        for(int i=0;i<seasons.size();i++){
            temp = temp + "\n"+seasons.get(i).m_season+" "+seasons.get(i).m_month+" "+seasons.get(i).m_date+"\n"+ "End of Season:"+seasons.get(i).m_endMonth+" "+seasons.get(i).m_endDate;
        }
        return temp;
    }

//    @Override
//    public int describeContents() {
//        return 0;
//    }
//
//    @Override
//    public void writeToParcel(Parcel dest, int flags) {
//        dest.writeString(triggerType);
//    }

    public class Season implements Serializable{
        private String m_season ="none";
        private String m_month = "none";
        private String m_date = "none";
        private String m_endMonth = "none";
        private String m_endDate = "none";
        private int calMonth = -1;
        private int calEndMonth = -1;
        public  Season(String season, String month,String date,String endMonth,String endDate){
            m_season = season;
            m_date =date;
            m_month = month;
            m_endMonth = endMonth;
            m_endDate = endDate;
            setCalMonth(month,false);
            setCalMonth(endMonth,true);

        }

        public Season(String seasonParse){
            String[] seasonParseString = seasonParse.split("~seasonDeliminator~");
            m_season = seasonParseString[0];
            m_month = seasonParseString[2];
            m_date = seasonParseString[1];
            m_endMonth = seasonParseString[4];
            m_endDate = seasonParseString[3];
        }

        public String myToString(){
            return m_season+"~seasonDeliminator~"+m_month+"~seasonDeliminator~"+m_date+"~seasonDeliminator~"+m_endMonth+"~seasonDeliminator~"+m_endDate;
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
    public String myToString(){
        String temp = this.triggerType+"~triggerTypeDeliminator~";
        for(int i=0;i<seasons.size();i++){
            if(i!=seasons.size()-1){
                temp = temp + seasons.get(i).myToString() +"~triggerSeasonDeliminator~";
            } else {
                temp = temp + seasons.get(i).myToString();
            }
        }
        return temp;
    }


    @Override
    public String getTriggerTypeAsHumanReadableString(){
        // split the string on camel case
        String[] words = triggerType.split("(?=[A-Z])");
        // remove the first word if it is "trigger"
        if (words[0].equals("trigger")) {
            String[] temp = new String[words.length - 1];
            for (int i = 1; i < words.length; i++) {
                temp[i - 1] = words[i];
            }
            words = temp;
        }
        String humanReadableString = "";
        for (String word : words) {
            humanReadableString += word + " ";
        }

        return humanReadableString;
    }

}
