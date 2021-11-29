package com.kf7mxe.dynamicwallpaper.utilis;

import androidx.room.TypeConverter;

import com.kf7mxe.dynamicwallpaper.models.Rule;
import com.kf7mxe.dynamicwallpaper.models.SubCollection;

import java.util.ArrayList;

public class RulesTypeConverter {
    @TypeConverter
    public static String rulesArrayListToString(ArrayList<Rule> rules){
        String rulesArrayString="";
        if(rules==null){
            return null;
        } else {
            for(int i=0;i<rules.size();i++){
                rulesArrayString = rulesArrayString +"~rulesDeliminator~"+rules.get(i).myToString();
            }
        }
        return rulesArrayString;
    }
    @TypeConverter
    public static ArrayList<Rule> rulesFromString(String rulesString){
        String[] splitRules = rulesString.split("~rulesDeliminator~");
        ArrayList<Rule> rules = new ArrayList<>();
        if (splitRules.length==1 && splitRules[0].equals("")){return rules;}
        for(String rule : splitRules){
            if(rule.equals("")){}
            else{
                rules.add(new Rule(rule));
            }
        }
        return rules;
    }


}
