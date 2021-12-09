package com.kf7mxe.dynamicwallpaper.dialogs;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kf7mxe.dynamicwallpaper.R;
import com.kf7mxe.dynamicwallpaper.RecyclerAdapters.HomeCollectionRecyclerViewAdapter;
import com.kf7mxe.dynamicwallpaper.databinding.FragmentSubcollectionListDialogListDialogBinding;
import com.kf7mxe.dynamicwallpaper.models.Collection;
import com.kf7mxe.dynamicwallpaper.models.SubCollection;
import com.kf7mxe.dynamicwallpaper.viewmodels.CollectionViewModel;

import java.util.ArrayList;

/**
 * <p>A fragment that shows a list of items as a modal bottom sheet.</p>
 * <p>You can show this modal bottom sheet from your activity like this:</p>
 * <pre>
 *     SubCollectionsListDialogFragment.newInstance(30).show(getSupportFragmentManager(), "dialog");
 * </pre>
 */
public class SubCollectionsListDialogFragment extends BottomSheetDialogFragment {

    // TODO: Customize parameter argument names
    private static final String ARG_ITEM_COUNT = "item_count";
    private FragmentSubcollectionListDialogListDialogBinding binding;

    // TODO: Customize parameters
    public static SubCollectionsListDialogFragment newInstance(long id) {
        final SubCollectionsListDialogFragment fragment = new SubCollectionsListDialogFragment();
        final Bundle args = new Bundle();
        args.putLong("collectionId", id);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentSubcollectionListDialogListDialogBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        final RecyclerView recyclerView = (RecyclerView) binding.selectSubCollectionRecycler;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        Long id = getArguments().getLong("collectionId");
        CollectionViewModel viewModel = new CollectionViewModel(getActivity().getApplication(),getContext());
        Collection collection = viewModel.getSpecificCollection(id);
        recyclerView.setAdapter(new SubCollectionsAdapter(collection.getSubCollectionArray()));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private class ViewHolder extends RecyclerView.ViewHolder {

        final TextView subCollection;

        ViewHolder(View view) {
            super(view);
            subCollection = view.findViewById(R.id.selectSubcollectionName);
        }

    }

    private class SubCollectionsAdapter extends RecyclerView.Adapter<ViewHolder> {

        private final ArrayList<SubCollection> mSubcollections;

        SubCollectionsAdapter(ArrayList<SubCollection> subcollections) {

            mSubcollections = subcollections;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.fragment_subcollection_list_dialog_list_dialog_item,parent,false);

            return new ViewHolder(view);

        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.subCollection.setText(mSubcollections.get(position).getName());
        }

        @Override
        public int getItemCount() {
            return mSubcollections.size();
        }

    }
}