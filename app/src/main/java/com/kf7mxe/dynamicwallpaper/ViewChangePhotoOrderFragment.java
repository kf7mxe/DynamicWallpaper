package com.kf7mxe.dynamicwallpaper;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kf7mxe.dynamicwallpaper.RecyclerAdapters.ViewChangeCollectionsImagesAdapter;
import com.kf7mxe.dynamicwallpaper.databinding.FragmentViewChangePhotoOrderBinding;
import com.kf7mxe.dynamicwallpaper.models.Collection;
import com.kf7mxe.dynamicwallpaper.viewmodels.CollectionViewModel;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ViewChangePhotoOrderFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ViewChangePhotoOrderFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private FragmentViewChangePhotoOrderBinding binding;

    private Long collectionId;
    private int subCollectionId;
    private Collection collection;

    private CollectionViewModel collectionViewModel;

    private FragmentManager fragmentManager;
    private NavController navController;

    public ViewChangePhotoOrderFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ChangePhotoOrderFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ViewChangePhotoOrderFragment newInstance(String param1, String param2) {
        ViewChangePhotoOrderFragment fragment = new ViewChangePhotoOrderFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        collectionViewModel =new CollectionViewModel(requireActivity().getApplication(),getContext());
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
            collectionViewModel.getAllCollections();
            List <Collection> cache = collectionViewModel.getAllCacheCollections();
            collectionId = getArguments().getLong("collectionId");
            subCollectionId = getArguments().getInt("selectedSubCollection",-1);
            collection = collectionViewModel.getSpecificCachCollection(collectionId);
            int pause = 0;
        } else {
            subCollectionId = -1;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentViewChangePhotoOrderBinding.inflate(getLayoutInflater());
        fragmentManager = getActivity().getSupportFragmentManager();
        navController = NavHostFragment.findNavController(this);


        GridLayoutManager gridLayoutManager=new GridLayoutManager(getContext(),3);
        binding.viewCollectionPhotosChangeOrderRecycler.setLayoutManager(gridLayoutManager);
        ViewChangeCollectionsImagesAdapter adapter = new ViewChangeCollectionsImagesAdapter(getContext(),collection,subCollectionId,navController);
        binding.viewCollectionPhotosChangeOrderRecycler.setAdapter(adapter);

        binding.saveCollectionOrderChangesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                collectionViewModel.saveCollectionToCache(collection);
                navController.navigate(R.id.action_viewChangePhotoOrderFragment_to_addCollectionFragment,getArguments());
            }
        });

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(binding.viewCollectionPhotosChangeOrderRecycler);

        return binding.getRoot();
    }



    ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.START | ItemTouchHelper.END,0) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {

            int fromPosition = viewHolder.getLayoutPosition();
            int toPosition = target.getLayoutPosition();
            if(fromPosition==collection.getPhotoNames().size()){
                return false;
            }

            Collections.swap(collection.getPhotoNames(),fromPosition,toPosition);
            recyclerView.getAdapter().notifyItemMoved(fromPosition,toPosition);
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

        }
    };
}