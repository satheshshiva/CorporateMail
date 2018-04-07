package com.sathesh.corporatemail.threads.service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.util.Log;

import com.sathesh.corporatemail.constants.Constants;
import com.sathesh.corporatemail.customexceptions.NoUserSignedInException;
import com.sathesh.corporatemail.ews.EWSConnection;
import com.sathesh.corporatemail.service.data.EmailMessage;
import com.sathesh.corporatemail.service.data.EventType;
import com.sathesh.corporatemail.service.data.ExchangeService;
import com.sathesh.corporatemail.service.data.FolderId;
import com.sathesh.corporatemail.service.data.GetItemResponse;
import com.sathesh.corporatemail.service.data.ItemEvent;
import com.sathesh.corporatemail.service.data.ItemId;
import com.sathesh.corporatemail.service.data.ItemSchema;
import com.sathesh.corporatemail.service.data.MessageBody;
import com.sathesh.corporatemail.service.data.NotificationEvent;
import com.sathesh.corporatemail.service.data.NotificationEventArgs;
import com.sathesh.corporatemail.service.data.PropertySet;
import com.sathesh.corporatemail.service.data.ServiceLocalException;
import com.sathesh.corporatemail.service.data.ServiceResponseCollection;
import com.sathesh.corporatemail.service.data.StreamingSubscription;
import com.sathesh.corporatemail.service.data.StreamingSubscriptionConnection;
import com.sathesh.corporatemail.service.data.SubscriptionErrorEventArgs;
import com.sathesh.corporatemail.service.data.WellKnownFolderName;
import com.sathesh.corporatemail.service.data.StreamingSubscriptionConnection.INotificationEventDelegate;
import com.sathesh.corporatemail.service.data.StreamingSubscriptionConnection.ISubscriptionErrorDelegate;

public class StreamMailNotificationServiceThread
extends Thread implements Constants,INotificationEventDelegate, ISubscriptionErrorDelegate
{
	StreamingSubscriptionConnection conn;
	StreamingSubscription subscription ;
	List  folder = new ArrayList();        
	ExchangeService service = new ExchangeService();

	private Context context;
	private int connectionTimout = MAIL_NOTIFICATION_SERVICE_CONN_TIMEOUT;		//The maximum time, in minutes, the connection will remain open. Lifetime must be between 1 and 30.

	public boolean shutdownCurrentThread=false;
	

	public StreamMailNotificationServiceThread(Context context)
	{

		this.context=context;
	}



	public void run()
	{

		try{
			service = EWSConnection.getServiceFromStoredCredentials(context);

			Log.i("MailNotificationService", service.getUrl().toString());
			folder.add(new FolderId(WellKnownFolderName.Inbox));  


			WellKnownFolderName sd = WellKnownFolderName.Inbox;
			FolderId folderId = new FolderId(sd);

			List folder = new ArrayList<FolderId>();
			folder.add(folderId);

			subscription = service.subscribeToStreamingNotifications(
					folder, EventType.NewMail);

			conn = new StreamingSubscriptionConnection(service, connectionTimout);		
			conn.addSubscription(subscription); 


			conn.addOnNotificationEvent(this);
			conn.addOnDisconnect(this);
			System.out.println("Opening Connection " + Calendar.getInstance().getTime());
			conn.open();

			EmailMessage msg= new EmailMessage(service);
			msg.setSubject("Testing Streaming Notification on 16 Aug 2010"); 
			msg.setBody(MessageBody.getMessageBodyFromText("Streaming Notification "));
			msg.getToRecipients().add("sathesh.shiva@wipro.com");
			msg.send();		

			Thread.sleep(20000);
			recursiveOpen(conn);

			conn.close();
			Log.d("MailNotificationService", "StreamMailNotificationServiceThread -> Exiting");

		}
		catch(NoUserSignedInException ne){
			try {
				Thread.sleep(120*1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		}
		catch(Exception e){
			e.printStackTrace();
		}

	}




	private void recursiveOpen(StreamingSubscriptionConnection conn2) {
		// TODO Auto-generated method stub
		try {
			System.out.println("reopening connection");
			if(conn2.getIsOpen()){
				conn2.close();
			}
			conn2.open();
			Thread.sleep(30*1000);
		} catch (ServiceLocalException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		if(!shutdownCurrentThread)
		recursiveOpen(conn2);
	}



	@Override
	public void notificationEventDelegate(Object sender,
			NotificationEventArgs args) {
		try {

			connection_OnNotificationEvent(sender,args);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}	
	@Override
	public void subscriptionErrorDelegate(Object sender,SubscriptionErrorEventArgs args) {
		try {
			connection_OnDisconnect(sender,args);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	void connection_OnDisconnect(Object sender, SubscriptionErrorEventArgs args)
	{
		try {
			/*conn.addSubscription(subscription); 


			conn.addOnNotificationEvent(this);
			conn.addOnDisconnect(this);*/
			System.out.println("disconnecting........" + Calendar.getInstance().getTime());
			System.out.println("Connection state 1" + conn.getIsOpen() + new Date());
			// conn.open();
			System.out.println("Connection state 2" + conn.getIsOpen() + new Date());

		}

		catch (Exception e) {
			e.printStackTrace();
		}

	}


	void connection_OnNotificationEvent(Object sender, NotificationEventArgs args) throws Exception
	{
		System.out.println("hi notification event==========");
		// Let's first retrieve the Ids of all the new mails
		List<ItemId> newMailsIds = new ArrayList<ItemId>();

		Iterator<NotificationEvent> it = args.getEvents().iterator();
		while (it.hasNext()) {
			ItemEvent itemEvent = (ItemEvent)it.next();
			if (itemEvent != null)
			{
				newMailsIds.add(itemEvent.getItemId());
			}

		}
		if (newMailsIds.size() > 0)
		{
			// Now retrieve the Subject property of all the new mails in one call to EWS
			ServiceResponseCollection<GetItemResponse> responses = service.bindToItems(
					newMailsIds,
					new PropertySet(ItemSchema.Subject));
			System.out.println("count=======" + responses.getCount());


			//this.listBox1.Items.Add(string.Format("{0} new mail(s)", newMailsIds.Count));

			for(GetItemResponse response : responses)
			{
				System.out.println("count=======" + responses.getClass().getName());
				System.out.println("subject=======" + response.getItem().getSubject() + "\n");
				// Console.WriteLine("subject====" + response.Item.Subject);
			}
		}

	}


}

