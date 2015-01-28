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

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.wipromail.sathesh.R;
import com.wipromail.sathesh.constants.Constants;

/**
 * Adapter for the planet data used in our drawer menu,
 */
public class DrawerRecyclerViewAdapter extends RecyclerView.Adapter<DrawerRecyclerViewAdapter.ViewHolder> {
    private String[] mDataset;
    private OnRecyclerViewClickListener mListener;

    // Constructor
    public DrawerRecyclerViewAdapter(String[] myDataset, OnRecyclerViewClickListener listener) {
        mDataset = myDataset;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater vi = LayoutInflater.from(parent.getContext());
        View v=null;
        switch(viewType) {
            case Type.HEADER_IMAGE:
                v = vi.inflate(R.layout.drawer_item_header_image, parent, false);
                break;
            case Type.ROW_ITEM:
                v = vi.inflate(R.layout.drawer_item_row, parent, false);
                break;
        }
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        if(position!=0) {
            //bind the data
            holder.mTextView.setText(mDataset[position]);
        }
        // bind the onclick listener for the this view(row)
        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onDrawerLayoutRecyclerViewClick(view, position);
            }
        });
    }

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

    /**
     * Custom ViewHolder
     */
    public static class ViewHolder extends RecyclerView.ViewHolder implements Constants{
        public final View view; // a row
        public final TextView mTextView;

        public ViewHolder(View v) {
            super(v);
            view=v; // for assigning the onclick listener
            mTextView = (TextView) view.findViewById(R.id.text1);
        }
    }

    @Override
    public int getItemCount() {
        return mDataset.length;
    }

    // With the following method we can check what type of view is being passed
    @Override
    public int getItemViewType(int position) {
        if (position==0) {
            return Type.HEADER_IMAGE;
        }
        return Type.ROW_ITEM;
    }
}
