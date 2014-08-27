/**
 * 
 */
package com.wipromail.sathesh.handlers;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;

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
			parent.setCurrentStatus(State.UPDATING);
			parent.updatingStatusUIChanges();
			break;

		case UPDATED:
			//successful update
			try {
				parent.setCurrentStatus(State.UPDATED);
				parent.softRefreshList();
				parent.getSwipeRefreshLayout().setRefreshing(false);
				parent.getBar_progressbar().setProgress(0);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				Utilities.generalCatchBlock(e, this.getClass());
			}
			break;

		case ERROR_AUTH_FAILED:
			parent.setCurrentStatus(State.ERROR_AUTH_FAILED);
			// for auth failed show an alert box
			parent.getTextswitcher().setText(parent.getActivity().getText(R.string.folder_auth_error));
			NotificationProcessing.showLoginErrorNotification(parent.getActivity().getApplicationContext());
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
			parent.setCurrentStatus(State.ERROR);
			parent.updateTextSwitcherIcons(View.GONE,View.GONE, View.VISIBLE, View.GONE, View.GONE);
			parent.getSwipeRefreshLayout().setRefreshing(false);
			parent.getBar_progressbar().setProgress(0);
			parent.getTextswitcher().setText(parent.getActivity().getText(R.string.folder_updater_error));
			break;
		}
	}
}
