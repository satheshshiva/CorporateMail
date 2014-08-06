package com.wipromail.sathesh.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.SubMenu;
import com.actionbarsherlock.view.Window;
import com.google.analytics.tracking.android.EasyTracker;
import com.wipromail.sathesh.BuildConfig;
import com.wipromail.sathesh.R;
import com.wipromail.sathesh.constants.Constants;
import com.wipromail.sathesh.constants.Constants.MailType;
import com.wipromail.sathesh.ews.EWSConnection;
import com.wipromail.sathesh.ews.NetworkCall;
import com.wipromail.sathesh.jsinterfaces.CommonWebChromeClient;
import com.wipromail.sathesh.service.data.ExchangeService;
import com.wipromail.sathesh.service.data.FindFoldersResults;
import com.wipromail.sathesh.service.data.Folder;
import com.wipromail.sathesh.service.data.FolderId;
import com.wipromail.sathesh.service.data.WellKnownFolderName;
import com.wipromail.sathesh.ui.OptionsUIContent;

public class OtherFoldersPageActivity extends SherlockActivity implements Constants{

	private WebView webview;
	private Handler mHandler = new Handler();
	private Activity activity;
	private Intent intent;
	private ActionBar myActionBar;
	private ExchangeService service;
	private StringBuilder builder;

	private String conversationHistoryId="";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_folders_page);
		//getSupportActionBar().setIcon(R.drawable.);
		activity=this;
		webview = (WebView)findViewById(R.id.folders_page_webview);

		WebSettings webSettings = webview.getSettings();
		webSettings.setAllowFileAccess(true);

		webSettings.setJavaScriptEnabled(true);	//this is important
		webSettings.setSupportZoom(false);
		webSettings.setBuiltInZoomControls(false);

		webview.setWebChromeClient(new CommonWebChromeClient());
		webview.addJavascriptInterface(new JavaScriptInterface(), "folderspage");

		webview.loadUrl("file:///android_asset/www/pages/otherfolderspage.html");
		builder = new StringBuilder();
		myActionBar = getSupportActionBar();
		myActionBar.setDisplayHomeAsUpEnabled(true);
		try {
			service = EWSConnection.getServiceFromStoredCredentials(activity.getApplicationContext());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		setSupportProgressBarIndeterminateVisibility(true);

		(new GetFolders()).execute();
	}

	@Override
	protected void onResume() {
		super.onResume();



	}

	private class JavaScriptInterface{
		JavaScriptInterface() {
		}

		private Intent intent = new Intent(activity, MailListViewActivity.class);
		/**
		 * This is not called on the UI thread. Post a runnable to invoke
		 * loadUrl on the UI thread.
		 */

		public void openOutbox() {
			intent.putExtra(MailListViewActivity.MAIL_TYPE_EXTRA, MailType.OUTBOX);
			intent.putExtra(MailListViewActivity.FOLDER_NAME_EXTRA, WellKnownFolderName.Outbox.toString());
			startActivity(intent);
		}

		public void openJunkEmail() {
			intent.putExtra(MailListViewActivity.MAIL_TYPE_EXTRA, MailType.JUNK_EMAIL);
			intent.putExtra(MailListViewActivity.FOLDER_NAME_EXTRA, WellKnownFolderName.JunkEmail.toString());
			startActivity(intent);
		}

		public void openConversationHistory() {
			intent.putExtra(MailListViewActivity.MAIL_TYPE_EXTRA, MailType.CONVERSATION_HISTORY);
			intent.putExtra(MailListViewActivity.FOLDER_ID_EXTRA, conversationHistoryId);
			intent.putExtra(MailListViewActivity.FOLDER_NAME_EXTRA, activity.getString(R.string.ConversationHistoryFolderName));
			startActivity(intent);
		}

		public void openInboxSubFolder(final String folderId , final String folderName) {
			intent.putExtra(MailListViewActivity.MAIL_TYPE_EXTRA, MailType.INBOX_SUBFOLDER_WITH_ID);
			intent.putExtra(MailListViewActivity.FOLDER_ID_EXTRA, folderId);
			intent.putExtra(MailListViewActivity.FOLDER_NAME_EXTRA, folderName);	// used for setting actionbar title
			startActivity(intent);
		}

		public void openFolderId(final String folderId , final String folderName) {
			mHandler.post(new Runnable() {
				public void run() {

					intent.putExtra(MailListViewActivity.MAIL_TYPE_EXTRA, MailType.FOLDER_WITH_ID);
					intent.putExtra(MailListViewActivity.FOLDER_ID_EXTRA, folderId);
					intent.putExtra(MailListViewActivity.FOLDER_NAME_EXTRA, folderName);

					startActivity(intent);
				}
			});

		}

	}


	private class GetFolders extends AsyncTask<Void, String, Void>{


		@Override
		protected Void doInBackground(Void... paramArrayOfParams) {
			// TODO Auto-generated method stub

			/*
			try {
				conversationHistoryId = getConversationHistoryFolderId(service);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			//Conversation History link
			if(!(conversationHistoryId.equals(""))){
				builder.append(getHTMLLinkInboxSubfolder(activity.getString(R.string.ConversationHistoryLinkName), conversationHistoryId));
			}
			 */

			//populate MsgRoot
			try {
				recursivePopulateFolders(service, FolderId.getFolderIdFromWellKnownFolderName(WellKnownFolderName.MsgFolderRoot), false);

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			/*
			//populate inbox subfolders
			try {
				recursivePopulateFolders(service, FolderId.getFolderIdFromWellKnownFolderName(WellKnownFolderName.Inbox), true);
				//recursivePopulateFolders(service, FolderId.getFolderIdFromWellKnownFolderName(WellKnownFolderName.SentItems));

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			//populate Sent items subfolders
			try {
				recursivePopulateFolders(service, FolderId.getFolderIdFromWellKnownFolderName(WellKnownFolderName.SentItems), false);
				//recursivePopulateFolders(service, FolderId.getFolderIdFromWellKnownFolderName(WellKnownFolderName.SentItems));

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			 */
			return null;
		}


		private  void recursivePopulateFolders( ExchangeService service, FolderId folderId, boolean isInboxSubfolder) throws Exception{
			// TODO Auto-generated method stub
			//EWS call
			if(BuildConfig.DEBUG){
				Log.i(TAG, "OtherFoldersPageActivity -> EWS Call");
			}
			FindFoldersResults findResults = NetworkCall.getFolders(service, folderId);
			if(BuildConfig.DEBUG){
				Log.i(TAG, "OtherFoldersPageActivity -> " + findResults.getFolders());
			}

			if(findResults.getTotalCount() > 0 ){
				if(folderId!=null && folderId.getFolderName()!=null){
					if(!folderId.getFolderName().toString().equals("MsgFolderRoot")){
						builder.append(getHTMLHeader(folderId.getFolderName().toString()));
					}
				}
			}
			for(Folder folder : findResults.getFolders())
			{     
				if(BuildConfig.DEBUG){
					//Log.i(TAG, folderId.getFolderName().toString());
					Log.i(TAG, "Count======"+folder.getChildFolderCount());                
					Log.i(TAG, "Name======="+folder.getDisplayName());
					Log.i(TAG, folder.getId().getUniqueId());
				}
				if(isInboxSubfolder){
					builder.append(getHTMLLinkInboxSubfolder(folder.getDisplayName(), folder.getId().getUniqueId()));
				}
				else{
					builder.append(getHTMLLink(folder.getDisplayName(), folder.getId().getUniqueId()));

				}
			}

			for(Folder folder : findResults.getFolders()){
				if(folder.getChildFolderCount() > 0){
					if(BuildConfig.DEBUG){
						Log.i(TAG, "PARENT " + folder.getDisplayName().toString());
					}
					builder.append(getHTMLHeader(folder.getDisplayName()));
					Thread.sleep(3*1000);
					recursivePopulateFolders(service, folder.getId(), isInboxSubfolder);
				}
			}
		}


		private  String getConversationHistoryFolderId( ExchangeService service) throws Exception{
			// TODO Auto-generated method stub
			//EWS Call
			FindFoldersResults findResults = NetworkCall.getFolders(service, FolderId.getFolderIdFromWellKnownFolderName(WellKnownFolderName.MsgFolderRoot));


			for(Folder folder : findResults.getFolders())
			{  
				if(folder.getDisplayName()!=null && folder.getDisplayName().equals("Conversation History")){

					return folder.getId().getUniqueId();
				}

			}
			return "";
		}


		@Override
		protected void onProgressUpdate(String... str) {

		}

		@Override
		protected void onPostExecute(Void a) {


			//	webview.loadUrl("javascript:document.getElementById('progressCircle').style.display='none'");
			webview.loadUrl("javascript:$('#mailListView').append(\"" + builder.toString() +"\");");
			webview.loadUrl("javascript:$('#mailListView').listview('refresh');");

			setSupportProgressBarIndeterminateVisibility(false);
		}


	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);


		//Submenu
		SubMenu subMenu = menu.addSubMenu("");
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


		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{


		if(item!=null && item.getItemId()==android.R.id.home){
			finish();
		}
		else if(item!=null && item.getTitle().equals(ACTIONBAR_SETTINGS)){
			intent = new Intent(this, PreferencesActivity.class);
			startActivity(intent);
		}
		else if(item!=null && item.getTitle().equals(ACTIONBAR_ABOUT)){
			intent = new Intent(this, AboutActivity.class);
			startActivity(intent);
		}

		return super.onOptionsItemSelected(item);

	}

	private String getHTMLHeader(String headerName){
		return "<li data-role=\\\"list-divider\\\" role=\\\"heading\\\">"  +  headerName + "</li>";
	}

	private String getHTMLLink(String linkName, String folderId){
		return "<li><a href=\\\"javascript:onOpenFolderId('"  +folderId  + "', '" + linkName +"');\\\">" + linkName + "</a></li>";
	}
	private String getHTMLLinkInboxSubfolder(String linkName, String folderId){
		return "<li><a href=\\\"javascript:onOpenInboxSubFolder('"  +folderId  + "', '" + linkName +"');\\\">" + linkName + "</a></li>";
	}

	//Google Analytics
	@Override
	public void onStart() {
		super.onStart();
		EasyTracker.getInstance().activityStart(this);
	}

	@Override
	public void onStop() {
		super.onStop();
		EasyTracker.getInstance().activityStop(this); 
	}
}
