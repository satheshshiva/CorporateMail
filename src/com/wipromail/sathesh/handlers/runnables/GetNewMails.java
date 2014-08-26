/**
 * 
 */
package com.wipromail.sathesh.handlers.runnables;

import java.net.UnknownHostException;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.wipromail.sathesh.BuildConfig;
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

/**
 * @author sathesh
 *
 */
public class GetNewMails implements Runnable, Constants{

	private MailListViewFragment parent;
	private Handler handler;
	private FindItemsResults<Item> findResults = null;
	private String mailFolderId="";
	private String mailFolderName="";

	public GetNewMails(MailListViewFragment parent, Handler handler){
		this.parent=parent;
		this.handler=handler;
		this.mailFolderId=parent.getMailFolderId();
		this.mailFolderName=parent.getMailFolderName();
	}

	ExchangeService service;

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		// TODO Auto-generated method stub
		int totalCachedRecords ;

		if (parent.activity != null) {
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


}
