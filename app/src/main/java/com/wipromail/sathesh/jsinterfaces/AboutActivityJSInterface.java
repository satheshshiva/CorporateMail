package com.wipromail.sathesh.jsinterfaces;

import android.util.Log;

import com.wipromail.sathesh.activity.AboutActivity;
import com.wipromail.sathesh.constants.Constants;

public final class AboutActivityJSInterface implements Constants{

	public static final String ABOUT_ACTIVITY_JS_INTERFACE_NAME="AboutActivity";
	
	public void updateButtonOnClick() {
		Log.d(TAG, "UPDATE ONCLICK CALLED");
		AboutActivity.downloadAndUpdate();
        }
}
