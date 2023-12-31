package com.kf7mxe.dynamicwallpaper.RecyclerAdapters;

import static android.content.Intent.ACTION_GET_CONTENT;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.kf7mxe.dynamicwallpaper.R;
import com.kf7mxe.dynamicwallpaper.models.Collection;
import com.kf7mxe.dynamicwallpaper.models.SubCollection;

import java.io.File;
import java.util.ArrayList;

public class SelectSubCollectionsImagesAdapter extends RecyclerView.Adapter<SelectSubCollectionsImagesAdapter.ViewHolder> {
    private ArrayList<String> m_data;
    private Context m_context;
    private Collection m_collection;
    private SubCollection m_subCollection;


    public SelectSubCollectionsImagesAdapter(Context context , Collection collection, SubCollection subCollection) {
        m_collection = collection;
        m_subCollection = subCollection;
        m_context =context;
        m_data = collection.getPhotoNames();
    }

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView image;
        private final ImageView checkMark;
        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View
            image = (ImageView) view.findViewById(R.id.collectionViewImagesOrderInCardImageView);
            checkMark = (ImageView) view.findViewById(R.id.checkMarkPhotoCard);
        }


        public ImageView getImageView() {
            return image;
        }

    }

    /**
     * Initialize the dataset of the Adapter.
     *
     * @param dataSet String[] containing the data to populate views to be used
     * by RecyclerView.
     */
    public SelectSubCollectionsImagesAdapter(ArrayList<String> dataSet) {
        m_data = dataSet;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public SelectSubCollectionsImagesAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item

        View view = LayoutInflater.from(m_context).inflate(R.layout.view_change_collection_photos_card,viewGroup,false);

        return new SelectSubCollectionsImagesAdapter.ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(SelectSubCollectionsImagesAdapter.ViewHolder viewHolder, final int position) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element


        RequestOptions options = new RequestOptions()
                .centerCrop()
                .placeholder(R.drawable.placeholder);
        File fileGlide = new File(m_context.getFilesDir().getAbsolutePath()+"/"+m_collection.getId()+"/"+m_data.get(position));
        if(fileGlide.isFile()){
            Glide.with(m_context).load(fileGlide.getAbsolutePath()).apply(options).into(viewHolder.image);
        }
        int itemPos = position;
        if(m_subCollection.getFileNames().contains(m_data.get(position))){
                viewHolder.checkMark.setVisibility(View.VISIBLE);
        }

        viewHolder.checkMark.setVisibility(View.INVISIBLE);

       viewHolder.getImageView().setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {

               if(m_subCollection.getFileNames().size()!=0) {
                   if (m_subCollection.getFileNames().contains(m_collection.getPhotoNames().get(itemPos))) {
                       viewHolder.checkMark.setVisibility(View.INVISIBLE);
                       m_subCollection.getFileNames().remove(m_collection.getPhotoNames().get(itemPos));
                   } else {
                       viewHolder.checkMark.setVisibility(View.VISIBLE);
                       m_subCollection.getFileNames().add(m_collection.getPhotoNames().get(itemPos));
                   }
               } else {
                   viewHolder.checkMark.setVisibility(View.VISIBLE);
                   m_subCollection.getFileNames().add(m_collection.getPhotoNames().get(itemPos));
               }
           }
       });



    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return m_data.size();
    }{

    }
}
