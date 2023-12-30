package com.kf7mxe.dynamicwallpaper;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.snackbar.Snackbar;
import com.kf7mxe.dynamicwallpaper.databinding.FragmentTriggerByWeatherBinding;
import com.kf7mxe.dynamicwallpaper.models.TriggerByWeather;

import java.util.ArrayList;
import java.util.Objects;

public class TriggerByWeatherFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String tempretureIs;
    private String tempretureLessThan;
    private String tempretureGreaterThan;
    private String tempretureBetweenLowerEnd;
    private String tempretureBetweenUpperEnd;
    private String tempretureUpdateEveryTime;
    private String tempretureItIsWeatherType;

    private String weatherLocationTypeIs;

    private ArrayList<String> weatherTypes = new ArrayList<>();

    private FragmentTriggerByWeatherBinding bindings;
    private NavController navController;

    public TriggerByWeatherFragment() {
        // Required empty public constructor
    }

    public static TriggerByWeatherFragment newInstance(String param1, String param2) {
        TriggerByWeatherFragment fragment = new TriggerByWeatherFragment();
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
            String mParam1 = getArguments().getString(ARG_PARAM1);
            String mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        bindings = FragmentTriggerByWeatherBinding.inflate(inflater, container, false);
        navController = NavHostFragment.findNavController(this);
        TriggerByWeather triggerByWeather = new TriggerByWeather();

        bindings.weatherRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            switch (checkedId) {
                case R.id.whenTempretureIsRadio:
                    bindings.whenTempretureIsEditText.setVisibility(View.VISIBLE);
                    bindings.whenTempretureIsLessThanEditText.setVisibility(View.GONE);
                    bindings.whenTempretureIsGreaterThanEditText.setVisibility(View.GONE);
                    bindings.whenTempretureIsBetweenLinearLayout.setVisibility(View.GONE);
                    bindings.whenItIsWeatherCondition.setVisibility(View.GONE);
                    break;
                case R.id.whenTempretureIsLessThan:
                    bindings.whenTempretureIsLessThanEditText.setVisibility(View.VISIBLE);
                    bindings.whenTempretureIsEditText.setVisibility(View.GONE);
                    bindings.whenTempretureIsGreaterThanEditText.setVisibility(View.GONE);
                    bindings.whenTempretureIsBetweenLinearLayout.setVisibility(View.GONE);
                    bindings.whenItIsWeatherCondition.setVisibility(View.GONE);
                    break;
                case R.id.whenTempretureIsGreaterThanRadio:
                    bindings.whenTempretureIsGreaterThanEditText.setVisibility(View.VISIBLE);
                    bindings.whenTempretureIsEditText.setVisibility(View.GONE);
                    bindings.whenTempretureIsLessThanEditText.setVisibility(View.GONE);
                    bindings.whenTempretureIsBetweenLinearLayout.setVisibility(View.GONE);
                    bindings.whenItIsWeatherCondition.setVisibility(View.GONE);
                    break;
                case R.id.whenTempretureIsBetweenRadio:
                    bindings.whenTempretureIsBetweenLinearLayout.setVisibility(View.VISIBLE);
                    bindings.whenTempretureIsEditText.setVisibility(View.GONE);
                    bindings.whenTempretureIsLessThanEditText.setVisibility(View.GONE);
                    bindings.whenTempretureIsGreaterThanEditText.setVisibility(View.GONE);
                    bindings.whenItIsWeatherCondition.setVisibility(View.GONE);
                    break;
                case R.id.whenWeatherConditionsRadio:
                    bindings.whenItIsWeatherCondition.setVisibility(View.VISIBLE);
                    bindings.whenTempretureIsEditText.setVisibility(View.GONE);
                    bindings.whenTempretureIsLessThanEditText.setVisibility(View.GONE);
                    bindings.whenTempretureIsGreaterThanEditText.setVisibility(View.GONE);
                    bindings.whenTempretureIsBetweenLinearLayout.setVisibility(View.GONE);
                    break;
            }
        });


        bindings.locationTypeRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            switch (checkedId) {
                case R.id.ipAddress:
                    weatherLocationTypeIs = "ipAddress";
                    break;
                case R.id.currentLocation:
                    weatherLocationTypeIs = "currentLocation";
                    break;
                case R.id.specificSetLocation:
                    weatherLocationTypeIs = "specificSetLocation";
                    break;
            }
        });

        bindings.whenTempretureIsEditText.getText().toString();
        bindings.whenTempretureIsLessThanEditText.getText().toString();
        bindings.whenTempretureIsGreaterThanEditText.getText().toString();
        bindings.lowEndTempretureEditText.getText().toString();
        bindings.highEndTempretureEditText.getText().toString();


        weatherTypes.add("Clear");
        weatherTypes.add("Mostly Clear");
        weatherTypes.add("Clouds");
        weatherTypes.add("Mostly Clouds");
        weatherTypes.add("Rain");
        weatherTypes.add("Light Rain");
        weatherTypes.add("Snow");
        weatherTypes.add("Light Snow");
        weatherTypes.add("Mostly Sunny");
        weatherTypes.add("Sunny");
        weatherTypes.add("Rain And Snow");
        weatherTypes.add("Thunderstorm");
        weatherTypes.add("Fog");
        weatherTypes.add("Windy");


        bindings.whenItIsWeatherCondition.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, weatherTypes));

        ArrayList<String> updateEveryTime =  triggerByWeather.getUpdateForcastEveryOptions();

        bindings.updateForcasdEverySpinner.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, updateEveryTime));

        bindings.goToActionsFromDateTrigger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (requiredFieldsFilled()) {
                    Bundle bundle = getArguments();
                    if (bindings.whenTempretureIsRadio.isChecked()) {
                        triggerByWeather.setWhenTempretureIs(bindings.whenTempretureIsEditText.getText().toString());
                    } else if (bindings.whenTempretureIsLessThan.isChecked()) {
                        triggerByWeather.setWhenTempretureIsLessThan(bindings.whenTempretureIsLessThanEditText.getText().toString());
                    } else if (bindings.whenTempretureIsGreaterThanRadio.isChecked()) {
                        triggerByWeather.setWhenTempretureIsGreaterThan(bindings.whenTempretureIsGreaterThanEditText.getText().toString());
                    } else if (bindings.whenTempretureIsBetweenRadio.isChecked()) {
                        triggerByWeather.setBetweenHighEndTempreture(bindings.highEndTempretureEditText.getText().toString());
                        triggerByWeather.setBetweenLowEndTempreture(bindings.lowEndTempretureEditText.getText().toString());
                    } else if (bindings.whenWeatherConditionsRadio.isChecked()) {
                        triggerByWeather.setWeatherCondition(bindings.whenItIsWeatherCondition.getSelectedItem().toString());
                    }
                    if (weatherLocationTypeIs != null) {
                        triggerByWeather.setLocationType(weatherLocationTypeIs);
                        if (weatherTypes.equals("specificSetLocation")) {
                            triggerByWeather.setSpecificLocation(bindings.specificSetLocationEditText.getText().toString());
                        }
                    }

                    triggerByWeather.setUpdateForcastEvery(bindings.updateForcasdEverySpinner.getSelectedItem().toString());

                    assert bundle != null;
                    bundle.putString("Trigger",triggerByWeather.myToString());
                    bundle.putString("TriggerType","triggerByWeather");
                    navController.navigate(R.id.action_triggerByWeatherFragment_to_selectActionsFragment,bundle);
                } else {
                    Snackbar.make(bindings.getRoot(), "Please fill all required fields", Snackbar.LENGTH_LONG).show();
                }
            }
        });
        return bindings.getRoot();
    }

    public boolean requiredFieldsFilled(){
        if (bindings.whenTempretureIsEditText.getText().toString().isEmpty() && bindings.whenTempretureIsLessThanEditText.getText().toString().isEmpty() && bindings.whenTempretureIsGreaterThanEditText.getText().toString().isEmpty() && bindings.lowEndTempretureEditText.getText().toString().isEmpty() && bindings.highEndTempretureEditText.getText().toString().isEmpty() && bindings.whenItIsWeatherCondition.getSelectedItem().toString().isEmpty()){
            Toast.makeText(getContext(), "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (weatherTypes.equals("specificSetLocation")) {
            if (bindings.specificSetLocationEditText.getText().toString().isEmpty()) {
                Toast.makeText(getContext(), "Please fill all required fields", Toast.LENGTH_SHORT).show();
                return false;
            }
            if (bindings.specificSetLocationEditText.getText().toString().split(",").length != 2) {
                Toast.makeText(getContext(), "Please enter a valid location", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        return true;
    }
}
