package com.wipromail.sathesh.sync;

import java.util.Date;

import android.content.Context;

import com.wipromail.sathesh.constants.Constants;
import com.wipromail.sathesh.ews.NetworkCall;
import com.wipromail.sathesh.service.data.ExchangeService;
import com.wipromail.sathesh.service.data.FindItemsResults;
import com.wipromail.sathesh.service.data.Item;
import com.wipromail.sathesh.service.data.ItemView;

public class SyncUsingSearchFilter implements Constants{

	
	private String syncState="";
	private ItemView latestMailsView ;
	private FindItemsResults<Item> findItemResults = null;
	
	
	public synchronized FindItemsResults<Item> getLatestMails(Context context, ExchangeService service, Date dateTime) throws Exception
	
	{

		latestMailsView = new ItemView(INBOX_LATESTMAIL_OFFSET);
		findItemResults = NetworkCall.getLatestMail(service, dateTime, latestMailsView);
		return findItemResults;
	}
	
	

	
}
