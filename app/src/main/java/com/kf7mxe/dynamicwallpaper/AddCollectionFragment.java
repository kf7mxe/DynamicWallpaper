package com.kf7mxe.dynamicwallpaper;

import static android.app.Activity.RESULT_OK;
import static android.content.Intent.ACTION_GET_CONTENT;

import android.Manifest;
import android.app.AlertDialog;
import android.app.WallpaperManager;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;
import com.kf7mxe.dynamicwallpaper.RecyclerAdapters.RulesRecyclerAdapter;
import com.kf7mxe.dynamicwallpaper.RecyclerAdapters.SubcollectionRecyclerViewAdapter;
import com.kf7mxe.dynamicwallpaper.databinding.FragmentAddCollectionBinding;
import com.kf7mxe.dynamicwallpaper.models.Collection;
import com.kf7mxe.dynamicwallpaper.models.Rule;
import com.kf7mxe.dynamicwallpaper.viewmodels.CollectionViewModel;
import com.yalantis.ucrop.UCrop;
import com.yalantis.ucrop.UCropActivity;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import androidx.activity.result.ActivityResultLauncher;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

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

    private Boolean updateCollection;

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
            updateCollection = getArguments().getBoolean("update");
            boolean fromAddOptionFragment = getArguments().getBoolean("fromAddOptionFragment",false);
            if(fromAddOptionFragment){
                collection = collectionViewModel.getSpecificCachCollection(collectionId);
            } else {
                collection = collectionViewModel.getSpecificCollection(collectionId);
                collectionViewModel.saveCollectionToCache(collection);
            }
            int pause =0;
        } else {
            collection = new Collection();
            collectionId = collectionViewModel.saveCollectionToCache(collection);
            updateCollection = false;
            collection.setId(collectionId);
        }

    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
        //menu.removeItem(R.id.deleteTopNavButton);
        if(updateCollection) {
            menu.getItem(1).setVisible(true);
            menu.getItem(1).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setCancelable(true);
                    builder.setTitle("Are you sure you want to delete this?");
                    builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            collectionViewModel.deleteFromDatabase(collection);
                            if (collection.getPhotoNames().size() != 0 && collection.getName().length() != 0) {
                                deleteRecursive(new File(Objects.requireNonNull(requireContext().getExternalFilesDir(ACTION_GET_CONTENT)).getAbsolutePath(), collection.getName()));
                            }
                            navController.navigate(R.id.action_addCollectionFragment_to_homeFragment);
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
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAddCollectionBinding.inflate(getLayoutInflater());
        fragmentManager = requireActivity().getSupportFragmentManager();
        navController = NavHostFragment.findNavController(this);
        wallpaperManager = WallpaperManager.getInstance(getContext());
        setHasOptionsMenu(true);



        sharedPreferences = requireActivity().getSharedPreferences("testing",Context.MODE_PRIVATE);

        binding.enterCollectionName.setText(collection.getName());


        setRandomImagesForPreview();

        binding.selectedImagePreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putLong("collectionId",collectionId);
                bundle.putBoolean("update",updateCollection);
                bundle.putBoolean("fromAddOptionFragment",true);
                navController.navigate(R.id.action_addCollectionFragment_to_viewChangePhotoOrderFragment,bundle);
            }
        });

        //binding.rulesRecyclerView.
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        LinearLayoutManager linearLayoutManagerForSubCollectionsRecycler = new LinearLayoutManager(getContext());
        binding.rulesRecyclerView.setLayoutManager(linearLayoutManager); // set LayoutManager to RecyclerView
        RulesRecyclerAdapter adapter = new RulesRecyclerAdapter(getActivity(),collection.getRules());
        SubcollectionRecyclerViewAdapter subcollectionRecyclerViewAdapter = new SubcollectionRecyclerViewAdapter(getActivity(),collection,navController);
        binding.rulesRecyclerView.setAdapter(adapter);
        binding.subcollectionRecyclerView.setLayoutManager(linearLayoutManagerForSubCollectionsRecycler);
        binding.subcollectionRecyclerView.setAdapter(subcollectionRecyclerViewAdapter);
        binding.collectionImageCountTextView.setText("Collection Images:"+collection.getPhotoNames().size());
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

                collectionViewModel.saveCollectionToCache(collection);
                Bundle bundle = new Bundle();
                bundle.putLong("collectionId",collectionId);
                bundle.putBoolean("update",updateCollection);
                bundle.putBoolean("fromAddOptionFragment",true);
                navController.navigate(R.id.action_addCollectionFragment_to_selectTriggersFragment,bundle);
            }
        });

        if(collection.getSubCollectionArray().size()>0){
            if(!binding.useSubcollectionCheckbox.isChecked()){
                binding.useSubcollectionCheckbox.setChecked(true);
                binding.addSubcollectionButton.setVisibility(View.VISIBLE);
            }
        }

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
                    Bundle bundle = new Bundle();
                    bundle.putLong("collectionId",collectionId);
                    bundle.putBoolean("fromAddOptionFragment",true);
                    bundle.putBoolean("update",updateCollection);
                navController.navigate(R.id.action_addCollectionFragment_to_selectImagesForSubCollectionFragment,bundle);
            }
        });

        binding.viewChangeCollectionImagesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putLong("collectionId",collectionId);
                bundle.putBoolean("update",updateCollection);
                bundle.putBoolean("fromAddOptionFragment",true);
                navController.navigate(R.id.action_addCollectionFragment_to_viewChangePhotoOrderFragment,bundle);
            }
        });

        binding.cancelNewCollection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(collection.getName().length()==0) {
                    collectionViewModel.deleteFromDatabase(collection);
                }
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


        ActivityResultLauncher<String> launcher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted ->{
            if(isGranted){
                //getAndDisplayLocation();

            } else {

            }
        });

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED){

        }
        else if(ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED){
            launcher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        else {
            launcher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            launcher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
        }




//        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
//            @Override
//            public void handleOnBackPressed() {
//                // Handle the back button event
//                if(collection.getName().length()==0){
//                    collectionViewModel.deleteFromDatabase(collection);
//                }
//            }
//        };
        //requireActivity().getOnBackPressedDispatcher().addCallback(getActivity(), callback);

        // The callback can be enabled or disabled here or in handleOnBackPressed()



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
//        startActivityForResult(Intent.createChooser(i, "Select Picture"), SELECT_PICTURE);
//        mGetContent.launch(Intent.createChooser(i, "Select Picture"));
        //registerForActivityResult
//        mGetContent.launch(Intent.createChooser(i, "Select Picture"));
        mGetContent.launch(i);


    }


    ActivityResultLauncher<Intent> mGetContent = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        assert data != null;
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
            });

    ActivityResultLauncher<Intent> cropLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                        Toast.makeText(getActivity(), "In the call uCrop", Toast.LENGTH_SHORT).show();
                    assert result.getData() != null;
                    final Uri resultUri = UCrop.getOutput(result.getData());



                }
            });


    private File getImageFile() throws IOException {
        String imageFileName = "JPEG_" + System.currentTimeMillis() + "_";
        File folderCollection = new File(Objects.requireNonNull(requireContext().getExternalFilesDir(ACTION_GET_CONTENT)).getAbsolutePath(),binding.enterCollectionName.getText().toString());
        folderCollection.mkdir();
        File file = new File(Objects.requireNonNull(requireContext().getExternalFilesDir(ACTION_GET_CONTENT)).getAbsolutePath()+"/"+binding.enterCollectionName.getText().toString()+"/"+imageFileName+".jpg");
        file.createNewFile();

        collection.getPhotoNames().add(imageFileName+".jpg");

        SharedPreferences.Editor myEditor = sharedPreferences.edit();
        myEditor.putString("testImage", Objects.requireNonNull(requireContext().getExternalFilesDir(ACTION_GET_CONTENT)).getAbsolutePath()+"/"+binding.enterCollectionName.getText().toString()+"/"+imageFileName+".jpg");
        myEditor.apply();

        return file;

    }

    private void setRandomImagesForPreview() {
        if (collection.getPhotoNames().size() > 0){
            int photoCount = collection.getPhotoNames().size();
            Random rand = new Random();
            int randomPhotoIndex1 = rand.nextInt(photoCount);
            int randomPhotoIndex2 = rand.nextInt(photoCount);
            int randomPhotoIndex3 = rand.nextInt(photoCount);
            int randomPhotoIndex4 = rand.nextInt(photoCount);
            int randomPhotoIndex5 = rand.nextInt(photoCount);
            //make sure all the random numbers are different
            while (randomPhotoIndex1 == randomPhotoIndex2 || randomPhotoIndex1 == randomPhotoIndex3 || randomPhotoIndex1 == randomPhotoIndex4 || randomPhotoIndex1 == randomPhotoIndex5){
                randomPhotoIndex1 = rand.nextInt(photoCount);
            }
            while (randomPhotoIndex2 == randomPhotoIndex1 || randomPhotoIndex2 == randomPhotoIndex3 || randomPhotoIndex2 == randomPhotoIndex4 || randomPhotoIndex2 == randomPhotoIndex5){
                randomPhotoIndex2 = rand.nextInt(photoCount);
            }
            while (randomPhotoIndex3 == randomPhotoIndex1 || randomPhotoIndex3 == randomPhotoIndex2 || randomPhotoIndex3 == randomPhotoIndex4 || randomPhotoIndex3 == randomPhotoIndex5){
                randomPhotoIndex3 = rand.nextInt(photoCount);
            }
            while (randomPhotoIndex4 == randomPhotoIndex1 || randomPhotoIndex4 == randomPhotoIndex2 || randomPhotoIndex4 == randomPhotoIndex3 || randomPhotoIndex4 == randomPhotoIndex5){
                randomPhotoIndex4 = rand.nextInt(photoCount);
            }
            while (randomPhotoIndex5 == randomPhotoIndex1 || randomPhotoIndex5 == randomPhotoIndex2 || randomPhotoIndex5 == randomPhotoIndex3 || randomPhotoIndex5 == randomPhotoIndex4){
                randomPhotoIndex5 = rand.nextInt(photoCount);
            }

            File fileImage = new File(Objects.requireNonNull(requireContext().getExternalFilesDir(ACTION_GET_CONTENT)).getAbsolutePath(), collection.getName() + "/" + collection.getPhotoNames().get(randomPhotoIndex1));
            File fileImage2 = new File(Objects.requireNonNull(requireContext().getExternalFilesDir(ACTION_GET_CONTENT)).getAbsolutePath(), collection.getName() + "/" + collection.getPhotoNames().get(randomPhotoIndex2));
            File fileImage3 = new File(Objects.requireNonNull(requireContext().getExternalFilesDir(ACTION_GET_CONTENT)).getAbsolutePath(), collection.getName() + "/" + collection.getPhotoNames().get(randomPhotoIndex3));
            File fileImage4 = new File(Objects.requireNonNull(requireContext().getExternalFilesDir(ACTION_GET_CONTENT)).getAbsolutePath(), collection.getName() + "/" + collection.getPhotoNames().get(randomPhotoIndex4));
            File fileImage5 = new File(Objects.requireNonNull(requireContext().getExternalFilesDir(ACTION_GET_CONTENT)).getAbsolutePath(), collection.getName() + "/" + collection.getPhotoNames().get(randomPhotoIndex5));

            Glide.with(requireContext()).load(fileImage).into(binding.imageView);
            Glide.with(requireContext()).load(fileImage2).into(binding.imageView2);
            Glide.with(requireContext()).load(fileImage3).into(binding.imageView3);
            Glide.with(requireContext()).load(fileImage4).into(binding.imageView4);
            Glide.with(requireContext()).load(fileImage5).into(binding.imageView5);
            binding.selectedImagePreview.setVisibility(View.VISIBLE);
            binding.selectedImagePreviewText.setVisibility(View.VISIBLE);
            // show subcollection checkbox and button
            binding.useSubcollectionCheckbox.setVisibility(View.VISIBLE);
            binding.addSubcollectionButton.setVisibility(View.VISIBLE);
            binding.viewChangeCollectionImagesButton.setVisibility(View.VISIBLE);
            binding.collectionImageCountTextView.setVisibility(View.VISIBLE);
            binding.subcollectionsTitleTextview.setVisibility(View.VISIBLE);
        } else {
            binding.selectedImagePreview.setVisibility(View.GONE);
            binding.selectedImagePreviewText.setVisibility(View.GONE);

            binding.viewChangeCollectionImagesButton.setVisibility(View.GONE);
            binding.collectionImageCountTextView.setVisibility(View.GONE);
            // hide subcollection checkbox and button
            binding.useSubcollectionCheckbox.setVisibility(View.GONE);
            binding.addSubcollectionButton.setVisibility(View.GONE);

            binding.subcollectionsTitleTextview.setVisibility(View.GONE);
        }
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
                .start(getContext(),cropLauncher);

        collectionViewModel.saveCollectionToCache(collection);
        binding.collectionImageCountTextView.setText("Collection Images:"+collection.getPhotoNames().size());

    }


    private Pair getScreenResolution(){
        DisplayMetrics displayMetrics = new DisplayMetrics();
        requireContext().getDisplay().getRealMetrics(displayMetrics);
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


    public void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            for (File child : Objects.requireNonNull(fileOrDirectory.listFiles())) {
                deleteRecursive(child);
            }
        }
        fileOrDirectory.delete();
    }


    // onresume
    @Override
    public void onResume() {
        super.onResume();
        // set the title of the toolbar
       // ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Collection");
        setRandomImagesForPreview();

    }

    // onpause
    @Override
    public void onPause() {
        super.onPause();
        // set the title of the toolbar
        //((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Collection");
    }


//

}