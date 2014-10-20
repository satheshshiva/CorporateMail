package com.wipromail.sathesh.activity;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import com.google.analytics.tracking.android.EasyTracker;
import com.wipromail.sathesh.BuildConfig;
import com.wipromail.sathesh.R;
import com.wipromail.sathesh.adapter.ComposeActivityAdapter;
import com.wipromail.sathesh.application.MailApplication;
import com.wipromail.sathesh.asynctask.DownloadAndUpdateAppAsyncTask;
import com.wipromail.sathesh.constants.Constants;
import com.wipromail.sathesh.customserializable.ContactSerializable;
import com.wipromail.sathesh.customui.Notifications;
import com.wipromail.sathesh.ews.EWSConnection;
import com.wipromail.sathesh.jsinterfaces.AboutActivityJSInterface;
import com.wipromail.sathesh.service.data.ExchangeService;
import com.wipromail.sathesh.update.CheckLatestVersion;


/* Source code for Automatic Download and Installation of APK: http://stackoverflow.com/questions/4967669/android-install-apk-programmatically */

/**
 * @author sathesh
 *
 */
public class AboutActivity extends SherlockActivity implements Constants{

	private static Activity activity;
	private WebView wv;

	private ExchangeService service;
	private boolean checkUpdatesOnload;
	public static String CHECK_UPDATES_ONLOAD_EXTRA ="CHECK_UPDATES_ONLOAD_EXTRA";

	private PackageInfo pInfo ;
	private Button bugOrSuggestionBtn;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_about);

		try {
			service = EWSConnection.getServiceFromStoredCredentials(this);}
		catch (Exception e) {
			e.printStackTrace();
		}


		checkUpdatesOnload = getIntent().getBooleanExtra(CHECK_UPDATES_ONLOAD_EXTRA , false);
		this.activity=this;
		wv = (WebView)activity.findViewById(R.id.aboutActivityWebView);

		//the following code will prevent new webview from opening when loading url
		wv.setWebViewClient(new WebViewClient() {
			public boolean shouldOverrideUrlLoading(WebView view, String url) {

				view.loadUrl(url);
				return true;
			}});

		wv.setWebChromeClient(new WebChromeClient() {
			public void onProgressChanged(WebView view, int progress)   
			{

				// Return the app name after finish loading
				if(progress == 100)
					setSupportProgressBarIndeterminateVisibility(false);
			}
		});

		WebSettings webSettings = wv.getSettings();
		webSettings.setSavePassword(false);
		webSettings.setSaveFormData(false);
		webSettings.setJavaScriptEnabled(true);	//this is important
		webSettings.setSupportZoom(false);
		//for displaying js alert in console

		//	wv.setWebChromeClient(new CommonWebChromeClient());

		wv.addJavascriptInterface(new AboutActivityJSInterface(), AboutActivityJSInterface.ABOUT_ACTIVITY_JS_INTERFACE_NAME);
		setSupportProgressBarIndeterminateVisibility(false);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		bugOrSuggestionBtn = (Button)findViewById(R.id.about_activity_bugOrSuggestion_btn);
		//hide the Bug/suggestion buttton if no user signed in.
		try {
			if(!(MailApplication.checkUserIfSignedIn(this))){
				bugOrSuggestionBtn.setVisibility(View.GONE);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onResume(){
		super.onResume();

		if(checkUpdatesOnload){
			checkUpdates();
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		if(item!=null && item.getItemId()==android.R.id.home){
			finish();
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		//Used to put dark icons on light action bar
		/*
		  boolean isLight=true;
	        menu.add("Save")
	            .setIcon(isLight ? R.drawable.ic_compose_inverse : R.drawable.ic_compose)
	            .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

	        menu.add("Search")
	            .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		 */
		return true;
	}

	public void onClickChkUpdate(View view) {
		checkUpdates();
	}


	private void checkUpdates() {
				new CheckLatestVersion(this,wv).startAsyncCheck();
	}

	public void onClickRateApp(View view) {
		Log.d(TAG, "OnClickRate app");
		try {
			MailApplication.openPlayStoreLink(this);
		}
		catch (ActivityNotFoundException e) {
			Notifications.showToast(this, getText(R.string.playstore_not_available), Toast.LENGTH_SHORT);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void downloadAndUpdate() {
		if (BuildConfig.DEBUG){
			new DownloadAndUpdateAppAsyncTask(activity).execute(APPLICATION_APK_DOWNLOAD_URL1_DEV);
		}
		else {
			new DownloadAndUpdateAppAsyncTask(activity).execute(APPLICATION_APK_DOWNLOAD_URL1_REL);	
		}
	}
	
	/** This will be invoked when the facebook like image is clicked
	 * @param view
	 */
	public void fbOnclick(View view){
		Intent fbIntent;
		try {
		    getPackageManager().getPackageInfo("com.facebook.katana", 0);
		    fbIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(FB_LIKE_URL_APP));
		    
		   } catch (Exception e) {
			   fbIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(FB_LIKE_URL_BROWSER));
		   }
		try{
		 startActivity(fbIntent);
		}catch(Exception e){e.printStackTrace();}
	}

	public void onClose(View view){
		finish();
	}

	/** open the Compose activity to send email to developer with prefilled developer details
	 * @param view
	 * @throws NameNotFoundException 
	 */
	public void onBugOrSuggestion(View view) throws NameNotFoundException{

		//create a ContactSerializable to hold the To value of the developer
		Bundle toBundle = new Bundle();
		String developerEmail = getText(R.string.bugsOrSuggestion_developer_email).toString();
		ContactSerializable developerContact = new ContactSerializable(developerEmail, developerEmail, true);	//true autoresolves the entry
		//put the ContactSerializable to a bundle
		toBundle.putSerializable(developerEmail, developerContact);	//the key value (developer email) for the bundle is not needed since ComposeActivity concerns only the values

		//get the app version info
		pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);

		ComposeActivityAdapter.startPrefilledCompose(this, 
				ComposeActivity.PREFILL_TYPE_BUGS_SUGGESTIONS,
				toBundle, null, null, 
				getString(R.string.bugsOrSuggestion_email_subject, pInfo.versionName),
				getString(R.string.bugsOrSuggestion_email_titlebar), 
				true);
	}

	//Google Analytics
	@Override
	public void onStart() {
		super.onStart();
		EasyTracker.getInstance().activityStart(this); // Add this method.
	}

	@Override
	public void onStop() {
		super.onStop();
		EasyTracker.getInstance().activityStop(this); // Add this method.
	}

}
