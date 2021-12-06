package com.kf7mxe.dynamicwallpaper.RecyclerAdapters;

import static android.content.Intent.ACTION_OPEN_DOCUMENT;

import android.content.Context;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.kf7mxe.dynamicwallpaper.R;
import com.kf7mxe.dynamicwallpaper.models.Collection;
import com.kf7mxe.dynamicwallpaper.models.SubCollection;

import java.io.File;
import java.util.ArrayList;

public class ViewChangeCollectionsImagesAdapter extends RecyclerView.Adapter<ViewChangeCollectionsImagesAdapter.ViewHolder> {
    private ArrayList<String> m_data;
    private Context m_context;
    private Collection m_collection;
    private NavController m_navController;


    public ViewChangeCollectionsImagesAdapter(Context context , Collection collection,NavController navController) {
        m_collection = collection;
        m_context =context;
        m_data = collection.getPhotoNames();
        m_navController = navController;

    }

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView image;
        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View
            image = (ImageView) view.findViewById(R.id.collectionViewImagesOrderInCardImageView);


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
    public ViewChangeCollectionsImagesAdapter(ArrayList<String> dataSet) {
        m_data = dataSet;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewChangeCollectionsImagesAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(m_context).inflate(R.layout.view_change_collection_photos_card,viewGroup,false);

        return new ViewChangeCollectionsImagesAdapter.ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewChangeCollectionsImagesAdapter.ViewHolder viewHolder, final int position) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        if(position==m_collection.getPhotoNames().size()){
            viewHolder.image.setImageDrawable(m_context.getDrawable(R.drawable.ic_baseline_add_photo_alternate_24));
            viewHolder.image.setScaleType(ImageView.ScaleType.FIT_XY);
            viewHolder.image.setPadding(10,10,10,10);
            viewHolder.getImageView().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    m_navController.navigate(R.id.action_viewChangePhotoOrderFragment_to_selectImagesForSubCollectionFragment);
                }
            });
            return;
        }


        RequestOptions options = new RequestOptions()
                .centerCrop()
                .placeholder(R.drawable.placeholder);
        File fileGlide = new File(m_context.getExternalFilesDir(ACTION_OPEN_DOCUMENT).getAbsolutePath()+"/"+m_collection.getName()+"/"+m_data.get(position));
        if(fileGlide.isFile()){
            Glide.with(m_context).load(fileGlide.getAbsolutePath()).apply(options).into(viewHolder.image);
        }
       // viewHolder.getSubCollectionTitle().setText(m_collections.get(position));

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return m_data.size()+1;
    }{

    }
}
