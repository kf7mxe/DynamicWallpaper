package com.kf7mxe.dynamicwallpaper.dialogs;

import static android.content.Intent.ACTION_OPEN_DOCUMENT;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.kf7mxe.dynamicwallpaper.R;
import com.kf7mxe.dynamicwallpaper.databinding.FragmentSelectSpecificImageListDialogListDialogBinding;
import com.kf7mxe.dynamicwallpaper.models.Collection;
import com.kf7mxe.dynamicwallpaper.viewmodels.CollectionViewModel;

import java.io.File;
import java.util.ArrayList;

/**
 * <p>A fragment that shows a list of items as a modal bottom sheet.</p>
 * <p>You can show this modal bottom sheet from your activity like this:</p>
 * <pre>
 *     SelectSpecificImageListDialogFragment.newInstance(30).show(getSupportFragmentManager(), "dialog");
 * </pre>
 */
public class SelectSpecificImageListDialogFragment extends BottomSheetDialogFragment {

    // TODO: Customize parameter argument names
    private static final String ARG_ITEM_COUNT = "item_count";
    private FragmentSelectSpecificImageListDialogListDialogBinding binding;

    // TODO: Customize parameters
    public static SelectSpecificImageListDialogFragment newInstance(long id) {
        final SelectSpecificImageListDialogFragment fragment = new SelectSpecificImageListDialogFragment();
        final Bundle args = new Bundle();
        args.putLong("collectionId", id);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentSelectSpecificImageListDialogListDialogBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        final RecyclerView recyclerView = (RecyclerView) binding.selectSpecificImageRecycler;
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        Long id = getArguments().getLong("collectionId");
        CollectionViewModel viewModel = new CollectionViewModel(getActivity().getApplication(),getContext());
        Collection collection = viewModel.getSpecificCollection(id);
        recyclerView.setAdapter(new SelectSpecificImageAdapter(collection));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private class ViewHolder extends RecyclerView.ViewHolder {

        //final TextView text;
        ImageView imageView;

        ViewHolder(View view) {
            super(view);
            imageView = view.findViewById(R.id.selectSpecificImageView);
            //text = binding.text;
        }

    }

    private class SelectSpecificImageAdapter extends RecyclerView.Adapter<ViewHolder> {

        private final ArrayList<String> m_photos;
        private Collection mCollection;
        SelectSpecificImageAdapter(Collection collection) {
            mCollection = collection;
            m_photos = collection.getPhotoNames();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.fragment_select_specific_image_list_dialog_list_dialog_item,parent,false);

            return new ViewHolder(view);

        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            //holder.text.setText(String.valueOf(position));
            RequestOptions options = new RequestOptions()
                    .centerCrop()
                    .placeholder(R.drawable.placeholder);
            File fileGlidePrivew1 = new File(getContext().getExternalFilesDir(ACTION_OPEN_DOCUMENT).getAbsolutePath()+"/"+mCollection.getName()+"/"+m_photos.get(position));
            if(fileGlidePrivew1.isFile()){
                Glide.with(getContext()).load(fileGlidePrivew1.getAbsolutePath()).apply(options).into(holder.imageView);
            }
        }

        @Override
        public int getItemCount() {
            return m_photos.size();
        }

    }
}