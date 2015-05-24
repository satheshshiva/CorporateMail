package com.wipromail.sathesh.adapter;

import android.app.Activity;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.wipromail.sathesh.R;
import com.wipromail.sathesh.activity.datapasser.MailListActivityDataPasser;
import com.wipromail.sathesh.constants.Constants;
import com.wipromail.sathesh.sqlite.db.cache.vo.DrawerMenuVO;

import java.util.List;

/**
 * Adapter for the planet data used in our drawer menu,
 */
public class DrawerRecyclerViewAdapter extends RecyclerView.Adapter<DrawerRecyclerViewAdapter.ViewHolder> implements Constants{
    private OnRecyclerViewClickListener listener;
    private List<DrawerMenuVO> drawerMenuVOList;
    private MailListActivityDataPasser activity;

    /**
     * Interface for receiving click events from cells.
     */
    public interface OnRecyclerViewClickListener {
        void onDrawerLayoutRecyclerViewClick(View view, int position, DrawerMenuVO drawerMenuVO);
    }

    // Constructor
    public DrawerRecyclerViewAdapter(final MailListActivityDataPasser activity, List<DrawerMenuVO> drawerMenuVOList, OnRecyclerViewClickListener listener) {
        this.listener = listener;

        this.drawerMenuVOList=drawerMenuVOList;
        this.activity = activity;

    }

    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup viewGroup, int viewType) {
        LayoutInflater vi = LayoutInflater.from(viewGroup.getContext());
        View view=null;
        switch(viewType) {
            default:
                view = vi.inflate(R.layout.drawer_item_row, viewGroup, false);
                break;
        }
        return new ViewHolder(view, viewType);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        final DrawerMenuVO drawerMenuVO = drawerMenuVOList.get(position);

        switch(holder.viewType) {
            default:
                holder.mailFolderNameTextView.setText(drawerMenuVO.getMenu_name());
                holder.fontIconView.setText(drawerMenuVO.getFont_icon());

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
                            listener.onDrawerLayoutRecyclerViewClick(view, position, drawerMenuVO);
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
                default:
                    mailFolderNameTextView = (TextView) view.findViewById(R.id.mailFolderName);
                    fontIconView = (TextView) view.findViewById(R.id.fontIconView);
                    break;
            }
        }
    }

    @Override
    public int getItemCount() {
        return this.drawerMenuVOList.size();
    }

    // Setting the view type as an int so that it will tell us back in row creation (onCreateViewHolder)
    @Override
    public int getItemViewType(int position) {
        return drawerMenuVOList.get(position).getType();
    }
}
