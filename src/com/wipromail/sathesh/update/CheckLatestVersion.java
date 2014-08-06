package com.wipromail.sathesh.update;

import android.app.ProgressDialog;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;
import android.webkit.WebView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.wipromail.sathesh.BuildConfig;
import com.wipromail.sathesh.R;
import com.wipromail.sathesh.application.MailApplication;
import com.wipromail.sathesh.asynctask.UpdateCheckerAsyncTask;
import com.wipromail.sathesh.asynctask.interfaces.GenericAsyncTask;
import com.wipromail.sathesh.constants.Constants;
import com.wipromail.sathesh.customui.Notifications;

/** This class will check for the latest available updates for the app
 * @author sathesh
 *
 */
public class CheckLatestVersion implements Constants, GenericAsyncTask {

	private SherlockActivity activity;
	private WebView wv;
	ProgressDialog dialog;
	String releaseType="";

	public CheckLatestVersion(SherlockActivity activity, WebView wv) {
		// TODO Auto-generated constructor stub
		this.activity = activity;
		this.wv=wv;
	}

	public void startAsyncCheck(){

		// TODO Auto-generated method stub

		try {
			//calling the actual async task
			new UpdateCheckerAsyncTask(this, activity, MailApplication.getAppVersionCode(activity)).execute();
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			Log.e(TAG, "CheckLatestVersion -> Error getting  existing version");
			e.printStackTrace();
		}
	}

	@Override
	public void activity_OnPreExecute() {
		// TODO Auto-generated method stub

		try {
			activity.setSupportProgressBarIndeterminateVisibility(true);
			dialog = ProgressDialog.show(activity, "", 
					activity.getString(R.string.app_updater_checking), true);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.e(TAG, "Exception occured on preexecute");
		}

	}


	@Override
	public void activity_onProgressUpdate(String... progress) {
		try{
			if(progress[0].equals(UpdateCheckerAsyncTask.STATUS_CHECKING)){
				dialog.setMessage(progress[1]);

			}
			else if(progress[0].equals(UpdateCheckerAsyncTask.STATUS_UPDATE_AVAILABLE)){
				updateAvailable(activity);

			}
			else if(progress[0].equals(UpdateCheckerAsyncTask.STATUS_NO_UPDATE)){
				noUpdateAvailable(activity);

			}
			else if(progress[0].equals(UpdateCheckerAsyncTask.STATUS_ERROR)){
				activity.setSupportProgressBarIndeterminateVisibility(false);
				dialog.dismiss();
				Notifications.showAlert(activity, activity.getText(R.string.app_updater_error) + "\nDetails: " + progress[1]);
			}
		}
		catch(Exception e){
		}
	}

	public void noUpdateAvailable(SherlockActivity activity) {
		// TODO Auto-generated method stub
		activity.setSupportProgressBarIndeterminateVisibility(false);
		dialog.dismiss();
		Notifications.showToast(activity,  activity.getText(R.string.app_updater_noupdates), Toast.LENGTH_SHORT);
	}

	public void updateAvailable(SherlockActivity activity) {
		// TODO Auto-generated method stub
		
		dialog.dismiss();
		Log.d(TAG, "CheckLatestVersion -> showing loading progress for change log page");
		wv.loadUrl(LOADING_HTML_URL);
		//wait for a sec for the above img to load properly. otherwise it wont be displayed..	
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if (BuildConfig.DEBUG){
			Log.d(TAG, "CheckLatestVersion -> loading change log page for DEV");
			wv.loadUrl(APPLICATION_CHANGELOG_URL_DEV);

			//wv.loadUrl("file:///android_asset/ChangeLog.html");
		}
		else {
			Log.d(TAG, "CheckLatestVersion -> loading change log page for REL");
			wv.loadUrl(APPLICATION_CHANGELOG_URL_REL);
		}
	}


	@Override
	public void activity_OnPostExecute() {
		// TODO Auto-generated method stub

	}


}