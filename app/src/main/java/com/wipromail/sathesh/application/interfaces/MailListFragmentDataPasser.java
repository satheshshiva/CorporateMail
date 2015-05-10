package com.wipromail.sathesh.application.interfaces;


import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.widget.ListView;

import com.wipromail.sathesh.adapter.MailListViewAdapter;
import com.wipromail.sathesh.cache.adapter.CachedMailHeaderAdapter;
import com.wipromail.sathesh.fragment.MailListViewFragment;

public interface MailListFragmentDataPasser {

    enum Status{
        UPDATING,
        UPDATED,
        WAITING,	//not the actual Thread.wait(). One thread2 will just exit and the other thread (thread1) will invoke it again once its done.
        ERROR,
        ERROR_AUTH_FAILED
    }

    enum UndoBarStatus{
        IDLE,
        DISPLAYED,
        DELETING
    }

	/** Refresh the List View from network
	 * 
	 */
    void refreshList();
	
	
	/** refreshes the List View from Local cache
	 * 
	 */
    void softRefreshList();


    SwipeRefreshLayout getSwipeRefreshLayout();

    MailListViewFragment.Status getMoreMailsThreadState();

    CachedMailHeaderAdapter getMailHeadersCacheAdapter();

    int getMailType();

    String getMailFolderId();

    long getTotalMailsInFolder();

    MailListViewFragment.Status getNewMailsThreadState();

    void getMoreMails();

    void setMoreMailsThreadState(Status waiting);

    Context getContext();

    void showMoreLoadingAnimation();

    MailListViewAdapter getAdapter();

    ListView getListView();

    void setUndoBarState(UndoBarStatus displayed);

}
