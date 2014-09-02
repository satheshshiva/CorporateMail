package com.wipromail.sathesh.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher.ViewFactory;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.wipromail.sathesh.BuildConfig;
import com.wipromail.sathesh.R;
import com.wipromail.sathesh.adapter.MailListViewAdapter;
import com.wipromail.sathesh.animation.ApplyAnimation;
import com.wipromail.sathesh.application.MailApplication;
import com.wipromail.sathesh.application.interfaces.MailListActivityDataPasser;
import com.wipromail.sathesh.application.interfaces.MailListFragmentDataPasser;
import com.wipromail.sathesh.cache.adapter.CachedMailHeaderCacheAdapter;
import com.wipromail.sathesh.constants.Constants;
import com.wipromail.sathesh.handlers.GetMoreMailsHandler;
import com.wipromail.sathesh.handlers.GetNewMailsHandler;
import com.wipromail.sathesh.handlers.runnables.GetMoreMailsRunnable;
import com.wipromail.sathesh.handlers.runnables.GetNewMailsRunnable;
import com.wipromail.sathesh.ui.listeners.MailListViewFragmentListener;
import com.wipromail.sathesh.util.Utilities;

/**
 * @author sathesh
 * This fragment is used to load only the MailFunctions.
 */

public class MailListViewFragment extends Fragment implements Constants, MailListFragmentDataPasser {

	// ListFragment is a very useful class that provides a simple ListView inside of a Fragment.
	// This class is meant to be sub-classed and allows you to quickly build up list interfaces
	// in your app.
	private MailListActivityDataPasser activityDataPasser ;
	public SherlockFragmentActivity activity ;
	private Context context ;

	private MailListViewAdapter adapter;
	private TextSwitcher textswitcher;

	private int mailType;
	private String  mailFolderName;
	private String mailFolderId;

	private ProgressBar circle_progressbar;

	private ImageView successIcon, failureIcon, readIcon, unreadIcon;

	private ActionBar myActionBar;

	private ProgressBar bar_progressbar;
	private ListView listView;
	private CachedMailHeaderCacheAdapter cacheAdapter;
	private int totalCachedRecords=0;

	private SwipeRefreshLayout swipeRefreshLayout ;
	private State newMailsThreadState;
	private State moreMailsThreadState;
	
	private long totalMailsInFolder=-1;

	private boolean fragmentAlreadyLoaded=false;
	
	/**
	 * @author sathesh
	 *
	 */
	public enum State{
		UPDATING,
		UPDATED,
		WAITING,	//not the actual Thread.wait(). One thread2 will just exit and the other thread (thread1) will invoke it again once its done.
		ERROR,
		ERROR_AUTH_FAILED
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_mail_list_view,
				container, false);

		activity = (SherlockFragmentActivity) getActivity();
		context = (SherlockFragmentActivity) getActivity();
		activityDataPasser = (MailListActivityDataPasser)getActivity();
		
		if (cacheAdapter==null){
			cacheAdapter = new CachedMailHeaderCacheAdapter(context);
		}
		setRetainInstance(true);
		
		if(activity != null){
			try {
				//List View Initialization
				listView = (ListView)view.findViewById(R.id.listView);

				//Text Switcher Initiliazation
				textswitcher = (TextSwitcher)view.findViewById(R.id.textswitcher);

				//Text Switcher customze with text view
				textswitcher.setFactory(new ViewFactory() {
					public View makeView() {
						// Custom text view since the default one auto applies center gravity
						TextView textView = new TextView(activity);
						textView.setGravity(Gravity.LEFT);
						textView.setTextSize(12);
						return textView;
					}
				});

				//animation for text switcher
				ApplyAnimation.setTitleInboxStatusTextSwitcher(activity, textswitcher);

				//mailtype, folname and folder id get from activity
				mailType = activityDataPasser.getMailType();
				mailFolderName = activityDataPasser.getMailFolderName();
				mailFolderId = activityDataPasser.getStrFolderId();

				//action bar initialize 
				myActionBar = activity.getSupportActionBar();
				//progress bar initialize 
				circle_progressbar = (ProgressBar)view.findViewById(R.id.circle_progressbar);

				//icons initialize
				successIcon = (ImageView)view.findViewById(R.id.success_icon);
				failureIcon = (ImageView)view.findViewById(R.id.failure_icon);
				readIcon = (ImageView)view.findViewById(R.id.read_icon);
				unreadIcon = (ImageView)view.findViewById(R.id.unread_icon);
				bar_progressbar = (ProgressBar)view.findViewById(R.id.bar_progressbar);

				//update mail type in the action bar title
				myActionBar.setTitle(getMailFolderDisplayName(mailType));
				myActionBar.setDisplayHomeAsUpEnabled(true);

				//initializes the adapter and associates the listview. 
				//this set  of code when placed when placed few lines before wont initialize and is giving empty listview. dont know why.
				//get the cursor

				if(adapter==null){	//on config change it wont be null
					//initialize the adapter
					adapter = new MailListViewAdapter(context, cacheAdapter.getMailHeaders(mailType, mailFolderName, mailFolderId));
				}

				// initializes the list view with the adapter. also will place all the cached mails in list view initially
				listView.setAdapter(adapter);

				//Initialize SwipeRefreshLayout
				swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);
				// the refresh listner. this would be called when the layout is pulled down
				swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
					@Override
					public void onRefresh() {
						//refresh list when the SwipeRefresh is pulled
						refreshList();
					}
				});
				// sets the colors used in the refresh animation
				int[] resources= MailApplication.getSwipeRefreshLayoutColorResources();
				if(resources.length==4){
					swipeRefreshLayout.setColorSchemeResources(resources[0],resources[1],resources[2],resources[3]);
				}
				
				//Action
				//if the activity is recreated, and if the thread is already updating then update the UI status
				if(newMailsThreadState==State.UPDATING) {
					updatingStatusUIChanges();
				}
				
				if(moreMailsThreadState==State.WAITING || moreMailsThreadState==State.UPDATING){
					showMoreLoadingAnimation();
					
				}
				//make a refresh only once when the screen is loaded first time. make a soft refresh on config change
				if(!fragmentAlreadyLoaded){
					refreshList();
				}
				else{
					//config change (Screen rotation)
					softRefreshList();
				}
				fragmentAlreadyLoaded=true; //tracks config change(screen rotation)

			} catch (Exception e) {
				// TODO Auto-generated catch block
				Utilities.generalCatchBlock(e, this.getClass());
			}
		}
		return view;
	}
	
	/** Show the more loading text with animation
	 * 
	 */
	public void showMoreLoadingAnimation() {
		// TODO Auto-generated method stub
		int totalRecordsInCache=-1;
		
		//show the loading animation with the no of mails reamaining in the end of listview
		try {
			totalRecordsInCache = cacheAdapter.getRecordsCount(mailType, mailFolderName, mailFolderId);
			
		} catch (Exception e) {
			// if exception in getting the no of mails in cache then just show loading mails symbol
		} 
		adapter.showMoreMailsLoadingAnimation(MORE_NO_OF_MAILS, totalRecordsInCache ,totalMailsInFolder);
	}

	/* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onActivityCreated(android.os.Bundle)
	 */
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {

		//contains all the UI listners for this fragment
		MailListViewFragmentListener listener = new MailListViewFragmentListener(this);
		
		super.onActivityCreated(savedInstanceState);
		listView.setOnScrollListener(listener);
		//ListView -Itemclick Listener
		listView.setOnItemClickListener(listener);
	}
	
	/** Refreshes the list from network
	 * @param showPulltoRefresh: Either show the big pull to refresh label while refreshing
	 */
	@Override
	public void refreshList(){

		if(newMailsThreadState!=State.UPDATING){
			//network call for getting the new mails
			Handler getNewMailsHandler = new GetNewMailsHandler(this);
			Thread t = new Thread(new GetNewMailsRunnable(this, getNewMailsHandler));
			t.start();
		}
	}

	/** Refreshes the listview from local cache
	 * 
	 */
	@Override
	public void softRefreshList(){
		if(BuildConfig.DEBUG){
			Log.d(TAG,  this.getClass() + " ->  Called Soft refresh list");
		}
		try {
			adapter.setListVOs(cacheAdapter.getMailHeaders(mailType, mailFolderName, mailFolderId));
			adapter.notifyDataSetChanged();
			updateTextSwitcherWithMailCount();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Utilities.generalCatchBlock(e, this.getClass());
		}
	}
	
	/** Gets More mails when the user scrolls down
	 * 
	 */
	public void getMoreMails(){
		if(moreMailsThreadState!=State.UPDATING){
			showMoreLoadingAnimation();
			
			//network call for getting the new mails
			Handler getMoreMailsHandler = new GetMoreMailsHandler(this);
			Thread t = new Thread(new GetMoreMailsRunnable(this, getMoreMailsHandler));
			t.start();
		}
	}

	/** This method gets the customized Display name for a folder. 
	 * Well Known Folder Name can be used as is, but some have 2 words without space.
	 * @param mailType
	 * @return
	 */
	private String getMailFolderDisplayName(int mailType) {
		// TODO Auto-generated method stub
		switch(mailType){
		case (MailType.SENT_ITEMS):
			return (activity.getString(R.string.ActionBarTitle_SentItems));
		case MailType.DELETED_ITEMS:
			return (activity.getString(R.string.ActionBarTitle_DeletedItems));
		case MailType.JUNK_EMAIL:
			return (activity.getString(R.string.ActionBarTitle_JunkEmail));
		case MailType.CONVERSATION_HISTORY:
			return (activity.getString(R.string.ActionBarTitle_ConversationHistory));
		default:
			return (mailFolderName);
		}
	}

	/** Make UI changes for the Updating status
	 * which includes progress bar, text switcher, swipe refresh
	 * 
	 */
	public void updatingStatusUIChanges() {
		// TODO Auto-generated method stub
		try {
			//progress bar - visible
			bar_progressbar.setVisibility(View.VISIBLE);
			//swipe refresh layout - visible
			swipeRefreshLayout.setRefreshing(true);
			//if total cached records in the folder is more than 0 then show msg "Checking for new mails" otherwise "Update folder"
			totalCachedRecords = cacheAdapter.getRecordsCount(mailType, mailFolderName, mailFolderId);
			if(totalCachedRecords>0){
				textswitcher.setText(activity.getString(R.string.folder_updater_checking, getMailFolderDisplayName(mailType)).toString());
			}
			else{
				textswitcher.setText(activity.getString(R.string.folder_updater_updating, getMailFolderDisplayName(mailType)).toString());
			}
			//text switcher - refreshing icon
			updateTextSwitcherIcons(View.VISIBLE,View.GONE,View.GONE, View.GONE, View.GONE);
			//progress bar at 40
			bar_progressbar.setProgress(40);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Utilities.generalCatchBlock(e, this.getClass());
		}
	}

	

	/** Updates the Text Switcher with the unread mail count. 
	 * usually called after successful update
	 * 
	 */
	public void updateTextSwitcherWithMailCount() throws Exception {
		//get the unread emails count
		int totalUnread = cacheAdapter.getUnreadMailCount(mailType, mailFolderName, mailFolderId);
		String successMsg="";
		//more than 1 unread email 
		if(totalUnread>1){
			//update text in text switcher. for inbox alone show as "new mail" for other folders show "unread"
			successMsg = (mailType==MailType.INBOX || mailType==MailType.INBOX_SUBFOLDER_WITH_ID) ?
					getString(R.string.new_mail_x,totalUnread):
					getString(R.string.unread_item_x,totalUnread);
			//update icon
			updateTextSwitcherIcons(View.GONE, View.GONE, View.GONE, View.GONE, View.VISIBLE);
		}
		//one unread email
		else if(totalUnread==1){
			//update text in text switcher. for inbox alone show as "new mail" for other folders show "unread"
			successMsg = (mailType==MailType.INBOX || mailType==MailType.INBOX_SUBFOLDER_WITH_ID) ?
					getString(R.string.new_mail_1,totalUnread):
					getString(R.string.unread_item_1,totalUnread);
			//update icon
			updateTextSwitcherIcons(View.GONE, View.GONE, View.GONE, View.GONE, View.VISIBLE);
		}
		//no new mail
		else{
			//update text in text switcher
			successMsg=getString(R.string.no_new_mail);
			//update icon
			updateTextSwitcherIcons(View.GONE, View.GONE, View.GONE, View.VISIBLE, View.GONE);
		}
		textswitcher.setText(successMsg);
	}

	

	/** This method sets the visibility of the icons inside the text switcher
	 * @param progressCircleVisibility
	 * @param successIconVisibility
	 * @param failureIconVisibility
	 */
	public void updateTextSwitcherIcons(int progressCircleVisibility, int successIconVisibility, int failureIconVisibility, int readIconVisibility, int unreadIconVisibility){
		circle_progressbar.setVisibility(progressCircleVisibility);
		successIcon.setVisibility(successIconVisibility);
		failureIcon.setVisibility(failureIconVisibility);
		readIcon.setVisibility(readIconVisibility);
		unreadIcon.setVisibility(unreadIconVisibility);
	}

	/*** GETTER SETTER PART ***/

	public State getNewMailsThreadState() {
		return newMailsThreadState;
	}

	public void setNewMailsThreadState(State currentStatus) {
		this.newMailsThreadState = currentStatus;
	}
	public String getMailFolderId() {
		return mailFolderId;
	}

	public void setMailFolderId(String mailFolderId) {
		this.mailFolderId = mailFolderId;
	}

	public String getMailFolderName() {
		return mailFolderName;
	}

	public void setMailFolderName(String mailFolderName) {
		this.mailFolderName = mailFolderName;
	}

	public ProgressBar getCircle_progressbar() {
		return circle_progressbar;
	}

	public void setCircle_progressbar(
			ProgressBar maillist_refresh_progressbar) {
		this.circle_progressbar = maillist_refresh_progressbar;
	}

	public ProgressBar getBar_progressbar() {
		return bar_progressbar;
	}

	public void setBar_progressbar(
			ProgressBar maillist_update_progressbar) {
		this.bar_progressbar = maillist_update_progressbar;
	}

	public SwipeRefreshLayout getSwipeRefreshLayout() {
		return swipeRefreshLayout;
	}

	public void setSwipeRefreshLayout(SwipeRefreshLayout swipeRefreshLayout) {
		this.swipeRefreshLayout = swipeRefreshLayout;
	}

	public TextSwitcher getTextswitcher() {
		return textswitcher;
	}

	public void setTextswitcher(TextSwitcher textswitcher) {
		this.textswitcher = textswitcher;
	}
	public MailListViewAdapter getAdapter() {
		return adapter;
	}

	public void setAdapter(MailListViewAdapter adapter) {
		this.adapter = adapter;
	}

	public CachedMailHeaderCacheAdapter getMailHeadersCacheAdapter() {
		return cacheAdapter;
	}

	public void setMailHeadersCacheAdapter(CachedMailHeaderCacheAdapter mailHeadersCacheAdapter) {
		this.cacheAdapter = mailHeadersCacheAdapter;
	}

	public int getMailType() {
		return mailType;
	}

	public void setMailType(int mailType) {
		this.mailType = mailType;
	}

	public State getMoreMailsThreadState() {
		return moreMailsThreadState;
	}

	public void setMoreMailsThreadState(State moreMailThreadState) {
		this.moreMailsThreadState = moreMailThreadState;
	}

	public long getTotalMailsInFolder() {
		return totalMailsInFolder;
	}

	public void setTotalMailsInFolder(long totalMailsInFolder) {
		this.totalMailsInFolder = totalMailsInFolder;
	}
}
