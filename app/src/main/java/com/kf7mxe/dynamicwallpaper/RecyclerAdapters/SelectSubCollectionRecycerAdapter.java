package com.kf7mxe.dynamicwallpaper.RecyclerAdapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.kf7mxe.dynamicwallpaper.R;
import com.kf7mxe.dynamicwallpaper.SelectActionsFragment;
import com.kf7mxe.dynamicwallpaper.models.SubCollection;
import com.kf7mxe.dynamicwallpaper.viewmodels.CollectionViewModel;

import java.util.ArrayList;

public class SelectSubCollectionRecycerAdapter extends RecyclerView.Adapter<SelectSubCollectionRecycerAdapter.ViewHolder>{

        private final ArrayList<SubCollection> mSubcollections;
        private CollectionViewModel viewModel;
        SharedPreferences sharedPreferences;
        SharedPreferences.Editor myEditor;
        private Context mContext;
        private SelectActionsFragment mFragment;

        public SelectSubCollectionRecycerAdapter(ArrayList<SubCollection> subcollections, SelectActionsFragment fragment, Context context) {
            mSubcollections = subcollections;
            mContext = context;
            mFragment = fragment;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            final TextView subCollection;

            ViewHolder(View view) {
                super(view);
                subCollection = view.findViewById(R.id.selectSubcollectionName);
            }

        }

        @Override
        public SelectSubCollectionRecycerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.fragment_subcollection_list_dialog_list_dialog_item,parent,false);

            return new SelectSubCollectionRecycerAdapter.ViewHolder(view);

        }

        @Override
        public void onBindViewHolder(SelectSubCollectionRecycerAdapter.ViewHolder holder, int position) {
            int puase = 0;
            final int item = position;
            holder.subCollection.setText(mSubcollections.get(position).getName());
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mFragment.updateSelectedSubcollection(item);
                }
            });

        }

        @Override
        public int getItemCount() {
            return mSubcollections.size();
        }

    }
