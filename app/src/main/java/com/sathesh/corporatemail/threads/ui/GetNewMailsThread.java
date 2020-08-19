/**
 * 
 */
package com.sathesh.corporatemail.threads.ui;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.sathesh.corporatemail.BuildConfig;
import com.sathesh.corporatemail.fragment.datapasser.MailListFragmentDataPasser.Status;
import com.sathesh.corporatemail.cache.adapter.CachedMailHeaderAdapter;
import com.sathesh.corporatemail.constants.Constants;
import com.sathesh.corporatemail.customexceptions.NoInternetConnectionException;
import com.sathesh.corporatemail.customexceptions.NoUserSignedInException;
import com.sathesh.corporatemail.ews.EWSConnection;
import com.sathesh.corporatemail.ews.NetworkCall;
import com.sathesh.corporatemail.fragment.MailListViewFragment;

import java.net.UnknownHostException;

import microsoft.exchange.webservices.data.core.ExchangeService;
import microsoft.exchange.webservices.data.core.enumeration.property.WellKnownFolderName;
import microsoft.exchange.webservices.data.core.exception.http.HttpErrorException;
import microsoft.exchange.webservices.data.core.exception.service.remote.ServiceRequestException;
import microsoft.exchange.webservices.data.core.service.item.Item;
import microsoft.exchange.webservices.data.search.FindItemsResults;

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
public class GetNewMailsThread extends Thread implements Runnable, Constants{

	private MailListViewFragment parent;
	private FindItemsResults<Item> findResults = null;
	private Handler handler;
	
	public GetNewMailsThread(MailListViewFragment parent, Handler handler){
		this.parent=parent;
		this.handler=handler;
	}

	ExchangeService service;

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		int totalCachedRecords ;

		if (parent.getActivity() != null) {
			try {
				threadMsg(Status.UPDATING);
				CachedMailHeaderAdapter headersCacheAdapter = parent.getMailHeadersCacheAdapter();
				
				//get the total no of records in cache and get all the same number of records.
				totalCachedRecords = parent.getMailHeadersCacheAdapter().getRecordsCount(parent.getMailType(), parent.getMailFolderId());
				
				service = EWSConnection.getServiceFromStoredCredentials(parent.getActivity().getApplicationContext());

				if(BuildConfig.DEBUG){
					Log.d(LOG_TAG, "MailListViewFragment -> Total records in cache"+totalCachedRecords);
				}

				//if the cache is present, then get the same number of rows from EWS as of the local no of rows
				int noOfMailsToFetch=Math.max(totalCachedRecords, MIN_NO_OF_MAILS);

				if(parent.getMailFolderId()!=null && !(parent.getMailFolderId().equals("")))
					//Ews call
					findResults = NetworkCall.getNItemsFromFolder(parent.getMailFolderId(), service, 0, noOfMailsToFetch);
				else
					//Ews call
					findResults = NetworkCall.getNItemsFromFolder(WellKnownFolderName.valueOf(parent.getMailFolderName()), service, 0, noOfMailsToFetch);

				if(findResults!=null){
					//delete the old cache and updates the new cache
					headersCacheAdapter.cacheNewData(findResults.getItems(),parent.getMailType(),
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
			catch(HttpErrorException | ServiceRequestException e){
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
			Log.e(LOG_TAG, "GetNewMails -> activity is null");
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
