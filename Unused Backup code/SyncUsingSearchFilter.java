package com.sathesh.corporatemail.sync;

import java.util.Date;

import android.content.Context;

import com.sathesh.corporatemail.constants.Constants;
import com.sathesh.corporatemail.ews.NetworkCall;
import com.sathesh.corporatemail.service.data.ExchangeService;
import com.sathesh.corporatemail.service.data.FindItemsResults;
import com.sathesh.corporatemail.service.data.Item;
import com.sathesh.corporatemail.service.data.ItemView;

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
