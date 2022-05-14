package com.kf7mxe.dynamicwallpaper;

import static android.content.Intent.ACTION_OPEN_DOCUMENT;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.kf7mxe.dynamicwallpaper.databinding.FragmentViewWallpaperBinding;
import com.kf7mxe.dynamicwallpaper.models.Collection;
import com.kf7mxe.dynamicwallpaper.viewmodels.CollectionViewModel;

import java.io.File;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ViewWallpaperFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ViewWallpaperFragment extends Fragment {

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

    private String wallpaperFileName;

    private CollectionViewModel collectionViewModel;

    private FragmentManager fragmentManager;
    private NavController navController;

    FragmentViewWallpaperBinding binding;

    public ViewWallpaperFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ViewWallpaperFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ViewWallpaperFragment newInstance(String param1, String param2) {
        ViewWallpaperFragment fragment = new ViewWallpaperFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
        //menu.removeItem(R.id.deleteTopNavButton);
            menu.getItem(1).setVisible(true);
            menu.getItem(1).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setCancelable(true);
                    builder.setTitle("Are you sure you want to delete this image?");
                    builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if(subCollectionId==-1){
                                collection.getPhotoNames().remove(wallpaperFileName);
                                new File(getContext().getExternalFilesDir(ACTION_OPEN_DOCUMENT).getAbsolutePath(), collection.getName()).delete();
                                collectionViewModel.saveCollectionToCache(collection);
                            } else {
                                collection.getSubCollectionArray().get(subCollectionId).getFileNames().remove(wallpaperFileName);
                            }
                            Bundle bundle = new Bundle();
                            bundle.putLong("collectionId",collection.getId());
                            bundle.putInt("subCollectionId",subCollectionId);
                            bundle.putBoolean("fromAddOptionFragment",true);
                            navController.navigate(R.id.action_viewWallpaperFragment_to_viewChangePhotoOrderFragment,bundle,
                                    new NavOptions.Builder().setPopUpTo(R.id.viewWallpaperFragment, true).build());
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    AlertDialog confirmDelete = builder.create();
                    confirmDelete.show();
                    return false;
                }

            });

        super.onCreateOptionsMenu(menu, inflater);
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        collectionViewModel =new CollectionViewModel(getActivity().getApplication(),getContext());
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
            collectionId = getArguments().getLong("collectionId");
            subCollectionId = getArguments().getInt("selectedSubCollection",-1);
            wallpaperFileName = getArguments().getString("fileName");
            collection = collectionViewModel.getSpecificCachCollection(collectionId);
        } else {
            subCollectionId = -1;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentViewWallpaperBinding.inflate(getLayoutInflater());
        navController = NavHostFragment.findNavController(this);
        setHasOptionsMenu(true);

        // Inflate the layout for this fragment
        RequestOptions options = new RequestOptions()
                .centerCrop()
                .placeholder(R.drawable.placeholder);
        File fileGlide = new File(getContext().getExternalFilesDir(ACTION_OPEN_DOCUMENT).getAbsolutePath() + "/" + collection.getName() + "/" + wallpaperFileName);
        if (fileGlide.isFile()) {
            Glide.with(getContext()).load(fileGlide.getAbsolutePath()).apply(options).into(binding.wallpaperImageView);
        }
        return binding.getRoot();
    }
}