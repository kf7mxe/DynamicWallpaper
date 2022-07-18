package com.kf7mxe.dynamicwallpaper.RecyclerAdapters;

import static android.content.Intent.ACTION_OPEN_DOCUMENT;

import androidx.navigation.NavController;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.card.MaterialCardView;
import com.kf7mxe.dynamicwallpaper.R;
import com.kf7mxe.dynamicwallpaper.models.Collection;

import java.io.File;
import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link PlaceholderItem}.
 * TODO: Replace the implementation with code for your data type.
 */
public class HomeCollectionRecyclerViewAdapter extends RecyclerView.Adapter<HomeCollectionRecyclerViewAdapter.ViewHolder> {
    private List<Collection> m_collections;
    private Context m_context;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor myEditor;
    private NavController m_navController;
    private Long selectedId;

    public HomeCollectionRecyclerViewAdapter(Context context, List<Collection> allCollections,NavController navController) {
        m_context =context;
        m_collections = allCollections;
        m_navController = navController;
        sharedPreferences = context.getSharedPreferences("sharedPrefrences",Context.MODE_PRIVATE);
        myEditor = sharedPreferences.edit();
        if(!sharedPreferences.getString("selectedCollection","").equals("")){
            selectedId = Long.parseLong(sharedPreferences.getString("selectedCollection",""));
        } else {selectedId=(long)0.0;}
    }

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final MaterialCardView card;
        private final Button editButton;
        private final Button selectButton;
        private final TextView nameOfCollection;
        private final LinearLayout triggers;
        private final LinearLayout actions;
        private final ImageView imagePreview1;
        private final ImageView imagePreview2;
        private final ImageView imagePreview3;
        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View
            selectButton = (Button) view.findViewById(R.id.selectCollectionCardButton);
            editButton = (Button) view.findViewById(R.id.editCollectionCardButton);
            card = (MaterialCardView) view.findViewById(R.id.collectionCardView);
            nameOfCollection = (TextView) view.findViewById(R.id.collectionNameCollectionListCard);
            triggers = (LinearLayout) view.findViewById(R.id.triggersLinearView);
            actions = (LinearLayout) view.findViewById(R.id.actionsLinearView);
            imagePreview1 = (ImageView) view.findViewById(R.id.imagePreview1);
            imagePreview2 = (ImageView) view.findViewById(R.id.imagePreview2);
            imagePreview3 = (ImageView) view.findViewById(R.id.imagePreview3);

        }

       // public TextView getTriggerTextView() {
           // return triggerTextView;
        //}
//        public TextView getActionTextView(){
//            return actionTextView;
//        }
//        public void setTriggerTextView(String text){
//            this.triggerTextView.setText(text);
//        }
//        public  void setActionTextView(String text){
//            this.actionTextView.setText(text);
//        }
    }

    /**
     * Initialize the dataset of the Adapter.
     *
     * @param dataSet String[] containing the data to populate views to be used
     * by RecyclerView.
     */
    public HomeCollectionRecyclerViewAdapter(List<Collection> dataSet) {
        m_collections = dataSet;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public HomeCollectionRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(m_context).inflate(R.layout.collection_list_item,viewGroup,false);

        return new HomeCollectionRecyclerViewAdapter.ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(HomeCollectionRecyclerViewAdapter.ViewHolder viewHolder, final int position) {


        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.nameOfCollection.setText(m_collections.get(position).getName());
        if(m_collections.get(position).getRules().size()!=0){
            viewHolder.triggers.removeAllViews();
            viewHolder.actions.removeAllViews();
            TextView triggerTitle =new TextView(m_context);
            TextView actionTitle = new TextView(m_context);
            triggerTitle.setText("Triggers");
            actionTitle.setText("Actions");
            viewHolder.triggers.addView(triggerTitle);
            viewHolder.actions.addView(actionTitle);
            for(int i=0;i<m_collections.get(position).getRules().size();i++){
                String trigger = m_collections.get(position).getRules().get(i).getTrigger().getDisplayType();
                String action = m_collections.get(position).getRules().get(i).getAction().getDisplayType();
                TextView newTriggerTextView =new TextView(m_context);
                newTriggerTextView.setText(trigger);
                TextView newActionTextView = new TextView(m_context);
                newActionTextView.setText(action);
                viewHolder.triggers.addView(newTriggerTextView);
                viewHolder.actions.addView(newActionTextView);
                View divider = new View(m_context);
                View actionDivider = new View(m_context);
                int[] attrs = { android.R.attr.listDivider };
                @SuppressLint("ResourceType") TypedArray ta = m_context.getApplicationContext().obtainStyledAttributes(attrs);
                Drawable dividerBackground = ta.getDrawable(0);
                ta.recycle();
                divider.setBackground(dividerBackground);
                divider.setLayoutParams(new LinearLayout.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT,5));
                actionDivider.setBackground(dividerBackground);
                actionDivider.setLayoutParams(new LinearLayout.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT,5));
                viewHolder.triggers.addView(divider);
                viewHolder.actions.addView(actionDivider);
            }
        }

        if(m_collections.get(position).getPhotoNames().size()!=0){
            RequestOptions options = new RequestOptions()
                    .centerCrop()
                    .placeholder(R.drawable.placeholder);
            File fileGlidePrivew1 = new File(m_context.getExternalFilesDir(ACTION_OPEN_DOCUMENT).getAbsolutePath()+"/"+m_collections.get(position).getName()+"/"+m_collections.get(position).getPhotoNames().get(0));
            if(fileGlidePrivew1.isFile()){
                Glide.with(m_context).load(fileGlidePrivew1.getAbsolutePath()).apply(options).into(viewHolder.imagePreview1);
            }
            if(m_collections.get(position).getPhotoNames().size()>1) {
                File fileGlidePrivew2 = new File(m_context.getExternalFilesDir(ACTION_OPEN_DOCUMENT).getAbsolutePath() + "/" + m_collections.get(position).getName() + "/" + m_collections.get(position).getPhotoNames().get(1));
                if (fileGlidePrivew2.isFile()) {
                    Glide.with(m_context).load(fileGlidePrivew2.getAbsolutePath()).apply(options).into(viewHolder.imagePreview2);
                }
            }
            if(m_collections.get(position).getPhotoNames().size()>2) {
                File fileGlidePrivew3 = new File(m_context.getExternalFilesDir(ACTION_OPEN_DOCUMENT).getAbsolutePath() + "/" + m_collections.get(position).getName() + "/" + m_collections.get(position).getPhotoNames().get(2));
                if (fileGlidePrivew3.isFile()) {
                    Glide.with(m_context).load(fileGlidePrivew3.getAbsolutePath()).apply(options).into(viewHolder.imagePreview3);
                }
            }
        }

        if(m_collections.get(position).getId()==selectedId){
            viewHolder.card.setChecked(true);
            viewHolder.selectButton.setText("Deselect");
        } else {
            viewHolder.card.setChecked(false);
            viewHolder.selectButton.setEnabled(true);
        }

        int item =position;

        viewHolder.selectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Long testPreviousRemoveBroadcast = selectedId;
                if(selectedId==0.0){

                } else if (viewHolder.selectButton.getText().equals("Deselect")) {
                    for (int i = 0; i < m_collections.size(); i++) {
                        if (m_collections.get(i).getId() == selectedId) {
                            m_collections.get(i).removeTriggersBroadcastRecievers(m_context);
                            break;
                        }
                    }
                    myEditor.putString("selectedCollection",Double.toString(0.0));
                    myEditor.commit();
                    notifyDataSetChanged();
                    return;
                } else {
                    int indexToRemove = Integer.parseInt(selectedId.toString()) - 1;
                    for (int i = 0; i < m_collections.size(); i++) {
                        if (m_collections.get(i).getId() == selectedId) {
                            m_collections.get(i).removeTriggersBroadcastRecievers(m_context);
                            break;
                        }
                    }
                }
                myEditor.putString("selectedCollection",Long.toString(m_collections.get(item).getId()));
                myEditor.commit();
                selectedId = m_collections.get(item).getId();
                m_collections.get(item).startTriggers(m_context);
//                viewHolder.selectButton.setEnabled(false);
                if(!viewHolder.card.isChecked()){
                    viewHolder.card.setChecked(true);
                } else {
                    viewHolder.card.setChecked(false);
                }
                notifyDataSetChanged();
            }
        });

        viewHolder.editButton.setOnClickListener(new View.OnClickListener()  {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putLong("collectionId",m_collections.get(item).getId());
                bundle.putBoolean("update",true);
                bundle.putBoolean("fromAddOptionFragment",false);
                m_navController.navigate(R.id.action_homeFragment_to_addCollectionFragment,bundle);
            }
        });

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return m_collections.size();
    }
}