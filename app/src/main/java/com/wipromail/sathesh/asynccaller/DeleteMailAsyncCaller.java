package com.wipromail.sathesh.asynccaller;

import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.Toast;

import com.wipromail.sathesh.R;
import com.wipromail.sathesh.asynctask.DeleteItemIdsAsyncTask;
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

	private ActionBarActivity activity;
	private Item item;
    private String itemId="";
	private boolean permanentDelete=false;
	
	public DeleteMailAsyncCaller(ActionBarActivity activity, Item item, String itemId, boolean permanentDelete){
		this.activity = activity;
		this.item = item;
        this.itemId=itemId;
		this.permanentDelete=permanentDelete;
	}
	
	public void startDeleteMailAsyncTask(){

		try {
			//calling the actual async task
			activity.finish();
            if(item!=null){
                new DeleteItemsAsyncTask(this, activity, item, permanentDelete).execute();
            }
            else if(itemId!=null && !itemId.equals("")){
                new DeleteItemIdsAsyncTask(this, activity, itemId, permanentDelete).execute();
            }
		} catch (Exception e) {
			Log.e(TAG, "DeleteMailAsyncTaskCaller -> Error while calling async task");
			e.printStackTrace();
		}
	}
	
	@Override
	public void activity_OnPreExecute() {
	}

	@Override
	public void activity_onProgressUpdate(String... progress) {
	}

	@Override
	public void activity_OnPostExecute() {
		Notifications.showToast(activity, activity.getString(R.string.viewmail_mail_deleted), Toast.LENGTH_SHORT);
	}

}
