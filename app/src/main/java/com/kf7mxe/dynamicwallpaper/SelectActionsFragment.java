package com.kf7mxe.dynamicwallpaper;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.kf7mxe.dynamicwallpaper.RecyclerAdapters.SelectSpecificWallpaperAdapter;
import com.kf7mxe.dynamicwallpaper.RecyclerAdapters.SelectSubCollectionRecycerAdapter;
import com.kf7mxe.dynamicwallpaper.databinding.FragmentSelectActionsBinding;
import com.kf7mxe.dynamicwallpaper.models.Action;
import com.kf7mxe.dynamicwallpaper.models.Collection;
import com.kf7mxe.dynamicwallpaper.models.Rule;
import com.kf7mxe.dynamicwallpaper.models.Trigger;
import com.kf7mxe.dynamicwallpaper.models.TriggerByDateTime;
import com.kf7mxe.dynamicwallpaper.models.TriggerBySeason;
import com.kf7mxe.dynamicwallpaper.viewmodels.CollectionViewModel;

import java.io.Serializable;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SelectActionsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SelectActionsFragment extends Fragment  {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private FragmentSelectActionsBinding bindings;
    private FragmentManager fragmentManager;

    private NavController navController;

    private SharedPreferences tempSharedPreferences;

    private int selectedSubCollection = -1;
    private String selectedSpecificImage = "";
    private BottomSheetDialog selectSubcollectionBottomSheet;
    private BottomSheetDialog selectSpecificWallpaperBottomSheet;


    private CollectionViewModel viewModel;
    private Trigger trigger;
    private Collection collection;
    public SelectActionsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SelectActionsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SelectActionsFragment newInstance(String param1, String param2) {
        SelectActionsFragment fragment = new SelectActionsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
         viewModel = new CollectionViewModel(getActivity().getApplication(),getContext());
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
            Bundle recievingBundle = getArguments();
            switch (recievingBundle.getString("TriggerType")){
                case "triggerByDateTime":
                    trigger = new TriggerByDateTime(recievingBundle.getString("Trigger"));
                    break;
                case "triggerBySeason":
                    trigger = new TriggerBySeason(recievingBundle.getString("Trigger"));
                    break;
            }
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        bindings = FragmentSelectActionsBinding.inflate(getLayoutInflater());
        fragmentManager = getActivity().getSupportFragmentManager();
        navController = NavHostFragment.findNavController(this);
        tempSharedPreferences =getActivity().getSharedPreferences("temp", Context.MODE_PRIVATE);
        Long id = getArguments().getLong("collectionId");
        collection = viewModel.getSpecificCollection(id);
        selectSubcollectionBottomSheet = new BottomSheetDialog(getContext());
        selectSpecificWallpaperBottomSheet = new BottomSheetDialog(getContext());
        selectSubcollectionBottomSheet.setContentView(R.layout.fragment_subcollection_list_dialog_list_dialog);
        selectSpecificWallpaperBottomSheet.setContentView(R.layout.fragment_select_specific_image_list_dialog_list_dialog);
        bindings.actionsRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int checkdActionId = bindings.actionsRadioGroup.getCheckedRadioButtonId();
                String checkedAction = getResources().getResourceEntryName(checkdActionId);
                switch (checkedAction){
                    case "selectActionNextInCollectionRadio":
                        bindings.selectSubcollectionSelectAction.setVisibility(View.GONE);
                        bindings.specificWallpaperTextview.setVisibility(View.GONE);
                        bindings.newSubcollectionTextview.setVisibility(View.GONE);
                        bindings.selectSpecificWallpaperSelectActionButton.setVisibility(View.GONE);
                        break;
                    case "selectActionSwitchToDiffSubColRadio":
                        bindings.selectSubcollectionSelectAction.setVisibility(View.VISIBLE);
                        bindings.newSubcollectionTextview.setVisibility(View.VISIBLE);
                        bindings.selectSpecificWallpaperSelectActionButton.setVisibility(View.GONE);
                        bindings.specificWallpaperTextview.setVisibility(View.GONE);
                        break;
                    case "selectActionRandomInCollSubRadio":
                        bindings.selectSubcollectionSelectAction.setVisibility(View.GONE);
                        bindings.specificWallpaperTextview.setVisibility(View.GONE);
                        bindings.newSubcollectionTextview.setVisibility(View.GONE);
                        bindings.selectSpecificWallpaperSelectActionButton.setVisibility(View.GONE);
                        break;
                    case "selectActionSpecificWallpaperRadio":
                        bindings.selectSpecificWallpaperSelectActionButton.setVisibility(View.VISIBLE);
                        bindings.specificWallpaperTextview.setVisibility(View.VISIBLE);
                        break;
                    default:
                        break;

                }
            }
        });
        SelectActionsFragment fragment = this;
        bindings.selectSubcollectionSelectAction.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                RecyclerView selectSubcollectionBottomSheetRecycler = selectSubcollectionBottomSheet.findViewById(R.id.selectSubCollectionRecycler);
                SelectSubCollectionRecycerAdapter adapter = new SelectSubCollectionRecycerAdapter(collection.getSubCollectionArray(),fragment,getContext());
                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
                selectSubcollectionBottomSheetRecycler.setLayoutManager(linearLayoutManager);
                selectSubcollectionBottomSheetRecycler.setAdapter(adapter);
                selectSubcollectionBottomSheet.show();
            }
        });




        bindings.selectSpecificWallpaperSelectActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RecyclerView selectSpecificWallpaperRecycler = selectSpecificWallpaperBottomSheet.findViewById(R.id.selectSpecificImageRecycler);
                SelectSpecificWallpaperAdapter adapter = new SelectSpecificWallpaperAdapter(collection,fragment,getContext());
                GridLayoutManager gridLayoutManager=new GridLayoutManager(getContext(),3);
                selectSpecificWallpaperRecycler.setLayoutManager(gridLayoutManager);
                selectSpecificWallpaperRecycler.setAdapter(adapter);
                selectSpecificWallpaperBottomSheet.show();
            }
        });

        bindings.saveRuleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(bindings.newSubcollectionTextview.getText().toString()=="Selected Subcollection To change to" || bindings.specificWallpaperTextview.getText().toString() =="Selected Specific Wallpaper"){
                    Snackbar.make(bindings.getRoot(),"Please Select Subcollection or specific wallpaper",Snackbar.LENGTH_LONG).setAnchorView(bindings.divider3).show();
                    return;
                }

                Action action = getData();
                Rule rule = new Rule(trigger,action);
                collection.getRules().add(rule);
                viewModel.saveCollection(collection);
                navController.navigate(R.id.action_selectActionsFragment_to_addCollectionFragment,getArguments());

            }
        });

        return bindings.getRoot();
    }

    public Action getData(){
        int checkdActionId = bindings.actionsRadioGroup.getCheckedRadioButtonId();
        String checkedAction = getResources().getResourceEntryName(checkdActionId);
        switch (checkedAction){
            case "selectActionNextInCollectionRadio":
                return new Action("selectActionNextInCollection","n/a","n/a");
            case "selectActionSwitchToDiffSubColRadio":
                String changeToSubCollection = bindings.newSubcollectionTextview.getText().toString();
                return new Action("selectActionSwitchToDiffSubColRadio",changeToSubCollection,"n/a");
            case "selectActionRandomInCollSubRadio":
                return new Action("selectActionRandomInCollSubRadio","n/a","n/a");
            case "selectActionSpecificWallpaperRadio":
                String changeToSpecificImage = bindings.specificWallpaperTextview.getText().toString();
                return new Action("selectActionSpecificWallpaperRadio","n/a",changeToSpecificImage);
            default:
                return new Action();

        }
    }

    public void updateSelectedSubcollection(int id){
        selectedSubCollection = id;
        if(selectedSubCollection!=-1){
            bindings.newSubcollectionTextview.setText("Selected Subcollection To change to: "+collection.getSubCollectionArray().get(selectedSubCollection).getName());
        }
        selectSubcollectionBottomSheet.dismiss();
    }
    public void updateSelectedSpecificWallpaper(String imageName){
        selectedSpecificImage = imageName;
        bindings.specificWallpaperTextview.setText("Selected Specific Wallpaper"+":"+imageName);
        selectSpecificWallpaperBottomSheet.dismiss();
    }

}