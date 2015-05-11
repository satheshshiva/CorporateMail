package com.wipromail.sathesh.jsinterfaces;

import android.util.Log;

import com.wipromail.sathesh.BuildConfig;
import com.wipromail.sathesh.constants.Constants;
import com.wipromail.sathesh.fragment.datapasser.AboutFragmentDataPasser;

public final class AboutActivityJSInterface implements Constants{

	//this
	public static final String ABOUT_ACTIVITY_JS_INTERFACE_NAME="about";	//this is the javascript DOM object
	AboutFragmentDataPasser fragmentDataPasser;

	public AboutActivityJSInterface(AboutFragmentDataPasser fragmentDataPasser){
		this.fragmentDataPasser = fragmentDataPasser;
	}

	@android.webkit.JavascriptInterface
	public void updateButtonOnClick() {
		if(BuildConfig.DEBUG) {
			Log.d(TAG, "UPDATE ONCLICK CALLED");
		}
		fragmentDataPasser.downloadAndUpdate();
	}
}
