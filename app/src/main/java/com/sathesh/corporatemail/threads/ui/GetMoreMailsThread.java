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
import com.sathesh.corporatemail.service.data.ExchangeService;
import com.sathesh.corporatemail.service.data.FindItemsResults;
import com.sathesh.corporatemail.service.data.HttpErrorException;
import com.sathesh.corporatemail.service.data.Item;
import com.sathesh.corporatemail.service.data.WellKnownFolderName;

import java.net.UnknownHostException;

/** This Runnable will load more emails from the network asynchronously which will be triggered when the user is scolling
 * 
 * NOTE: One general rule for handler - Don't pass the variables to the constructor and use inside the handler class. Because on Configuration change
 * all the View elements will get new elements and Handler thread will refer to the old elements. Always use parent.getElement() to get the latest element from 
 * the activity or fragment
 * @author sathesh
 *
 */
public class GetMoreMailsThread extends Thread implements Runnable, Constants{

	private MailListViewFragment parent;
	private FindItemsResults<Item> findResults = null;
	private Handler handler;
	
	public GetMoreMailsThread(MailListViewFragment parent, Handler handler){
		this.parent=parent;
		this.handler=handler;
	}

	ExchangeService service;

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		int cacheRecordsCount ;

		if (parent.getActivity() != null) {
			try {
				sendHandlerMsg(Status.UPDATING);
				CachedMailHeaderAdapter headersCacheAdapter = parent.getMailHeadersCacheAdapter();
				
				//get the total no of records in cache and get all the same number of records.
				cacheRecordsCount = headersCacheAdapter.getRecordsCount(parent.getMailType(), parent.getMailFolderId());
				
				service = EWSConnection.getServiceFromStoredCredentials(parent.getActivity().getApplicationContext());

				if(BuildConfig.DEBUG){
					Log.d(TAG, "GetMoreMailsRunnable -> Total records in cache"+cacheRecordsCount);
				}

				if(parent.getMailFolderId()!=null && !(parent.getMailFolderId().equals("")))
					//Ews call
					findResults = NetworkCall.getNItemsFromFolder(parent.getMailFolderId(), service, cacheRecordsCount, MORE_NO_OF_MAILS);
				else
					//Ews call
					findResults = NetworkCall.getNItemsFromFolder(WellKnownFolderName.valueOf(parent.getMailFolderName()), service, cacheRecordsCount, MORE_NO_OF_MAILS);

				if(findResults!=null){
					//delete the old cache and updates the new cache
					headersCacheAdapter.cacheNewData(findResults.getItems(),parent.getMailType(),
							parent.getMailFolderName(), parent.getMailFolderId(), false);	//update the new records. dont delele the old records
					
					parent.setTotalMailsInFolder(findResults.getTotalCount());	//set the total no of mails in this folder
				}
				sendHandlerMsg(Status.UPDATED);
			}
			catch (final NoUserSignedInException e) {
				sendHandlerMsg(Status.ERROR);
				e.printStackTrace();
			}
			catch (UnknownHostException e) {
				sendHandlerMsg(Status.ERROR);
				e.printStackTrace();
			}
			catch(NoInternetConnectionException nic){
				sendHandlerMsg(Status.ERROR);
				nic.printStackTrace();
			}
			catch(HttpErrorException e){
				if(e.getMessage().toLowerCase().contains("Unauthorized".toLowerCase())){
					//unauthorised
					sendHandlerMsg(Status.ERROR_AUTH_FAILED);
				}
				else
				{
					sendHandlerMsg(Status.ERROR);
				}
				e.printStackTrace();
			}
			catch (Exception e) {
				sendHandlerMsg(Status.ERROR);
				e.printStackTrace();
			}
		}
		else{
			Log.e(TAG, "GetMoreMailsRunnable -> activity is null");
		}
	}	//end run()


	/** Private method for bundling the message and sending it to the handler
	 * @param status
	 */
	private void sendHandlerMsg(Status status) {

		if (status!=null) {
			Message msgObj = handler.obtainMessage();
			Bundle b = new Bundle();
			b.putSerializable("state", status);
			msgObj.setData(b);
			handler.sendMessage(msgObj);
		}

	}
}
