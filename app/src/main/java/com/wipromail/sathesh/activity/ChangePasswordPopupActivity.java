package com.wipromail.sathesh.activity;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

import com.google.analytics.tracking.android.EasyTracker;
import com.wipromail.sathesh.R;
import com.wipromail.sathesh.constants.Constants;


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
