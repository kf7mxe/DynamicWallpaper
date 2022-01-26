package com.kf7mxe.dynamicwallpaper;

import android.app.DatePickerDialog;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.kf7mxe.dynamicwallpaper.databinding.FragmentTriggerByDateBinding;
import com.kf7mxe.dynamicwallpaper.models.TriggerByDate;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TriggerByDateFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TriggerByDateFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private FragmentTriggerByDateBinding bindings;
    private FragmentManager fragmentManager;

    private NavController navController;


    public TriggerByDateFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment TriggerByDateFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TriggerByDateFragment newInstance(String param1, String param2) {
        TriggerByDateFragment fragment = new TriggerByDateFragment();
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
        bindings = FragmentTriggerByDateBinding.inflate(getLayoutInflater());
        fragmentManager = getActivity().getSupportFragmentManager();
        navController = NavHostFragment.findNavController(this);

        bindings.dateTriggerDateIconImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setDateFromDateSelectorDialog(bindings.dateTextTriggerDateTextView);
            }
        });

        bindings.goToActionsFromDateTrigger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(),"test "+bindings.dateTextTriggerDateTextView.getText().toString().split(" ").length,Toast.LENGTH_LONG);
                if(requiredFieldsFilled()){

                    String[] dateTrigger = bindings.dateTextTriggerDateTextView.getText().toString().split(" ");
                    Bundle bundle = getArguments();

                    TriggerByDate trigger = new TriggerByDate();
                    if(dateTrigger.length>=2){
                        trigger.setM_date(dateTrigger[0]);
                        trigger.setM_month(dateTrigger[1]);
                    }
                    bundle.putString("Trigger",trigger.myToString());
                    bundle.putString("TriggerType","triggerByDate");
                    navController.navigate(R.id.action_triggerByDateFragment_to_selectActionsFragment,bundle);
                } else {
                    Snackbar.make(bindings.getRoot(),"Fill Required Fields",Snackbar.LENGTH_LONG).setAnchorView(bindings.divider2);
                }
            }
        });
        return bindings.getRoot();
    }

    private boolean requiredFieldsFilled(){
                String stringTest = bindings.dateTextTriggerDateTextView.getText().toString();
                String[] testArray = bindings.dateTextTriggerDateTextView.getText().toString().split(" ");
                if(bindings.dateTextTriggerDateTextView.getText().toString().split(" ").length!=0){
                    return true;
                }
                return false;
    }

    private void setDateFromDateSelectorDialog(TextView textView){
        Calendar mcurrentDate = Calendar.getInstance();
        int mYear = mcurrentDate.get(Calendar.YEAR);
        int mMonth = mcurrentDate.get(Calendar.MONTH);
        int mDay = mcurrentDate.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog mDatePicker = new DatePickerDialog(getActivity() , new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datepicker, int selectedyear, int selectedmonth, int selectedday) {
                Calendar myCalendar = Calendar.getInstance();
                myCalendar.set(Calendar.YEAR, selectedyear);
                myCalendar.set(Calendar.MONTH, selectedmonth);
                myCalendar.set(Calendar.DAY_OF_MONTH, selectedday);
                String myFormat = "MMM dd"; //Change as you need
                SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
                textView.setText(sdf.format(myCalendar.getTime()));
            }
        }, mYear, mMonth, mDay);
        //mDatePicker.setTitle("Select date");
        mDatePicker.show();
    }


}