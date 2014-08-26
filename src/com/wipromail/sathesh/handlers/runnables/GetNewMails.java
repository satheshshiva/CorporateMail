/**
 * 
 */
package com.wipromail.sathesh.handlers.runnables;

import java.net.UnknownHostException;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextSwitcher;

import com.wipromail.sathesh.BuildConfig;
import com.wipromail.sathesh.R;
import com.wipromail.sathesh.application.MailApplication;
import com.wipromail.sathesh.application.NotificationProcessing;
import com.wipromail.sathesh.constants.Constants;
import com.wipromail.sathesh.customexceptions.NoInternetConnectionException;
import com.wipromail.sathesh.customexceptions.NoUserSignedInException;
import com.wipromail.sathesh.ews.EWSConnection;
import com.wipromail.sathesh.ews.NetworkCall;
import com.wipromail.sathesh.fragment.MailListViewFragment;
import com.wipromail.sathesh.fragment.MailListViewFragment.State;
import com.wipromail.sathesh.service.data.ExchangeService;
import com.wipromail.sathesh.service.data.FindItemsResults;
import com.wipromail.sathesh.service.data.HttpErrorException;
import com.wipromail.sathesh.service.data.Item;
import com.wipromail.sathesh.service.data.WellKnownFolderName;
import com.wipromail.sathesh.ui.AuthFailedAlertDialog;
import com.wipromail.sathesh.util.Utilities;

/**
 * @author sathesh
 *
 */
public class GetNewMails implements Runnable, Constants{

	private MailListViewFragment parent;
	private FindItemsResults<Item> findResults = null;

	public GetNewMails(MailListViewFragment parent){
		this.parent=parent;
	}

	ExchangeService service;

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		// TODO Auto-generated method stub
		int totalCachedRecords ;

		if (parent.getActivity() != null) {
			try {
				threadMsg(State.UPDATING);
				
				//get the total no of records in cache and get all the same number of records.
				totalCachedRecords = parent.getTotalNumberOfRecordsInCache();
				
				service = EWSConnection.getServiceFromStoredCredentials(parent.getActivity().getApplicationContext());

				if(BuildConfig.DEBUG){
					Log.d(TAG, "MailListViewFragment -> Total records in cache"+totalCachedRecords);
				}

				//if the cache is present, then get the same number of rows from EWS as of the local no of rows
				int noOfMailsToFetch=(totalCachedRecords>MIN_NO_OF_MAILS?totalCachedRecords:MIN_NO_OF_MAILS);

				if(parent.getMailFolderId()!=null && !(parent.getMailFolderId().equals("")))
					//Ews call
					findResults = NetworkCall.getFirstNItemsFromFolder(parent.getMailFolderId(), service, noOfMailsToFetch);
				else
					//Ews call
					findResults = NetworkCall.getFirstNItemsFromFolder(WellKnownFolderName.valueOf(parent.getMailFolderName()), service, noOfMailsToFetch);

				//empties the cache for this 
				if(findResults!=null){
					parent.cacheNewData(findResults.getItems(), true);
				}
				threadMsg(State.UPDATED);

			}
			catch (final NoUserSignedInException e) {
				threadMsg(State.ERROR);
				e.printStackTrace();
			}
			catch (UnknownHostException e) {
				threadMsg(State.ERROR);
				e.printStackTrace();

			}
			catch(NoInternetConnectionException nic){
				threadMsg(State.ERROR);
				nic.printStackTrace();
			}
			catch(HttpErrorException e){
				if(e.getMessage().toLowerCase().contains("Unauthorized".toLowerCase())){
					//unauthorised
					threadMsg(State.ERROR_AUTH_FAILED);
				}
				else
				{
					threadMsg(State.ERROR);
				}
				e.printStackTrace();
			}
			catch (Exception e) {
				threadMsg(State.ERROR);
				e.printStackTrace();
			}
		}
		else{
			Log.e(TAG, "GetNewMails -> activity is null");
		}
	}	//end run()


	/** Private method for bundling the message and sending it to the handler
	 * @param state
	 */
	private void threadMsg(State state) {

		if (state!=null) {
			Message msgObj = handler.obtainMessage();
			Bundle b = new Bundle();
			b.putSerializable("state", state);
			msgObj.setData(b);
			handler.sendMessage(msgObj);
		}

	}

	/*** HANDLER ****/
	
	private Handler handler = new Handler(){
		@Override
		 public void handleMessage(Message msg) {
			System.out.println("Message Obtained " + msg.getData().getSerializable("state"));
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
	};
}
