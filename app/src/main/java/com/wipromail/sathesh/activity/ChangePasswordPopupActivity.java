package com.wipromail.sathesh.activity;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
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
public class ChangePasswordPopupActivity extends Activity implements Constants{

	private static Activity activity;

	private WebView wv;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_change_password);

	}

	@Override
	public void onResume(){
		super.onResume();
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
