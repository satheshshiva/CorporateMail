package com.sathesh.corporatemail.application;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

import android.app.Application;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.sathesh.corporatemail.R;
import com.sathesh.corporatemail.constants.Constants;

//@ReportsCrashes(formKey = "", // will not be used
//mailTo = "satheshshiva@gmail.com",
//mode = ReportingInteractionMode.TOAST,
//resToastText = R.string.crash_toast_text)
@ReportsCrashes(formKey = "dDkwdFJDQTEwWkUySGpHcUF5SjBPMWc6MQ",
forceCloseDialogAfterToast = false ,
mode = ReportingInteractionMode.TOAST,
resToastText = R.string.crash_toast_text) 

public class MyApplication extends Application implements Constants{

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
