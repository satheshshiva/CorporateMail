package com.sathesh.corporatemail.adapter;

import android.app.Activity;
import android.graphics.Color;
import androidx.recyclerview.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sathesh.corporatemail.R;
import com.sathesh.corporatemail.activity.datapasser.MailListActivityDataPasser;
import com.sathesh.corporatemail.application.MyActivity;
import com.sathesh.corporatemail.constants.Constants;
import com.sathesh.corporatemail.constants.DrawerMenuRowType;
import com.sathesh.corporatemail.sqlite.db.cache.dao.DrawerMenuDAO;
import com.sathesh.corporatemail.sqlite.db.cache.vo.FolderVO;
import com.sathesh.corporatemail.ui.components.FavouritesDialog;
import com.sathesh.corporatemail.util.Utilities;

import java.util.List;

/**
 * Main Drawer Menu adapter
 */
public class DrawerRecyclerViewAdapter extends RecyclerView.Adapter<DrawerRecyclerViewAdapter.ViewHolder> implements Constants{
    private OnRecyclerViewClickListener listener;
    private List<FolderVO> faves, folderVOList;
    private MailListActivityDataPasser activity;
    private DrawerMenuDAO drawerMenuDAO;

    /**
     * Interface for receiving click events from cells.
     */
    public interface OnRecyclerViewClickListener {
        void onDrawerLayoutRecyclerViewClick(View view, int position, FolderVO folderVO);
    }

    // Constructor
    public DrawerRecyclerViewAdapter(final MailListActivityDataPasser activity, OnRecyclerViewClickListener listener) {
        this.listener = listener;
        this.activity = activity;

        this.drawerMenuDAO = new DrawerMenuDAO((MyActivity) activity);

        try {
            updateDataSets();
        } catch (Exception e) {
            Utilities.generalCatchBlock(e,this);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup viewGroup, int viewType) {
        LayoutInflater vi = LayoutInflater.from(viewGroup.getContext());
        View view=null;
        switch(viewType) {
            case DrawerMenuRowType.CONTACTS_HEADER:
            case DrawerMenuRowType.FAVOURITES_HEADER:
                view = vi.inflate(R.layout.drawer_header_row, viewGroup, false);
                break;
            case DrawerMenuRowType.FAVOURITE_HELPTEXT:
                view = vi.inflate(R.layout.drawer_helptext_row, viewGroup, false);
                break;
            default:
                view = vi.inflate(R.layout.drawer_item_row, viewGroup, false);
                break;
        }
        return new ViewHolder(view, viewType);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        final FolderVO folderVO = folderVOList.get(position);

        switch(holder.viewType) {

            case DrawerMenuRowType.CONTACTS_HEADER:
            case DrawerMenuRowType.FAVOURITES_HEADER:
                //for header row
                holder.mailFolderNameTextView.setText(folderVO.getName());
                break;

            //for empty clear everthing
            case DrawerMenuRowType.EMPTY_ROW:
                holder.mailFolderNameTextView.setText("");
                holder.fontIconView.setText("");
                break;
            // dedicated row to show the help text if there are no favourites
            case DrawerMenuRowType.FAVOURITE_HELPTEXT:
                if(faves !=null && faves.size()>0) {
                    // Favourites are present. Hide the Help text
                    holder.helptextLayout.getLayoutParams().height=0;
                }
                else{
                    // No Favourites. Show the help text
                    holder.faveHelptext.setText(folderVO.getName());
                    holder.helptextLayout.getLayoutParams().height=(int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, HELP_TEXT_LAYOUT_HEIGHT, ((MyActivity)activity).getResources().getDisplayMetrics());
                }
                break;

            //for item row
            default:
                holder.mailFolderNameTextView.setText(folderVO.getName());
                holder.fontIconView.setText(folderVO.getFont_icon());

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
                            listener.onDrawerLayoutRecyclerViewClick(view, position, folderVO);
                        }
                    });

                    //setting onClick listener for the row
                    holder.view.setOnLongClickListener(new View.OnLongClickListener(){

                        @Override
                        public boolean onLongClick(View v) {
                            if(folderVO.getType() == DrawerMenuRowType.FAVOURITE_FOLDERS){
                                FavouritesDialog.removeFavourite(activity, folderVO, drawerMenuDAO);
                            }
                            return true;
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
        public  TextView mailFolderNameTextView,fontIconView, faveHelptext;
        public RelativeLayout helptextLayout;

        public ViewHolder(View v, int viewType) {
            super(v);
            this.viewType = viewType;
            view=v; // for assigning the onclick listener

            switch(this.viewType) {
                default:
                    mailFolderNameTextView = (TextView) view.findViewById(R.id.mailFolderName);
                    fontIconView = (TextView) view.findViewById(R.id.fontIconView);
                    faveHelptext = (TextView) view.findViewById(R.id.helpText);
                    helptextLayout = (RelativeLayout) view.findViewById(R.id.helptextLayout);
                    break;
            }
        }
    }

    @Override
    public int getItemCount() {
        return this.folderVOList.size();
    }

    // Setting the view type as an int so that it will tell us back in row creation (onCreateViewHolder)
    @Override
    public int getItemViewType(int position) {
        return folderVOList.get(position).getType();
    }

    /** Private method for updating local VOs. Should be called before every notifydataSetChanged.
     *
     */
    public List<FolderVO> updateDataSets() throws Exception {
        this.folderVOList = drawerMenuDAO.getAllRecords();
        this.faves = drawerMenuDAO.getFaves();
        return folderVOList;

    }
}
