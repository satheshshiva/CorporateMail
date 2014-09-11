package com.wipromail.sathesh.update;

import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;

import com.actionbarsherlock.app.SherlockActivity;
import com.wipromail.sathesh.R;
import com.wipromail.sathesh.activity.AboutActivity;
import com.wipromail.sathesh.application.MailApplication;
import com.wipromail.sathesh.application.SharedPreferencesAdapter;
import com.wipromail.sathesh.asynctask.UpdateCheckerAsyncTask;
import com.wipromail.sathesh.asynctask.interfaces.GenericAsyncTask;
import com.wipromail.sathesh.constants.Constants;
import com.wipromail.sathesh.customui.Notifications;
import com.wipromail.sathesh.util.Utilities;


/** This class will be used for checking the app version updates for the user.
 * @author sathesh
 *
 */
public class AutoUpdater implements Constants, GenericAsyncTask{

	private static SherlockActivity activity;

	/** This is the entry point for auto check updater. this will check for time difference between 
	 * the last successful update check and will procees when it exceed the specified no of day limit
	 * @param activity
	 */
	public  static void autoCheckForUpdates(SherlockActivity activity){

		AutoUpdater.activity=activity;
		Date date;
		if(MailApplication.isAutoUdpateNotifyEnabled(activity)){
		try{
			date = getLastUpdateCheckTime();
		}
		catch(IllegalArgumentException ie){
			ie.printStackTrace();
			Log.e(TAG, "AutoUpdater -> Not checking for updates since the last stored date was not in a readable format");
			return;
		}
		long dateDiff=0;
		if(date!=null){
			dateDiff = Utilities.getNumberOfDaysFromToday(date);
			if(dateDiff >= UPDATE_AUTO_CHECK_NO_OF_DAYS){
				startUpdateCheck();
			}
			else{
				Log.i(TAG, "AutoUdpater -> Not checinking for new version since the last check was " + dateDiff + " day(s) ago");
			}

		}
		else{
			//will be called for the first time update checking is done
			startUpdateCheck();
		}
		}
		else{
			Log.i(TAG, "AutoUpdater -> Not checking for updates since the option is not enabled in settings");
		}
	}

	/** when update check is needed, this will be called to start the async task and to check for update in internet
	 * 
	 */
	private static void startUpdateCheck() {
		// TODO Auto-generated method stub
		try {
			//calling the actual async task
			new UpdateCheckerAsyncTask(new AutoUpdater(), activity, MailApplication.getAppVersionCode(activity)).execute();
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			Log.e(TAG, "AutoUpdater -> Error getting  existing version");
			e.printStackTrace();
		}
	}

	/* The async task will call this for pre execute
	 * (non-Javadoc)
	 * @see com.wipromail.sathesh.interfaces.GenericAsyncTask#activity_OnPreExecute()
	 */
	@Override
	public void activity_OnPreExecute() {
		// TODO Auto-generated method stub
	}

	/* The async task will call this on progress update
	 * (non-Javadoc)
	 * @see com.wipromail.sathesh.interfaces.GenericAsyncTask#activity_onProgressUpdate(java.lang.String[])
	 */
	@Override
	public void activity_onProgressUpdate(String... progress) {
		// TODO Auto-generated method stub

		if(progress[0].equals(UpdateCheckerAsyncTask.STATUS_CHECKING)){
		}
		else if(progress[0].equals(UpdateCheckerAsyncTask.STATUS_UPDATE_AVAILABLE)){
			updateAvailable(activity);

		}
		else if(progress[0].equals(UpdateCheckerAsyncTask.STATUS_NO_UPDATE)){
			noUpdateAvailable(activity);

		}
		else if(progress[0].equals(UpdateCheckerAsyncTask.STATUS_ERROR)){
			Log.e(TAG, "AutoUpdater -> Error occured while checking for updates");
		}
	}

	@Override
	public void activity_OnPostExecute() {
		// TODO Auto-generated method stub

	}


	/**this is called when there is no update available
	 * 
	 * @param activity
	 */
	public void noUpdateAvailable(SherlockActivity activity) {
		// TODO Auto-generated method stub

		Log.i(TAG, "AutoUpdater -> no update availble");
		storeUpdateCheckTime(new Date());
	}

	/**This will be called when there is an update is avialble for the user to download
	 * 
	 * @param activity
	 */
	public void updateAvailable(SherlockActivity activity) {
		// TODO Auto-generated method stub

		final Activity _activity = activity;
		Log.i(TAG, "AutoUpdater -> update availble");
		storeUpdateCheckTime(new Date());

		try {
			buildAlertDialog(_activity,_activity.getString(R.string.auto_update_check_new_update_title), 
					_activity.getString(R.string.auto_update_check_new_update_msg), 
					_activity.getString(R.string.auto_update_check_new_update_pos_button), 
					_activity.getString(R.string.auto_update_check_new_update_neg_button)
					);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Log.e(TAG, "Error occured while showing the alert box that a new update is avialble ");
			e.printStackTrace();
		}
	}

	/** stores the time to Shared preferences storage when update check is made
	 * @param date
	 */
	private void storeUpdateCheckTime(Date date ){
		try {
			if(date != null){
				SharedPreferencesAdapter.storeLastSuccessfulAutoUpdateCheck(activity, date.toString());
			}
			else{
				Log.e(TAG, "Last auto update check time is not stored due to error: Date is null");
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Log.e(TAG, "Last auto update check time is not stored due to error " + e.getMessage());
		}
	}

	/** gets the last date in which an update check is made
	 * @return
	 * @throws IllegalArgumentException
	 */
	private static Date getLastUpdateCheckTime() throws IllegalArgumentException{
		String dateStr="";
		Date date=null;
		try {
			dateStr=SharedPreferencesAdapter.getLastautoUpdateCheck(activity);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Log.e(TAG, "Last auto update check time is not retrieved due to error " + e.getMessage());
		}
		if(!dateStr.equals("")){
			date = new Date(dateStr);

		}
		return date;

	}

	/** builds the alert dialog to display in screen to show that there is an udpate avialble
	 * @param context
	 * @param title
	 * @param message
	 * @param positiveMsg
	 * @param negativeMsg
	 */
	public  void buildAlertDialog(Activity activity, String title, String message, String positiveMsg, String negativeMsg){

		final Activity _activity = activity;
		AlertDialog newUpdateBox =new AlertDialog.Builder(_activity) 
		//set message, title, and icon
		.setTitle(title) 
		.setMessage(message) 
		.setPositiveButton(positiveMsg	, new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int whichButton) { 
				Intent intent = new Intent(_activity,  AboutActivity.class);
				intent.putExtra(AboutActivity.CHECK_UPDATES_ONLOAD_EXTRA, true);
				_activity.startActivity(intent);
			}   

		})
		.setNegativeButton(negativeMsg, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {

				dialog.dismiss();
				Notifications.showInfo(_activity, _activity.getString(R.string.auto_update_check_dismiss_dialog) );

			}
		})
		.create();

		newUpdateBox.show();
	}

}
