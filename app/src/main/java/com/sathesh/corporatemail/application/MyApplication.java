package com.sathesh.corporatemail.application;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

import android.app.Application;

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
	
	  @Override
	  public void onCreate() {
	      super.onCreate();

	      // The following line triggers the initialization of ACRA
	      ACRA.init(this);
	  }
	 
}
