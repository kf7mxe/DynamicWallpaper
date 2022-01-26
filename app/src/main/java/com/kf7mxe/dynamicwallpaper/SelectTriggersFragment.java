package com.kf7mxe.dynamicwallpaper;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kf7mxe.dynamicwallpaper.databinding.FragmentSelectTriggersBinding;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SelectTriggersFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SelectTriggersFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private FragmentSelectTriggersBinding bindings;
    private FragmentManager fragmentManager;

    private NavController navController;

    public SelectTriggersFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SelectTriggersFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SelectTriggersFragment newInstance(String param1, String param2) {
        SelectTriggersFragment fragment = new SelectTriggersFragment();
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
        bindings = FragmentSelectTriggersBinding.inflate(getLayoutInflater());
        fragmentManager = getActivity().getSupportFragmentManager();
        navController = NavHostFragment.findNavController(this);
        // Inflate the layout for this fragment


        bindings.goToTriggerSetUpFromSeasonTriggerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int id = bindings.triggersRadioGroup.getCheckedRadioButtonId();
                switch (id){
                    case R.id.selectTriggersDateTimeRadio:
                        navController.navigate(R.id.action_selectTriggersFragment_to_triggerByDateTimeFragment,getArguments());
                        break;
                    case R.id.selectBySeasonTriggerRadio:
                        navController.navigate(R.id.action_selectTriggersFragment_to_bySeasonFragment,getArguments());
                        break;
                    case R.id.selectDateTriggerRadio:
                        navController.navigate(R.id.action_selectTriggersFragment_to_triggerByDateFragment,getArguments());
                        break;
                    case R.id.selectLocationTriggerRadio:
                        break;
                    case R.id.selectCalendarEventTriggerRadio:
                        break;
                    case R.id.selectTempretureTriggerRadio:
                        break;
                    default:
                }
            }
        });

        return bindings.getRoot();
    }
}