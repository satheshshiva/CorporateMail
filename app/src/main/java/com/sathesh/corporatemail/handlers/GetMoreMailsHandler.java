/**
 * 
 */
package com.sathesh.corporatemail.handlers;

import android.os.Handler;
import android.os.Message;

import com.sathesh.corporatemail.application.MailApplication;
import com.sathesh.corporatemail.fragment.datapasser.MailListFragmentDataPasser.Status;
import com.sathesh.corporatemail.constants.Constants;
import com.sathesh.corporatemail.fragment.MailListViewFragment;
import com.sathesh.corporatemail.util.Utilities;

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
			MailApplication.stopMNWorker(parent.getActivity().getApplicationContext());
			break;

		case ERROR:
			parent.setMoreMailsThreadState(Status.ERROR);
			parent.softRefreshList(); //so that the loading synmbol will be gone
			break;
		}
	}
}
