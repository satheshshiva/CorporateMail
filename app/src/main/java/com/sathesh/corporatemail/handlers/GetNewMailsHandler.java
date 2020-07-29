/**
 *
 */
package com.sathesh.corporatemail.handlers;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;

import com.sathesh.corporatemail.BuildConfig;
import com.sathesh.corporatemail.R;
import com.sathesh.corporatemail.application.MailApplication;
import com.sathesh.corporatemail.application.NotificationProcessing;
import com.sathesh.corporatemail.fragment.datapasser.MailListFragmentDataPasser.Status;
import com.sathesh.corporatemail.constants.Constants;
import com.sathesh.corporatemail.fragment.MailListViewFragment;
import com.sathesh.corporatemail.ui.components.AuthFailedAlertDialog;
import com.sathesh.corporatemail.util.Utilities;

/** Handler method for GetNewMailsRunnable
 * Fragment: MailListViewFragment
 * @author sathesh
 *
 */
public class GetNewMailsHandler extends Handler implements Constants {

	/*** HANDLER ****/
	private MailListViewFragment parent;
	private Activity activity;

	public GetNewMailsHandler(MailListViewFragment parent, Activity activity){
		this.parent=parent;
		this.activity = activity;
	}

	@Override
	public void handleMessage(Message msg) {
		Status status = (MailListViewFragment.Status)msg.getData().getSerializable("state");
		switch(status){

			case UPDATING:
				parent.setNewMailsThreadState(Status.UPDATING);
				parent.updatingStatusUIChanges();
				break;

			case UPDATED:
				//successful update
				try {
					parent.setNewMailsThreadState(Status.UPDATED);
					parent.softRefreshList();
					parent.getSwipeRefreshLayout().setRefreshing(false);

					if(BuildConfig.DEBUG){
						Log.d(LOG_TAG, "GetNewMailsRunnable ->  GetNewMail Thread state is updated. GetMoreMails thread state is  " + parent.getMoreMailsThreadState());
					}

					//if the other thread (GetMoreMailsRunnable) is waiting for this to complete, then call it again
					if(parent.getMoreMailsThreadState() == Status.WAITING){
						//get the total no of records in cache
						int totalCachedRecords = parent.getMailHeadersCacheAdapter().getRecordsCount(parent.getMailType()
								, parent.getMailFolderId());
						//if the total cached records is less than the total no. of mails in the folder then run the "more loading" thread
						//which has been waiting for this thread
						if(totalCachedRecords < parent.getTotalMailsInFolder()){
							parent.getMoreMails();
							if(BuildConfig.DEBUG){
								Log.d(LOG_TAG, "GetNewMailsRunnable ->  Calling the GetMoreMails as it was in the Wait state");
							}
						}
						else{
							parent.setMoreMailsThreadState(Status.UPDATED);
						}
					}
				} catch (Exception e) {
					Utilities.generalCatchBlock(e, this);
				}
				break;

			case ERROR_AUTH_FAILED:
				try {
					parent.setNewMailsThreadState(Status.ERROR_AUTH_FAILED);
					// for auth failed show an alert box
					parent.getTextswitcher().setText(parent.getActivity().getText(R.string.folder_auth_error));
					parent.updateTextSwitcherIcons(View.GONE,View.GONE, View.VISIBLE, View.GONE, View.GONE);
					MailApplication.getInstance().setWrongPwd(true);
					NotificationProcessing.showLoginErrorNotification(parent.getActivity().getApplicationContext());
					if(parent.getMoreMailsThreadState() == Status.WAITING){
						parent.setMoreMailsThreadState(Status.ERROR_AUTH_FAILED);
						parent.softRefreshList();
					}
					if(parent.isAdded()){
						AuthFailedAlertDialog.showAlertdialog(activity, activity.getApplicationContext());
					}
					else{
						Log.e(LOG_TAG, "Authentication failed. Not able to add the alert dialog due to isAdded() is false");
					}
					// stop the Mail notification worker
					MailApplication.stopMNWorker(parent.getActivity().getApplicationContext());
				} catch (Exception e) {
					Utilities.generalCatchBlock(e, this);
				}
				break;

			case ERROR:
				try{
					parent.setNewMailsThreadState(Status.ERROR);
					if(parent.getMoreMailsThreadState() == Status.WAITING){
						parent.getMoreMails();
					}
					parent.updateTextSwitcherIcons(View.GONE,View.GONE, View.VISIBLE, View.GONE, View.GONE);
					parent.getSwipeRefreshLayout().setRefreshing(false);
					parent.getTextswitcher().setText(parent.getActivity().getText(R.string.folder_updater_error));
				} catch (Exception e) {
					Utilities.generalCatchBlock(e, this);
				}
				break;
		}
	}
}
