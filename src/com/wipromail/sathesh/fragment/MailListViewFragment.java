package com.wipromail.sathesh.fragment;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
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
import com.wipromail.sathesh.activity.MailListViewActivity;
import com.wipromail.sathesh.activity.ViewMailActivity;
import com.wipromail.sathesh.adapter.MailListViewAdapter;
import com.wipromail.sathesh.animation.ApplyAnimation;
import com.wipromail.sathesh.application.MailApplication;
import com.wipromail.sathesh.application.interfaces.MailListActivityDataPasser;
import com.wipromail.sathesh.application.interfaces.MailListFragmentDataPasser;
import com.wipromail.sathesh.cache.CacheAdapter;
import com.wipromail.sathesh.constants.Constants;
import com.wipromail.sathesh.handlers.runnables.GetNewMails;
import com.wipromail.sathesh.service.data.Item;
import com.wipromail.sathesh.sqlite.db.dao.CachedMailHeaderDAO;
import com.wipromail.sathesh.sqlite.db.pojo.vo.CachedMailHeaderVO;
import com.wipromail.sathesh.util.Utilities;

/**
 * @author sathesh
 * This fragment is used to load only the MailFunctions.
 */

public class MailListViewFragment extends Fragment implements Constants, OnScrollListener, OnItemClickListener, MailListFragmentDataPasser {

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
	private CachedMailHeaderDAO dao;
	private int totalCachedRecords=0;

	private int preLast;
	private boolean  loadingSymbolShown=false;
	private SwipeRefreshLayout swipeRefreshLayout ;
	private State currentStatus;

	private boolean fragmentAlreadyLoaded=false;

	/**
	 * @author sathesh
	 *
	 */
	public enum State{
		UPDATING,
		UPDATED,
		UPDATE_LIST,
		UPDATE_CACHE_DONE,
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
		//DAO for local cache
		dao = new CachedMailHeaderDAO(context);
		setRetainInstance(true);

		if(activity != null){
			try {
				//List View Initialization
				listView = (ListView)view.findViewById(R.id.listView);
				//ListView -OnScroll Listener
				listView.setOnScrollListener(this);
				//ListView -Itemclick Listener
				listView.setOnItemClickListener(this);

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
					adapter = new MailListViewAdapter(context, getCachedHeaderData());
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

				//if the activity is recreated, and if the thread is already updating then update the UI status
				if((currentStatus==State.UPDATING) || (currentStatus==State.UPDATE_LIST)){
					updatingStatusUIChanges();
				}

				//make a refresh only once when the screen is loaded first time. make a soft refresh on config change
				if(!fragmentAlreadyLoaded){
					refreshList();
				}
				else{
					//config change (Screen rotation)
					softRefreshList();
				}
				fragmentAlreadyLoaded=true; //make unncessary recreation of new objects after config change(screen rotation)

			} catch (Exception e) {
				// TODO Auto-generated catch block
				Utilities.generalCatchBlock(e, this.getClass());
			}
		}
		return view;
	}

	/** Refreshes the list from network
	 * @param showPulltoRefresh: Either show the big pull to refresh label while refreshing
	 */
	@Override
	public void refreshList(){

		if(!(currentStatus==State.UPDATING) && !(currentStatus==State.UPDATE_LIST)){
			//network call for getting the new mails
			Thread t = new Thread(new GetNewMails(this));
			t.start();
		}
	}

	/** Refreshes the listview from local cache
	 * 
	 */
	@Override
	public void softRefreshList(){
		try {
			adapter.setListVOs(getCachedHeaderData());
			adapter.notifyDataSetChanged();
			updateTextSwitcherWithMailCount();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Utilities.generalCatchBlock(e, this.getClass());
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

	

	/** The UI changes when the current status is updating
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
			totalCachedRecords = getTotalNumberOfRecordsInCache();
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

	/** Writes the array List of items to cache
	 * @param items
	 * @param emptyCache	Empties the cache before writing
	 */
	public void cacheNewData(ArrayList<Item> items, boolean emptyCache)  {
		// TODO Auto-generated method stub
		try {
			if(emptyCache){
				deleteCache();
			}
			CacheAdapter.writeCacheData(context, mailType, mailFolderName, mailFolderId, items);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			if(BuildConfig.DEBUG)
				e.printStackTrace();
		}	
	}

	/** Updates the Text Switcher with the unread mail count. 
	 * usually called after successful update
	 * 
	 */
	public void updateTextSwitcherWithMailCount() throws Exception {
		//get the unread emails count
		int totalUnread = getUnreadMailsInCache();
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

	/** This method will return the cached mail header data list of VOs
	 * @return
	 */
	private List<CachedMailHeaderVO> getCachedHeaderData(){
		List<CachedMailHeaderVO> mailListHeaderData=null;
		try {
			// mail type 8 and 9 have folder id. The rest can be determined by the mailType
			if(mailType!=8 && mailType!=9){
				mailListHeaderData = dao.getAllRecordsByMailType(mailType);
			}
			else{
				//by folder id
				mailListHeaderData = dao.getAllRecordsByFolderId(mailFolderId);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			if(BuildConfig.DEBUG)
				e.printStackTrace();
		}
		return mailListHeaderData;

	}

	/** Gets the total number of record mail headers in cache for this particular mail type
	 * @throws Exception 
	 * 
	 */
	public int getTotalNumberOfRecordsInCache() throws Exception {
		// TODO Auto-generated method stub
		int totalCachedRecords;
		if(mailType!=8 && mailType!=9){
			totalCachedRecords=dao.getRecordsCountByMailType(mailType);
		}
		else{
			//by folder id
			totalCachedRecords=dao.getRecordsCountByFolderId(mailFolderId);
		}
		return totalCachedRecords;
	}

	/** Gets the total number of record mail headers in cache for this particular mail type
	 * @throws Exception 
	 * 
	 */
	private int getUnreadMailsInCache() throws Exception {
		// TODO Auto-generated method stub
		int totalUnread;
		if(mailType!=8 && mailType!=9){
			totalUnread=dao.getUnreadByMailType(mailType);
		}
		else{
			//by folder id
			totalUnread=dao.getUnreadCountByFolderId(mailFolderId);
		}
		return totalUnread;
	}

	/** Delete all the cached mail headers for this particular mail type
	 * @throws Exception 
	 * 
	 */
	private void deleteCache() throws Exception {
		// TODO Auto-generated method stub
		// mail type 8 and 9 have folder id. The rest can be determined by the mailType
		if(mailType!=8 && mailType!=9){
			dao.deleteAllByMailType(mailType);
		}
		else{
			//by folder id
			dao.deleteAllByFolderId(mailFolderId);
		}
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
			if(swipeRefreshLayout!=null)  swipeRefreshLayout.setEnabled(enable);

			//Last Item Listener - loads more mails

			final int lastItem = firstVisibleItem + visibleItemCount;
			if(!loadingSymbolShown && lastItem == totalItemCount) {
				if(preLast!=lastItem){ //to avoid multiple calls for last item
					if(BuildConfig.DEBUG){
						Log.d(TAG, "MailListViewFragment -> Last Item listener");
					}

					adapter.scrolledToLast();
					loadingSymbolShown=true;

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
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO Auto-generated method stub

		//the pull to refresh list view starts from instead of 0.. fix for that
		CachedMailHeaderVO vo;

		try{
			vo = (CachedMailHeaderVO) parent.getItemAtPosition(position);
			Intent viewMailIntent = new Intent(activity.getBaseContext(), ViewMailActivity.class);
			viewMailIntent.putExtra(MailListViewActivity.EXTRA_MESSAGE_CACHED_HEADER, vo);
			startActivity(viewMailIntent);
		}
		catch(Exception e){
			if(BuildConfig.DEBUG)
				e.printStackTrace();
		}
	}

	public State getCurrentStatus() {
		return currentStatus;
	}

	public void setCurrentStatus(State currentStatus) {
		this.currentStatus = currentStatus;
	}
	public String getMailFolderId() {
		return mailFolderId;
	}

	public void setMailFolderId(String mailFolderId) {
		this.mailFolderId = mailFolderId;
	}

	public CachedMailHeaderDAO getDao() {
		return dao;
	}

	public void setDao(CachedMailHeaderDAO dao) {
		this.dao = dao;
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
}
