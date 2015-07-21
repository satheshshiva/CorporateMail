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
import com.wipromail.sathesh.application.MyActivity;
import com.wipromail.sathesh.constants.Constants;
import com.wipromail.sathesh.constants.DrawerMenuRowType;
import com.wipromail.sathesh.sqlite.db.cache.dao.MoreFoldersDAO;
import com.wipromail.sathesh.sqlite.db.cache.vo.MoreFoldersVO;

import java.util.List;

/**
 * More Folders Drawer Menu adapter
 */
public class DrawerRecyclerViewMoreFoldersAdapter extends RecyclerView.Adapter<DrawerRecyclerViewMoreFoldersAdapter.ViewHolder> implements Constants{
    private OnRecyclerViewClick2Listener listener;
    private List<MoreFoldersVO> drawerMenuVOList;
    private MailListActivityDataPasser activity;
    private MoreFoldersDAO moreFoldersDAO;

    /**
     * Interface for receiving click events from cells.
     */
    public interface OnRecyclerViewClick2Listener {
        void onDrawerLayoutRecyclerView2Click(View view, int position, MoreFoldersVO drawerMenuVO);
    }

    // Constructor
    public DrawerRecyclerViewMoreFoldersAdapter(final MailListActivityDataPasser activity, OnRecyclerViewClick2Listener listener) {
        this.listener = listener;
        this.activity = activity;
        this.moreFoldersDAO = new MoreFoldersDAO((MyActivity)activity);

    }

    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup viewGroup, int viewType) {
        LayoutInflater vi = LayoutInflater.from(viewGroup.getContext());
        View view;
        switch(viewType) {
            //for header load this layout
            case DrawerMenuRowType.MoreFolders.HEADER:
                view = vi.inflate(R.layout.drawer_header_row, viewGroup, false);
                break;
            default:
                //for item row load this layout
                view = vi.inflate(R.layout.drawer_item_row, viewGroup, false);
                break;
        }
        return new ViewHolder(view, viewType);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        final MoreFoldersVO drawerMenuVO = drawerMenuVOList.get(position);

        switch(holder.viewType) {
            case DrawerMenuRowType.MoreFolders.HEADER:
                //for header row
                if(drawerMenuVO.getName().equalsIgnoreCase("MsgFolderRoot")){
                    // for root display text "More Folders"
                    holder.mailFolderNameTextView.setText(activity.getString(R.string.drawer_menu_more_folders));
                }
                else {
                    holder.mailFolderNameTextView.setText(drawerMenuVO.getName());
                }
                break;

            //for empty clear everthing
            case DrawerMenuRowType.EMPTY_ROW:
                holder.mailFolderNameTextView.setText("");
                holder.fontIconView.setText("");
                break;

            default:
                holder.mailFolderNameTextView.setText(drawerMenuVO.getName());
                holder.fontIconView.setText(drawerMenuVO.getFont_icon());

                // setting row on click listener
                if (holder.view != null) {

                    // Highlight the row if its a selected position
                    if ( activity.getDrawerLayoutSelectedPosition2() == position) {
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
                            listener.onDrawerLayoutRecyclerView2Click(view, position, drawerMenuVO);
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

    /** Private method for updating local VOs. Should be called before every notifydataSetChanged.
     *
     */
    public List<MoreFoldersVO> updateVO() throws Exception {
        this.drawerMenuVOList = moreFoldersDAO.getAllRecords();
        return drawerMenuVOList;

    }
}
