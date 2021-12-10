package com.kf7mxe.dynamicwallpaper.models;

import java.io.Serializable;
import java.util.ArrayList;

public class SubCollection implements Serializable {
    private String name;
    private ArrayList<String> fileNames = new ArrayList<>();
    public SubCollection(){

    }
    public SubCollection(String createFromString){
        if(createFromString==null || createFromString.length()==0){return;}
       String[] fileNamesStringSplit = createFromString.split("~subCollectionFileNames~");
        this.name = fileNamesStringSplit[0];
           for(int i=0;i<fileNamesStringSplit.length;i++) {
               if (i != 0) {
                   fileNames.add(fileNamesStringSplit[i]);
               }
           }
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName(){
        return this.name;
    }

    public void setFileNames(ArrayList<String> fileNames) {
        this.fileNames = fileNames;
    }
    public ArrayList<String> getFileNames(){
        return this.fileNames;
    }

    public void addFileName(String fileName){
        this.fileNames.add(fileName);
    }

    public void removeFileName(int index){
        this.fileNames.remove(index);
    }

    public void removeFileName(String objectToRemove){
        this.fileNames.remove(objectToRemove);
    }

    public String myToString(){
        String temp = "";
        temp = temp + this.name+"~subCollectionFileNames~";
        if(this.fileNames.size()==0){
            return "";
        }
        for(String fileName:this.fileNames){
            temp = temp + fileName + "~subCollectionFileNames~";
        }
        return temp;
    }

}
