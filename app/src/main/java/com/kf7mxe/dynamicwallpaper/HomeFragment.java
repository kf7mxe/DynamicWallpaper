package com.kf7mxe.dynamicwallpaper;

import static android.content.Intent.ACTION_OPEN_DOCUMENT;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.opengl.Visibility;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.kf7mxe.dynamicwallpaper.RecyclerAdapters.HomeCollectionRecyclerViewAdapter;
import com.kf7mxe.dynamicwallpaper.models.Collection;
import com.kf7mxe.dynamicwallpaper.placeholder.PlaceholderContent;
import com.kf7mxe.dynamicwallpaper.databinding.FragmentHomeBinding;
import com.kf7mxe.dynamicwallpaper.recievers.AlarmActionReciever;
import com.kf7mxe.dynamicwallpaper.viewmodels.CollectionViewModel;

import java.text.ParseException;
import java.util.Calendar;
import java.util.List;

/**
 * A fragment representing a list of Items.
 */
public class HomeFragment extends Fragment {

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 1;

    NavController navController;
    private FragmentManager fragmentManager;
    private CollectionViewModel collectionViewModel;
    private FragmentHomeBinding binding;
    private List<Collection> collectionList;


    private SharedPreferences sharedPreferences;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public HomeFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static HomeFragment newInstance(int columnCount) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        collectionViewModel =new CollectionViewModel(getActivity().getApplication(),getContext());
        collectionList = collectionViewModel.getAllCollections();
        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        binding = FragmentHomeBinding.inflate(getLayoutInflater());
        fragmentManager = getActivity().getSupportFragmentManager();
        navController = NavHostFragment.findNavController(this);

        sharedPreferences = getActivity().getSharedPreferences("sharedPrefrences",Context.MODE_PRIVATE);
        String selectedCollectionString = sharedPreferences.getString("selectedCollection","");
        try {
            if(collectionViewModel.getSpecificCollection(Long.parseLong(selectedCollectionString))!=null){
                binding.displaySelectedCollectionInHomeTextView.setText(collectionViewModel.getSpecificCollection(Long.parseLong(selectedCollectionString)).getName());
            }
        } catch (NumberFormatException e){

            //int pause = 0;
        }

//        if(!selectedCollectionString.equals("")){
//            startRunningCollections();
//        }

        // Set the adapter
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        binding.allCollectionsRecycler.setLayoutManager(linearLayoutManager);

        if(!collectionViewModel.isEmpty()){
            binding.allCollectionsRecycler.setVisibility(View.VISIBLE);
            binding.noCollections.setVisibility(View.GONE);
            binding.hitPlusButtonToAddColTextview.setVisibility(View.GONE);
            HomeCollectionRecyclerViewAdapter adapter = new HomeCollectionRecyclerViewAdapter(getContext(),collectionViewModel.getAllCollections(),navController);
            binding.allCollectionsRecycler.setAdapter(adapter);
        }
        else {
            binding.allCollectionsRecycler.setVisibility(View.INVISIBLE);
            binding.noCollections.setVisibility(View.VISIBLE);
            binding.hitPlusButtonToAddColTextview.setVisibility(View.VISIBLE);
        }



        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navController.navigate(R.id.action_homeFragment_to_addCollectionFragment);

            }
        });

        return binding.getRoot();
    }
}