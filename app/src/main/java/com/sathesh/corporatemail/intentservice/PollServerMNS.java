package com.sathesh.corporatemail.intentservice;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.util.Log;

import com.commonsware.cwac.wakeful.WakefulIntentService;
import com.sathesh.corporatemail.BuildConfig;
import com.sathesh.corporatemail.R;
import com.sathesh.corporatemail.application.MailApplication;
import com.sathesh.corporatemail.application.NotificationProcessing;
import com.sathesh.corporatemail.cache.adapter.CachedMailBodyAdapter;
import com.sathesh.corporatemail.cache.adapter.CachedMailHeaderAdapter;
import com.sathesh.corporatemail.constants.Constants;
import com.sathesh.corporatemail.customexceptions.NoInternetConnectionException;
import com.sathesh.corporatemail.customexceptions.NoUserSignedInException;
import com.sathesh.corporatemail.ews.EWSConnection;
import com.sathesh.corporatemail.ews.MailFunctions;
import com.sathesh.corporatemail.ews.MailFunctionsImpl;
import com.sathesh.corporatemail.ews.NetworkCall;
import com.sathesh.corporatemail.service.MailNotificationService;
import com.sathesh.corporatemail.threads.service.PullMailNotificationServiceThread;
import com.sathesh.corporatemail.util.Utilities;

import java.net.UnknownHostException;

import microsoft.exchange.webservices.data.core.ExchangeService;
import microsoft.exchange.webservices.data.core.enumeration.notification.EventType;
import microsoft.exchange.webservices.data.core.exception.http.HttpErrorException;
import microsoft.exchange.webservices.data.core.service.item.EmailMessage;
import microsoft.exchange.webservices.data.notification.GetEventsResults;
import microsoft.exchange.webservices.data.notification.ItemEvent;
import microsoft.exchange.webservices.data.notification.PullSubscription;
import microsoft.exchange.webservices.data.property.complex.AttachmentCollection;

public class PollServerMNS extends WakefulIntentService implements Constants{


	public PollServerMNS() {
		super("PollServerMNS");
	}

	private ExchangeService service;

	private GetEventsResults events;
	private Context context;


	public boolean shutdownCurrentThread=false;

	private MediaPlayer pollSound;
	private PullSubscription subscription;
	private NotificationManager mNM ;
	private static AlarmManager alarmManager;
	private static 	PendingIntent pendingIntent;
    public static CachedMailHeaderAdapter cachedMailHeaderAdapter;
    public static CachedMailBodyAdapter cachedMailBodyAdapter;
    private MailFunctions mailFunctions = new MailFunctionsImpl();
	private EmailMessage message;
	private int thisnewMailCounter=0;	// this will reset for every poll

	/* this method will be invoked when the Alarm Manager interval time elapses. This is the entry point
	 * 
	 */
	@Override
	public void doWakefulWork(Intent intent) {

		try{
			this.context=this;
            cachedMailHeaderAdapter = new CachedMailHeaderAdapter(context);
            cachedMailBodyAdapter = new CachedMailBodyAdapter(context);
			mNM  = (NotificationManager)context.getSystemService(context.NOTIFICATION_SERVICE);

			pollSound = MediaPlayer.create(context, R.raw.sound);
			alarmManager = PullMailNotificationServiceThread.getAlarmManager();
			pendingIntent=PullMailNotificationServiceThread.getPendingIntent();
			subscription = PullMailNotificationServiceThread.getPullSubscription();
			service = EWSConnection.getServiceFromStoredCredentials(context);
			//REMOVE THIS
			//Log.d(TAG, "Polling using the password " +  ((WebCredentials)service.getCredentials()).getPwd());

			if(subscription != null){
				if(BuildConfig.DEBUG){
					Log.i(TAG_MNS, "PollServerMNS -> Polling Server");
					if (BuildConfig.DEBUG){
						//pollSound.start();
					}
				}
				pollServer();
			}
			else{
				Log.e(TAG, "PollServerMNS -> Subscription is null. Probably an application upgrade. Calling subscripyion thread by starting service  ");
				MailApplication.startMNSService(context);
			}
		}
		catch(NoUserSignedInException ne){
			Log.e(TAG_MNS, "PollServerMNS -> No User has signed in");
			handleGeneralException(ne);

		}

		catch (UnknownHostException e) {
			Log.e(TAG_MNS, "PollServerMNS -> " + e.getMessage());

		}
		catch(NoInternetConnectionException nic){
			Log.e(TAG_MNS, "PollServerMNS -> " + nic);

		}
		catch(HttpErrorException e){

			Log.e(TAG_MNS, "PollServerMNS -> " + e.getMessage());

			if(e.getMessage().toLowerCase().contains("Unauthorized".toLowerCase())){
				//unauthorised
				try{
					MailApplication.stopMNSService(context);
					showLoginErrorNotification();
				}
				catch(Exception le){};
			}
			else
			{
				Log.e(TAG_MNS, "PollServerMNS -> " + e.getMessage());
				handleGeneralException(e);
			}
		}

		catch(Exception e){
			Log.e(TAG_MNS, "PollServerMNS -> " + e.getMessage());
			handleGeneralException(e);
		}
	}

	private void showLoginErrorNotification() {
		NotificationProcessing.showLoginErrorNotification(context);
	}

	private void pollServer() throws Exception {

		events = NetworkCall.pullSubscriptionPoll(context,subscription);

		// Loop through all item-related events.
		for(final ItemEvent itemEvent : events.getItemEvents())
		{   
			if(itemEvent.getEventType()== EventType.NewMail)
			{
				Log.d(TAG_MNS, "PollServerMNS -> New Mail received");

				new Runnable() {

					@Override
					public void run() {
						try {
							message = NetworkCall.bindEmailMessage(context, service, itemEvent);
							thisnewMailCounter++;	// new mails in this poll
							MailNotificationService.newMailNotificationCounter++;	//total new mails

							if(null!= message){

                              writeToCache(message);

                                //show notification only if its not read
								if(!(message.getIsRead())){

									if( null !=  message.getSender() && null !=  message.getSender().getName() && null !=   message.getSubject()){
										showNewMailNotification(message.getSender().getName() , message.getSubject());
									}
									else if(null !=  message.getSender() && null !=  message.getSender().getName() ){
										showNewMailNotification(message.getSender().getName(), context.getText(R.string.mnsServiceNotificationWithNoSubject).toString());
									}
									else
									{
										showNewMailNotification(context.getText(R.string.mnsServiceNotificationWithNullMessage).toString());
									}
								}
								else
								{
									Log.d(TAG, "PollServerMNS -> Message already read. Not showing alert");
								}
							}
							else
							{
								showNewMailNotification(context.getText(R.string.mnsServiceNotificationWithNullMessage).toString());
							}
						} 
						catch(NoUserSignedInException ne){
							Log.e(TAG_MNS, "PollServerMNS -> No User has signed in");
							handleGeneralException(ne);
						}

						catch (UnknownHostException e) {
							Log.e(TAG_MNS, "PollServerMNS -> " + e.getMessage());
						}
						catch(NoInternetConnectionException nic){
							Log.e(TAG_MNS, "PollServerMNS -> " + nic);

						}
						catch(HttpErrorException e){

							Log.e(TAG_MNS, "PollServerMNS -> " + e.getMessage());

							if(e.getMessage().toLowerCase().contains("Unauthorized".toLowerCase())){
								//unauthorised
								try{
									// stop the MNS service
									MailApplication.stopMNSService(context);
									showLoginErrorNotification();
								}
								catch(Exception le){};
							}
							else
							{
								Log.e(TAG_MNS, "PollServerMNS -> " + e.getMessage());
								handleGeneralException(e);
							}
						}

						catch(Exception e){
							Log.e(TAG_MNS, "PollServerMNS -> " + e.getMessage());
							handleGeneralException(e);
						}
					}

					private void showNewMailNotification(String... args) {

						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
							// Create the NotificationChannel, but only on API 26+ because
							// the NotificationChannel class is new and not in the support library
							NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_NEW_MAIL, context.getString(R.string.app_name), NotificationManager.IMPORTANCE_HIGH);
							channel.setDescription(NOTIFICATION_CHANNEL_NEW_MAIL_DESC);
							// Register the channel with the system
							mNM.createNotificationChannel(channel);
						}
						NotificationProcessing.showNewMailNotification(context, thisnewMailCounter, MailNotificationService.newMailNotificationCounter, args);
					}
				}.run();
			}
		}
	}

    /** Writes the message item to cache
     *
     * @param message
     */
    private void writeToCache(EmailMessage message) {
        AttachmentCollection attachmentCollection;
        int totalInlineImgs =0;

        //CACHING MAIL HEADER
        try {
            //caching mail header
            cachedMailHeaderAdapter.cacheNewData(message, MailType.INBOX, "Inbox", "");
        } catch (Exception e) {
            Utilities.generalCatchBlock(e,"Exception while caching mail header during notifications processing", this);
        }

        //CACHING MAIL BODY
        try {
            attachmentCollection = message.getAttachments();

            //get the total number of inline images
            totalInlineImgs = MailApplication.getTotalNoOfInlineImgs(attachmentCollection, this);

            //INLINE IMGS PRESENT
            if (totalInlineImgs > 0) {
               //replace all the inline image "cid" tags with "file://" tags
                String bodyWithImg = MailApplication.getBodyWithImgHtml(context, mailFunctions.getBody(message) , attachmentCollection, mailFunctions.getItemId(message), this);

                //writing VO to cache with the custom body
                cachedMailBodyAdapter.cacheNewData(message,bodyWithImg, MailType.INBOX, "Inbox", "" );

                // download and cache images. html body will be refreshed after each img download to show the imgs
                MailApplication.cacheInlineImages(context, attachmentCollection, mailFunctions.getItemId(message), bodyWithImg, null, this);
            }
            //NO INLINE IMGS
            else {
                //writing VO to cache
                cachedMailBodyAdapter.cacheNewData(message, MailType.INBOX, "Inbox", "");

            }
        } catch (Exception e) {
            Utilities.generalCatchBlock(e, "Exception while caching mail body during notifications processing", this);
        }

    }

    private void handleGeneralException(Exception ne) {
		ne.printStackTrace();
		Log.e(TAG_MNS, "PollServerMNS -> Exception " + ne.getMessage());
		//renew subscrption when exception is related to subscription
			if(ne!=null && ne.getMessage()!=null && ne.getMessage().equalsIgnoreCase("subscription")){
				MailNotificationService.notifyMNSThread();
			}
	}
}
