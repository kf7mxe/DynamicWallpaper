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
