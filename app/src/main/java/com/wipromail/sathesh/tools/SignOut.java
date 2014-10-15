package com.wipromail.sathesh.tools;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.wipromail.sathesh.application.MailApplication;
import com.wipromail.sathesh.application.NotificationProcessing;
import com.wipromail.sathesh.constants.Constants;
import com.wipromail.sathesh.util.Utilities;

/**This class contains method for signing out
 * @author sathesh
 *
 */
public class SignOut implements Constants{


	/** 
	 * THIS METHOD SHOULD BE UPDATED WHENEVER A NEW SHARED PREFERENCE FILE IS ADDED
	 * the following files in the Shared preferences library will be deleted when the user sign out and the "reset settings" option is not selected.
	 * All the files except the preferences file(which stores appln settings, signature, etc) should be deleted.
	 * @return
	 */
	private String[] getSharedPreferencesFilesToDelete(){
		String[] sharedPrefToDel = new String[3];
		sharedPrefToDel[0] = "CRED_PREFS_NAME.xml";
		sharedPrefToDel[1] = "SYSTEM_VARIABLES.xml";
		sharedPrefToDel[2] = "USER_ACCT_DETAILS.xml";
		return sharedPrefToDel;
	}

	/** deletes the cahce databases and shred preferences folder
	 * @param context
	 * @return
	 */
	public boolean signOutAndResetAllSettings(Activity activity, Context context){
		try {
			signOutGeneralActions(context);
			//delete all the shared preferences from memory and file system. passing null will delete all the files
			Utilities.deleteSharedPreferences(context, null);
			
			restartApp(activity, context);
			
			return true;
		} catch (Exception e) {
			Log.e(TAG, "SignOut -> Error occured : signOutAndResetAllSettings :" + e.getMessage());
			e.printStackTrace();
			return false;
		}
	}


	/** deletes the cache databases and shred preferences except the settings file in shared pref folder
	 * @param context
	 * @return
	 */
	public boolean signOutAndRetainSettings(Activity activity, Context context){
		try {

			signOutGeneralActions(context);
			//delete the explicitly given files in the function getSharedPreferencesFilesToDelete
			Log.d(TAG, "SignOut -> Files for delettion " + getSharedPreferencesFilesToDelete().length);
			Utilities.deleteSharedPreferences(context, getSharedPreferencesFilesToDelete());
			restartApp(activity, context);
			return true;

		} catch (Exception e) {
			Log.e(TAG, "SignOut -> Error occured : signOutAndRetainSettings :" + e.getMessage());
			e.printStackTrace();
			return false;
		}
	}


	/** general things to handle on sign out. clear cache,db, stop MNS service
	 * 
	 */
	private void signOutGeneralActions(Context context){
		
		//clear cache
		CacheClear.clearFullCacheAndDbDir(context);

		//stop the MNS service
		MailApplication.stopMNSService(context);
		
		//clear all notification
		NotificationProcessing.cancelAllNotifications(context);
	}

	
	private void restartApp(Activity activity, Context context) {

		activity.finish();
		
		Intent mStartActivity = new Intent(context, MailApplication.mainApplicationActivity());
		int mPendingIntentId = 123456;
		PendingIntent mPendingIntent = PendingIntent.getActivity(context, mPendingIntentId,    mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
		AlarmManager mgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 500, mPendingIntent);
		
		//this wont work if the mainApplicationActivity is not on stack. i.e. when the user pressess the notification, inbox is directly opened
		/*Intent intent = new Intent(context, MailApplication.mainApplicationActivity());
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		context.startActivity(intent);*/
		/* Tried on all of the following for restarting the app. Everything has a disadvantage.
		Intent mStartActivity = new Intent(context, MailApplication.mainApplicationActivity());
		int mPendingIntentId = 123456;
		PendingIntent mPendingIntent = PendingIntent.getActivity(context, mPendingIntentId,    mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
		AlarmManager mgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
	//	mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 2000, mPendingIntent);
		android.os.Process.killProcess(android.os.Process.myPid());
		
			 moveTaskToBack(true);
		*/
	}

}

