/**
 *
 */
package com.wipromail.sathesh.ui.listeners;

import android.content.Intent;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.wipromail.sathesh.BuildConfig;
import com.wipromail.sathesh.R;
import com.wipromail.sathesh.activity.MailListViewActivity;
import com.wipromail.sathesh.activity.ViewMailActivity;
import com.wipromail.sathesh.constants.Constants;
import com.wipromail.sathesh.fragment.MailListViewFragment;
import com.wipromail.sathesh.fragment.MailListViewFragment.Status;
import com.wipromail.sathesh.sqlite.db.cache.vo.CachedMailHeaderVO;
import com.wipromail.sathesh.util.Utilities;

/**
 * @author sathesh
 *
 */
public class MailListViewListener implements  OnScrollListener, OnItemClickListener, AbsListView.MultiChoiceModeListener,Constants{
    private MailListViewFragment parent;
    private int preLast=-1;

    public MailListViewListener(MailListViewFragment parent){
        this.parent=parent;
    }

    /** LISTVIEW **/

    /**  ONSCROLL LISTENER
     * Load more mails when the last of the mail is in the list view is visible. Also some code for SwipeRefreshLayout
     * (non-Javadoc)
     * @see android.widget.AbsListView.OnScrollListener#onScroll(android.widget.AbsListView, int, int, int)
     */
    @Override
    public void onScroll(AbsListView lw, final int firstVisibleItem,
                         final int visibleItemCount, final int totalItemCount) {

        try {
            //Determine whether its our listview listener
            switch(lw.getId()) {
                case R.id.listView:
                    //Log.d(TAG, "First Visible: " + firstVisibleItem + ". Visible Count: " + visibleItemCount+ ". Total Items:" + totalItemCount);

                    //SWIPE REFRESH
                    //enable Swipe Refresh
                    boolean enable = false;

                    //if there are no items in the list view then enable Swipe Refresh layout
                    if(totalItemCount==0){
                        enable=true;
                    }
                    else if(lw != null && lw.getChildCount() > 0){
                        // check if the first item of the list is visible
                        boolean firstItemVisible = lw.getFirstVisiblePosition() == 0;
                        // check if the top of the first item is visible
                        boolean topOfFirstItemVisible = lw.getChildAt(0).getTop() == 0;
                        // enabling or disabling the refresh layout
                        enable = firstItemVisible && topOfFirstItemVisible;
                    }
                    if(parent.getSwipeRefreshLayout()!=null)  parent.getSwipeRefreshLayout().setEnabled(enable);

                    //Last Item Listener - loads more mails

                    //gets the id for the last item
                    int lastItem = firstVisibleItem + visibleItemCount;

                    //if the last item shown is equal to the total no of items,
                    if(lastItem == totalItemCount) {
                        //to avoid multiple calls for last item
                        if(preLast!=lastItem){
                            //if the more mails thread is not in updating or waiting state
                            if(parent.getMoreMailsThreadState()!= Status.UPDATING && parent.getMoreMailsThreadState()!= Status.WAITING){
                                // if the get new mails thread is not updating
                                int totalCachedRecords = parent.getMailHeadersCacheAdapter().getRecordsCount(parent.getMailType()
                                        , parent.getMailFolderId());
                                //if total cached records is less than minimum no of mails.
                                //this check is to stop initially showing the progress when there only few mails
                                if(totalCachedRecords>=MIN_NO_OF_MAILS){
                                    //if the total number of cached records is less than the total mails in folder
                                    //getTotalMailsInFolder() will be -1 when the GetNewMailThreads is not yet completed even for 1 time.
                                    if(parent.getTotalMailsInFolder()==-1 || totalCachedRecords < parent.getTotalMailsInFolder()){

                                        // if the new mails thread is not updating
                                        if(parent.getNewMailsThreadState() != Status.UPDATING){
                                            if(BuildConfig.DEBUG){
                                                Log.d(TAG, "MailListViewFragment -> Last Item listener");
                                            }
                                            //Call the More mails thread
                                            parent.getMoreMails();	// spawns a thread for network call
                                            lastItem++;	//since loading symbol shown one extra row was added dnamically. To compensate that add 1 to the last visibile item
                                        }
                                        else{
                                            //enter the More Loading thread in to Waiting state. Whent eh New mails thread is done it will invoke this thread again
                                            parent.setMoreMailsThreadState(Status.WAITING);
                                            parent.showMoreLoadingAnimation(); // shows the loading symbol in the end of list view
                                            if(BuildConfig.DEBUG) Log.d(TAG, "MailListViewListener -> went in wait state as GetNewMails is currently updating");
                                        }
                                    }
                                }
                            }
                            if(BuildConfig.DEBUG){
                                Log.d(TAG, "PreLast "+ preLast + " Last Item "+ lastItem);
                            }
                            preLast = lastItem;
                        }
                    }
            }
        } catch (Exception e) {
            Utilities.generalCatchBlock(e, this);
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    /** ITEM CLICK LISTENER
     * (non-Javadoc)
     * @see android.widget.AdapterView.OnItemClickListener#onItemClick(android.widget.AdapterView, android.view.View, int, long)
     */
    @Override
    public void onItemClick(AdapterView<?> parent1, View view, int position,
                            long id) {

        //the pull to refresh list view starts from instead of 0.. fix for that
        CachedMailHeaderVO vo;
        try{
            if(parent1.getItemAtPosition(position)!=null){
                vo = (CachedMailHeaderVO) parent1.getItemAtPosition(position);
                Intent viewMailIntent = new Intent(parent.getActivity().getBaseContext(), ViewMailActivity.class);
                viewMailIntent.putExtra(MailListViewActivity.EXTRA_MESSAGE_CACHED_HEADER, vo);
                parent.getActivity().startActivity(viewMailIntent);
            }
        }
        catch(Exception e){
            if(BuildConfig.DEBUG)
                e.printStackTrace();
        }
    }

    /** ACTION MODE **/

    /** ITEM SELECTED LISTENER
     *
     * @param mode
     * @param position
     * @param id
     * @param checked
     */
    @Override
    public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
        try {

            // Capture total checked items
            final int checkedCount = parent.getListView().getCheckedItemCount();
            // Set the CAB title according to total checked items
            mode.setTitle(checkedCount + " Selected");

            Log.d(TAG, "position Checked " + position + " id " + id );
        } catch (Exception e) {
            Utilities.generalCatchBlock(e, this);
        }
    }


    /** Oncreate - Load the menus in create
     *
     * @param mode
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        mode.getMenuInflater().inflate(R.menu.actionmode_maillistview, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    /** Item click action in the action mode menu bar
     *
     * @param mode
     * @param item
     * @return
     */
    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.actionMode_deleteMail:

                // Close CAB
                mode.finish();

                return true;
            default:
                return false;
        }
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
    }

}
