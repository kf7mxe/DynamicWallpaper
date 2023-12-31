package com.kf7mxe.dynamicwallpaper;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.kf7mxe.dynamicwallpaper.databinding.FragmentTriggerLocationBinding;
import com.kf7mxe.dynamicwallpaper.models.TriggerByLocation;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TriggerLocationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TriggerLocationFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private boolean runningQOrLater = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q;
    private final int REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE = 33;
    private final int REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 34;
    private MapView mMapView;

    private FragmentTriggerLocationBinding binding;

    private final int REQUEST_PERMISSIONS_REQUEST_CODE = 1;

    private LocationManager locationManager;

    private NavController navController;

    IMapController mapController;

    double latitude, longitude;

    private String endEnterTrigger = "enter";


    public TriggerLocationFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment TriggerLocationFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TriggerLocationFragment newInstance(String param1, String param2) {
        TriggerLocationFragment fragment = new TriggerLocationFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentTriggerLocationBinding.inflate(getLayoutInflater());
        navController = NavHostFragment.findNavController(this);

        int screenWidth = getResources().getDisplayMetrics().widthPixels/2;

        binding.radiusCircle.getLayoutParams().width = screenWidth;
        binding.radiusCircle.getLayoutParams().height = screenWidth;

        Context ctx = getContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

        binding.map.setTileSource(TileSourceFactory.MAPNIK);

        requestPermissionsIfNecessary(new String[]{
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
        });


        binding.getMyLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    getLocation();
                    GeoPoint currentLocation = new GeoPoint(latitude, longitude);
                    mapController = binding.map.getController();
                    mapController.setCenter(currentLocation);
                    mapController.setZoom(15);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        binding.map.setBuiltInZoomControls(true);
        binding.map.setMultiTouchControls(true);

        MyLocationNewOverlay myLocationNewOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(getContext()),binding.map);
        myLocationNewOverlay.enableMyLocation();

        binding.enterExitTriggerGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if(checkedId == R.id.enterTrigger){
                endEnterTrigger = "enter";
            }else if(checkedId == R.id.exitTrigger){
                endEnterTrigger = "exit";
            }
        });

        binding.goToActionsFromDateTrigger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = getArguments();
                TriggerByLocation triggerByLocation = new TriggerByLocation();
                triggerByLocation.setLatitude(latitude);
                triggerByLocation.setLongitude(longitude);
                triggerByLocation.setRadius(Double.toString(getRadius()));
                triggerByLocation.setEndEnterTrigger(endEnterTrigger);
                bundle.putString("TriggerType", "triggerByLocation");
                bundle.putString("Trigger", triggerByLocation.myToString());
                navController.navigate(R.id.action_triggerLocationFragment_to_selectActionsFragment, bundle);
            }
        });

        return binding.getRoot();
    }

    //get radius for location from center of map
    public double getRadius(){
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        IGeoPoint center = binding.map.getMapCenter();
        IGeoPoint edgeOfScreen = binding.map.getProjection().fromPixels(screenWidth,0);
        return getDistanceBetweenTwoPointsInMeters(center.getLatitude(),center.getLongitude(),edgeOfScreen.getLatitude(),edgeOfScreen.getLongitude());
    }

    public double getDistanceBetweenTwoPointsInMeters(double lat1, double lon1, double lat2, double lon2){
        Location locationA = new Location("point A");
        locationA.setLatitude(lat1);
        locationA.setLongitude(lon1);
        Location locationB = new Location("point B");
        locationB.setLatitude(lat2);
        locationB.setLongitude(lon2);
        double distance = locationA.distanceTo(locationB);
        return distance;
    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
//        ArrayList<String> permissionsToRequest = new ArrayList<>();
//        for (int i = 0; i < grantResults.length; i++) {
//            permissionsToRequest.add(permissions[i]);
//        }
//        if (permissionsToRequest.size() > 0) {
//            ActivityCompat.requestPermissions(
//                    getActivity(),
//                    permissionsToRequest.toArray(new String[0]),
//                    REQUEST_PERMISSIONS_REQUEST_CODE);
//        }
//    }


    private ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(),
                    isGranted -> {
                        if (isGranted) {
                            // Permission granted, access location
                            // ... your location-related code
                        } else {
                            // Permission denied, handle the denial
                            // ... inform the user or disable location features
                        }
                    });


    private void requestPermissionsIfNecessary(String[] permissions) {

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        } else {
            Toast.makeText(getContext(), "Location permission is required to use this app", Toast.LENGTH_SHORT).show();
            // Permission already granted, access location
            // ... your location-related code
                        locationManager = (LocationManager) requireActivity().getSystemService(Context.LOCATION_SERVICE);

            getLocation();
                        GeoPoint currentLocation = new GeoPoint(latitude, longitude);
                        mapController = binding.map.getController();
                        mapController.setCenter(currentLocation);
                        mapController.setZoom(15);
        }

        // request permissions
//        registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),
//                result -> {
//                    if (result == null) {
//                        return;
//                    } else {
//                        if (result.get(Manifest.permission.ACCESS_FINE_LOCATION) && result.get(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
//                            // Permission is granted
////                        getLocation();
////                        GeoPoint currentLocation = new GeoPoint(latitude, longitude);
////                        mapController = binding.map.getController();
////                        mapController.setCenter(currentLocation);
////                        mapController.setZoom(15);
//                        } else {
//                            // Permission is denied
//                            Toast.makeText(getContext(), "Location permission is required to use this app", Toast.LENGTH_SHORT).show();
//                            OnGPS();
//                        }
//                    }
//                }
//        ).launch(permissions);



//        ArrayList<String> permissionsToRequest = new ArrayList<>();
//        for (String permission : permissions) {
//            if (ContextCompat.checkSelfPermission(getContext(), permission)
//                    != PackageManager.PERMISSION_GRANTED) {
//                // Permission is not granted
//                permissionsToRequest.add(permission);
//            }
//        }
//        if (permissionsToRequest.size() > 0) {
//            String[] permissionsToRequestArray = new String[permissionsToRequest.size()];
//            permissionsToRequestArray = permissionsToRequest.toArray(permissionsToRequestArray);
//                registerForActivityResult(
//                        new ActivityResultContracts.RequestMultiplePermissions(),
//                        result -> {
//                            if (result.get(Manifest.permission.ACCESS_FINE_LOCATION) && result.get(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
//                                // Permission is granted
//                                getLocation();
//                                GeoPoint currentLocation = new GeoPoint(latitude, longitude);
//                                mapController = binding.map.getController();
//                                mapController.setCenter(currentLocation);
//                                mapController.setZoom(15);
//                            } else {
//                                // Permission is denied
//                                Toast.makeText(getContext(), "Location permission is required to use this app", Toast.LENGTH_SHORT).show();
//                                OnGPS();
//                            }
//                        }
//                ).launch(permissionsToRequestArray);
//
//


//        }







//        if (permissionsToRequest.size()==0){
//            // get current location
//            locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
//                getLocation();
//                Toast.makeText(getContext(), "latitude"+latitude, Toast.LENGTH_SHORT).show();
//            GeoPoint currentLocation = new GeoPoint(latitude, longitude);
//
//            mapController = binding.map.getController();
//            mapController.setCenter(currentLocation);
//            mapController.setZoom(15);
//
//        }
    }

    private void OnGPS() {
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
    }
    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireActivity(), Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, REQUEST_PERMISSIONS_REQUEST_CODE);

        } else {
            try {
                Location locationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (locationGPS != null) {
                    double lat = locationGPS.getLatitude();
                    double longi = locationGPS.getLongitude();
                    latitude = locationGPS.getLatitude();
                    longitude = locationGPS.getLongitude();
                } else {
                    Toast.makeText(getContext(), "Unable to find location.", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}