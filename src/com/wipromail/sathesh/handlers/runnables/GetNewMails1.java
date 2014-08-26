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
import com.wipromail.sathesh.customexceptions.NoInternetConnectionException;
import com.wipromail.sathesh.customexceptions.NoUserSignedInException;
import com.wipromail.sathesh.ews.EWSConnection;
import com.wipromail.sathesh.ews.NetworkCall;
import com.wipromail.sathesh.fragment.MailListViewFragment;
import com.wipromail.sathesh.fragment.MailListViewFragment.State;
import com.wipromail.sathesh.service.data.ExchangeService;
import com.wipromail.sathesh.service.data.HttpErrorException;
import com.wipromail.sathesh.service.data.WellKnownFolderName;

/**
 * @author sathesh
 *
 */
public class GetNewMails1 extends MailListViewFragment implements Runnable{

	MailListViewFragment parent;
	Handler handler1;

	public GetNewMails1(MailListViewFragment parent, Handler handler){
		this.parent=parent;
		this.handler1=handler;
	}

	ExchangeService service;

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		// TODO Auto-generated method stub
		if (activity != null) {

			try {

				//get the total no of records in cache and get all the same number of records.
				totalCachedRecords = getTotalNumberOfRecordsInCache();
				threadMsg(State.UPDATING);
				currentStatus=State.UPDATING;

				threadMsg(State.UPDATE_CACHE_DONE);

				service = EWSConnection.getServiceFromStoredCredentials(activity.getApplicationContext());

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
					cacheNewData(findResults.getItems(), true);
				}
				threadMsg(State.UPDATED);
				currentStatus=State.UPDATED;

			}
			catch (final NoUserSignedInException e) {
				threadMsg(State.ERROR);
				currentStatus=State.ERROR;
				e.printStackTrace();
			}
			catch (UnknownHostException e) {
				threadMsg(State.ERROR);
				currentStatus=State.ERROR;
				e.printStackTrace();

			}
			catch(NoInternetConnectionException nic){
				threadMsg(State.ERROR);
				currentStatus=State.ERROR;
				nic.printStackTrace();
			}
			catch(HttpErrorException e){
				if(e.getMessage().toLowerCase().contains("Unauthorized".toLowerCase())){
					//unauthorised
					threadMsg(State.ERROR_AUTH_FAILED);
					currentStatus=State.ERROR_AUTH_FAILED;
				}
				else
				{
					threadMsg(State.ERROR);
					currentStatus=State.ERROR;
				}
				e.printStackTrace();
			}
			catch (Exception e) {
				threadMsg(State.ERROR);
				currentStatus=State.ERROR;
				e.printStackTrace();
			}
		}
	}

	
	private void threadMsg(State state) {
		 
        if (state!=null) {
            Message msgObj = handler1.obtainMessage();
            Bundle b = new Bundle();
            b.putSerializable("state", state);
            msgObj.setData(b);
            handler1.sendMessage(msgObj);
        }
        
	//end of async task
}

	
}
