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
import com.wipromail.sathesh.fragment.MailListViewFragment.State;
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
		State state = (MailListViewFragment.State)msg.getData().getSerializable("state");
		switch(state){

		case UPDATING:
			parent.setNewMailsThreadState(State.UPDATING);
			parent.updatingStatusUIChanges();
			break;

		case UPDATED:
			//successful update
			try {
				parent.setNewMailsThreadState(State.UPDATED);
				parent.softRefreshList();
				parent.getSwipeRefreshLayout().setRefreshing(false);
				parent.getBar_progressbar().setProgress(0);

				if(BuildConfig.DEBUG){
					Log.d(TAG, "GetNewMailsRunnable ->  GetNewMail Thread state is updated. GetMoreMails thread state is  " + parent.getMoreMailsThreadState());
				}
				
				//if the other thread (GetMoreMailsRunnable) is waiting for this to complete, then call it again
				if(parent.getMoreMailsThreadState() == State.WAITING){
					parent.getMoreMails();
					if(BuildConfig.DEBUG){
						Log.d(TAG, "GetNewMailsRunnable ->  Calling the GetMoreMails as it was in the Wait state");
					}
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				Utilities.generalCatchBlock(e, this.getClass());
			}
			break;

		case ERROR_AUTH_FAILED:
			parent.setNewMailsThreadState(State.ERROR_AUTH_FAILED);
			// for auth failed show an alert box
			parent.getTextswitcher().setText(parent.getActivity().getText(R.string.folder_auth_error));
			NotificationProcessing.showLoginErrorNotification(parent.getActivity().getApplicationContext());
			if(parent.getMoreMailsThreadState() == State.WAITING){
				parent.setMoreMailsThreadState(State.ERROR_AUTH_FAILED);
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
			parent.setNewMailsThreadState(State.ERROR);
			if(parent.getMoreMailsThreadState() == State.WAITING){
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
