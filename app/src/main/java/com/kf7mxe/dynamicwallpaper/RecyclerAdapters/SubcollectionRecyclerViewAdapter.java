package com.kf7mxe.dynamicwallpaper.RecyclerAdapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.kf7mxe.dynamicwallpaper.R;
import com.kf7mxe.dynamicwallpaper.models.Rule;
import com.kf7mxe.dynamicwallpaper.models.SubCollection;

import java.util.ArrayList;

public class SubcollectionRecyclerViewAdapter extends RecyclerView.Adapter<SubcollectionRecyclerViewAdapter.ViewHolder> {
    private ArrayList<SubCollection> m_collections;
    private Context m_context;

    public SubcollectionRecyclerViewAdapter(Context context, ArrayList<SubCollection> allCollections) {
        m_context =context;
        m_collections = allCollections;
    }

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView subCollectionTitle;
        private final Button viewSubCollectionButton;
        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View

            subCollectionTitle = (TextView) view.findViewById(R.id.subCollectionNameCardTextview);
            viewSubCollectionButton = (Button) view.findViewById(R.id.viewSubCollectionButtonOnCard);

        }

        public TextView getSubCollectionTitle() {
            return subCollectionTitle;
        }
        public Button getViewSubCollection(){
            return viewSubCollectionButton;
        }

    }

    /**
     * Initialize the dataset of the Adapter.
     *
     * @param dataSet String[] containing the data to populate views to be used
     * by RecyclerView.
     */
    public SubcollectionRecyclerViewAdapter(ArrayList<SubCollection> dataSet) {
        m_collections = dataSet;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public SubcollectionRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(m_context).inflate(R.layout.subcollection_card,viewGroup,false);

        return new SubcollectionRecyclerViewAdapter.ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(SubcollectionRecyclerViewAdapter.ViewHolder viewHolder, final int position) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.getSubCollectionTitle().setText(m_collections.get(position).getName());
        viewHolder.getViewSubCollection().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(m_context, "test"+position, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return m_collections.size();
    }{

    }
}
