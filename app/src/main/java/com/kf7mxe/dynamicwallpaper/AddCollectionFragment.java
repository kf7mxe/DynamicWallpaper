package com.kf7mxe.dynamicwallpaper;

import static android.app.Activity.RESULT_OK;
import static android.content.Intent.ACTION_OPEN_DOCUMENT;

import android.Manifest;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import android.provider.DocumentsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.kf7mxe.dynamicwallpaper.databinding.FragmentAddCollectionBinding;
import com.yalantis.ucrop.UCrop;
import com.yalantis.ucrop.view.CropImageView;

import java.io.File;
import java.io.IOException;

import androidx.activity.result.ActivityResultLauncher;
import androidx.navigation.fragment.NavHostFragment;

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

    private Uri selectedImageUri;
    private Uri pickerInitialUri;

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
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAddCollectionBinding.inflate(getLayoutInflater());
        fragmentManager = getActivity().getSupportFragmentManager();
        navController = NavHostFragment.findNavController(this);
        binding.addRuleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navController.navigate(R.id.action_addCollectionFragment_to_selectTriggersFragment);
            }
        });

        binding.selectImagesFromGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImageSelector();
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



//        Uri test = new Uri()



//        CropImage.activity()
//                .setGuidelines(CropImageView.Guidelines.ON)
//                .start(this);
//
//// start cropping activity for pre-acquired image saved on the device
//        CropImage.activity(imageUri)
//                .start(this);
//
//// for fragment (DO NOT use `getActivity()`)
//        CropImage.activity()
//                .start(getContext(), this);

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




                    selectedImageUri = data.getData();
                if (null != selectedImageUri) {
                    cropImage(selectedImageUri);
                    Toast.makeText(getActivity(), "It worked I grabbed an image", Toast.LENGTH_SHORT).show();
                    // update the preview image in the layout
                   // IVPreviewImage.setImageURI(selectedImageUri);
                }
            }


        }

        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            Toast.makeText(getActivity(), "In the call uCrop", Toast.LENGTH_SHORT).show();
            final Uri resultUri = UCrop.getOutput(data);
        } else if (resultCode == UCrop.RESULT_ERROR) {
            final Throwable cropError = UCrop.getError(data);
            Toast.makeText(getActivity(), "Error calling uCrop", Toast.LENGTH_SHORT).show();
        }
    }






    private File getImageFile() throws IOException {
        String imageFileName = "JPEG_" + System.currentTimeMillis() + "_";
        //File file = getContext().getExternalFilesDir(ACTION_OPEN_DOCUMENT);
        File folder = new File(getContext().getExternalFilesDir(ACTION_OPEN_DOCUMENT).getAbsolutePath(), "cropedFolder");
        File folderCollection = new File(getContext().getExternalFilesDir(ACTION_OPEN_DOCUMENT).getAbsolutePath(),"testCollection");
        folderCollection.mkdir();
        folder.mkdir();
        File file = new File(getContext().getExternalFilesDir(ACTION_OPEN_DOCUMENT).getAbsolutePath()+"/testCollection/"+imageFileName+".jpg");
        file.createNewFile();
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


        UCrop.of(sourceUri, destinationUri)
                .withAspectRatio(9, 16)
                .withMaxResultSize(3000, 6000)
                .start(getActivity());
    }




}