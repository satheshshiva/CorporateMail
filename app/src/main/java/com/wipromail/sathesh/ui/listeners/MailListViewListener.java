/**
 *
 */
package com.wipromail.sathesh.ui.listeners;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.cocosw.undobar.UndoBarController;
import com.wipromail.sathesh.BuildConfig;
import com.wipromail.sathesh.R;
import com.wipromail.sathesh.activity.MailListViewActivity;
import com.wipromail.sathesh.activity.ViewMailActivity;
import com.wipromail.sathesh.constants.Constants;
import com.wipromail.sathesh.fragment.MailListViewFragment;
import com.wipromail.sathesh.fragment.MailListViewFragment.Status;
import com.wipromail.sathesh.ui.action.DeleteMailsUndoBarAction;
import com.wipromail.sathesh.ui.interfaces.UndoBarAction;
import com.wipromail.sathesh.sqlite.db.cache.vo.CachedMailHeaderVO;
import com.wipromail.sathesh.util.Utilities;
import com.wipromail.sathesh.ui.vo.MailListViewContent;

import java.util.ArrayList;

/**
 * @author sathesh
 *
 */
public class MailListViewListener implements  OnScrollListener, OnItemClickListener, AbsListView.MultiChoiceModeListener, UndoBarController.AdvancedUndoListener, Constants {
    private MailListViewFragment parent;
    private int preLast=-1;
    private ArrayList<CachedMailHeaderVO> curentlySelectedVOs = new ArrayList<CachedMailHeaderVO>();

    public MailListViewListener(MailListViewFragment parent){
        this.parent=parent;
    }

    /** LISTVIEW LISTENERS **/

    /**  ListView - On Scroll
     *
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
                   // Log.d(TAG, "First Visible: " + firstVisibleItem + ". Visible Count: " + visibleItemCount+ ". Total Items:" + totalItemCount);

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
                                            //enter the More Loading thread in to Waiting state. Whent eh New mails thread is currently updating.
                                            // when New Mail Loading thread is done it will invoke More Mails Loading thread again
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

    /** ListView - on scroll state changed
     *
     * @param view
     * @param scrollState
     */
    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    /**  ListView - Item Click
     *
     * (non-Javadoc)
     * @see android.widget.AdapterView.OnItemClickListener#onItemClick(android.widget.AdapterView, android.view.View, int, long)
     */
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position,
                            long id) {

        //the pull to refresh list view starts from instead of 0.. fix for that
        CachedMailHeaderVO vo;
        MailListViewContent listViewContent;

        try{
            if(adapterView.getItemAtPosition(position)!=null){
                listViewContent = (MailListViewContent)adapterView.getItemAtPosition(position);

                switch(listViewContent.getType()) {
                    case MailListViewContent.types.MAIL:
                        //open the mail
                        vo = listViewContent.getMailVO();
                        Intent viewMailIntent = new Intent(parent.getActivity().getBaseContext(), ViewMailActivity.class);
                        viewMailIntent.putExtra(MailListViewActivity.EXTRA_MESSAGE_CACHED_HEADER, vo);
                        //start the view mail activity
                        parent.getActivity().startActivity(viewMailIntent);
                        break;
                    case MailListViewContent.types.DATE_HEADER:
                        //setting all child mails for this date header to selected
                        toggleSelectionChildMails(position, true);
                        break;
                    case MailListViewContent.types.LOADING_MORE_MAILS:
                        break;
                    default:
                        break;

                }
            }
        }
        catch(Exception e){
            if(BuildConfig.DEBUG)
                e.printStackTrace();
        }
    }

    /*** CAB LISTENERS ***/

    /** CAB - List Item select
     * a data structure curentlySelectedVOs will hold the selected items mail vos.
     * listview.getCheckedIds or similar will do the job but it will also give the date headers and loading more mails
     * row along with the result
     * @param mode
     * @param position
     * @param id
     * @param checked
     */
    @Override
    public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
        MailListViewContent listViewContent;
        CachedMailHeaderVO vo;
        try {
            if(parent.getAdapter().getItem(position) !=null) {

                listViewContent = (MailListViewContent)parent.getAdapter().getItem(position);

                switch (listViewContent.getType()) {

                    case MailListViewContent.types.MAIL:
                        vo = listViewContent.getMailVO();
                        if(vo!=null){
                            if (checked) {
                                curentlySelectedVOs.add(vo);
                            } else {
                                curentlySelectedVOs.remove(vo);
                            }
                        }

                        // Set the CAB title according to total checked items
                        mode.setTitle(parent.getActivity().getString(R.string.cabSelected, curentlySelectedVOs.size()));
                        break;
                    case MailListViewContent.types.DATE_HEADER:
                        // check if the date header is selected.
                        if(parent.getListView().getCheckedItemPositions().get(position)) {

                            //setting all child mails for this date header to selected
                            toggleSelectionChildMails(position, true);
                            //remove selection for the date header
                            parent.getListView().setItemChecked(position, false);
                            //note: this will again call this same method again
                        }

                        break;
                    case MailListViewContent.types.LOADING_MORE_MAILS:
                        break;
                    default:
                        break;
                }
            }
            else{
                Log.e(TAG, "parent.getAdapter().getItem(position) is null");
            }


        } catch (Exception e) {
            Utilities.generalCatchBlock(e, this);
        }
    }


    /** CAB - OnCreate
     * Load the menus in create
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

    /** CAB - OnPrepareActionMode
     *
     * @param mode
     * @param menu
     * @return
     */
    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    /** CAB - Icon Click
     * Action item in the CAB is clicked
     *
     * @param mode
     * @param item
     * @return
     */
    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        try {
            // we will clone the selected VOs since it will be cleared in CAB destroy
            final ArrayList<CachedMailHeaderVO> selectedVOs = (ArrayList<CachedMailHeaderVO>)curentlySelectedVOs.clone();
            switch (item.getItemId()) {
                case R.id.actionMode_deleteMail:
                    try {
                        if(parent.getMailType() != MailType.DELETED_ITEMS) {
                            //delete the items in cache first and will update UI
                            parent.getMailHeadersCacheAdapter().deleteItems(selectedVOs);

                            //update the UI list (for updating the cached deletions in UI)
                            parent.softRefreshList();

                            parent.setUndoBarState(MailListViewFragment.UndoBarStatus.DISPLAYED);
                            //create an undo action bar with the anonymous class to make a network call to delete
                            final Bundle b = new Bundle();
                            // DeleteMailsUndoBarAction class will have the actions what to do on execute and undo button clicked
                            UndoBarAction undoBarAction = new DeleteMailsUndoBarAction(parent, selectedVOs);

                            //this serializable anonymus class will be executed during the undo bar hides (OnHide())
                            b.putSerializable("serializable", undoBarAction);
                            // Close CAB
                            mode.finish();

                            //show the undo bar
                            new UndoBarController.UndoBar(parent.getActivity())
                                    .message(parent.getActivity().getString(R.string.undoBar_deletedMails, selectedVOs.size()))
                                    .duration(3000)
                                    .listener(this).token(b).show();
                        }else{
                            //Deleted Items folder
                            // Close CAB
                            mode.finish();

                        }

                    } catch (Exception e) {
                        parent.setUndoBarState(MailListViewFragment.UndoBarStatus.IDLE);
                    }

                    return true;
                default:
                    return false;
            }
        }
        catch (Exception e){return false;}
    }

    /** CAB - On destroy
     *
     * @param mode
     */
    @Override
    public void onDestroyActionMode(ActionMode mode) {
        if(curentlySelectedVOs !=null){
            curentlySelectedVOs.clear();
        }
    }

    /*** UNDO BAR LISTENERS ***/

    /** Undo Bar - on Hide
     *
     * @param parcelable
     */
    @Override
    public void onHide(@Nullable Parcelable parcelable) {
        if(BuildConfig.DEBUG) Log.d(TAG, "UndoBar - OnHide() called");
        if (parcelable != null) {
            UndoBarAction action = (UndoBarAction)((Bundle) parcelable).getSerializable("serializable");
            if(action!=null){
                action.executeAction();
            }
        }
    }

    /** Undo Bar - On Clear
     *
     * @param parcelables
     */
    @Override
    public void onClear(@NonNull Parcelable[] parcelables) {
        if(BuildConfig.DEBUG) Log.d(TAG, "OnClear - OnClear() called");

    }

    /** Undo Bar - On Undo
     *
     * @param parcelable
     */
    @Override
    public void onUndo(@Nullable Parcelable parcelable) {
        if(BuildConfig.DEBUG) Log.d(TAG, "UndoBar - OnUndo() called");
        if (parcelable != null) {
            UndoBarAction action = (UndoBarAction)((Bundle) parcelable).getSerializable("serializable");
            if(action!=null){
                action.undoAction();
            }
        }
        parent.setUndoBarState(MailListViewFragment.UndoBarStatus.IDLE);

    }

    /** Select or unselects the mails below a particular date header
     *
     * @param dateHeaderPos position of date header in listview
     * @param childsSelect  true for selection, false for unselection
     */
    private void toggleSelectionChildMails(int dateHeaderPos, boolean childsSelect) {
        // we have to select or unselect all the mails under this date header
        int _position=dateHeaderPos;
        MailListViewContent nextContent;
        while(++_position < parent.getAdapter().getCount()) {
            //next mail
            nextContent = (MailListViewContent) parent.getListView().getItemAtPosition(_position);
            if(nextContent!=null) {
                if (nextContent.getType() == MailListViewContent.types.MAIL) {
                    //if the mail is already selected then do nothing
                    if (parent.getListView().getCheckedItemPositions().get(_position) == childsSelect) {
                        continue;
                    } else {
                        //if the mail is not selected then select it
                        parent.getListView().setItemChecked(_position, childsSelect);
                        //note: this will again call this same method
                    }
                } else {
                    //for types other than mail is reached then exit out of loop
                    break;
                }
            }else{
                continue;   //nextContent is null. may not be possible
            }
        }
    }

}
