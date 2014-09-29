/**
 * 
 */
package com.wipromail.sathesh.handlers.runnables;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.wipromail.sathesh.BuildConfig;
import com.wipromail.sathesh.cache.adapter.CachedMailHeaderAdapter;
import com.wipromail.sathesh.constants.Constants;
import com.wipromail.sathesh.customexceptions.NoInternetConnectionException;
import com.wipromail.sathesh.customexceptions.NoUserSignedInException;
import com.wipromail.sathesh.ews.EWSConnection;
import com.wipromail.sathesh.ews.NetworkCall;
import com.wipromail.sathesh.fragment.MailListViewFragment;
import com.wipromail.sathesh.fragment.MailListViewFragment.Status;
import com.wipromail.sathesh.service.data.ExchangeService;
import com.wipromail.sathesh.service.data.FindItemsResults;
import com.wipromail.sathesh.service.data.HttpErrorException;
import com.wipromail.sathesh.service.data.Item;
import com.wipromail.sathesh.service.data.WellKnownFolderName;

import java.net.UnknownHostException;

/** This Runnable will load the new set of mails from the network asynchronously for the particular mail type
 * Handler: GetMoreMailsHandler
 * Fragment: MailListViewFragment
 * 
 * NOTE: One general rule for handler - Don't pass the variables to the constructor and use inside the handler class. Because on Configuration change
 * all the View elements will get new elements and Handler thread will refer to the old elements. Always use parent.getElement() to get the latest element from 
 * the activity or fragment
 * @author sathesh
 *
 */
public class GetNewMailsRunnable implements Runnable, Constants{

	private MailListViewFragment parent;
	private FindItemsResults<Item> findResults = null;
	private Handler handler;
	
	public GetNewMailsRunnable(MailListViewFragment parent, Handler handler){
		this.parent=parent;
		this.handler=handler;
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
				threadMsg(Status.UPDATING);
				CachedMailHeaderAdapter headersCacheAdapter = parent.getMailHeadersCacheAdapter();
				
				//get the total no of records in cache and get all the same number of records.
				totalCachedRecords = parent.getMailHeadersCacheAdapter().getRecordsCount(parent.getMailType(), parent.getMailFolderId());
				
				service = EWSConnection.getServiceFromStoredCredentials(parent.getActivity().getApplicationContext());

				if(BuildConfig.DEBUG){
					Log.d(TAG, "MailListViewFragment -> Total records in cache"+totalCachedRecords);
				}

				//if the cache is present, then get the same number of rows from EWS as of the local no of rows
				int noOfMailsToFetch=(totalCachedRecords>MIN_NO_OF_MAILS?totalCachedRecords:MIN_NO_OF_MAILS);

				if(parent.getMailFolderId()!=null && !(parent.getMailFolderId().equals("")))
					//Ews call
					findResults = NetworkCall.getNItemsFromFolder(parent.getMailFolderId(), service, 0, noOfMailsToFetch);
				else
					//Ews call
					findResults = NetworkCall.getNItemsFromFolder(WellKnownFolderName.valueOf(parent.getMailFolderName()), service, 0, noOfMailsToFetch);

				if(findResults!=null){
					//delete the old cache and updates the new cache
					headersCacheAdapter.cacheNewData(parent.getActivity(), 
							findResults.getItems(),parent.getMailType(), 
							parent.getMailFolderName(), parent.getMailFolderId(), true);
					
					parent.setTotalMailsInFolder(findResults.getTotalCount());	//set the total no of mails in this folder
				}
				threadMsg(Status.UPDATED);

			}
			catch (final NoUserSignedInException e) {
				threadMsg(Status.ERROR);
				e.printStackTrace();
			}
			catch (UnknownHostException e) {
				threadMsg(Status.ERROR);
				e.printStackTrace();

			}
			catch(NoInternetConnectionException nic){
				threadMsg(Status.ERROR);
				nic.printStackTrace();
			}
			catch(HttpErrorException e){
				if(e.getMessage().toLowerCase().contains("Unauthorized".toLowerCase())){
					//unauthorised
					threadMsg(Status.ERROR_AUTH_FAILED);
				}
				else
				{
					threadMsg(Status.ERROR);
				}
				e.printStackTrace();
			}
			catch (Exception e) {
				threadMsg(Status.ERROR);
				e.printStackTrace();
			}
		}
		else{
			Log.e(TAG, "GetNewMails -> activity is null");
		}
	}	//end run()


	/** Private method for bundling the message and sending it to the handler
	 * @param status
	 */
	private void threadMsg(Status status) {

		if (status!=null) {
			Message msgObj = handler.obtainMessage();
			Bundle b = new Bundle();
			b.putSerializable("state", status);
			msgObj.setData(b);
			handler.sendMessage(msgObj);
		}

	}
}
