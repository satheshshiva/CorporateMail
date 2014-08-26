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
	private String mailFolderId="";
	private String mailFolderName="";
	private SwipeRefreshLayout swipeRefreshLayout;
	private ProgressBar bar_progressbar;
	private Activity activity;
	private TextSwitcher textswitcher;

	public GetNewMails(MailListViewFragment parent){
		this.parent=parent;
		this.activity=parent.getActivity();
		this.mailFolderId=parent.getMailFolderId();
		this.mailFolderName=parent.getMailFolderName();
		this.bar_progressbar=parent.getBar_progressbar();
		this.swipeRefreshLayout=parent.getSwipeRefreshLayout();
		this.textswitcher=parent.getTextswitcher();
	}

	ExchangeService service;

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		// TODO Auto-generated method stub
		int totalCachedRecords ;

		if (activity != null) {
			try {
				totalCachedRecords = parent.getTotalNumberOfRecordsInCache();
				//get the total no of records in cache and get all the same number of records.
				threadMsg(State.UPDATING);
				parent.setCurrentStatus(State.UPDATING);
				
				threadMsg(State.UPDATE_CACHE_DONE);

				service = EWSConnection.getServiceFromStoredCredentials(parent.getActivity().getApplicationContext());

				if(BuildConfig.DEBUG){
					Log.d(TAG, "MailListViewFragment -> Total records in cache"+totalCachedRecords);
				}

				//if the cache is present, then get the same number of rows from EWS as of the local no of rows
				int noOfMailsToFetch=(totalCachedRecords>MIN_NO_OF_MAILS?totalCachedRecords:MIN_NO_OF_MAILS);

				if(mailFolderId!=null && !(mailFolderId.equals("")))
					//Ews call
					findResults = NetworkCall.getFirstNItemsFromFolder(mailFolderId, service, noOfMailsToFetch);
				else
					//Ews call
					findResults = NetworkCall.getFirstNItemsFromFolder(WellKnownFolderName.valueOf(mailFolderName), service, noOfMailsToFetch);

				//empties the cache for this 
				if(findResults!=null){
					parent.cacheNewData(findResults.getItems(), true);
				}
				threadMsg(State.UPDATED);
				parent.setCurrentStatus(State.UPDATED);

			}
			catch (final NoUserSignedInException e) {
				threadMsg(State.ERROR);
				parent.setCurrentStatus(State.ERROR);
				e.printStackTrace();
			}
			catch (UnknownHostException e) {
				threadMsg(State.ERROR);
				parent.setCurrentStatus(State.ERROR);
				e.printStackTrace();

			}
			catch(NoInternetConnectionException nic){
				threadMsg(State.ERROR);
				parent.setCurrentStatus(State.ERROR);
				nic.printStackTrace();
			}
			catch(HttpErrorException e){
				if(e.getMessage().toLowerCase().contains("Unauthorized".toLowerCase())){
					//unauthorised
					threadMsg(State.ERROR_AUTH_FAILED);
					parent.setCurrentStatus(State.ERROR_AUTH_FAILED);
				}
				else
				{
					threadMsg(State.ERROR);
					parent.setCurrentStatus(State.ERROR);
				}
				e.printStackTrace();
			}
			catch (Exception e) {
				threadMsg(State.ERROR);
				parent.setCurrentStatus(State.ERROR);
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
			System.out.println("Handler Message Obtainied " + msg);
			State state = (MailListViewFragment.State)msg.getData().getSerializable("state");
			switch(state){

			case UPDATING:
				parent.updatingStatusUIChanges();
				break;

			case UPDATE_CACHE_DONE:
				bar_progressbar.setProgress(65);
				break;

			case UPDATE_LIST:
				bar_progressbar.setProgress(90);
				break;

			case UPDATED:
				//successful update
				try {
					parent.softRefreshList();
					swipeRefreshLayout.setRefreshing(false);
					parent.updateTextSwitcherWithMailCount();
					bar_progressbar.setProgress(0);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					Utilities.generalCatchBlock(e, this.getClass());
				}
				break;

			case ERROR_AUTH_FAILED:
				// for auth failed show an alert box
				textswitcher.setText(activity.getText(R.string.folder_auth_error));
				NotificationProcessing.showLoginErrorNotification(activity.getApplicationContext());
				if(parent.isAdded()){
					AuthFailedAlertDialog.showAlertdialog(activity, activity.getApplicationContext());
				}
				else{
					Log.e(TAG, "Authentication failed. Not able to add the alert dialog due to isAdded() is false");
				}
				// stop the MNS service
				MailApplication.stopMNSService(activity.getApplicationContext());
				break;

			case ERROR:
				parent.updateTextSwitcherIcons(View.GONE,View.GONE, View.VISIBLE, View.GONE, View.GONE);
				swipeRefreshLayout.setRefreshing(false);
				bar_progressbar.setProgress(0);
				textswitcher.setText(activity.getText(R.string.folder_updater_error));
				break;
			}
		 }
	};
}
