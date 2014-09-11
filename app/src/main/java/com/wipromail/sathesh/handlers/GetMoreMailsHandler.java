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
import com.wipromail.sathesh.fragment.MailListViewFragment.Status;
import com.wipromail.sathesh.ui.AuthFailedAlertDialog;
import com.wipromail.sathesh.util.Utilities;

/** Handler method for GetMoreMailsRunnable
 * Fragment: MailListViewFragment
 * @author sathesh
 *
 */
public class GetMoreMailsHandler extends Handler implements Constants {

	/*** HANDLER ****/
	private MailListViewFragment parent;

	public GetMoreMailsHandler(MailListViewFragment parent){
		this.parent=parent;
	}

	@Override
	public void handleMessage(Message msg) {
		Status status = (MailListViewFragment.Status)msg.getData().getSerializable("state");
		switch(status){

		case UPDATING:
			parent.setMoreMailsThreadState(Status.UPDATING);
			break;

		case UPDATED:
			//successful update
			try {
				parent.setMoreMailsThreadState(Status.UPDATED);
				parent.softRefreshList();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				Utilities.generalCatchBlock(e, this.getClass());
			}
			break;

		case ERROR_AUTH_FAILED:
			parent.setMoreMailsThreadState(Status.ERROR_AUTH_FAILED);
			// stop the MNS service
			MailApplication.stopMNSService(parent.getActivity().getApplicationContext());
			break;

		case ERROR:
			parent.setMoreMailsThreadState(Status.ERROR);
			parent.softRefreshList(); //so that the loading synmbol will be gone
			break;
		}
	}
}
