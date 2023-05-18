package com.kf7mxe.dynamicwallpaper.utilis;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.color.DynamicColors;
import com.kf7mxe.dynamicwallpaper.R;

import java.util.ArrayList;

public class DynamicColorUtils {




    public static void setDynamicColor(View view, Context context){
        ArrayList<View> views = getViewsByTag((ViewGroup) view.getRootView(), "background");
        for(View v:views){

            v.setBackgroundColor(context.getResources().getColor(R.color.material_dynamic_neutral_variant40));
        }
    }
    private static ArrayList<View> getViewsByTag(ViewGroup root, String tag){
        ArrayList<View> views = new ArrayList<View>();
        final int childCount = root.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = root.getChildAt(i);
            if (child instanceof ViewGroup) {
                views.addAll(getViewsByTag((ViewGroup) child, tag));
            }

            final Object tagObj = child.getTag();
            if (tagObj != null && tagObj.equals(tag)) {
                views.add(child);
            }

        }
        return views;
    }

//    private static ArrayList<View> getViewsByTag(ViewGroup root, String tag){
//        ArrayList<View> views = new ArrayList<View>();
//        final int childCount = root.getChildCount();
//        for (int i = 0; i < childCount; i++) {
//            final View child = root.getChildAt(i);
//            if (child instanceof ViewGroup) {
//                views.addAll(getViewsByTag((ViewGroup) child, tag));
//            }
//
//            final Object tagObj = child.getTag();
//            if (tagObj != null && tagObj.equals(tag)) {
//                views.add(child);
//            }
//
//        }
//        return views;
//    }
}
