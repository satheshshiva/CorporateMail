package com.sathesh.corporatemail.update;

import android.app.ProgressDialog;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;
import android.webkit.WebView;
import android.widget.Toast;

import com.sathesh.corporatemail.BuildConfig;
import com.sathesh.corporatemail.R;
import com.sathesh.corporatemail.application.MailApplication;
import com.sathesh.corporatemail.application.MyActivity;
import com.sathesh.corporatemail.asynctask.UpdateCheckerAsyncTask;
import com.sathesh.corporatemail.asynctask.interfaces.GenericAsyncTask;
import com.sathesh.corporatemail.constants.Constants;
import com.sathesh.corporatemail.customui.Notifications;

/** This class will check for the latest available updates for the app
 * @author sathesh
 *
 */
public class CheckLatestVersion implements Constants, GenericAsyncTask {

	private MyActivity activity;
	private WebView wv;
	ProgressDialog dialog;
	String releaseType="";

	public CheckLatestVersion(MyActivity activity, WebView wv) {
		this.activity = activity;
		this.wv=wv;
	}

	public void startAsyncCheck(){

		try {
			//calling the actual async task
			new UpdateCheckerAsyncTask(this, activity, MailApplication.getAppVersionCode(activity)).execute();
		} catch (NameNotFoundException e) {
			Log.e(TAG, "CheckLatestVersion -> Error getting  existing version");
			e.printStackTrace();
		}
	}

	@Override
	public void activity_OnPreExecute() {

		try {
			dialog = ProgressDialog.show(activity, "",
					activity.getString(R.string.app_updater_checking), true);

		} catch (Exception e) {
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
				dialog.dismiss();
				Notifications.showAlert(activity, activity.getText(R.string.app_updater_error) + "\nDetails: " + progress[1]);
			}
		}
		catch(Exception e){
		}
	}

	public void noUpdateAvailable(MyActivity activity) {
		dialog.dismiss();
		Notifications.showToast(activity,  activity.getText(R.string.app_updater_noupdates), Toast.LENGTH_SHORT);
	}

	public void updateAvailable(MyActivity activity) {

		dialog.dismiss();
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

	}


}