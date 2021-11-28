package com.kf7mxe.dynamicwallpaper.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.io.Serializable;
import java.util.ArrayList;

@Entity
public class Collection {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name="id")
    private long id;
    @ColumnInfo(name="name")
    private String name;
    @ColumnInfo(name="sub_collections")
    @TypeConverters(com.kf7mxe.dynamicwallpaper.utilis.SubcollectionTypeConverter.class)
    private ArrayList<SubCollection> subCollectionArray;
    @ColumnInfo(name="sub_collections_index")
    private int selectedSubCollectionArrayIndex;
    @ColumnInfo(name="sub_collection_selected_image_index")
    private int subCollectionSelectedImageIndex;
    @ColumnInfo(name="rules")
    @TypeConverters(com.kf7mxe.dynamicwallpaper.utilis.RulesTypeConverter.class)
    private ArrayList<Rule> rules;
    @ColumnInfo(name="selected_image_index")
    private int selectedImageIndex;
    @ColumnInfo(name="photo_names")
    @TypeConverters(com.kf7mxe.dynamicwallpaper.utilis.PhotoNamesTypeConverter.class)
    private ArrayList<String> photoNames ;

    public Collection(){
        this.name="";
        this.subCollectionArray = new ArrayList<>();
        this.rules = new ArrayList<>();
        this.photoNames  = new ArrayList<>();
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addRule(Rule newRule){
        this.rules.add(newRule);
    }

    public void addSubCollection(SubCollection newSubCollection){
        this.subCollectionArray.add(newSubCollection);
    }

    public void addImage(String fileName){
        photoNames.add(fileName);
    }

    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }

    public ArrayList<String> getPhotoNames() {
        return photoNames;
    }

    public void setPhotoNames(ArrayList<String> photoNames) {
        this.photoNames = photoNames;
    }

    public ArrayList<SubCollection> getSubCollectionArray() {
        return subCollectionArray;
    }

    public ArrayList<Rule> getRules() {
        return rules;
    }

    public int getSelectedImageIndex() {
        return selectedImageIndex;
    }

    public int getSelectedSubCollectionArrayIndex() {
        return selectedSubCollectionArrayIndex;
    }

    public void setRules(ArrayList<Rule> rules) {
        this.rules = rules;
    }

    public void setSelectedImageIndex(int selectedImageIndex) {
        this.selectedImageIndex = selectedImageIndex;
    }

    public void setSelectedSubCollectionArrayIndex(int selectedSubCollectionArrayIndex) {
        this.selectedSubCollectionArrayIndex = selectedSubCollectionArrayIndex;
    }

    public void setSubCollectionArray(ArrayList<SubCollection> subCollectionArray) {
        this.subCollectionArray = subCollectionArray;
    }

    public void setSubCollectionSelectedImageIndex(int subCollectionSelectedImageIndex) {
        this.subCollectionSelectedImageIndex = subCollectionSelectedImageIndex;
    }

    public int getSubCollectionSelectedImageIndex() {
        return subCollectionSelectedImageIndex;
    }
}
