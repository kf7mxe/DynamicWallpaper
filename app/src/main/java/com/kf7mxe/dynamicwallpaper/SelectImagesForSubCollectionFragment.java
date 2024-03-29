package com.kf7mxe.dynamicwallpaper;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.snackbar.Snackbar;
import com.kf7mxe.dynamicwallpaper.RecyclerAdapters.SelectSubCollectionsImagesAdapter;
import com.kf7mxe.dynamicwallpaper.databinding.FragmentSelectImagesForSubCollectionBinding;
import com.kf7mxe.dynamicwallpaper.models.Collection;
import com.kf7mxe.dynamicwallpaper.models.SubCollection;
import com.kf7mxe.dynamicwallpaper.viewmodels.CollectionViewModel;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SelectImagesForSubCollectionFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SelectImagesForSubCollectionFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private Long collectionId;
    private int subCollectionId;
    private Collection collection;
    private CollectionViewModel collectionViewModel;
    private SubCollection newSubCollection;
    private NavController navController;

    private FragmentSelectImagesForSubCollectionBinding binding;

    public SelectImagesForSubCollectionFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SelectImagesForSubCollectionFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SelectImagesForSubCollectionFragment newInstance(String param1, String param2) {
        SelectImagesForSubCollectionFragment fragment = new SelectImagesForSubCollectionFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        collectionViewModel =new CollectionViewModel(getActivity().getApplication(),getContext());
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
            collectionId = getArguments().getLong("collectionId");
            subCollectionId = getArguments().getInt("subCollectionId",-1);
            collection = collectionViewModel.getSpecificCachCollection(collectionId);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSelectImagesForSubCollectionBinding.inflate(getLayoutInflater());
        if(subCollectionId==-1){
                newSubCollection = new SubCollection();
            } else {
                newSubCollection = collection.getSubCollectionArray().get(subCollectionId);
                binding.enterNewSubCollectionName.setText(collection.getSubCollectionArray().get(subCollectionId).getName());
            }
            navController = NavHostFragment.findNavController(this);
            GridLayoutManager gridLayoutManager=new GridLayoutManager(getContext(),3);
            binding.selectImagesForSubCollectionRecycler.setLayoutManager(gridLayoutManager);
            SelectSubCollectionsImagesAdapter adapter = new SelectSubCollectionsImagesAdapter(getContext(),collection,newSubCollection);
            binding.selectImagesForSubCollectionRecycler.setAdapter(adapter);

            binding.saveImagesInSubCollection.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(binding.enterNewSubCollectionName.getText().toString().length()==0){
                        Snackbar.make(binding.getRoot(),"Please add title",Snackbar.LENGTH_LONG).setAnchorView(binding.selectImagesForSubCollectionRecycler).show();
                        return;
                    }
                    newSubCollection.setName(binding.enterNewSubCollectionName.getText().toString());
                    if(subCollectionId==-1){
                        collection.getSubCollectionArray().add(newSubCollection);
                    } else {
                        collection.getSubCollectionArray().set(subCollectionId,newSubCollection);
                    }
                    collectionViewModel.saveCollectionToCache(collection);

                    navController.navigate(R.id.action_selectImagesForSubCollectionFragment_to_addCollectionFragment,getArguments());
                }
            });


        return binding.getRoot();
    }
}