package com.kf7mxe.dynamicwallpaper;

import static android.app.Activity.RESULT_OK;
import static android.content.Intent.ACTION_OPEN_DOCUMENT;

import android.Manifest;
import android.app.WallpaperManager;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.os.Environment;
import android.os.FileUtils;
import android.provider.DocumentsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.kf7mxe.dynamicwallpaper.RecyclerAdapters.RulesRecyclerAdapter;
import com.kf7mxe.dynamicwallpaper.RecyclerAdapters.SubcollectionRecyclerViewAdapter;
import com.kf7mxe.dynamicwallpaper.database.RoomDB;
import com.kf7mxe.dynamicwallpaper.databinding.FragmentAddCollectionBinding;
import com.kf7mxe.dynamicwallpaper.models.Collection;
import com.kf7mxe.dynamicwallpaper.models.Rule;
import com.kf7mxe.dynamicwallpaper.viewmodels.CollectionViewModel;
import com.yalantis.ucrop.UCrop;
import com.yalantis.ucrop.view.CropImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import androidx.activity.result.ActivityResultLauncher;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.room.Room;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AddCollectionFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class AddCollectionFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    int SELECT_PICTURE = 200;

    private static final int CREATE_FILE = 1;

    private  String currentPhotoPath;

    private FragmentAddCollectionBinding binding;
    private FragmentManager fragmentManager;

    private NavController navController;

    private String collectionName;

    private List<Collection> collectionList;
    private ArrayList<Rule> rules;
    private ArrayList<String> imageNames;

    private Long collectionId;
    private Collection collection;
    private Uri selectedImageUri;
    private Uri pickerInitialUri;

    private CollectionViewModel collectionViewModel;

    private SharedPreferences sharedPreferences;

    private WallpaperManager wallpaperManager;
    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AddCollectionFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AddCollectionFragment newInstance(String param1, String param2) {
        AddCollectionFragment fragment = new AddCollectionFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public AddCollectionFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        collectionViewModel =new CollectionViewModel(getActivity().getApplication(),getContext());
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
            collectionId = getArguments().getLong("collectionId");
            collection = collectionViewModel.getSpecificCollection(collectionId);
            int pause =0;
        } else {
            collection = new Collection();
            List<Collection> test = collectionViewModel.getAllCollections();
            collectionId = collectionViewModel.saveCollection(collection);
            collection.setId(collectionId);
        }

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAddCollectionBinding.inflate(getLayoutInflater());
        fragmentManager = getActivity().getSupportFragmentManager();
        navController = NavHostFragment.findNavController(this);
        wallpaperManager = WallpaperManager.getInstance(getContext());

        sharedPreferences = getActivity().getSharedPreferences("testing",Context.MODE_PRIVATE);

        binding.enterCollectionName.setText(collection.getName());

        //binding.rulesRecyclerView.
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        LinearLayoutManager linearLayoutManagerForSubCollectionsRecycler = new LinearLayoutManager(getContext());
        binding.rulesRecyclerView.setLayoutManager(linearLayoutManager); // set LayoutManager to RecyclerView
        RulesRecyclerAdapter adapter = new RulesRecyclerAdapter(getActivity(),collection.getRules());
        SubcollectionRecyclerViewAdapter subcollectionRecyclerViewAdapter = new SubcollectionRecyclerViewAdapter(getActivity(),collection.getSubCollectionArray());
        binding.rulesRecyclerView.setAdapter(adapter);
        binding.subcollectionRecyclerView.setLayoutManager(linearLayoutManagerForSubCollectionsRecycler);
        binding.subcollectionRecyclerView.setAdapter(subcollectionRecyclerViewAdapter);

        int pause = 0;
        binding.enterCollectionName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                collection.setName(s.toString());
            }
        });

        binding.addRuleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                collectionViewModel.saveCollection(collection);
                Bundle bundle = new Bundle();
                bundle.putLong("collectionId",collectionId);
                navController.navigate(R.id.action_addCollectionFragment_to_selectTriggersFragment,bundle);
            }
        });

        binding.useSubcollectionCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    binding.addSubcollectionButton.setVisibility(View.VISIBLE);
                }else{
                    binding.addSubcollectionButton.setVisibility(View.INVISIBLE);
                }
            }
        });

        binding.addSubcollectionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.addSubcollectionButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Bundle bundle = new Bundle();
                        bundle.putLong("collectionId",collectionId);
                        navController.navigate(R.id.action_addCollectionFragment_to_selectImagesForSubCollectionFragment,bundle);
                    }
                });
            }
        });

        binding.viewChangeCollectionImagesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putLong("collectionId",collectionId);
                navController.navigate(R.id.action_addCollectionFragment_to_viewChangePhotoOrderFragment,bundle);
            }
        });

        binding.cancelNewCollection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navController.navigate(R.id.action_addCollectionFragment_to_homeFragment);
            }
        });

        binding.selectImagesFromGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                collectionName = binding.enterCollectionName.getText().toString();
                if(binding.enterCollectionName.getText().toString()==null || binding.enterCollectionName.getText().length()==0){
                    Snackbar.make(binding.getRoot(),"Please Enter Collection name first",Snackbar.LENGTH_LONG).setAnchorView(binding.useSubcollectionCheckbox).show();
                    return;
                }
                openImageSelector();
            }
        });

        binding.saveCollectionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(collection!=null){
                    collectionViewModel.saveCollection(collection);
                    navController.navigate(R.id.action_addCollectionFragment_to_homeFragment);
                }
            }
        });


        ActivityResultLauncher launcher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted ->{
            if(isGranted){
                //getAndDisplayLocation();

            } else {

            }
        });

        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED){

        }
        else if(ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED){
            launcher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        else {
            launcher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            launcher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
        }


        // Inflate the layout for this fragment
        return binding.getRoot();
    }


    public void openImageSelector(){
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);
        i.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);

        // pass the constant to compare it
        // with the returned requestCode
        startActivityForResult(Intent.createChooser(i, "Select Picture"), SELECT_PICTURE);


    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == getActivity().RESULT_OK) {

            // compare the resultCode with the
            // SELECT_PICTURE constant
            if (requestCode == SELECT_PICTURE) {
                // Get the url of the image from data

                if (data.getClipData() != null) {
                    Toast.makeText(getContext(), "In the get clip data", Toast.LENGTH_SHORT).show();
                    ClipData mClipData = data.getClipData();
                    int cout = data.getClipData().getItemCount();
                    for (int i = 0; i < cout; i++) {
                        // adding imageuri in array
                        Uri imageurl = data.getClipData().getItemAt(i).getUri();
                        cropImage(imageurl);
                    }

                }
                else if (data.getData() != null) {
                    cropImage(data.getData());
                }
            }


        }

        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            Toast.makeText(getActivity(), "In the call uCrop", Toast.LENGTH_SHORT).show();
            final Uri resultUri = UCrop.getOutput(data);
            ;
        } else if (resultCode == UCrop.RESULT_ERROR) {
            final Throwable cropError = UCrop.getError(data);
            Toast.makeText(getActivity(), "Error calling uCrop", Toast.LENGTH_SHORT).show();
        }
    }






    private File getImageFile() throws IOException {
        String imageFileName = "JPEG_" + System.currentTimeMillis() + "_";
        File folderCollection = new File(getContext().getExternalFilesDir(ACTION_OPEN_DOCUMENT).getAbsolutePath(),binding.enterCollectionName.getText().toString());
        folderCollection.mkdir();
        File file = new File(getContext().getExternalFilesDir(ACTION_OPEN_DOCUMENT).getAbsolutePath()+"/"+binding.enterCollectionName.getText().toString()+"/"+imageFileName+".jpg");
        file.createNewFile();

        collection.getPhotoNames().add(imageFileName+".jpg");

        SharedPreferences.Editor myEditor = sharedPreferences.edit();
        myEditor.putString("testImage",getContext().getExternalFilesDir(ACTION_OPEN_DOCUMENT).getAbsolutePath()+"/"+binding.enterCollectionName.getText().toString()+"/"+imageFileName+".jpg");
        myEditor.commit();

        return file;

    }



    private void cropImage(Uri sourceUri){
        File file;
//
        try {
            //getImageFile();
            file = getImageFile(); // 1
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), "In the call uCrop"+e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            //uiHelper.toast(this, "Please take another image");
            return;
        }
//        Uri uri;
        Uri destinationUri = Uri.fromFile(file);

        Pair screenResolution = getScreenResolution();
        Pair aspectRatio = getAspectRatio((Integer)screenResolution.first,(Integer)screenResolution.second);

        UCrop.of(sourceUri, destinationUri)
                .withAspectRatio((Integer)aspectRatio.first, (Integer)aspectRatio.second)
                .withMaxResultSize((Integer)screenResolution.first, (Integer)screenResolution.second)
                .start(getActivity());

        collectionViewModel.saveCollection(collection);
    }


    private Pair getScreenResolution(){
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getContext().getDisplay().getRealMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;
        int height = displayMetrics.heightPixels;
        return new Pair<Integer,Integer>(width,height);

    }

    private Pair getAspectRatio(int width, int height){
        final int greatestCommonDenominator = greatestCommonDenominator(width, height);
        return new Pair(width/greatestCommonDenominator,height/greatestCommonDenominator);
    }

    private int greatestCommonDenominator(int p, int q){
        if(q==0) {
            return p;
        } else {
            return greatestCommonDenominator(q,p%q);
        }
    }


public void testImage(String path){
    File file = new File(path);

    //File file = new File(resultUri.getPath());
    //Bitmap testBitmap = BitmapFactory.decodeFile( resultUri.getPath());
    try {
        InputStream inputStream = new FileInputStream(file);
        Bitmap bitmap =BitmapFactory.decodeStream(inputStream);
        if(bitmap!=null){
            wallpaperManager.setBitmap(bitmap);
        }
    } catch (IOException e) {
        e.printStackTrace();
    }
}



//

}