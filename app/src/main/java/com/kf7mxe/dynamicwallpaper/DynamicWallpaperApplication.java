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
            DynamicColors.applyToActivitiesIfAvailable(this);
    }



}
