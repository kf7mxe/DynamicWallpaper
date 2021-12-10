package com.kf7mxe.dynamicwallpaper.RecyclerAdapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.kf7mxe.dynamicwallpaper.R;
import com.kf7mxe.dynamicwallpaper.models.Collection;
import com.kf7mxe.dynamicwallpaper.models.Rule;

import java.util.ArrayList;
import java.util.List;

public class RulesRecyclerAdapter extends RecyclerView.Adapter<RulesRecyclerAdapter.ViewHolder> {
    private ArrayList<Rule> m_collections;
    private Context m_context;

    public RulesRecyclerAdapter(Context context, ArrayList<Rule> allCollections) {
        m_context =context;
        m_collections = allCollections;
    }

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView triggerTextView;
        private final TextView actionTextView;
        private ImageView deleteButton;
        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View
            deleteButton = (ImageView) view.findViewById(R.id.deleteRuleButton);
            triggerTextView = (TextView) view.findViewById(R.id.triggerType);
            actionTextView = (TextView) view.findViewById(R.id.ruleCardAction);

        }

        public TextView getTriggerTextView() {
            return triggerTextView;
        }
        public TextView getActionTextView(){
            return actionTextView;
        }
        public void setTriggerTextView(String text){
            this.triggerTextView.setText(text);
        }
        public  void setActionTextView(String text){
            this.actionTextView.setText(text);
        }
    }

    /**
     * Initialize the dataset of the Adapter.
     *
     * @param dataSet String[] containing the data to populate views to be used
     * by RecyclerView.
     */
    public RulesRecyclerAdapter(ArrayList<Rule> dataSet) {
        m_collections = dataSet;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(m_context).inflate(R.layout.rule_card,viewGroup,false);

        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        int item = position;

        viewHolder.setTriggerTextView(m_collections.get(position).getTrigger().getTriggerType());
        viewHolder.setActionTextView(m_collections.get(position).getAction().getType());
        viewHolder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                m_collections.remove(item);
                notifyDataSetChanged();
            }
        });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return m_collections.size();
    }
}
