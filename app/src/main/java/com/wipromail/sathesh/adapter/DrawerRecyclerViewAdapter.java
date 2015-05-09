package com.wipromail.sathesh.adapter;

import android.app.Activity;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.wipromail.sathesh.R;
import com.wipromail.sathesh.application.interfaces.MailListActivityDataPasser;
import com.wipromail.sathesh.constants.Constants;

/**
 * Adapter for the planet data used in our drawer menu,
 */
public class DrawerRecyclerViewAdapter extends RecyclerView.Adapter<DrawerRecyclerViewAdapter.ViewHolder> implements Constants{
    private int itemCount=0;
    private OnRecyclerViewClickListener listener;
    private String[] mailFolders;
    private String[] mailFolderIcons;
    private MailListActivityDataPasser activity;

    /**
     * Interface for receiving click events from cells.
     */
    public interface OnRecyclerViewClickListener {
        public void onDrawerLayoutRecyclerViewClick(View view, int position);
    }

    /**
     * Interface for type of row specification
     */
    public interface Type {
        public final int HEADER_IMAGE=0;
        public final int HEADER_ROW=1;
        public final int ROW_ITEM=2;
    }

    // Constructor
    public DrawerRecyclerViewAdapter(final MailListActivityDataPasser activity, String[] mailFolders, String[] mailFolderIcons, OnRecyclerViewClickListener listener) {
        this.listener = listener;

        this.mailFolders = mailFolders;
        this.mailFolderIcons = mailFolderIcons;
        this.activity = activity;

        // calculate the total item count.
        itemCount +=  mailFolders.length;
        //header image (1) + mail folders length
    }

    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup viewGroup, int viewType) {
        LayoutInflater vi = LayoutInflater.from(viewGroup.getContext());
        View view=null;
        switch(viewType) {
            case Type.HEADER_IMAGE:
                break;
            case Type.HEADER_ROW:
                break;
            case Type.ROW_ITEM:
                view = vi.inflate(R.layout.drawer_item_row, viewGroup, false);
                break;
        }
        return new ViewHolder(view, viewType);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        switch(holder.viewType) {
            case Type.HEADER_IMAGE:
                break;
            case Type.HEADER_ROW:
                break;
            case Type.ROW_ITEM:
                holder.mailFolderNameTextView.setText(mailFolders[position]);
                holder.fontIconView.setText(mailFolderIcons[position]);

                // setting row on click listener
                if (holder.view != null) {

                    // Highlight the row if its a selected position
                    if ( activity.getDrawerLayoutSelectedPosition() == position) {
                        //selected row
                        holder.itemView.setBackgroundColor(((Activity)activity).getResources().getColor(R.color.LightGrey));
                        //font icon
                        holder.fontIconView.setTextColor(Color.BLACK);
                        holder.fontIconView.setAlpha(1f);
                        // text view
                        holder.mailFolderNameTextView.setTextColor(Color.BLACK);
                        holder.mailFolderNameTextView.setAlpha(1f);

                    }
                    else {
                        //normal row
                        holder.itemView.setBackgroundColor(Color.TRANSPARENT);
                        //font icon
                        holder.fontIconView.setTextColor(Color.BLACK);
                        holder.fontIconView.setAlpha(.6f);
                        // textview
                        holder.mailFolderNameTextView.setTextColor(Color.BLACK);
                        holder.mailFolderNameTextView.setAlpha(.8f);
                    }

                    //setting onClick listener for the row
                    holder.view.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            //here you inform view that something was change - view will be invalidated
                            notifyDataSetChanged();
                            listener.onDrawerLayoutRecyclerViewClick(view, position);
                        }
                    });
                }
                break;
        }
    }

    /**
     * Custom ViewHolder. One object will be created for each row in the OnCreateViewHolder
     */
    public static class ViewHolder extends RecyclerView.ViewHolder implements Constants{
        public int viewType;
        public  View view; // a row
        public  TextView mailFolderNameTextView;
        public  TextView fontIconView;

        public ViewHolder(View v, int viewType) {
            super(v);
            this.viewType = viewType;
            view=v; // for assigning the onclick listener

            switch(this.viewType) {
                case Type.HEADER_IMAGE:
                    break;
                case Type.HEADER_ROW:
                    break;
                case Type.ROW_ITEM:
                    mailFolderNameTextView = (TextView) view.findViewById(R.id.mailFolderName);
                    fontIconView = (TextView) view.findViewById(R.id.fontIconView);
                    break;
            }
        }
    }

    @Override
    public int getItemCount() {
        return itemCount;
    }

    // Setting the view type as an int so that it will tell us back in row creation (onCreateViewHolder)
    @Override
    public int getItemViewType(int position) {
        return Type.ROW_ITEM;
    }
}
