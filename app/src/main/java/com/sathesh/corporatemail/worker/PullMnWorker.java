package com.sathesh.corporatemail.worker;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.sathesh.corporatemail.R;
import com.sathesh.corporatemail.application.MailApplication;
import com.sathesh.corporatemail.application.NotificationProcessing;
import com.sathesh.corporatemail.application.SharedPreferencesAdapter;
import com.sathesh.corporatemail.cache.adapter.CachedMailBodyAdapter;
import com.sathesh.corporatemail.cache.adapter.CachedMailHeaderAdapter;
import com.sathesh.corporatemail.constants.Constants;
import com.sathesh.corporatemail.customexceptions.NoInternetConnectionException;
import com.sathesh.corporatemail.datamodels.PullSubscriptionParams;
import com.sathesh.corporatemail.ews.EWSConnection;
import com.sathesh.corporatemail.ews.MailFunctions;
import com.sathesh.corporatemail.ews.MailFunctionsImpl;
import com.sathesh.corporatemail.ews.NetworkCall;
import com.sathesh.corporatemail.util.Utilities;

import java.util.ArrayList;
import java.util.List;

import microsoft.exchange.webservices.data.core.ExchangeService;
import microsoft.exchange.webservices.data.core.enumeration.notification.EventType;
import microsoft.exchange.webservices.data.core.enumeration.property.WellKnownFolderName;
import microsoft.exchange.webservices.data.core.exception.http.HttpErrorException;
import microsoft.exchange.webservices.data.core.exception.service.remote.ServiceRequestException;
import microsoft.exchange.webservices.data.core.service.item.EmailMessage;
import microsoft.exchange.webservices.data.notification.GetEventsResults;
import microsoft.exchange.webservices.data.notification.ItemEvent;
import microsoft.exchange.webservices.data.notification.PullSubscription;
import microsoft.exchange.webservices.data.property.complex.AttachmentCollection;
import microsoft.exchange.webservices.data.property.complex.FolderId;

public class PullMnWorker extends Worker implements Constants{

	private ExchangeService service;

	private GetEventsResults events;
	private Context context;


	private PullSubscription subscription;
	private NotificationManager mNM ;
	public static CachedMailHeaderAdapter cachedMailHeaderAdapter;
	public static CachedMailBodyAdapter cachedMailBodyAdapter;
	private MailFunctions mailFunctions = new MailFunctionsImpl();
	private EmailMessage message;
	private int thisnewMailCounter=0;	// this will reset for every poll
	List<FolderId> folder  = new ArrayList<>();;
	MediaPlayer pollSound;

	public PullMnWorker(
			@NonNull Context context,
			@NonNull WorkerParameters params) {
		super(context, params);
		this.context = context;
		folder.add(new FolderId(WellKnownFolderName.Inbox));
		pollSound=MediaPlayer.create(context, R.raw.sound);
	}

	/* this method will be invoked when the Alarm Manager interval time elapses. This is the entry point
	 *
	 */
	@Override
	public Result doWork() {
		Log.i(LOG_TAG_PullMnWorker, "PullMnWorker -> Entering PullMnWorker");
		try{
			cachedMailHeaderAdapter = new CachedMailHeaderAdapter(context);
			cachedMailBodyAdapter = new CachedMailBodyAdapter(context);
			mNM  = (NotificationManager)context.getSystemService(context.NOTIFICATION_SERVICE);

			service = EWSConnection.getServiceFromStoredCredentials(context);

			PullSubscriptionParams pullSubscriptionParams = SharedPreferencesAdapter.getPullSubscriptionParams(context);
			if ("".equals(pullSubscriptionParams.getSubscriptionId()) ){
				subscribe(folder);
			}else{
				subscription = new PullSubscription(service);
				subscription.setId(pullSubscriptionParams.getSubscriptionId());
				subscription.setWaterMark(pullSubscriptionParams.getWatermark());
			}

			if(subscription != null){
				//The time out for the doWork is 10 minutes. We have to make the doWork occupied for 9.x mins to make our notifications efficient.
				//
				for(int i=0; i<18; i++){
					pollServer(subscription);
					Thread.sleep(30 * 1000);
				}
				return pollServer(subscription);
			}
			else{
				return handleGeneralException(new Exception("Subscription is null"));
			}
		}
		catch(HttpErrorException | ServiceRequestException e){
			if(e.getMessage()!=null && e.getMessage().toLowerCase().contains("Unauthorized".toLowerCase())){
				//unauthorised
				MailApplication.stopMNWorker(context);
				NotificationProcessing.showLoginErrorNotification(context);
				return Result.success();
			}
			return handleGeneralException(e);
		}
		catch(NoInternetConnectionException e){
			Log.e(LOG_TAG_PullMnWorker, "No internet. Sending the worker to the retry queue");
			return Result.retry();
		}
		catch(Exception e){
			return handleGeneralException(e);
		}finally {
			Log.d(LOG_TAG_PullMnWorker, "PullMnWorker -> Exiting PullMnWorker");
		}
	}

	private void subscribe(List<FolderId> folder) throws Exception{
		Log.i(LOG_TAG_PullMnWorker, "PullMnWorker -> Making a new pull subscription");
		subscription = NetworkCall.subscribePull(context, service, folder);
		Log.d(LOG_TAG_PullMnWorker, "PullMnWorker -> Storing subscription id/watermark");
		SharedPreferencesAdapter.setPullSubscriptionParams(context, new PullSubscriptionParams(subscription.getId(), subscription.getWaterMark()));
	}

	private Result pollServer(PullSubscription subscription) throws Exception {
		String id = subscription.getId();
		String watermark = subscription.getWaterMark();

		Log.d(LOG_TAG_PullMnWorker, "PullMnWorker -> Polling Server");
		//pollSound.start();

		//EWS Call
		events = NetworkCall.pullSubscriptionPoll(context,subscription);

		// Loop through all item-related events.
		for(final ItemEvent itemEvent : events.getItemEvents())
		{
			if(itemEvent.getEventType()== EventType.NewMail)
			{
				Log.i(LOG_TAG_PullMnWorker, "PullMnWorker -> New Mail received");

				message = NetworkCall.bindEmailMessage(context, service, itemEvent);
				thisnewMailCounter++;	// new mails in this poll
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
						Log.i(LOG_TAG, "PullMnWorker -> Message already read. Not showing alert");
					}
				}
				else
				{
					showNewMailNotification(context.getText(R.string.mnsServiceNotificationWithNullMessage).toString());
				}
			}
		}
		if (!subscription.getId().equals(id) || !subscription.getWaterMark().equals(watermark)) {
			Log.d(LOG_TAG_PullMnWorker, "PullMnWorker -> Storing subscription id/watermark");
			SharedPreferencesAdapter.setPullSubscriptionParams(context, new PullSubscriptionParams(subscription.getId(), subscription.getWaterMark()));
		}
		Log.d(LOG_TAG_PullMnWorker, "PullMnWorker -> poll completed successfully");
		return Result.success();
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
		NotificationProcessing.showNewMailNotification(context, thisnewMailCounter, args);
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

	private Result handleGeneralException(Exception ge) {
		Log.e(LOG_TAG_PullMnWorker, "PullMnWorker -> Exception: " + ge.getMessage());
		ge.printStackTrace();
		//renew subscription when exception is related to subscription
		if(ge.getMessage()!=null && (ge.getMessage().contains("subscription")
				|| ge.getMessage().contains("watermark is invalid")
				|| ge.getMessage().contains("watermark not valid") )){
			try {
				Log.e(LOG_TAG_PullMnWorker, "PullMnWorker -> Subscription expired. Renewing subscription " );
				subscribe(folder);
				return doWork();
			}catch(Exception e){
				Log.e(LOG_TAG_PullMnWorker, "PullMnWorker -> Renew exception " + e.getMessage());
				e.printStackTrace();
				return Result.failure();
			}
		}
		return Result.failure();
	}
}
