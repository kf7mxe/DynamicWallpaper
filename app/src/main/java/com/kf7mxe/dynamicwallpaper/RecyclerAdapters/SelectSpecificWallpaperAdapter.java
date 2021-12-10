package com.kf7mxe.dynamicwallpaper.RecyclerAdapters;

import static android.content.Intent.ACTION_OPEN_DOCUMENT;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.kf7mxe.dynamicwallpaper.R;
import com.kf7mxe.dynamicwallpaper.SelectActionsFragment;
import com.kf7mxe.dynamicwallpaper.models.Collection;

import java.io.File;
import java.util.ArrayList;

public class SelectSpecificWallpaperAdapter extends RecyclerView.Adapter<SelectSpecificWallpaperAdapter.ViewHolder>  {
        private final ArrayList<String> m_photos;
        private Collection mCollection;
        private Context mContext;
        private SelectActionsFragment mFragment;
    public SelectSpecificWallpaperAdapter(Collection collection, SelectActionsFragment fragment, Context context) {
            mCollection = collection;
            mContext = context;
            mFragment = fragment;
            m_photos = collection.getPhotoNames();
        }

        @NonNull
        @Override
        public SelectSpecificWallpaperAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.fragment_select_specific_image_list_dialog_list_dialog_item,parent,false);

            return new SelectSpecificWallpaperAdapter.ViewHolder(view);

        }

    protected class ViewHolder extends RecyclerView.ViewHolder {

        //final TextView text;
        ImageView imageView;

        ViewHolder(View view) {
            super(view);
            imageView = view.findViewById(R.id.selectSpecificImageView);
            //text = binding.text;
        }

    }

        @Override
        public void onBindViewHolder(SelectSpecificWallpaperAdapter.ViewHolder holder, int position) {
            //holder.text.setText(String.valueOf(position));
            RequestOptions options = new RequestOptions()
                    .centerCrop()
                    .placeholder(R.drawable.placeholder);
            File fileGlidePrivew1 = new File(mContext.getExternalFilesDir(ACTION_OPEN_DOCUMENT).getAbsolutePath()+"/"+mCollection.getName()+"/"+m_photos.get(position));
            if(fileGlidePrivew1.isFile()){
                Glide.with(mContext).load(fileGlidePrivew1.getAbsolutePath()).apply(options).into(holder.imageView);
            }
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mFragment.updateSelectedSpecificWallpaper(m_photos.get(position));
                }
            });
        }

        @Override
        public int getItemCount() {
            return m_photos.size();
        }


}
