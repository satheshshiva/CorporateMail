package com.sathesh.corporatemail.tools;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.sathesh.corporatemail.activity.MailListViewActivity;
import com.sathesh.corporatemail.activity.MainActivity;
import com.sathesh.corporatemail.application.MailApplication;
import com.sathesh.corporatemail.application.NotificationProcessing;
import com.sathesh.corporatemail.constants.Constants;
import com.sathesh.corporatemail.util.Utilities;

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
			Utilities.deleteSharedPreferences(context, (String[]) null);
			
			restartApp(activity, context);
			
			return true;
		} catch (Exception e) {
			Log.e(LOG_TAG, "SignOut -> Error occured : signOutAndResetAllSettings :" + e.getMessage());
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
			Log.d(LOG_TAG, "SignOut -> Files for delettion " + getSharedPreferencesFilesToDelete().length);
			Utilities.deleteSharedPreferences(context, getSharedPreferencesFilesToDelete());
			restartApp(activity, context);
			return true;

		} catch (Exception e) {
			Log.e(LOG_TAG, "SignOut -> Error occured : signOutAndRetainSettings :" + e.getMessage());
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
		MailApplication.stopMNWorker(context);
		
		//clear all notification
		NotificationProcessing.cancelAllNotifications(context);
	}

	
	private void restartApp(Activity activity, Context context) {

        Intent intent;

		//activity.finish();
        // closes the app
        intent = new Intent(context, MailListViewActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(MailListViewActivity.SIGN_OUT_EXTRA, true);
        activity.startActivity(intent);

        intent = new Intent(context, MainActivity.class);
        activity.startActivity(intent);

		//Reopen the app after some time using alarm manager. this code was used before because the current actity is finish() before this line
        // and the code for start activity was not getting executed.
      /* Intent mStartActivity = new Intent(context, MailApplication.mainApplicationActivity());
		int mPendingIntentId = 123456;
		PendingIntent mPendingIntent = PendingIntent.getActivity(context, mPendingIntentId,    mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
		AlarmManager mgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 500, mPendingIntent);
*/

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

