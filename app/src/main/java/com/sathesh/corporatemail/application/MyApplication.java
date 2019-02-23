package com.sathesh.corporatemail.application;

import android.app.Application;
import android.support.multidex.MultiDexApplication;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.sathesh.corporatemail.R;
import com.sathesh.corporatemail.constants.Constants;

public class MyApplication extends MultiDexApplication implements Constants{

	private static GoogleAnalytics sAnalytics;
	private static Tracker sTracker;

	  @Override
	  public void onCreate() {
	      super.onCreate();

	      // The following line triggers the initialization of ACRA
	     // ACRA.init(this);

		  sAnalytics = GoogleAnalytics.getInstance(this);
	  }


	/**
	 * Gets the default {@link Tracker} for this {@link Application}.
	 * @return tracker
	 */
	synchronized public Tracker getDefaultTracker() {
		// To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
		if (sTracker == null) {
			sTracker = sAnalytics.newTracker(R.xml.global_tracker);
		}

		return sTracker;
	}
}
