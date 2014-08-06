package com.wipromail.sathesh.asynccaller;

import android.util.Log;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.wipromail.sathesh.R;
import com.wipromail.sathesh.asynctask.DeleteItemsAsyncTask;
import com.wipromail.sathesh.asynctask.interfaces.GenericAsyncTask;
import com.wipromail.sathesh.constants.Constants;
import com.wipromail.sathesh.customui.Notifications;
import com.wipromail.sathesh.service.data.Item;

/** This class calls the DeleteMailAsyncTask to delete the email
 * @author sathesh
 *
 */
public class DeleteMailAsyncCaller implements Constants, GenericAsyncTask{

	private SherlockActivity activity;
	private Item item;
	private boolean permanentDelete=false;
	
	public DeleteMailAsyncCaller(SherlockActivity activity, Item item, boolean permanentDelete){
		this.activity = activity;
		this.item = item;
		this.permanentDelete=permanentDelete;
	}
	
	public void startDeleteMailAsyncTask(){

		try {
			//calling the actual async task
			activity.finish();
			new DeleteItemsAsyncTask(this, activity, item, permanentDelete).execute();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Log.e(TAG, "DeleteMailAsyncTaskCaller -> Error while calling async task");
			e.printStackTrace();
		}
	}
	
	@Override
	public void activity_OnPreExecute() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void activity_onProgressUpdate(String... progress) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void activity_OnPostExecute() {
		// TODO Auto-generated method stub
		Notifications.showToast(activity, activity.getString(R.string.viewmail_mail_deleted), Toast.LENGTH_SHORT);
	}

}
