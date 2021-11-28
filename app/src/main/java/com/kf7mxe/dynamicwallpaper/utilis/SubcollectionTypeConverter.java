package com.kf7mxe.dynamicwallpaper.utilis;

import androidx.room.TypeConverter;

import com.kf7mxe.dynamicwallpaper.models.SubCollection;

import java.util.ArrayList;

public class SubcollectionTypeConverter {

    @TypeConverter
    public static String subCollectionArrayListToString(ArrayList<SubCollection> subCollections){
        String subCollectionsString = "";
        if(subCollections==null){
            return null;
        } else {
            for(int i=0;i<subCollections.size();i++){
                subCollectionsString = subCollectionsString +"~subcollectiondeliminator~" + subCollections.get(i).myToString();
            }
        }
        return subCollectionsString;
    }
    @TypeConverter
    public static ArrayList<SubCollection> subCollectionFromString(String subCollectionString){
        String[] splitSubCollection = subCollectionString.split("~subcollectiondeliminator~");
        ArrayList<SubCollection> arraySubCollections = new ArrayList<>();
        for (String subCollection: splitSubCollection){
            arraySubCollections.add(new SubCollection(subCollection));
        }
        return arraySubCollections;
    }
}
