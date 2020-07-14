package com.sathesh.corporatemail.threads.service;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager.WakeLock;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import com.sathesh.corporatemail.R;
import com.sathesh.corporatemail.application.MailApplication;
import com.sathesh.corporatemail.application.NotificationProcessing;
import com.sathesh.corporatemail.broadcastreceiver.MNSAlarmSetter;
import com.sathesh.corporatemail.constants.Constants;
import com.sathesh.corporatemail.customexceptions.NoInternetConnectionException;
import com.sathesh.corporatemail.customexceptions.NoUserSignedInException;
import com.sathesh.corporatemail.customui.Notifications;
import com.sathesh.corporatemail.ews.EWSConnection;
import com.sathesh.corporatemail.ews.NetworkCall;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import microsoft.exchange.webservices.data.core.ExchangeService;
import microsoft.exchange.webservices.data.core.enumeration.property.WellKnownFolderName;
import microsoft.exchange.webservices.data.core.exception.http.HttpErrorException;
import microsoft.exchange.webservices.data.notification.GetEventsResults;
import microsoft.exchange.webservices.data.notification.PullSubscription;
import microsoft.exchange.webservices.data.property.complex.FolderId;

public class PullSubscriptionThread
extends Thread implements Constants
{

	private List  folder = new ArrayList();        
	private ExchangeService service = new ExchangeService();

	private GetEventsResults events;
	private static Context context;

	private Notification notification;
	private PendingIntent contentIntent;


	private static PullSubscription subscription;

	private Message msg = Message.obtain();



	private WakeLock wl;
	private Intent intent;
	private static 	PendingIntent pendingIntent;
	private static boolean repeatingPollAlarmSet;

	private static AlarmManager alarmManager;

	public static AlarmManager getAlarmManager() {
		return alarmManager;
	}

	public static PullSubscription getPullSubscription() {
		Log.d(TAG_MNS, "retrieving Pull Subscription" + subscription);
		return subscription;
	}

	public static PendingIntent getPendingIntent() {
		return pendingIntent;
	}

		
	public PullSubscriptionThread(Context context )
	{
		this.context=context;
		alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

	}



	public void run()
	{

		try{
			service = EWSConnection.getServiceFromStoredCredentials(context);

			service.setTraceEnabled(false);

			folder.add(new FolderId(WellKnownFolderName.Inbox));


			WellKnownFolderName sd = WellKnownFolderName.Inbox;
			FolderId folderId = new FolderId(sd);

			List folder = new ArrayList<FolderId>();
			folder.add(folderId);

			Log.d(TAG_MNS, "PullSubscriptionThread -> Sequence initiated. Making a new subscription");


			if(!repeatingPollAlarmSet){
				setNewRepeatingPoll();
				msg = Message.obtain();		//prevents message already in use exception
				handler.sendMessage(msg);
				repeatingPollAlarmSet=true;
			}
			
			//EWS Call..
			//if the call is successful thread will go to TIMED_WAIT state. 
			// If exception occurs(no internet etc) it will enter wait. the alarm which is set above will notify this WAIT thread if exception occured
			subscription = NetworkCall.subscribePull(context,service, folder);

			Log.d(TAG_MNS, "Setting Pull Subscription" + subscription);

			msg.obj=context.getString(R.string.mns_service_started);
			
			/*
			if(!repeatingPollAlarmSet){
				setNewRepeatingPoll();
				msg = Message.obtain();		//prevents message already in use exception
				handler.sendMessage(msg);
				repeatingPollAlarmSet=true;
			}
*/
			//The current thread will go on a wait state until the pull subscription renewal time expires or a notify is called for the current thread.
			waitUntilNextSubscriptionOrNotify(PULL_SUBSCRIPTION_RENEWAL);
			
			cancelRepeatingPollAlarm();
			Log.d(TAG_MNS, "PullSubscriptionThread -> Renewing the subscription");
			run();

		}
		catch(NoUserSignedInException ne){

			Log.e(TAG_MNS, "PullSubscriptionThread -> No User has signed in");
			waitUntilNotify();		

		}

		catch (UnknownHostException e) {
			Log.e(TAG_MNS, "PullSubscriptionThread -> " + e.getMessage());
			waitUntilNotify();

		}

		catch(NoInternetConnectionException nic){
			Log.e(TAG_MNS, "PullSubscriptionThread -> " + nic);
			waitUntilNotify();

		}
		catch(HttpErrorException e){

			Log.e(TAG_MNS, "PullSubscriptionThread -> " + e.getMessage());

			if(e.getMessage().toLowerCase().contains("Unauthorized".toLowerCase())){
				//unauthorised
				try{
					MailApplication.stopMNSService(context);
					NotificationProcessing.showLoginErrorNotification(context);
				}
				catch(Exception le){};
				waitUntilNotify();
			}
			else
			{
				Log.e(TAG_MNS, "PullSubscriptionThread -> " + e.getMessage());
				handleGeneralException(e);
			}
		}

		catch (InterruptedException e) {
			// TODO Auto-generated catch block
			try{
				cancelRepeatingPollAlarm();
			}catch(Exception ae){}
			Log.d(TAG_MNS, "PullSubscriptionThread -> Thread Interupted(in run())..PullSubscriptionThread Alarm cancelled. Exiting");
		//	waitUntilNotify();
		}
		catch(Exception e){
			Log.e(TAG_MNS, "PullSubscriptionThread -> " + e.getMessage());
			
			handleGeneralException(e);


		}

	}

	
	//will wait in this method when any exception occurs other than interrupted exception
	private void waitUntilNotify() {
		// TODO Auto-generated method stub
		//waitThisThread();
		
		synchronized(this)  {
			Log.d(TAG_MNS, "PullSubscriptionThread -> Thread entering wait sincean exception might have occured");
			try {
				wait();
				Log.i(TAG_MNS, "PullSubscriptionThread -> Thread resumed from wait mode ");
				run();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				try{
					cancelRepeatingPollAlarm();
				}catch(Exception ae){e.printStackTrace();}
				Log.d(TAG_MNS, "PullSubscriptionThread -> Thread Interupted(when waiting in waitUntilNotify() method)..PollServer Alarm cancelled. Exiting");
			}}
		
	}

	private void waitUntilNextSubscriptionOrNotify(long pullDuration) throws InterruptedException {
		// TODO Auto-generated method stub
			synchronized(this)  {
		wait(pullDuration);
			}
			
		
	}
//
//	private  void waitThisThread() {
//		// TODO Auto-generated method stub
//			
//			synchronized(this)  {
//				Log.d(TAG_MNS, "PullSubscriptionThread -> Thread entering wait");
//				try {
//					wait();
//				} catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					try{
//						cancelRepeatingPollAlarm();
//					}catch(Exception ae){}
//					Log.d(TAG_MNS, "PullSubscriptionThread -> Thread Interupted(in waitThisThread())..PollServer Alarm cancelled. Exiting");
//					throw new Interru
//					//handleGeneralException(e);
//				}
//			}
//			
//			
//			
//	
//	}

	public static void cancelRepeatingPollAlarm() {
		// TODO Auto-generated method stub

		if(null != pendingIntent && null != alarmManager){
			try{
				alarmManager.cancel(pendingIntent);
				repeatingPollAlarmSet=false;
				Log.d(TAG_MNS, "PullSubscriptionThread -> cancelRepeatingPollAlarm() -> Alarm cancelled");
			}
			catch(Exception e){
				Log.e(TAG_MNS, "PullSubscriptionThread -> Error while cancelling alarm");
				e.printStackTrace();
			}
		}
		
	}

	/** This will set a new repeating alarm for polling the server
	 * @throws Exception 
	 * 
	 */
	private static void setNewRepeatingPoll() throws Exception {
		setNewRepeatingPoll(MailApplication.getPullFrequency(context));
	}
	private static void setNewRepeatingPoll(Long millis) throws Exception {
	Log.i(TAG_MNS, "PullSubscriptionThread -> Setting up alarm for PollServerMNS thread");
	AlarmManager mgr=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

	Intent i  = new Intent(context, MNSAlarmSetter.class);

	//intent.putExtra("asd", context);
	pendingIntent = PendingIntent.getBroadcast(context, 0,
			i, PendingIntent.FLAG_CANCEL_CURRENT);

	mgr.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
			SystemClock.elapsedRealtime()+60000,
			Long.valueOf(millis),
			pendingIntent);


	Log.d(TAG_MNS, "Alarm set: " + millis/1000 + " seconds" );
	}

	private void handleGeneralException(Exception ne) {
		// TODO Auto-generated method stub
		ne.printStackTrace();
		cancelRepeatingPollAlarm();
		Log.e(TAG_MNS, "PullSubscriptionThread -> Exception " + ne.getMessage());
		waitUntilNotify();
	}



	public static boolean isRepeatingPollAlarmSet() {
		return repeatingPollAlarmSet;
	}

	public static void setRepeatingPollAlarmSet(boolean repeatingPollAlarmSet) {
		PullSubscriptionThread.repeatingPollAlarmSet = repeatingPollAlarmSet;
	}


	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if(null != msg && null != msg.obj){
			Notifications.showToast(context, msg.obj.toString(), Toast.LENGTH_SHORT);
			}
		}
		};

	/**
	 * @throws Exception 
	 * 
	 */
	public static void resetAlarm(Long millis) throws Exception {
		// TODO Auto-generated method stub
		cancelRepeatingPollAlarm();
		setNewRepeatingPoll(millis);
		
	}

}


