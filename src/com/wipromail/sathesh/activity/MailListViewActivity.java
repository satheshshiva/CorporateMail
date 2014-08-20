package com.wipromail.sathesh.activity;

import static com.wipromail.sathesh.constants.Constants.TAG;

import java.io.File;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher;
import android.widget.ViewSwitcher.ViewFactory;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import com.google.analytics.tracking.android.EasyTracker;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.wipromail.sathesh.R;
import com.wipromail.sathesh.animation.ApplyAnimation;
import com.wipromail.sathesh.application.MailApplication;
import com.wipromail.sathesh.application.NotificationProcessing;
import com.wipromail.sathesh.application.interfaces.MailListDataPasser;
import com.wipromail.sathesh.cache.CacheDirectories;
import com.wipromail.sathesh.constants.Constants;
import com.wipromail.sathesh.fragment.MailListViewFragment;
import com.wipromail.sathesh.service.data.ServiceLocalException;
import com.wipromail.sathesh.util.Utilities;

public class MailListViewActivity extends SherlockFragmentActivity implements Constants,ViewSwitcher.ViewFactory,MailListDataPasser, OnRefreshListener<ListView>{

	private Intent intent;
	private TextSwitcher titlebar_inbox_status_textswitcher;
	private TextView textViewLoginId;
	private MailListViewFragment mailListViewFragment;
	private PullToRefreshListView mPullRefreshListView;
	private ImageButton maillist_refresh_button,maillist_compose_button;
	private ProgressBar maillist_refresh_progressbar;
	private ProgressBar maillist_update_progressbar;

	public final static String MAIL_TYPE_EXTRA = "MAIL_TYPE_EXTRA";
	public final static String FOLDER_ID_EXTRA = "FOLDER_ID_EXTRA";
	public final static String FOLDER_NAME_EXTRA = "FOLDER_NAME_EXTRA";

	public static final String EXTRA_MESSAGE_CACHED_HEADER = "cachedMailHeaderToOpen";

	private int mailType ;
	private String mailFolderName;
	private String strFolderId="";

	private ActionBar myActionBar;

	private ImageView successIcon, failureIcon, readIcon, unreadIcon;

	@Override
	public TextSwitcher getTitlebar_inbox_status_textswitcher() {
		return titlebar_inbox_status_textswitcher;
	}



	public void setTitlebar_inbox_status_textswitcher(
			TextSwitcher titlebar_inbox_status_textswitcher) {
		this.titlebar_inbox_status_textswitcher = titlebar_inbox_status_textswitcher;
	}
	@Override
	public void onDestroy() {
		super.onDestroy();

		//deleting cache mail images
		boolean success=false;
		try {
			File cacheDir = new File(getMailCacheImageDirectory());
			if(cacheDir.exists()){
				success=Utilities.deleteDirectory(cacheDir);
				Log.d(TAG, "Deleting image cache directory " +((success)?"successful":"failed"));
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Log.d(TAG, "Exception while deleting cache" + e.getMessage());
			e.printStackTrace();
		}
	}


	public String getMailCacheImageDirectory() throws ServiceLocalException, Exception{
		return CacheDirectories.getApplicationCacheDirectory(this)+"/" + CACHE_DIRECTORY_MAILCACHE ;
		//return MailApplication.getApplicationCacheDirectory(activity).toString() ;
	}

	@Override
	public TextView getTextViewLoginId() {
		// TODO Auto-generated method stub
		return textViewLoginId;
	}

	public void setTextViewLoginId(
			TextView textViewLoginId) {
		this.textViewLoginId = textViewLoginId;
	}


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		mailType = getIntent().getIntExtra(MAIL_TYPE_EXTRA,0);
		strFolderId = getIntent().getStringExtra(FOLDER_ID_EXTRA);
		setMailFolderName(getIntent().getStringExtra(FOLDER_NAME_EXTRA));

		//load the fragment. If different fragment or layout has to be loaded for a mail type, then an "if" condition has to be given here.
		setContentView(R.layout.activity_mail_list_view);

		//Initializing the Text Switcher to use in fragment
		titlebar_inbox_status_textswitcher = (TextSwitcher)findViewById(R.id.titlebar_inbox_status_textswitcher);
		setMaillist_refresh_progressbar((ProgressBar)findViewById(R.id.maillist_refresh_progressbar));
		titlebar_inbox_status_textswitcher.setFactory(new ViewFactory() {
            
            public View makeView() {
                // TODO Auto-generated method stub
                TextView textView = new TextView(MailListViewActivity.this);
                textView.setGravity(Gravity.LEFT);
                textView.setTextSize(12);
                return textView;
            }
        });

		ApplyAnimation.setTitleInboxStatusTextSwitcher(this, titlebar_inbox_status_textswitcher);

		//Initializing the Login id text view to be used in fragment
		textViewLoginId=(TextView)findViewById(R.id.titlebar_loginDisplayName); 

		//loading the pull to refresh fragment list
		mailListViewFragment = (MailListViewFragment) getSupportFragmentManager().findFragmentById(
				R.id.mailListViewFragment);

		// Get PullToRefreshListView from Fragment
		mPullRefreshListView = mailListViewFragment.getPullToRefreshListView();

		// Set a listener to be invoked when the list should be refreshed.
		mPullRefreshListView.setOnRefreshListener(this);
		mPullRefreshListView.setRefreshingLabel(getText(R.string.pullToRefresh_checking_big));
		mailListViewFragment.setListShown(true);

		//Initialize Action Bar
		myActionBar = getSupportActionBar();
		myActionBar.setDisplayHomeAsUpEnabled(true);

		maillist_update_progressbar = (ProgressBar)findViewById(R.id.maillist_update_progressbar);
		//initialize icons
		setSuccessIcon((ImageView)findViewById(R.id.maillist_success_icon));
		setFailureIcon((ImageView)findViewById(R.id.maillist_failure_icon));
		setReadIcon((ImageView)findViewById(R.id.maillist_read_icon));
		setUnreadIcon((ImageView)findViewById(R.id.maillist_unread_icon));

	}

	@Override
	public void onNewIntent(Intent intent){
		super.onNewIntent(intent);
		// this will be called  bcos the flag "singleTask" specified for this activity
		Log.d(TAG, "MailListViewActivity -> onNewIntent() called");
	}
	@Override
	public void onStart() {
		super.onStart();
		//Notificaition
		Log.i(TAG, "MailListViewActivity -> Starting MNS Service");
		MailApplication.startMNSService(this);
		EasyTracker.getInstance().activityStart(this); // Add this method.
	}

	//Google Analytics
	@Override
	public void onStop() {
		super.onStop();
		EasyTracker.getInstance().activityStop(this); // Add this method.
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		mailListViewFragment.createOptionsMenu(menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		mailListViewFragment.optionsItemSelected(item);
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();

		try {
			NotificationProcessing.cancelAllNotifications(this);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void onBackPressed(){
		mailListViewFragment.backPressed();
	}
	@Override
	public View makeView() {
		// TODO Auto-generated method stub
		TextView t = new TextView(this);
		t.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
		t.setTextSize(11);
		return t;
	}

	@Override
	public void onRefresh(PullToRefreshBase<ListView> refreshView) {
		// TODO Auto-generated method stub
		mailListViewFragment.refreshList(false);
	}

	@Override
	public PullToRefreshListView getPullRefreshListView() {
		// TODO Auto-generated method stub
		return mPullRefreshListView;
	}

	public void refreshOnClick(View view){
		mailListViewFragment.refreshList(false);
	}

	public void composeOnClick(View view){
		intent = new Intent(this, ComposeActivity.class);
		startActivity(intent);
	}

	public void moreButtonOnClick(View view){
		openOptionsMenu();
	}

	@Override
	public ImageButton getMaillist_refresh_button() {
		return maillist_refresh_button;
	}

	public void setMaillist_refresh_button(ImageButton maillist_refresh_button) {
		this.maillist_refresh_button = maillist_refresh_button;
	}

	@Override
	public ImageButton getMaillist_compose_button() {
		return maillist_compose_button;
	}

	public void setMaillist_compose_button(ImageButton maillist_compose_button) {
		this.maillist_compose_button = maillist_compose_button;
	}

	@Override
	public ProgressBar getMaillist_refresh_progressbar() {
		return maillist_refresh_progressbar;
	}

	public void setMaillist_refresh_progressbar(
			ProgressBar maillist_refresh_progressbar) {
		this.maillist_refresh_progressbar = maillist_refresh_progressbar;
	}

	@Override
	public ActionBar getMyActionBar() {
		return myActionBar;
	}

	public void setMyActionBar(ActionBar myActionBar) {
		this.myActionBar = myActionBar;
	}

	@Override
	public ImageView getSuccessIcon() {
		return successIcon;
	}

	public void setSuccessIcon(ImageView successIcon) {
		this.successIcon = successIcon;
	}

	@Override
	public ImageView getFailureIcon() {
		return failureIcon;
	}

	public void setFailureIcon(ImageView failureIcon) {
		this.failureIcon = failureIcon;
	}

	@Override
	public ImageView getReadIcon() {
		return readIcon;
	}

	public void setReadIcon(ImageView readIcon) {
		this.readIcon = readIcon;
	}
	@Override
	public ImageView getUnreadIcon() {
		return unreadIcon;
	}

	public void setUnreadIcon(ImageView unreadIcon) {
		this.unreadIcon = unreadIcon;
	}
	@Override
	public ProgressBar getMaillist_update_progressbar() {
		return maillist_update_progressbar;
	}

	public void setMaillist_update_progressbar(
			ProgressBar maillist_update_progressbar) {
		this.maillist_update_progressbar = maillist_update_progressbar;
	}

	@Override
	public int getMailType() {
		return mailType;
	}

	public void setMailType(int mAIL_TYPE) {
		mailType = mAIL_TYPE;
	}

	@Override
	public String getStrFolderId() {
		return strFolderId;
	}

	public void setStrFolderId(String strFolderId) {
		strFolderId = strFolderId;
	}

	@Override
	public String getMailFolderName() {
		return mailFolderName;
	}

	public void setMailFolderName(String mailFolderName) {
		this.mailFolderName = mailFolderName;
	}
}
