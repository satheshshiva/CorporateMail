/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wipromail.sathesh.adapter;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.wipromail.sathesh.R;
import com.wipromail.sathesh.constants.Constants;
import com.wipromail.sathesh.fragment.MailListViewFragment;

/**
 * Adapter for the planet data used in our drawer menu,
 */
public class DrawerRecyclerViewAdapter extends RecyclerView.Adapter<DrawerRecyclerViewAdapter.ViewHolder> implements Constants{
    private int itemCount=0;
    private OnRecyclerViewClickListener listener;
    private String[] mailFolders;
    private String[] mailFolderIcons;
    private MailListViewFragment parent;

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
    public DrawerRecyclerViewAdapter(final MailListViewFragment parent, String[] mailFolders, String[] mailFolderIcons, OnRecyclerViewClickListener listener) {
        this.listener = listener;

        this.mailFolders = mailFolders;
        this.mailFolderIcons = mailFolderIcons;
        this.parent = parent;

        // calculate the total item count.
        itemCount += 1 + mailFolders.length;
        //header image (1) + mail folders length
    }

    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup viewGroup, int viewType) {
        LayoutInflater vi = LayoutInflater.from(viewGroup.getContext());
        View view=null;
        switch(viewType) {
            case Type.HEADER_IMAGE:
                view = vi.inflate(R.layout.drawer_item_header_image, viewGroup, false);
                break;
            case Type.HEADER_ROW:
                break;
            case Type.ROW_ITEM:
                view = vi.inflate(R.layout.drawer_item_row, viewGroup, false);

                //setting on click listener for the row item
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //get the selected position from the tag stored in OnBind
                        parent.setDrawerLayoutSelectedPosition(((ViewHolder) view.getTag()).getLayoutPosition());

                        //here you inform view that something was change - view will be invalidated
                        notifyDataSetChanged();
                        view.requestFocus();
                        listener.onDrawerLayoutRecyclerViewClick(view, parent.getDrawerLayoutSelectedPosition());
                    }
                });
                break;
        }
        return new ViewHolder(view, viewType);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {

        switch(holder.viewType) {
            case Type.HEADER_IMAGE:
                break;
            case Type.HEADER_ROW:
                break;
            case Type.ROW_ITEM:
                holder.mTextView.setText(mailFolders[position-1]);
                holder.fontIconView.setText(mailFolderIcons[position-1]);

                // setting row on click listener
                if (holder.view != null) {
                    // Highlight the row if its a selected position
                    if ( parent.getDrawerLayoutSelectedPosition() == position)
                        holder.itemView.setBackgroundColor(Color.GRAY);
                    else
                        holder.itemView.setBackgroundColor(Color.WHITE);
                    holder.view.setTag(holder); // used to get the selected position in OnCreateViewHolder
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
        public  TextView mTextView;
        public  TextView fontIconView;

        public ViewHolder(View v, int vt) {
            super(v);
            viewType = vt;
            view=v; // for assigning the onclick listener

            switch(viewType) {
                case Type.HEADER_IMAGE:
                    break;
                case Type.HEADER_ROW:
                    break;
                case Type.ROW_ITEM:
                    mTextView = (TextView) view.findViewById(R.id.text1);
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
        if (position==0) {
            return Type.HEADER_IMAGE;
        }
        return Type.ROW_ITEM;
    }
}
