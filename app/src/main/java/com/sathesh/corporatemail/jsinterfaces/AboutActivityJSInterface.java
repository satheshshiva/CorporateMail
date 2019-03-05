package com.sathesh.corporatemail.jsinterfaces;

import android.content.Context;
import androidx.fragment.app.Fragment;
import android.util.Log;

import com.sathesh.corporatemail.BuildConfig;
import com.sathesh.corporatemail.application.MailApplication;
import com.sathesh.corporatemail.constants.Constants;
import com.sathesh.corporatemail.fragment.datapasser.AboutFragmentDataPasser;

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

	@android.webkit.JavascriptInterface
	public void playstoreLink(String packageUrl) {
		if(BuildConfig.DEBUG) {
			Log.d(TAG, "packageUrl " + packageUrl);
		}
		try {
			MailApplication.openPlayStoreLink((Context)(((Fragment)fragmentDataPasser).getActivity()),packageUrl);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
