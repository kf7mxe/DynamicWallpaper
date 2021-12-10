package com.kf7mxe.dynamicwallpaper.utilis;

import androidx.room.TypeConverter;

import com.kf7mxe.dynamicwallpaper.models.SubCollection;

import java.util.ArrayList;

public class SubcollectionTypeConverter {

    @TypeConverter
    public static String subCollectionArrayListToString(ArrayList<SubCollection> subCollections){
        String subCollectionsString = "";
        if(subCollections==null || subCollections.size()==0){
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
        ArrayList<SubCollection> arraySubCollections = new ArrayList<>();
        if(subCollectionString==null){
           return arraySubCollections;
        }
        String[] splitSubCollection = subCollectionString.split("~subcollectiondeliminator~");
        for (String subCollection: splitSubCollection){
            if(subCollection.length()!=0){
                arraySubCollections.add(new SubCollection(subCollection));
            }
        }
        return arraySubCollections;
    }
}
