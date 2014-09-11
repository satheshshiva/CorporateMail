package com.wipromail.sathesh.threads;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.wipromail.sathesh.constants.Constants;
import com.wipromail.sathesh.ews.EWSConnection;
import com.wipromail.sathesh.service.data.ExchangeService;
import com.wipromail.sathesh.service.data.FindItemsResults;
import com.wipromail.sathesh.service.data.Item;
import com.wipromail.sathesh.service.data.ItemView;
import com.wipromail.sathesh.service.data.WellKnownFolderName;

public class Inbox implements Runnable, Constants {

	private Handler handlerInbox;

	private ExchangeService service;
	public FindItemsResults<Item> findResults;
	Message msgInbox;
	
	public Inbox(FindItemsResults<Item> findResults, Handler handlerInbox) {
		// TODO Auto-generated constructor stub
		this.handlerInbox = handlerInbox;

	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		try{
			Log.d(TAG, "Thread has started");
			
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
				Log.e(TAG, "Error occured");
			}
		}
	}

	
}
