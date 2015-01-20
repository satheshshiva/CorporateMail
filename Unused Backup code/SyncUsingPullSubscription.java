package com.wipromail.sathesh.sync;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.wipromail.sathesh.application.SharedPreferencesAdapter;
import com.wipromail.sathesh.constants.Constants;
import com.wipromail.sathesh.customexceptions.NoInternetConnectionException;
import com.wipromail.sathesh.ews.NetworkCall;
import com.wipromail.sathesh.service.data.ChangeCollection;
import com.wipromail.sathesh.service.data.ExchangeService;
import com.wipromail.sathesh.service.data.FolderId;
import com.wipromail.sathesh.service.data.GetEventsResults;
import com.wipromail.sathesh.service.data.ItemChange;
import com.wipromail.sathesh.service.data.PullSubscription;
import com.wipromail.sathesh.service.data.WellKnownFolderName;

public class SyncUsingPullSubscription implements Constants{

	private String watermark="";

	private GetEventsResults events;

	private PullSubscription subscription=null;
	public String getWatermarkFromStorage(Context context) throws Exception  {
		watermark = SharedPreferencesAdapter.getSyncPullMethodWatermark(context);
		return watermark;
	}

	public void storeWatermark(Context context, String watermark) throws Exception {
		SharedPreferencesAdapter.storeSyncPullMethodWatermark(context, watermark);
		this.watermark = watermark;
	}


	public synchronized GetEventsResults doSync(Context context, ExchangeService service) throws Exception

	{

		List  folder = new ArrayList();
		folder.add(new FolderId(WellKnownFolderName.Inbox));   

		//EWS call
		if (null == subscription){
			subscription = doSubscription(context, service, folder, getWatermarkFromStorage(context));
		}
		//EWSCall
		events = pollServer(context, subscription);
		
		//IMPLEMENTATIION WRONG... USE ONLY SUBSCRIBE ID
		//store next watermark
		storeWatermark(context, events.getNewWatermark1());

		return events;
	}

	public GetEventsResults pollServer(Context context, PullSubscription subscription) throws NoInternetConnectionException, Exception {
		// TODO Auto-generated method stub
		return NetworkCall.pullSubscriptionPoll(context, subscription);
	}

	public PullSubscription doSubscription(Context context, ExchangeService service, List  folder, String watermark) throws NoInternetConnectionException, Exception{
		return  NetworkCall.subscribePullInboxSync(context, service, folder, getWatermarkFromStorage(context) );

	}


}
