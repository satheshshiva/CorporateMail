package com.wipromail.sathesh.fragment;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
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
import com.wipromail.sathesh.application.NotificationProcessing;
import com.wipromail.sathesh.application.interfaces.MailListActivityDataPasser;
import com.wipromail.sathesh.application.interfaces.MailListFragmentDataPasser;
import com.wipromail.sathesh.cache.CacheAdapter;
import com.wipromail.sathesh.constants.Constants;
import com.wipromail.sathesh.customexceptions.NoInternetConnectionException;
import com.wipromail.sathesh.customexceptions.NoUserSignedInException;
import com.wipromail.sathesh.ews.EWSConnection;
import com.wipromail.sathesh.ews.NetworkCall;
import com.wipromail.sathesh.service.data.ExchangeService;
import com.wipromail.sathesh.service.data.FindItemsResults;
import com.wipromail.sathesh.service.data.HttpErrorException;
import com.wipromail.sathesh.service.data.Item;
import com.wipromail.sathesh.service.data.WellKnownFolderName;
import com.wipromail.sathesh.sqlite.db.dao.CachedMailHeaderDAO;
import com.wipromail.sathesh.sqlite.db.pojo.vo.CachedMailHeaderVO;
import com.wipromail.sathesh.ui.AuthFailedAlertDialog;
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
	private SherlockFragmentActivity activity ;
	private Context context ;

	private MailListViewAdapter adapter;
	private TextSwitcher titlebar_inbox_status_textswitcher;

	private int mailType;
	private String  mailFolderName;
	private String mailFolderId;

	boolean cacheLoaded=false;

	/** Type of the status of this activity
	 * @author sathesh
	 *
	 */
	private interface status{
		public final int UPDATING=1;
		public final int UPDATED=2;
		public final int UPDATE_LIST=3;
		public final int UPDATE_CACHE_DONE=4;
		public final int ERROR=5;
		public final int ERROR_AUTH_FAILED=6;
	}

	private int currentStatus=-1;
	private ProgressBar maillist_refresh_progressbar;

	private ImageView successIcon, failureIcon, readIcon, unreadIcon;


	private ActionBar myActionBar;

	private ProgressBar maillist_update_progressbar;
	private FindItemsResults<Item> findResults = null;
	private ListView listView;
	private CachedMailHeaderDAO dao;
	private int totalCachedRecords=0;

	private int preLast;
	private boolean  loadingSymbolShown=false;
	private SwipeRefreshLayout swipeRefreshLayout ;


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

		if(activity != null){
			try {
				//List View Initialization
				listView = (ListView)view.findViewById(R.id.listView);
				//ListView -OnScroll Listener
				listView.setOnScrollListener(this);
				//ListView -Itemclick Listener
				listView.setOnItemClickListener(this);

				//Text Switcher Initiliazation
				titlebar_inbox_status_textswitcher = (TextSwitcher)view.findViewById(R.id.titlebar_inbox_status_textswitcher);

				//Text Switcher customze with text view
				titlebar_inbox_status_textswitcher.setFactory(new ViewFactory() {
					public View makeView() {
						// Custom text view since the default one auto applies center gravity
						TextView textView = new TextView(activity);
						textView.setGravity(Gravity.LEFT);
						textView.setTextSize(12);
						return textView;
					}
				});

				//animation for text switcher
				ApplyAnimation.setTitleInboxStatusTextSwitcher(activity, titlebar_inbox_status_textswitcher);

				//mailtype, folname and folder id get from activity
				mailType = activityDataPasser.getMailType();
				mailFolderName = activityDataPasser.getMailFolderName();
				mailFolderId = activityDataPasser.getStrFolderId();

				//action bar initialize 
				myActionBar = activity.getSupportActionBar();
				//progress bar initialize 
				maillist_refresh_progressbar = (ProgressBar)view.findViewById(R.id.maillist_refresh_progressbar);

				//icons initialize
				successIcon = (ImageView)view.findViewById(R.id.maillist_success_icon);
				failureIcon = (ImageView)view.findViewById(R.id.maillist_failure_icon);
				readIcon = (ImageView)view.findViewById(R.id.maillist_read_icon);
				unreadIcon = (ImageView)view.findViewById(R.id.maillist_unread_icon);
				maillist_update_progressbar = (ProgressBar)view.findViewById(R.id.maillist_update_progressbar);

				//update mail type in the action bar title
				myActionBar.setTitle(getMailFolderDisplayName(mailType));
				myActionBar.setDisplayHomeAsUpEnabled(true);

				//initializes the adapter and associates the listview. 
				//this set  of code when placed when placed few lines before wont initialize and is giving empty listview. dont know why.
				//get the cursor

				//initialize the adapter
				adapter = new MailListViewAdapter(context, getCachedHeaderData());
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

				//get the total number of records in cache
				totalCachedRecords = getTotalNumberOfRecordsInCache();

				//refresh list view
				refreshList();

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

		if(!(currentStatus==status.UPDATING) && !(currentStatus==status.UPDATE_LIST)){

			maillist_update_progressbar.setVisibility(View.VISIBLE);
			maillist_update_progressbar.setProgress(20);
			textSwitcherIcons(View.VISIBLE, View.GONE, View.GONE, View.GONE, View.GONE);
			//network call for getting the new mails
			(new GetNewMails()).execute();
		}
	}

	/** Refreshes the listview from local cache
	 * 
	 */
	@Override
	public void softRefreshList(){
		adapter.setListVOs(getCachedHeaderData());
		adapter.notifyDataSetChanged();
	}

	/** This method gets the customized Display name for a folder. 
	 * Well Known Folder Name can be used as is, but some have 2 words without space.
	 * @param mailType
	 * @return
	 */
	private String getMailFolderDisplayName(int mailType) {
		// TODO Auto-generated method stub
		if(mailType==MailType.SENT_ITEMS){
			return (activity.getString(R.string.ActionBarTitle_SentItems));
		}
		else if(mailType==MailType.DELETED_ITEMS){
			return (activity.getString(R.string.ActionBarTitle_DeletedItems));
		}
		else if(mailType==MailType.JUNK_EMAIL){
			return (activity.getString(R.string.ActionBarTitle_JunkEmail));
		}
		else if(mailType==MailType.CONVERSATION_HISTORY){
			return (activity.getString(R.string.ActionBarTitle_ConversationHistory));
		}
		else{
			return (mailFolderName);
		}
	}

	/**
	 * @author sathesh
	 *
	 */
	private class GetNewMails extends AsyncTask<Void, Integer, Void>{

		ExchangeService service;

		@Override
		protected Void doInBackground(Void... paramArrayOfParams) {
			// TODO Auto-generated method stub

			if (activity != null) {

				try {

					//get the total no of records in cache and get all the same number of records.
					totalCachedRecords = getTotalNumberOfRecordsInCache();

					publishProgress(status.UPDATING);
					currentStatus=status.UPDATING;

					publishProgress(status.UPDATE_CACHE_DONE);

					service = EWSConnection.getServiceFromStoredCredentials(activity.getApplicationContext());

					if(BuildConfig.DEBUG){
						Log.d(TAG, "MailListViewFragment -> Total records in cache"+totalCachedRecords);
					}

					//if the cache is present, then get the same number of rows from EWS as of the local no of rows
					int noOfMailsToFetch=(totalCachedRecords>MIN_NO_OF_MAILS?totalCachedRecords:MIN_NO_OF_MAILS);

					if(mailFolderId!=null && !(mailFolderId.equals("")))
						//Ews call
						findResults = NetworkCall.getFirstNItemsFromFolder(mailFolderId, service, noOfMailsToFetch);
					else
						//Ews call
						findResults = NetworkCall.getFirstNItemsFromFolder(WellKnownFolderName.valueOf(mailFolderName), service, noOfMailsToFetch);

					//empties the cache for this 
					if(findResults!=null){
						cacheNewData(findResults.getItems(), true);
					}

					publishProgress(status.UPDATE_LIST);
					currentStatus=status.UPDATE_LIST;
				}
				catch (final NoUserSignedInException e) {
					publishProgress(status.ERROR);
					currentStatus=status.ERROR;
					e.printStackTrace();
				}
				catch (UnknownHostException e) {
					publishProgress(status.ERROR);
					currentStatus=status.ERROR;
					e.printStackTrace();

				}
				catch(NoInternetConnectionException nic){
					publishProgress(status.ERROR);
					currentStatus=status.ERROR;
					nic.printStackTrace();
				}
				catch(HttpErrorException e){
					if(e.getMessage().toLowerCase().contains("Unauthorized".toLowerCase())){
						//unauthorised
						publishProgress(status.ERROR_AUTH_FAILED);
						currentStatus=status.ERROR_AUTH_FAILED;
					}
					else
					{
						publishProgress(status.ERROR);
						currentStatus=status.ERROR;
					}
					e.printStackTrace();
				}
				catch (Exception e) {
					publishProgress(status.ERROR);
					currentStatus=status.ERROR;
					e.printStackTrace();
				}
			}
			return null;
		}

		@Override
		protected void onProgressUpdate(Integer... _int) {
			if(_int[0]==status.UPDATING){
				swipeRefreshLayout.setRefreshing(true);
				//if total cached records in the folder is more than 0 then show msg "Checking for new mails" otherwise "Update folder"
				if(totalCachedRecords>0){
					titlebar_inbox_status_textswitcher.setText(activity.getString(R.string.folder_updater_checking, getMailFolderDisplayName(mailType)).toString());
				}
				else{
					titlebar_inbox_status_textswitcher.setText(activity.getString(R.string.folder_updater_progress, getMailFolderDisplayName(mailType)).toString());
				}
				textSwitcherIcons(View.VISIBLE,View.GONE,View.GONE, View.GONE, View.GONE);
				maillist_update_progressbar.setProgress(40);

			}
			if(_int[0]==status.UPDATE_CACHE_DONE){
				maillist_update_progressbar.setProgress(65);

			}
			else if(_int[0]==status.UPDATE_LIST){
				maillist_update_progressbar.setProgress(90);
			}
			else  if(_int[0]==status.UPDATED){
				//successful update
				try {
					swipeRefreshLayout.setRefreshing(false);
					updateTextSwitcherWithMailCount();
					maillist_update_progressbar.setProgress(0);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					Utilities.generalCatchBlock(e, this.getClass());
				}

			}
			else if(_int[0]==status.ERROR_AUTH_FAILED){
				// for auth failed show an alert box
				titlebar_inbox_status_textswitcher.setText(activity.getText(R.string.folder_auth_error));
				NotificationProcessing.showLoginErrorNotification(context);
				if(isAdded()){
					AuthFailedAlertDialog.showAlertdialog(activity, context);
				}
				else{
					Log.e(TAG, "Authentication failed. Not able to add the alert dialog due to isAdded() is false");
				}
				// stop the MNS service
				MailApplication.stopMNSService(context);
			}
			else  if(_int[0]==status.ERROR){
				textSwitcherIcons(View.GONE,View.GONE, View.VISIBLE, View.GONE, View.GONE);
				swipeRefreshLayout.setRefreshing(false);
				maillist_update_progressbar.setProgress(0);
				titlebar_inbox_status_textswitcher.setText(activity.getText(R.string.folder_updater_error));


			}
		}

		@Override
		protected void onPostExecute(Void a) {
			try {
				//refresh the display from the cache (which is now updated with new records)
				softRefreshList();
				if(currentStatus==status.UPDATE_LIST){
					publishProgress(status.UPDATED);
					currentStatus=status.UPDATED;
				}

			}
			catch(Exception e){
				if(BuildConfig.DEBUG)
					e.printStackTrace();
			}
		}

		//end of async task
	}

	/** Writes the array List of items to cache
	 * @param items
	 * @param emptyCache	Empties the cache before writing
	 */
	private void cacheNewData(ArrayList<Item> items, boolean emptyCache)  {
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
			//update text
			successMsg=getString(R.string.new_mail_x,totalUnread);
			//update icon
			textSwitcherIcons(View.GONE, View.GONE, View.GONE, View.GONE, View.VISIBLE);
		}
		//one unread email
		else if(totalUnread==1){
			//update text in text switcher
			successMsg=getString(R.string.new_mail_1);
			//update icon
			textSwitcherIcons(View.GONE, View.GONE, View.GONE, View.GONE, View.VISIBLE);
		}
		//no new mail
		else{
			//update text in text switcher
			successMsg=getString(R.string.no_new_mail);
			//update icon
			textSwitcherIcons(View.GONE, View.GONE, View.GONE, View.VISIBLE, View.GONE);
		}
		titlebar_inbox_status_textswitcher.setText(successMsg);
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
	private int getTotalNumberOfRecordsInCache() throws Exception {
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
	private void textSwitcherIcons(int progressCircleVisibility, int successIconVisibility, int failureIconVisibility, int readIconVisibility, int unreadIconVisibility){
		maillist_refresh_progressbar.setVisibility(progressCircleVisibility);
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
}
