/**
 * 
 */
package com.wipromail.sathesh.handlers;

import android.os.Handler;
import android.os.Message;

import com.wipromail.sathesh.application.MailApplication;
import com.wipromail.sathesh.application.interfaces.MailListFragmentDataPasser.Status;
import com.wipromail.sathesh.constants.Constants;
import com.wipromail.sathesh.fragment.MailListViewFragment;
import com.wipromail.sathesh.util.Utilities;

/** Handler method for GetMoreMailsRunnable
 * Fragment: MailListViewFragment
 * @author sathesh
 *
 */
public class GetMoreMailsHandler extends Handler implements Constants {

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
				Utilities.generalCatchBlock(e, this);
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
