/**
 * 
 */
package com.wipromail.sathesh.handlers;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;

import com.wipromail.sathesh.BuildConfig;
import com.wipromail.sathesh.R;
import com.wipromail.sathesh.application.MailApplication;
import com.wipromail.sathesh.application.NotificationProcessing;
import com.wipromail.sathesh.constants.Constants;
import com.wipromail.sathesh.fragment.MailListViewFragment;
import com.wipromail.sathesh.fragment.MailListViewFragment.Status;
import com.wipromail.sathesh.ui.AuthFailedAlertDialog;
import com.wipromail.sathesh.util.Utilities;

/** Handler method for GetNewMailsRunnable
 * Fragment: MailListViewFragment
 * @author sathesh
 *
 */
public class GetNewMailsHandler extends Handler implements Constants {

	/*** HANDLER ****/
	private MailListViewFragment parent;

	public GetNewMailsHandler(MailListViewFragment parent){
		this.parent=parent;
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
				parent.getBar_progressbar().setProgress(0);

				if(BuildConfig.DEBUG){
					Log.d(TAG, "GetNewMailsRunnable ->  GetNewMail Thread state is updated. GetMoreMails thread state is  " + parent.getMoreMailsThreadState());
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
							Log.d(TAG, "GetNewMailsRunnable ->  Calling the GetMoreMails as it was in the Wait state");
						}
					}
					else{
						parent.setMoreMailsThreadState(Status.UPDATED);
					}
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				Utilities.generalCatchBlock(e, this);
			}
			break;

		case ERROR_AUTH_FAILED:
			parent.setNewMailsThreadState(Status.ERROR_AUTH_FAILED);
			// for auth failed show an alert box
			parent.getTextswitcher().setText(parent.getActivity().getText(R.string.folder_auth_error));
			NotificationProcessing.showLoginErrorNotification(parent.getActivity().getApplicationContext());
			if(parent.getMoreMailsThreadState() == Status.WAITING){
				parent.setMoreMailsThreadState(Status.ERROR_AUTH_FAILED);
				parent.softRefreshList();
			}
			if(parent.isAdded()){
				AuthFailedAlertDialog.showAlertdialog(parent.getActivity(), parent.getActivity().getApplicationContext());
			}
			else{
				Log.e(TAG, "Authentication failed. Not able to add the alert dialog due to isAdded() is false");
			}
			// stop the MNS service
			MailApplication.stopMNSService(parent.getActivity().getApplicationContext());
			break;

		case ERROR:
			parent.setNewMailsThreadState(Status.ERROR);
			if(parent.getMoreMailsThreadState() == Status.WAITING){
				parent.getMoreMails();
			}
			parent.updateTextSwitcherIcons(View.GONE,View.GONE, View.VISIBLE, View.GONE, View.GONE);
			parent.getSwipeRefreshLayout().setRefreshing(false);
			parent.getBar_progressbar().setProgress(0);
			parent.getTextswitcher().setText(parent.getActivity().getText(R.string.folder_updater_error));
			break;
		}
	}
}
