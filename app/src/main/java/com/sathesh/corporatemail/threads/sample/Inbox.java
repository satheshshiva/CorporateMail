package com.sathesh.corporatemail.threads.sample;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.sathesh.corporatemail.constants.Constants;

import microsoft.exchange.webservices.data.core.ExchangeService;
import microsoft.exchange.webservices.data.core.enumeration.property.WellKnownFolderName;
import microsoft.exchange.webservices.data.core.service.item.Item;
import microsoft.exchange.webservices.data.search.FindItemsResults;
import microsoft.exchange.webservices.data.search.ItemView;

public class Inbox implements Runnable, Constants {

	private Handler handlerInbox;

	private ExchangeService service;
	public FindItemsResults<Item> findResults;
	Message msgInbox;
	
	public Inbox(FindItemsResults<Item> findResults, Handler handlerInbox) {
		this.handlerInbox = handlerInbox;

	}

	@Override
	public void run() {
		try{
			Log.d(LOG_TAG, "Thread has started");
			
			msgInbox = Message.obtain();
			msgInbox.arg1 = 2;
			handlerInbox.sendMessage(msgInbox);
			
			ItemView viewEWS = new ItemView(5);
			String username="";
			String password="";
		//	service = EWSConnection.getService(username ,password);
			
			msgInbox = Message.obtain();
			msgInbox.arg1 = 5;
			handlerInbox.sendMessage(msgInbox);
			
			findResults = service.findItems(WellKnownFolderName.Inbox, viewEWS);
			
			msgInbox = Message.obtain();
			msgInbox.arg1 = 7;
			handlerInbox.sendMessage(msgInbox);
			
			msgInbox.obj = findResults;
			handlerInbox.sendMessage(msgInbox);
    	
    	msgInbox = Message.obtain();

		}
		catch(Exception e){
			if(msgInbox!=null && handlerInbox != null){
			msgInbox = Message.obtain();
			msgInbox.arg1 = 9999;
			msgInbox.obj = e;
			handlerInbox.sendMessage(msgInbox);
			}
			else
			{
				e.printStackTrace();
				Log.e(LOG_TAG, "Error occured");
			}
		}
	}

	
}
