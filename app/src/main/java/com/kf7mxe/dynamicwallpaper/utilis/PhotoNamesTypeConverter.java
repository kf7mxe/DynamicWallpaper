

package com.kf7mxe.dynamicwallpaper.utilis;

import androidx.room.TypeConverter;

import com.kf7mxe.dynamicwallpaper.models.Rule;
import com.kf7mxe.dynamicwallpaper.models.SubCollection;
import java.util.ArrayList;

public class PhotoNamesTypeConverter {
    @TypeConverter
    public static String photoNamesListToString(ArrayList<String> photoNamesList){
        String photoArrayString = "";
        if(photoNamesList == null || photoNamesList.size()==0){
            return null;
        } else {
            for(String photoName:photoNamesList){
                photoArrayString = photoArrayString + "~photoNamesDeliminator~"+ photoName;
            }
        }
        return photoArrayString;
    }

    @TypeConverter
    public static ArrayList<String> photoNamesFromString(String photoNamesString){
        if(photoNamesString==null){return new ArrayList<String>();}
        String[] splitPhotoNames = photoNamesString.split("~photoNamesDeliminator~");
        ArrayList<String> photoNames = new ArrayList<>();
        for(String photoname:splitPhotoNames){
            if(!photoname.equals("")){
                photoNames.add(photoname);
            }
        }
        return photoNames;
    }
}
