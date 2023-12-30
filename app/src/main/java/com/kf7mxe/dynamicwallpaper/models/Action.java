package com.kf7mxe.dynamicwallpaper.models;

import java.io.Serializable;

public class Action implements Serializable {
    String type;
    String changeToSubCollection;
    String changeToSecificImage;

    public Action(){

    }

    public Action(String type,String changeToSubCollection,String changeToSecificImage){
        this.type =type;
        this.changeToSubCollection = changeToSubCollection;
        this.changeToSecificImage = changeToSecificImage;
    }

    public String getType() {
        return type;
    }

    public String getHumanReadableType(){
        String type = "";
        switch (this.type){
            case "selectActionNextInCollection":
                type = "Go to Next Wallpaper in Colection";
                break;
            case "selectActionSwitchToDiffSubColRadio":
                type="Switch To Different Sub Collection";
                break;
            case "selectActionRandomInCollSubRadio":
                type="Go to Random Wallpaper in Collection or subcollection";
                break;
            case "selectActionSpecificWallpaperRadio":
                type="Go to Specific Wallpaper";
                break;
        }
        return type;
    }

    public String getDisplayType(){
        String type = "";
        switch (this.type){
            case "selectActionNextInCollection":
                type = "Go to Next Wallpaper in Colection";
                break;
            case "selectActionSwitchToDiffSubColRadio":
                type="Switch To Different Sub Collection \n Selected SubCollection"+changeToSubCollection;
                break;
            case "selectActionRandomInCollSubRadio":
                type="Go to Random Wallpaper in Collection or subcollection";
                break;
            case "selectActionSpecificWallpaperRadio":
                type="Go to Specific Wallpaper \n Selected Wallpaper:"+changeToSecificImage;
                break;
        }
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Action(String fromString){
        String[] fromStringSplit = fromString.split("~actionTypeDeliminator~");
        this.type = fromStringSplit[0];
        String[] changeToSubCollectionSplit = fromStringSplit[1].split("~actionSubCollection~");
        this.changeToSubCollection = changeToSubCollectionSplit[0];
        this.changeToSecificImage = changeToSubCollectionSplit[1];
    }

    public String myToString() {
        return type + "~actionTypeDeliminator~" + this.changeToSubCollection +"~actionSubCollection~"+this.changeToSecificImage;
    }


}
