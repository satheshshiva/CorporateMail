package com.sathesh.corporatemail.sync;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.sathesh.corporatemail.application.SharedPreferencesAdapter;
import com.sathesh.corporatemail.constants.Constants;
import com.sathesh.corporatemail.customexceptions.NoInternetConnectionException;
import com.sathesh.corporatemail.ews.NetworkCall;
import com.sathesh.corporatemail.service.data.ChangeCollection;
import com.sathesh.corporatemail.service.data.ExchangeService;
import com.sathesh.corporatemail.service.data.FolderId;
import com.sathesh.corporatemail.service.data.GetEventsResults;
import com.sathesh.corporatemail.service.data.ItemChange;
import com.sathesh.corporatemail.service.data.PullSubscription;
import com.sathesh.corporatemail.service.data.WellKnownFolderName;

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
