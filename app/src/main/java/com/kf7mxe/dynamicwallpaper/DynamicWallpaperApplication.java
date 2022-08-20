package com.kf7mxe.dynamicwallpaper;

import android.app.Application;
import android.os.Build;
import android.widget.Toast;

import com.google.android.material.color.DynamicColors;

public class DynamicWallpaperApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        //Parse SDK stuff goes here

        if(Integer.parseInt(Build.VERSION.RELEASE)<12){
            //set theme from DynamicColors
            setTheme(R.style.DynamicWallpaper);
        }
        if(Integer.parseInt(Build.VERSION.RELEASE)<=12) {
            setTheme(R.style.DynamicWallpaper_dynamic);
            DynamicColors.applyToActivitiesIfAvailable(this);
        }
    }



}
