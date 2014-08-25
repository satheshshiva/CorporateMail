package com.wipromail.sathesh.activity;

import static com.wipromail.sathesh.constants.Constants.TAG;

import java.io.File;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.SubMenu;
import com.actionbarsherlock.view.Window;
import com.google.analytics.tracking.android.EasyTracker;
import com.wipromail.sathesh.BuildConfig;
import com.wipromail.sathesh.R;
import com.wipromail.sathesh.application.MailApplication;
import com.wipromail.sathesh.application.NotificationProcessing;
import com.wipromail.sathesh.application.interfaces.MailListDataPasser;
import com.wipromail.sathesh.constants.Constants;
import com.wipromail.sathesh.fragment.MailListViewFragment;
import com.wipromail.sathesh.ui.OptionsUIContent;
import com.wipromail.sathesh.util.Utilities;

public class MailListViewActivity extends SherlockFragmentActivity implements Constants,MailListDataPasser{

	private MailListViewFragment mailListViewFragment;

	public final static String MAIL_TYPE_EXTRA = "MAIL_TYPE_EXTRA";
	public final static String FOLDER_ID_EXTRA = "FOLDER_ID_EXTRA";
	public final static String FOLDER_NAME_EXTRA = "FOLDER_NAME_EXTRA";

	public static final String EXTRA_MESSAGE_CACHED_HEADER = "cachedMailHeaderToOpen";

	private int mailType ;
	private String mailFolderName;
	private String strFolderId="";

	/** ON CREATE **
	 * Gets the mailType folder id and folder name from the intent params. 
	 * Makes it ready by getters so that fragment can make use of
	 * (non-Javadoc)
	 * @see android.support.v4.app.FragmentActivity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		mailType = getIntent().getIntExtra(MAIL_TYPE_EXTRA,0);
		strFolderId = getIntent().getStringExtra(FOLDER_ID_EXTRA);
		mailFolderName = getIntent().getStringExtra(FOLDER_NAME_EXTRA);

		//load the fragment. If different fragment or layout has to be loaded for a mail type, then an "if" condition has to be given here.
		setContentView(R.layout.activity_mail_list_view);

		// declaring the fragment (list fragment). used for calling the refresh in the fragment
		mailListViewFragment = (MailListViewFragment) getSupportFragmentManager()
				.findFragmentById(R.id.mailListViewFragment);
	}
	
	/** ON START **
	 * Starts the MNS service 
	 * (non-Javadoc)
	 * @see android.support.v4.app.FragmentActivity#onStart()
	 */
	@Override
	public void onStart() {
		super.onStart();
		//Notification Service
		if(BuildConfig.DEBUG)
			Log.i(TAG, "MailListViewActivity -> Starting MNS Service");
		MailApplication.startMNSService(this);
		EasyTracker.getInstance().activityStart(this); // Add this method.
	}

	/** ON STOP  **
	 * Google Analytics
	 *  (non-Javadoc)
	 * @see com.actionbarsherlock.app.SherlockFragmentActivity#onStop()
	 */
	@Override
	public void onStop() {
		super.onStop();
		EasyTracker.getInstance().activityStop(this); // Add this method.
	}

	/** ON DESTROY **
	 * Delete the cached images 
	 * (non-Javadoc)
	 * @see com.actionbarsherlock.app.SherlockFragmentActivity#onDestroy()
	 */
	@Override
	public void onDestroy() {
		super.onDestroy();

		//deleting cache mail images
		boolean success=false;
		try {
			File cacheDir = new File(MailApplication.getMailCacheImageDirectory(this.getApplicationContext()));
			if(cacheDir.exists()){
				success=Utilities.deleteDirectory(cacheDir);
				if(BuildConfig.DEBUG){
					Log.d(TAG, "Deleting image cache directory " +((success)?"successful":"failed"));}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			if(BuildConfig.DEBUG){
				Log.d(TAG, "Exception while deleting cache" + e.getMessage());
				e.printStackTrace();
			}
		}
	}

	/** OPTION ITEMS **/
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

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
		return true;
	}
	
	/** OPTION ITEM SELECTED **/
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		if(item!=null && item.getItemId()==android.R.id.home){
			Intent intent = new Intent(this, HomePageActivity.class);
			startActivity(intent);
			finish();
		}
		else if(item!=null && item.getTitle().equals(ACTIONBAR_REFRESH))
		{
			//this shld use an interface to call this method. kind of lazy to create this interface
			mailListViewFragment.refreshList();
		}
		else if(item!=null && item.getTitle().equals(ACTIONBAR_COMPOSE)){
			Intent intent = new Intent(this, ComposeActivity.class);
			startActivity(intent);
		}
		else if(item!=null && item.getTitle().equals(ACTIONBAR_SETTINGS)){
			Intent intent = new Intent(this, PreferencesActivity.class);
			startActivity(intent);
		}
		else if(item!=null && item.getTitle().equals(ACTIONBAR_ABOUT)){
			Intent intent = new Intent(this, AboutActivity.class);
			startActivity(intent);
		}
		return super.onOptionsItemSelected(item);
	}
	
	/** ON RESUME **/
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();

		try {
			//cancel all the notifications
			NotificationProcessing.cancelAllNotifications(this);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/* When back Presed finish this activity
	 * (non-Javadoc)
	 * @see android.support.v4.app.FragmentActivity#onBackPressed()
	 */
	@Override
	public void onBackPressed(){
		finish();
	}

	
	/** GETTER SETTER PART **/
	@Override
	public int getMailType() {
		return mailType;
	}

	@Override
	public String getStrFolderId() {
		return strFolderId;
	}

	@Override
	public String getMailFolderName() {
		return mailFolderName;
	}

}
