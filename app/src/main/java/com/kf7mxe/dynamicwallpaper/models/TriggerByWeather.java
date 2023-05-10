package com.kf7mxe.dynamicwallpaper.models;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.text.format.Formatter;
import android.util.JsonReader;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.kf7mxe.dynamicwallpaper.recievers.AlarmActionReciever;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class TriggerByWeather extends Trigger implements Serializable {
    private String triggerType="triggerByWeather";
    private String whenTempretureIs;
    private String whenTempretureIsLessThan;
    private String whenTempretureIsGreaterThan;
    private String betweenLowEndTempreture;
    private String betweenHighEndTempreture;
    private String weatherCondition;
    private String updateForcastEvery;

    private ArrayList<String> updateForcastEveryOptions = new ArrayList<String>(Arrays.asList("Hourly","6 Hours","12 Hours","24 Hours","2 Days"));

    private String locationType="ipAddress";
    private String location = "auto";
    private String latLocation;
    private String longLocation;

    public TriggerByWeather(){

    }

    public TriggerByWeather(String newWhenTempretureIs, String newWhenTempretureIsLessThan, String newWhenTempretureIsGreaterThan, String newBetweenLowEndTempreture, String newBetweenHighEndTempreture, String newWeatherCondition, String newUpdateEvery){
        this.whenTempretureIs = newWhenTempretureIs;
        this.whenTempretureIsLessThan = newWhenTempretureIsLessThan;
        this.whenTempretureIsGreaterThan = newWhenTempretureIsGreaterThan;
        this.betweenLowEndTempreture = newBetweenLowEndTempreture;
        this.betweenHighEndTempreture = newBetweenHighEndTempreture;
        this.weatherCondition = newWeatherCondition;
        this.updateForcastEvery = newUpdateEvery;
    }

    public TriggerByWeather(String splitString){
        String tempString=splitString;
        if(splitString.contains("~triggerTypeDeliminator~")){
            String[] temp = splitString.split("~triggerTypeDeliminator~");
            if(temp.length==1){tempString=temp[0];} else{
                tempString = temp[1];
            }
        }
        String[] triggerDateTimeSplit = tempString.split("~triggerByWeather~");
        if (triggerDateTimeSplit.length>1) {
            if (!triggerDateTimeSplit[0].equals("null")) {
                this.whenTempretureIs = triggerDateTimeSplit[0];
            }
        }
        if (triggerDateTimeSplit.length>2) {
            if (!triggerDateTimeSplit[1].equals("null")) {
                this.whenTempretureIsLessThan = triggerDateTimeSplit[1];
            }
        }
        if (triggerDateTimeSplit.length>2) {
            if (!triggerDateTimeSplit[2].equals("null")) {
                this.whenTempretureIsGreaterThan = triggerDateTimeSplit[2];
            }
        }
        if (triggerDateTimeSplit.length>2) {
            if (!triggerDateTimeSplit[3].equals("null")) {
                this.betweenLowEndTempreture = triggerDateTimeSplit[3];
            }
        }
        if (triggerDateTimeSplit.length>2) {
            if (!triggerDateTimeSplit[4].equals("null")) {
                this.betweenHighEndTempreture = triggerDateTimeSplit[4];
            }
        }
        if (triggerDateTimeSplit.length>2) {
            if (!triggerDateTimeSplit[5].equals("null")) {
                this.weatherCondition = triggerDateTimeSplit[5];
            }
        }
        if (triggerDateTimeSplit.length>2) {
            if (!triggerDateTimeSplit[6].equals("null")) {
                this.updateForcastEvery = triggerDateTimeSplit[6];
            }
        }
        if (triggerDateTimeSplit.length>2) {
            if (!triggerDateTimeSplit[7].equals("null")) {
                this.locationType = triggerDateTimeSplit[7];
            }
        }
        if (triggerDateTimeSplit.length>2) {
            if (!triggerDateTimeSplit[8].equals("null")) {
                this.latLocation = triggerDateTimeSplit[8];
            }
        }
        if (triggerDateTimeSplit.length>2) {
            if (!triggerDateTimeSplit[9].equals("null")) {
                this.longLocation = triggerDateTimeSplit[9];
            }
        }
    }

    public String getUpdateForcastEvery () {
        return updateForcastEvery;
    }

    public ArrayList<String> getUpdateForcastEveryOptions () {
        return updateForcastEveryOptions;
    }

    public void setUpdateForcastEvery (String newUpdateForcastEvery) {
        this.updateForcastEvery = newUpdateForcastEvery;
    }

    public String getWhenTempretureIs() {
        return whenTempretureIs;
    }

    public void setWhenTempretureIs(String whenTempretureIs) {
        this.whenTempretureIs = whenTempretureIs;
    }

    public void setWhenTempretureIs(double whenTempretureIs) {
        this.whenTempretureIs = String.valueOf(whenTempretureIs);
    }

    public String getWhenTempretureIsLessThan() {
        return whenTempretureIsLessThan;
    }

    public void setWhenTempretureIsLessThan(String whenTempretureIsLessThan) {
        this.whenTempretureIsLessThan = whenTempretureIsLessThan;
    }

    public void setWhenTempretureIsLessThan(double whenTempretureIsLessThan) {
        this.whenTempretureIsLessThan = String.valueOf(whenTempretureIsLessThan);
    }

    public String getWhenTempretureIsGreaterThan() {
        return whenTempretureIsGreaterThan;
    }

    public void setWhenTempretureIsGreaterThan(String whenTempretureIsGreaterThan) {
        this.whenTempretureIsGreaterThan = whenTempretureIsGreaterThan;
    }

    public void setWhenTempretureIsGreaterThan(double whenTempretureIsGreaterThan) {
        this.whenTempretureIsGreaterThan = String.valueOf(whenTempretureIsGreaterThan);
    }

    public String getBetweenLowEndTempreture() {
        return betweenLowEndTempreture;
    }

    public void setBetweenLowEndTempreture(String betweenLowEndTempreture) {
        this.betweenLowEndTempreture = betweenLowEndTempreture;
    }

    public void setBetweenLowEndTempreture(double betweenLowEndTempreture) {
        this.betweenLowEndTempreture = String.valueOf(betweenLowEndTempreture);
    }

    public String getBetweenHighEndTempreture() {
        return betweenHighEndTempreture;
    }

    public void setBetweenHighEndTempreture(String betweenHighEndTempreture) {
        this.betweenHighEndTempreture = betweenHighEndTempreture;
    }

    public void setBetweenHighEndTempreture(double betweenHighEndTempreture) {
        this.betweenHighEndTempreture = String.valueOf(betweenHighEndTempreture);
    }

    public String getWeatherCondition() {
        return weatherCondition;
    }

    public void setWeatherCondition(String weatherCondition) {
        this.weatherCondition = weatherCondition;
    }

    public void setLocationType(String newLocationType){
        this.locationType = newLocationType;
    }

    public void setSpecificLocation(String location){
        String[] splitLocation = location.split(",");
        if (splitLocation.length<2) {
            return;
        }
        latLocation = splitLocation[0];
        longLocation = splitLocation[1];

    }

    public String myToString(){
        String returnString = "";
        if (this.whenTempretureIs != null) {
            returnString = returnString + this.whenTempretureIs;
        } else {
            returnString = returnString + "null";
        }
        returnString = returnString + "~triggerByWeather~";
        if (this.whenTempretureIsLessThan != null) {
            returnString = returnString + this.whenTempretureIsLessThan;
        } else {
            returnString = returnString + "null";
        }
        returnString = returnString + "~triggerByWeather~";
        if (this.whenTempretureIsGreaterThan != null) {
            returnString = returnString + this.whenTempretureIsGreaterThan;
        } else {
            returnString = returnString + "null";
        }
        returnString = returnString + "~triggerByWeather~";
        if (this.betweenLowEndTempreture != null) {
            returnString = returnString + this.betweenLowEndTempreture;
        } else {
            returnString = returnString + "null";
        }
        returnString = returnString + "~triggerByWeather~";
        if (this.betweenHighEndTempreture != null) {
            returnString = returnString + this.betweenHighEndTempreture;
        } else {
            returnString = returnString + "null";
        }
        returnString = returnString + "~triggerByWeather~";
        if (this.weatherCondition != null) {
            returnString = returnString + this.weatherCondition;
        } else {
            returnString = returnString + "null";
        }
        returnString = returnString + "~triggerByWeather~";
        if (this.updateForcastEvery != null) {
            returnString = returnString + "~triggerByWeather~" + this.updateForcastEvery;
        } else {
            returnString = returnString + "~triggerByWeather~" + "null";
        }
        returnString = returnString + "~triggerByWeather~";
        if (this.locationType != null) {
            returnString = returnString + "~triggerByWeather~" + this.locationType;
        } else {
            returnString = returnString + "~triggerByWeather~" + "null";
        }
        returnString = returnString + "~triggerByWeather~";
        if (this.latLocation != null) {
            returnString = returnString + "~triggerByWeather~" + this.latLocation;
        } else {
            returnString = returnString + "~triggerByWeather~" + "null";
        }
        returnString = returnString + "~triggerByWeather~";
        return returnString;
    }

    public void removeWeatherTriggersToUpdate(Context context) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
//        for(int i=0;i<this.getRules().size();i++){
//            Intent intent = new Intent(context.getApplicationContext(), AlarmActionReciever.class);
//            PendingIntent pi=null;
//            intent.putExtra("selectedCollection", getId());
//            intent.putExtra("actionIndex",i);
//            pi = PendingIntent.getBroadcast(context,getIdAsInt()+1001+i, intent
//                    , PendingIntent.FLAG_MUTABLE);
//
//            if(pi!=null){
//                am.cancel(pi);
//            }
//        }
    }

    public void setWeatherTriggersToUpdate(Context context){
        if (locationType.equals("ipAddress")) {
            Pair<String,String> latLong = getLatLonFromIp();
            if (latLong == null) {
                return;
            }
            String url = getWeatherUrl(latLong);
            JSONArray weatherJsonArray= getWeatherForcast(url);

            setUpWeatherPredictionTriggers(weatherJsonArray);



        }
        if (locationType.equals("specificLocation")) {
            String url = getWeatherUrl(new Pair<>(latLocation,longLocation));
            JSONArray weatherJsonArray = getWeatherForcast(url);
        }
        if (locationType.equals("currentLocation")) {
            Pair<String,String> latLong = getLatLonLocationFromGps(context);
            String url = getWeatherUrl(latLong);
            JSONArray weatherJsonArray = getWeatherForcast(url);
        }


    }

    public void setUpWeatherPredictionTriggers(JSONArray weatherDays) {
        if (weatherDays == null) {
            return;
        }

//        ArrayList<String> oneDay

        updateForcastEveryOptions.indexOf("24 Hours");

        int numberOfDaysToPredict = 0;
//        int currentDaysPrediction = 0;
//        for (int i = 0; i < weatherDays.length(); i++) {
//            try {
//
//            }
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }

    }

    public Pair<String,String> getLatLonFromIp(){
        Pair<String,String> latLongPair = null;
        String ipAddress = getIPAddress(true);
        // get a json object from http://ip-api.com/json/
        String addressToGet = "https://ipapi.co/" + ipAddress + "/latlong/";

        // make a request to get the json object using okhttp
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(addressToGet)
                .build();

        try (Response response = client.newCall(request).execute()) {
            String resonseString = response.body().toString();
            String[] latLong = resonseString.split(",");
             latLongPair = new Pair<>(latLong[0],latLong[1]);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return latLongPair;
    }


    public String getWeatherUrl(Pair<String,String> latLong){
        String getWeatherUrlForCooridinates = null;
        String addressToGet = "https://api.weather.gov/points/" + latLong.first + "," + latLong.second;
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(addressToGet)
                .build();

        try (Response response = client.newCall(request).execute()) {
            // convert response to JSONObject
            String resonseString = response.body().toString();
            JSONObject returnJson = new JSONObject(resonseString);
            getWeatherUrlForCooridinates = (String) returnJson.get("properties.forecast");


        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return getWeatherUrlForCooridinates;
    }

public JSONArray getWeatherForcast(String weatherUrl){
    JSONArray weatherForcast = null;
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(weatherUrl)
                .build();

        try (Response response = client.newCall(request).execute()) {
            // convert response to JSONObject
            String resonseString = response.body().toString();
            JSONObject returnJson = new JSONObject(resonseString);
            weatherForcast =  returnJson.getJSONArray ("properties.periods");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return weatherForcast;
    }

    public static String getIPAddress(boolean useIPv4) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress();
                        //boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
                        boolean isIPv4 = sAddr.indexOf(':')<0;

                        if (useIPv4) {
                            if (isIPv4)
                                return sAddr;
                        } else {
                            if (!isIPv4) {
                                int delim = sAddr.indexOf('%'); // drop ip6 zone suffix
                                return delim<0 ? sAddr.toUpperCase() : sAddr.substring(0, delim).toUpperCase();
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) { } // for now eat exceptions
        return "";
    }

    public Pair getLatLonLocationFromGps(Context context){
        final int REQUEST_PERMISSIONS_REQUEST_CODE = 1;
        if (ActivityCompat.checkSelfPermission(
                context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(context, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSIONS_REQUEST_CODE);
        } else {
            Location location = ((LocationManager) context.getSystemService(Context.LOCATION_SERVICE)).getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location != null) {
                latLocation = String.valueOf(location.getLatitude());
                longLocation = String.valueOf(location.getLongitude());
            }
        }
        return new Pair(latLocation,longLocation);
    }
}
