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

import com.kf7mxe.dynamicwallpaper.databinding.FragmentBySeasonBinding;
import com.kf7mxe.dynamicwallpaper.models.TriggerBySeason;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link BySeasonFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BySeasonFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private FragmentBySeasonBinding bindings;
    private FragmentManager fragmentManager;

    private TriggerBySeason trigger;

    private NavController navController;
    public BySeasonFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment BySeasonFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static BySeasonFragment newInstance(String param1, String param2) {
        BySeasonFragment fragment = new BySeasonFragment();
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
        bindings = FragmentBySeasonBinding.inflate(getLayoutInflater());
        fragmentManager = getActivity().getSupportFragmentManager();
        navController = NavHostFragment.findNavController(this);

        trigger = new TriggerBySeason();

        bindings.winterStartImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setDateFromDateSelectorDialog(bindings.winterStarttextView);
            }
        });
        bindings.winterendImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setDateFromDateSelectorDialog(bindings.winterStart1textView);
            }
        });

        bindings.springStartImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setDateFromDateSelectorDialog(bindings.springStarttextView);
            }
        });
        bindings.springEndImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setDateFromDateSelectorDialog(bindings.springEndTextView11);
            }
        });
        bindings.summerStartImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setDateFromDateSelectorDialog(bindings.summerStartTextView);
            }
        });

        bindings.summerEndImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setDateFromDateSelectorDialog(bindings.summerEndTextView);
            }
        });

        bindings.fallStartimageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setDateFromDateSelectorDialog(bindings.fallStartDatetextView);
            }
        });

        bindings.fallEndimageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setDateFromDateSelectorDialog(bindings.fallEndtextView);
            }
        });

        bindings.goToActionsFromSelectBySeasonTrigger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDates();
                Bundle bundle = getArguments();
                bundle.putString("Trigger",trigger.myToString());
                bundle.putString("TriggerType","triggerBySeason");
                navController.navigate(R.id.action_bySeasonFragment_to_selectActionsFragment,bundle);
            }
        });
        // Inflate the layout for this fragment
        return bindings.getRoot();
    }

    public void getDates(){
        if(bindings.winterCheckBox.isChecked()){
            String[] winterStartTemp = bindings.winterStart1textView.getText().toString().split(" ");
            String[] winterEndTemp = bindings.winterStart1textView.getText().toString().split(" ");
            trigger.addSeason("winter",winterStartTemp[0],winterStartTemp[1],winterEndTemp[0],winterEndTemp[1]);
        }
        if(bindings.springCheckBox.isChecked()){
            String[] springStartTemp = bindings.springStarttextView.getText().toString().split(" ");
            String[] springEndTemp = bindings.springEndTextView11.getText().toString().split(" ");
            trigger.addSeason("spring",springStartTemp[0],springStartTemp[1],springEndTemp[0],springEndTemp[1]);
        }
        if(bindings.fallCheckBox.isChecked()){
            String[] fallStartTemp = bindings.fallStartDatetextView.getText().toString().split(" ");
            String[] fallEndTemp = bindings.fallEndtextView.getText().toString().split(" ");
            trigger.addSeason("fall",fallStartTemp[0],fallStartTemp[1],fallEndTemp[0],fallEndTemp[1]);
        }
        if(bindings.summerCheckbox.isChecked()){
            String[] summerStartTemp = bindings.summerStartTextView.getText().toString().split(" ");
            String[] summerEndTemp = bindings.summerEndTextView.getText().toString().split(" ");
            trigger.addSeason("summer",summerStartTemp[0],summerStartTemp[1],summerEndTemp[0],summerEndTemp[1]);
        }
    }

    public void setDateFromDateSelectorDialog(TextView textView){
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