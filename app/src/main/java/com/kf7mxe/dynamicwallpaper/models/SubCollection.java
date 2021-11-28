package com.kf7mxe.dynamicwallpaper.models;

import java.util.ArrayList;

public class SubCollection {
    private String name;
    private ArrayList<String> fileNames = new ArrayList<>();
    public SubCollection(){

    }
    public SubCollection(String createFromString){
        if(createFromString==null || createFromString.length()==0){return;}
       String[] nameSplit = createFromString.split("~subCollectionNameDeliminator~");
       if(nameSplit[0]!=null){
           this.name = nameSplit[0];
       } else if(nameSplit[1]!=null){
           String[] fileNamesStringSplit = nameSplit[1].split("~subCollectionFileNames~");
           for(String fileName:fileNamesStringSplit){
               fileNames.add(fileName);
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
        temp = temp + this.name+"subCollectionNameDeliminator";
        for(String fileName:this.fileNames){
            temp = temp + fileName + "~subCollectionFileNames~";
        }
        return temp;
    }

}
