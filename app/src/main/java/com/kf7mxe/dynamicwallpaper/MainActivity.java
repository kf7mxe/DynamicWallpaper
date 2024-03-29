package com.kf7mxe.dynamicwallpaper;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.color.DynamicColors;
import com.kf7mxe.dynamicwallpaper.databinding.ActivityMainBinding;
import com.kf7mxe.dynamicwallpaper.utilis.DynamicColorUtils;
import com.kf7mxe.dynamicwallpaper.viewmodels.CollectionViewModel;

import android.os.Debug;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import android.Manifest;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // if device is running android 12
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {



            DynamicColorUtils dynamicColorUtils = new DynamicColorUtils();
            dynamicColorUtils.setDynamicColor(binding.getRoot(),this);

        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence channelName = "My Channel";
            String channelDescription = "My Channel Description";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("channel_id", channelName, importance);
            channel.setDescription(channelDescription);

            // Register the channel with the system
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

//        String addressToGet = "https://ipapi.co/24.48.0.1/latlong/";
//
//        // make a request to get the json object using okhttp
//        OkHttpClient client = new OkHttpClient();
//        // make a request with a get request method and the address to get
//        Request request = new Request.Builder()
//                .url(addressToGet)
//                .build();


//        super.setTheme();



        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();

        Objects.requireNonNull(getSupportActionBar()).setHomeAsUpIndicator(R.drawable.round_arrow_back_24);



        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);





    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.deleteTopNavButton) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

    // on destroy
    @Override
    protected void onDestroy() {
        super.onDestroy();
        CollectionViewModel collectionViewModel =new CollectionViewModel(getApplication(),this);
        collectionViewModel.deleteAllItemsInCache();
    }




}