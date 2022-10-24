package com.kf7mxe.dynamicwallpaper;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.kf7mxe.dynamicwallpaper.databinding.FragmentTriggerByWeatherBinding;

import java.util.ArrayList;

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

        bindings.weatherRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            switch (checkedId) {
                case R.id.whenTempretureIsRadio:
                    bindings.whenTempretureIsEditText.setVisibility(View.VISIBLE);
                    bindings.whenTempretureIsLessThanEditText.setVisibility(View.GONE);
                    bindings.whenTempretureIsGreaterThanEditText.setVisibility(View.GONE);
                    bindings.whenTempretureIsBetweenLinearLayout.setVisibility(View.GONE);
                    bindings.whenItIsWeatherType.setVisibility(View.GONE);
                    break;
                case R.id.whenTempretureIsLessThan:
                    bindings.whenTempretureIsLessThanEditText.setVisibility(View.VISIBLE);
                    bindings.whenTempretureIsEditText.setVisibility(View.GONE);
                    bindings.whenTempretureIsGreaterThanEditText.setVisibility(View.GONE);
                    bindings.whenTempretureIsBetweenLinearLayout.setVisibility(View.GONE);
                    bindings.whenItIsWeatherType.setVisibility(View.GONE);
                    break;
                case R.id.whenTempretureIsGreaterThanRadio:
                    bindings.whenTempretureIsGreaterThanEditText.setVisibility(View.VISIBLE);
                    bindings.whenTempretureIsEditText.setVisibility(View.GONE);
                    bindings.whenTempretureIsLessThanEditText.setVisibility(View.GONE);
                    bindings.whenTempretureIsBetweenLinearLayout.setVisibility(View.GONE);
                    bindings.whenItIsWeatherType.setVisibility(View.GONE);
                    break;
                case R.id.whenTempretureIsBetweenRadio:
                    bindings.whenTempretureIsBetweenLinearLayout.setVisibility(View.VISIBLE);
                    bindings.whenTempretureIsEditText.setVisibility(View.GONE);
                    bindings.whenTempretureIsLessThanEditText.setVisibility(View.GONE);
                    bindings.whenTempretureIsGreaterThanEditText.setVisibility(View.GONE);
                    bindings.whenItIsWeatherType.setVisibility(View.GONE);
                    break;
                case R.id.whenItIsRadioRadio:
                    bindings.whenItIsWeatherType.setVisibility(View.VISIBLE);
                    bindings.whenTempretureIsEditText.setVisibility(View.GONE);
                    bindings.whenTempretureIsLessThanEditText.setVisibility(View.GONE);
                    bindings.whenTempretureIsGreaterThanEditText.setVisibility(View.GONE);
                    bindings.whenTempretureIsBetweenLinearLayout.setVisibility(View.GONE);
                    break;
            }
        });

        bindings.whenTempretureIsEditText.getText().toString();
        bindings.whenTempretureIsLessThanEditText.getText().toString();
        bindings.whenTempretureIsGreaterThanEditText.getText().toString();
        bindings.lowEndTempretureEditText.getText().toString();
        bindings.highEndTempretureEditText.getText().toString();

        ArrayList<String> weatherTypes = new ArrayList<>();
        weatherTypes.add("Clear");
        weatherTypes.add("Clouds");
        weatherTypes.add("Rain");

        bindings.whenItIsWeatherType.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, weatherTypes));

        ArrayList<String> updateEveryTime = new ArrayList<>();
        updateEveryTime.add("1 hour");
        updateEveryTime.add("5 hours");
        updateEveryTime.add("12 hours");
        updateEveryTime.add("24 hours");
        updateEveryTime.add("12 hours when plugged in");

        bindings.updateForcasdEverySpinner.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, updateEveryTime));

        bindings.goToActionsFromDateTrigger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navController.navigate(R.id.action_triggerByWeatherFragment_to_selectActionsFragment);
            }
        });
        return bindings.getRoot();
    }
}
