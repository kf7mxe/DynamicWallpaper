package com.kf7mxe.dynamicwallpaper;

import android.app.TimePickerDialog;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TimePicker;

import com.google.android.material.snackbar.Snackbar;
import com.kf7mxe.dynamicwallpaper.databinding.FragmentTriggerByTimeIntervalBinding;
import com.kf7mxe.dynamicwallpaper.models.TriggerByTimeInterval;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TriggerByTimeIntervalFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TriggerByTimeIntervalFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private FragmentTriggerByTimeIntervalBinding bindings;
    private FragmentManager fragmentManager;

    private String timeToTrigger="none";

    private NavController navController;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public TriggerByTimeIntervalFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment TriggerByDateTimeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TriggerByTimeIntervalFragment newInstance(String param1, String param2) {
        TriggerByTimeIntervalFragment fragment = new TriggerByTimeIntervalFragment();
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
        // Inflate the layout for this fragment
        bindings = FragmentTriggerByTimeIntervalBinding.inflate(getLayoutInflater());
        fragmentManager = getActivity().getSupportFragmentManager();
        navController = NavHostFragment.findNavController(this);

        ArrayAdapter<CharSequence> adapter=ArrayAdapter.createFromResource(getContext(), R.array.repeat_interval_type, android.R.layout.simple_spinner_dropdown_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        bindings.repeatIntervalTypeSpinner.setAdapter(adapter);
        bindings.repeatIntervalTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position){
                    case 0:
                    case 1:
                    case 2:
                        bindings.repeatDayOfWeekChipGroup.setVisibility(View.GONE);
                        bindings.repeateDayOfWeekTitle.setVisibility(View.GONE);
                        bindings.dateToChangeOn.setVisibility(View.GONE);
                        bindings.monthlyDayToChangeOn.setVisibility(View.GONE);
                        break;
                    case 3:
                        bindings.repeatDayOfWeekChipGroup.setVisibility(View.VISIBLE);
                        bindings.repeateDayOfWeekTitle.setVisibility(View.VISIBLE);
                        bindings.dateToChangeOn.setVisibility(View.GONE);
                        bindings.monthlyDayToChangeOn.setVisibility(View.GONE);
                        break;
                    case 4:
                        bindings.repeatDayOfWeekChipGroup.setVisibility(View.GONE);
                        bindings.repeateDayOfWeekTitle.setVisibility(View.GONE);
//                        bindings.dateToChangeOn.setVisibility(View.VISIBLE);
//                        bindings.monthlyDayToChangeOn.setVisibility(View.VISIBLE);
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        bindings.timeToTriggerInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){timePickerListener();}

            }
        });


        bindings.goToActionsFromTimeIntervalTrigger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(requiredFieldsFilled()) {
                    Bundle bundle = getArguments();
                    TriggerByTimeInterval trigger = getInputedDated();
                    bundle.putString("Trigger",trigger.myToString());
                    bundle.putString("TriggerType","triggerByTimeInterval");
                    navController.navigate(R.id.action_triggerByDateTimeFragment_to_selectActionsFragment, bundle);
                } else {
                    Snackbar.make(bindings.getRoot(),"Fill required Fields",Snackbar.LENGTH_LONG).setAnchorView(bindings.timeToTriggerInput).show();
                }
            }
        });

        return bindings.getRoot();
    }

    public TriggerByTimeInterval getInputedDated(){
        String enteredRepeatIntervalAmountInput = bindings.enterRepeatIntervalAmountInput.getText().toString();
        String enteredRepeatIntervalTypeSpinner = bindings.repeatIntervalTypeSpinner.getSelectedItem().toString();
        //timeToTrigger;
        List<Integer> checkedIds = bindings.repeatDayOfWeekChipGroup.getCheckedChipIds();
        String daysOfWeeks = "";
        for(int ids:checkedIds){
            daysOfWeeks = daysOfWeeks + getResources().getResourceEntryName(ids).replace("Chip","")+",";
        }
        if(daysOfWeeks.length()==0){
            daysOfWeeks="none";
        }
        return new TriggerByTimeInterval(enteredRepeatIntervalAmountInput,enteredRepeatIntervalTypeSpinner,timeToTrigger,daysOfWeeks,Boolean.toString(bindings.exactInterval.isChecked()));
    }

    public boolean requiredFieldsFilled(){
        if(bindings.enterRepeatIntervalAmountInput.getText().toString().length()==0 || bindings.enterRepeatIntervalAmountInput.getText().toString()==null ||bindings.repeatIntervalTypeSpinner.getSelectedItem().toString().length()==0 || bindings.repeatIntervalTypeSpinner.getSelectedItem().toString() ==null ){
            return false;
        } else {
            return true;
        }
    }

    public void timePickerListener(){
        TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hourOfDay, int minutes) {
                timeToTrigger = hourOfDay+":"+minutes;
                bindings.timeToTriggerInput.setText(timeToTrigger);
            }
        }, 0, 0, false);
        timePickerDialog.show();
    }

}