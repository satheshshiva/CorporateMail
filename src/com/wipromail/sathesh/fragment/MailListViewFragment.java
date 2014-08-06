package com.wipromail.sathesh.fragment;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextSwitcher;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.SubMenu;
import com.handmark.pulltorefresh.extras.listfragment.PullToRefreshListFragment;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.wipromail.sathesh.BuildConfig;
import com.wipromail.sathesh.R;
import com.wipromail.sathesh.activity.AboutActivity;
import com.wipromail.sathesh.activity.ComposeActivity;
import com.wipromail.sathesh.activity.HomePageActivity;
import com.wipromail.sathesh.activity.MailListViewActivity;
import com.wipromail.sathesh.activity.PreferencesActivity;
import com.wipromail.sathesh.activity.ViewMailActivity;
import com.wipromail.sathesh.adapter.MailListViewAdapter;
import com.wipromail.sathesh.application.MailApplication;
import com.wipromail.sathesh.application.NotificationProcessing;
import com.wipromail.sathesh.application.interfaces.MailListDataPasser;
import com.wipromail.sathesh.cache.CacheAdapter;
import com.wipromail.sathesh.constants.Constants;
import com.wipromail.sathesh.customexceptions.NoInternetConnectionException;
import com.wipromail.sathesh.customexceptions.NoUserSignedInException;
import com.wipromail.sathesh.ews.EWSConnection;
import com.wipromail.sathesh.ews.MailFunctions;
import com.wipromail.sathesh.ews.MailFunctionsImpl;
import com.wipromail.sathesh.ews.NetworkCall;
import com.wipromail.sathesh.service.data.ExchangeService;
import com.wipromail.sathesh.service.data.FindItemsResults;
import com.wipromail.sathesh.service.data.HttpErrorException;
import com.wipromail.sathesh.service.data.Item;
import com.wipromail.sathesh.service.data.WellKnownFolderName;
import com.wipromail.sathesh.sqlite.db.dao.CachedMailHeaderDAO;
import com.wipromail.sathesh.sqlite.db.pojo.vo.CachedMailHeaderVO;
import com.wipromail.sathesh.ui.AuthFailedAlertDialog;
import com.wipromail.sathesh.ui.OptionsUIContent;

/**
 * @author sathesh
 * This fragment is used to load only the MailFunctions.
 */

public class MailListViewFragment extends PullToRefreshListFragment implements Constants, OnScrollListener {

	// ListFragment is a very useful class that provides a simple ListView inside of a Fragment.
	// This class is meant to be sub-classed and allows you to quickly build up list interfaces
	// in your app.
	private MailListDataPasser activityDataPasser ;
	private SherlockFragmentActivity activity ;
	private Context context ;

	private MailFunctions mailFunctions = new MailFunctionsImpl();
	private MailListViewAdapter adapter;
	private TextSwitcher titlebar_inbox_status_textswitcher;

	private int mailType;
	private String  mailFolderName;
	private String mailFolderId;

	boolean cacheLoaded=false;
	private PullToRefreshListView mPullRefreshListView;
	private final String STATUS_UPDATING="STATUS_UPDATING";
	private final String STATUS_UPDATED="STATUS_UPDATED";
	private final String STATUS_UPDATE_LIST="STATUS_UPDATE_LIST";
	private final String STATUS_UPDATE_CACHE_DONE="STATUS_UPDATE_CACHE_DONE";
	private final String STATUS_ERROR="STATUS_ERROR";

	private String currentStatus="";
	private ProgressBar maillist_refresh_progressbar;

	private ImageView successIcon, failureIcon;


	private ActionBar myActionBar;

	private ProgressBar maillist_update_progressbar;
	private FindItemsResults<Item> findResults = null;
	private ListView listView;
	private int listViewIndex=0;
	private int listViewTop=0;
	private CachedMailHeaderDAO dao;
	private int totalCachedRecords=0;

	private int preLast;
	private List<CachedMailHeaderVO> mailListHeaderData ;
	private String[] listViewItemIds= new String[0];
	private Cursor cursor;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		activity = (SherlockFragmentActivity) getActivity();
		context = (SherlockFragmentActivity) getActivity();
		activityDataPasser = (MailListDataPasser)getActivity();

		//DAO for local cache
		dao = new CachedMailHeaderDAO(context);
	}

	@Override
	public void onResume() {
		super.onResume();
		if(activity != null){
			listView = this.getListView();
			listView.setOnScrollListener(this);
			
			titlebar_inbox_status_textswitcher = (TextSwitcher)activity.findViewById(R.id.titlebar_inbox_status_textswitcher);
			
			//THE UI ELEMENTS IN THE FRAGMENTS MUST BE INITIALIZED IN THE ACTIVITY ITSELF
			mailType = activityDataPasser.getMailType();
			mailFolderName = activityDataPasser.getMailFolderName();
			mailFolderId = activityDataPasser.getStrFolderId();
			
			myActionBar = activityDataPasser.getMyActionBar();
			mPullRefreshListView = activityDataPasser.getPullRefreshListView();
			maillist_refresh_progressbar = activityDataPasser.getMaillist_refresh_progressbar();
			myActionBar = activityDataPasser.getMyActionBar();
			successIcon = activityDataPasser.getSuccessIcon();
			failureIcon = activityDataPasser.getFailureIcon();
			maillist_update_progressbar = activityDataPasser.getMaillist_update_progressbar();

			//update mail type in the action bar title
			myActionBar.setTitle(getMailFolderDisplayName(mailType));
		
			//initializes the cursor, adapter and associates the listview. 
			//this set  of code when placed when placed few lines before wont initilize and is giving empty listview. dont know why.
			//get the cursor
			Cursor mailListHeaderData = getCachedHeaderDataCursor();
			activity.startManagingCursor(mailListHeaderData);
			//initialize the adapter
			adapter = new MailListViewAdapter(context,
	                R.layout.listview_maillist, mailListHeaderData, 
	                new String[] {"MAIL_FROM"}, 
	                new int[] {R.id.listview_maillist_from  },CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
			setListAdapter(adapter);
			
			//refresh the list view
			refreshList(false);
		}
	}


	/** Refreshes the list view
	 * @param showPulltoRefresh: Either show the big pull to refresh label while refreshing
	 */
	public void refreshList(boolean showPulltoRefresh){

		if(!(currentStatus.equals(STATUS_UPDATING)) && !(currentStatus.equals(STATUS_UPDATE_LIST))){
			
			maillist_update_progressbar.setVisibility(View.VISIBLE);
			maillist_update_progressbar.setProgress(20);
			if(showPulltoRefresh){
				showPullToRefresh();
			}
			displayProgressIcon();
			//network call for getting the new mails
			(new GetNewMails()).execute();
		}
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
	/* This is executed when an item in the list view is clicked. Fetch the item id from the list adapter (which was stored as a string array during creation).
	 * get the item id from the cache and pass to the View mail intent
	 * (non-Javadoc)
	 * @see android.support.v4.app.ListFragment#onListItemClick(android.widget.ListView, android.view.View, int, long)
	 */
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {

		//the pull to refresh list view starts from instead of 0.. fix for that
		position=positionFixForPullToRefresh(position);

		ListAdapter listAdapter = getListAdapter();
		String itemId = (String)listAdapter.getItem(position);

		String subject="", from="", to="", cc="";
		Date receivedDate=null;
		try{
			mailListHeaderData = getCachedHeaderData();

			CachedMailHeaderVO mailListHeader = mailListHeaderData.get(position);
			subject= mailListHeader.getMail_subject();
			from=mailListHeader.getMail_from();
			to=mailListHeader.getMail_to();
			cc=mailListHeader.getMail_cc();
			receivedDate=mailListHeader.getMail_datetimereceived();
		}
		catch(Exception e){
			e.printStackTrace();
		}

		if(BuildConfig.DEBUG)
			Log.d(TAG, "Opening Item with " + "subject= " + subject +  " from= " + from + " to= " + to + " cc= " + cc +" received date= " + ((receivedDate!=null) ? receivedDate.toString():""));
		Intent viewMailIntent = new Intent(activity.getBaseContext(), ViewMailActivity.class);

		viewMailIntent.putExtra(MailListViewActivity.EXTRA_MESSAGE_ITEMID, itemId);
		viewMailIntent.putExtra(MailListViewActivity.EXTRA_MESSAGE_FROM, from);
		viewMailIntent.putExtra(MailListViewActivity.EXTRA_MESSAGE_TO, to);
		viewMailIntent.putExtra(MailListViewActivity.EXTRA_MESSAGE_CC, cc);
		viewMailIntent.putExtra(MailListViewActivity.EXTRA_MESSAGE_DATETIMERECEIVED,receivedDate.toString());
		viewMailIntent.putExtra(MailListViewActivity.EXTRA_MESSAGE_SUBJECT, subject);
		viewMailIntent.putExtra(MailListViewActivity.EXTRA_MESSAGE_MAIL_TYPE, mailType);
		startActivity(viewMailIntent);
	}

	private int positionFixForPullToRefresh(int position) {
		// TODO Auto-generated method stub
		return position-1;
	}

	/**
	 * @author sathesh
	 *
	 */
	private class GetNewMails extends AsyncTask<Void, String, Void>{

		ExchangeService service;

		@Override
		protected Void doInBackground(Void... paramArrayOfParams) {
			// TODO Auto-generated method stub

			if (activity != null) {
				publishProgress(STATUS_UPDATING, "Updating");
				currentStatus=STATUS_UPDATING;

				try {

					publishProgress(STATUS_UPDATE_CACHE_DONE, "Cache done");

					service = EWSConnection.getServiceFromStoredCredentials(activity.getApplicationContext());

					//get the total no of records in cache and get all the same number of records.
					totalCachedRecords = getTotalNumberOfRecordsInCache();

					if(BuildConfig.DEBUG){
						Log.d(TAG, "MailListViewFragment -> Total records "+totalCachedRecords);
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

					publishProgress(STATUS_UPDATE_LIST, "Almost done");
					currentStatus=STATUS_UPDATE_LIST;
				}
				catch (final NoUserSignedInException e) {
					publishProgress(STATUS_ERROR,  "Trouble getting login details\n\nDetails: " +e.getMessage());
					currentStatus=STATUS_ERROR;
					e.printStackTrace();
				}
				catch (UnknownHostException e) {
					publishProgress(STATUS_ERROR,  "Error Occured\n\nDetails: " +e.getMessage());
					currentStatus=STATUS_ERROR;
					e.printStackTrace();

				}
				catch(NoInternetConnectionException nic){
					publishProgress(STATUS_ERROR,  nic.toString());
					currentStatus=STATUS_ERROR;
					nic.printStackTrace();
				}
				catch(HttpErrorException e){
					if(e.getMessage().toLowerCase().contains("Unauthorized".toLowerCase())){
						//unauthorised
						publishProgress(STATUS_ERROR,  "Authentication failed");
						currentStatus=STATUS_ERROR;
					}
					else
					{
						publishProgress(STATUS_ERROR,  "Error Occured\n\nDetails: " +e.getMessage());
						currentStatus=STATUS_ERROR;
					}
					e.printStackTrace();
				}
				catch (Exception e) {
					publishProgress(STATUS_ERROR, "Error Occured \n\nDetails: " +e.getMessage());
					currentStatus=STATUS_ERROR;
					e.printStackTrace();
				}
			}
			return null;
		}

		@Override
		protected void onProgressUpdate(String... str) {
			if(str[0].equals(STATUS_UPDATING)){
				activity.setSupportProgressBarIndeterminateVisibility(true);	//this is not needed here bcos the progress is not set to false in oncreate
				//if total cached records in the folder is more than 0 then show msg "Checking for new mails" otherwise "Update folder"
				if(totalCachedRecords>0){
					titlebar_inbox_status_textswitcher.setText(activity.getString(R.string.folder_updater_checking, getMailFolderDisplayName(mailType)).toString());
				}
				else{
					titlebar_inbox_status_textswitcher.setText(activity.getString(R.string.folder_updater_progress, getMailFolderDisplayName(mailType)).toString());
				}
				mPullRefreshListView.setLastUpdatedLabel(activity.getText(R.string.pullToRefresh_checking_small).toString());
				displayProgressIcon();
				maillist_update_progressbar.setVisibility(View.VISIBLE);
				maillist_update_progressbar.setProgress(40);

			}
			if(str[0].equals(STATUS_UPDATE_CACHE_DONE)){
				activity.setSupportProgressBarIndeterminateVisibility(false);	//this is not needed here bcos the progress is not set to false in oncreate
				maillist_update_progressbar.setProgress(65);

			}
			else if(str[0].equals(STATUS_UPDATE_LIST)){
				activity.setSupportProgressBarIndeterminateVisibility(false);
				mPullRefreshListView.setLastUpdatedLabel(activity.getText(R.string.folder_checking_almostDone).toString());
				titlebar_inbox_status_textswitcher.setText(activity.getText(R.string.folder_checking_almostDone).toString());
				displayProgressIcon();
				maillist_update_progressbar.setVisibility(View.VISIBLE);
				maillist_update_progressbar.setProgress(90);

			}
			else  if(str[0].equals(STATUS_UPDATED)){
				activity.setSupportProgressBarIndeterminateVisibility(false);
				titlebar_inbox_status_textswitcher.setText(getUpdatedProgressString(activity));
				mPullRefreshListView.setLastUpdatedLabel(getUpdatedProgressString(activity));
				hidePullToRefresh();
				displaySuccessIcon();
				maillist_update_progressbar.setVisibility(View.GONE);
				maillist_update_progressbar.setProgress(0);
			}
			else  if(str[0].equals(STATUS_ERROR)){
				activity.setSupportProgressBarIndeterminateVisibility(false);
				hidePullToRefresh();
				displayFailureIcon();
				maillist_update_progressbar.setVisibility(View.GONE);
				maillist_update_progressbar.setProgress(0);
				if(!(str[1].equalsIgnoreCase("Authentication failed"))){
					titlebar_inbox_status_textswitcher.setText(activity.getText(R.string.folder_updater_error));
					mPullRefreshListView.setLastUpdatedLabel(activity.getText(R.string.folder_updater_error));
				}
				else{
					// for auth failed show an alert box
					activity.setSupportProgressBarIndeterminateVisibility(false);
					titlebar_inbox_status_textswitcher.setText("Authentication failed");
					mPullRefreshListView.setLastUpdatedLabel("Authentication Failed");
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
			}
		}

		@Override
		protected void onPostExecute(Void a) {
			try {
				//refresh the display with the cache (which is now updated with new records)
				updateListViewWithCachedData();
				if(currentStatus.equalsIgnoreCase(STATUS_UPDATE_LIST)){
					publishProgress(STATUS_UPDATED, "Updated");
					currentStatus=STATUS_UPDATED;
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

	private void showPullToRefresh(){
		mPullRefreshListView.setRefreshing();
	}
	private void hidePullToRefresh(){
		//notify pull to refresh
		mPullRefreshListView.onRefreshComplete();
	}

	/** Initializes the adapter and list view
	 * 
	 */
	private void updateListViewWithCachedData(){
		Cursor mailListHeaderData = getCachedHeaderDataCursor();
		activity.stopManagingCursor(adapter.getCursor());
		adapter.changeCursor(mailListHeaderData);
		activity.startManagingCursor(mailListHeaderData);
	}

	/** This method will return the cached mail header data 
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
	
	/** This method will return the cached mail header data cursor
	 * @return
	 */
	private Cursor getCachedHeaderDataCursor(){
		Cursor mailListHeaderData=null;
		try {
			// mail type 8 and 9 have folder id. The rest can be determined by the mailType
			if(mailType!=8 && mailType!=9){
				mailListHeaderData = dao.getAllRecordsByMailTypeCursor(mailType);
			}
			else{
				//by folder id
				mailListHeaderData = dao.getAllRecordsByFolderIdCursor(mailFolderId);
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

	private String getUpdatedProgressString(Context context){
		//return context.getString(R.string.folder_updater_success, (new SimpleDateFormat(TITLEBAR_UPDATED_DATEFORMAT)).format(lastSyncDate.getTime()));
		return context.getString(R.string.folder_updater_success);
	}

	public void displayProgressIcon(){
		maillist_refresh_progressbar.setVisibility(View.VISIBLE);
		successIcon.setVisibility(View.GONE);
		failureIcon.setVisibility(View.GONE);
	}

	public void displaySuccessIcon(){
		maillist_refresh_progressbar.setVisibility(View.GONE);
		successIcon.setVisibility(View.VISIBLE);
		failureIcon.setVisibility(View.GONE);
	}

	public void displayFailureIcon(){
		maillist_refresh_progressbar.setVisibility(View.GONE);
		successIcon.setVisibility(View.GONE);
		failureIcon.setVisibility(View.VISIBLE);
	}

	public void backPressed() {
		// TODO Auto-generated method stub
		Intent intent = new Intent(activity, HomePageActivity.class);
		startActivity(intent);
		activity.finish();
	}

	public void actionbarHomePressed() {
		// TODO Auto-generated method stub
		Intent intent = new Intent(activity, HomePageActivity.class);
		startActivity(intent);
		activity.finish();
	}

	public void createOptionsMenu(Menu menu){

		//Always Visible menu
		menu.add(ACTIONBAR_COMPOSE)
		.setIcon(OptionsUIContent.getComposeIcon())
		.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS|MenuItem.SHOW_AS_ACTION_WITH_TEXT);

		//Submenu
		SubMenu subMenu = menu.addSubMenu("");

		subMenu.add(ACTIONBAR_REFRESH)
		.setIcon(OptionsUIContent.getRefreshIcon())
		.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

		subMenu
		.add(ACTIONBAR_SETTINGS)
		.setIcon(OptionsUIContent.getSettingsIcon())
		.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

		subMenu
		.add(ACTIONBAR_ABOUT)
		.setIcon(OptionsUIContent.getAboutIcon())
		.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

		//Overflow submenu icon
		MenuItem subMenuItem = subMenu.getItem();
		subMenuItem.setIcon(OptionsUIContent.getMoreoverFlowIcon());
		subMenuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
	}

	public void optionsItemSelected(MenuItem item){
		if(item!=null && item.getItemId()==android.R.id.home){
			actionbarHomePressed();
		}
		else if(item!=null && item.getTitle().equals(ACTIONBAR_REFRESH))
		{
			refreshList(false);
		}
		else if(item!=null && item.getTitle().equals(ACTIONBAR_COMPOSE)){
			Intent intent = new Intent(activity, ComposeActivity.class);
			startActivity(intent);
		}
		else if(item!=null && item.getTitle().equals(ACTIONBAR_SETTINGS)){
			Intent intent = new Intent(activity, PreferencesActivity.class);
			startActivity(intent);
		}
		else if(item!=null && item.getTitle().equals(ACTIONBAR_ABOUT)){
			Intent intent = new Intent(activity, AboutActivity.class);
			startActivity(intent);
		}

	}

	/* This is the OnScroll Listener implementation for this listview. 
	 * Load more mails when the last of the mail is in the list view is visible.
	 * (non-Javadoc)
	 * @see android.widget.AbsListView.OnScrollListener#onScroll(android.widget.AbsListView, int, int, int)
	 */
	@Override
	public void onScroll(AbsListView lw, final int firstVisibleItem,
			final int visibleItemCount, final int totalItemCount) {

		switch(lw.getId()) {
		case android.R.id.list:     
			//	Log.d(TAG, "First Visible: " + firstVisibleItem + ". Visible Count: " + visibleItemCount+ ". Total Items:" + totalItemCount);
			final int lastItem = firstVisibleItem + visibleItemCount;
			if(lastItem == totalItemCount) {
				if(preLast!=lastItem){ //to avoid multiple calls for last item
					if(BuildConfig.DEBUG)
						Log.d(TAG, "MailListViewFragment -> Last Item listener");

					
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
}
