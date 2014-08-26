/**
 * 
 */
package com.wipromail.sathesh.ui.listeners;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.wipromail.sathesh.BuildConfig;
import com.wipromail.sathesh.R;
import com.wipromail.sathesh.activity.MailListViewActivity;
import com.wipromail.sathesh.activity.ViewMailActivity;
import com.wipromail.sathesh.adapter.MailListViewAdapter;
import com.wipromail.sathesh.constants.Constants;
import com.wipromail.sathesh.fragment.MailListViewFragment;
import com.wipromail.sathesh.sqlite.db.pojo.vo.CachedMailHeaderVO;

/**
 * @author sathesh
 *
 */
public class MailListViewFragmentListener implements  OnScrollListener, OnItemClickListener, Constants{
	private MailListViewFragment parent;
	
	public MailListViewFragmentListener(MailListViewFragment parent){
		this.parent=parent;
	}
	
	/* This is the OnScroll Listener implementation for this listview. 
	 * Load more mails when the last of the mail is in the list view is visible.
	 * (non-Javadoc)
	 * @see android.widget.AbsListView.OnScrollListener#onScroll(android.widget.AbsListView, int, int, int)
	 */
	@Override
	public void onScroll(AbsListView lw, final int firstVisibleItem,
			final int visibleItemCount, final int totalItemCount) {
		//Determine whether its our listener
		switch(lw.getId()) {
		case R.id.listView:  
			//Log.d(TAG, "First Visible: " + firstVisibleItem + ". Visible Count: " + visibleItemCount+ ". Total Items:" + totalItemCount);
			//enable Swipe Refresh
			boolean enable = false;
			if(lw != null && lw.getChildCount() > 0){
				// check if the first item of the list is visible
				boolean firstItemVisible = lw.getFirstVisiblePosition() == 0;
				// check if the top of the first item is visible
				boolean topOfFirstItemVisible = lw.getChildAt(0).getTop() == 0;
				// enabling or disabling the refresh layout
				enable = firstItemVisible && topOfFirstItemVisible;
			}
			if(parent.getSwipeRefreshLayout()!=null)  parent.getSwipeRefreshLayout().setEnabled(enable);

			//Last Item Listener - loads more mails
			int preLast=-1;
			
			final int lastItem = firstVisibleItem + visibleItemCount;
			
			if(!parent.isLoadingSymbolShown() && lastItem == totalItemCount) {
				if(preLast!=lastItem){ //to avoid multiple calls for last item
					if(BuildConfig.DEBUG){
						Log.d(TAG, "MailListViewFragment -> Last Item listener");
					}
					parent.getAdapter().scrolledToLast();
					parent.setLoadingSymbolShown(true);

					// adapter.notifyDataSetChanged();
					preLast = lastItem;
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see android.widget.AbsListView.OnScrollListener#onScrollStateChanged(android.widget.AbsListView, int)
	 */
	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		// TODO Auto-generated method stub

	}
	
	/* (non-Javadoc)
	 * @see android.widget.AdapterView.OnItemClickListener#onItemClick(android.widget.AdapterView, android.view.View, int, long)
	 */
	@Override
	public void onItemClick(AdapterView<?> parent1, View view, int position,
			long id) {
		// TODO Auto-generated method stub

		//the pull to refresh list view starts from instead of 0.. fix for that
		CachedMailHeaderVO vo;

		try{
			vo = (CachedMailHeaderVO) parent1.getItemAtPosition(position);
			Intent viewMailIntent = new Intent(parent.getActivity().getBaseContext(), ViewMailActivity.class);
			viewMailIntent.putExtra(MailListViewActivity.EXTRA_MESSAGE_CACHED_HEADER, vo);
			parent.getActivity().startActivity(viewMailIntent);
		}
		catch(Exception e){
			if(BuildConfig.DEBUG)
				e.printStackTrace();
		}
	}
}
