package com.wipromail.sathesh.threads.service;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.widget.Toast;

import com.wipromail.sathesh.R;
import com.wipromail.sathesh.application.MailApplication;
import com.wipromail.sathesh.constants.Constants;
import com.wipromail.sathesh.customexceptions.NoInternetConnectionException;
import com.wipromail.sathesh.customexceptions.NoUserSignedInException;
import com.wipromail.sathesh.customui.Notifications;
import com.wipromail.sathesh.ews.EWSConnection;
import com.wipromail.sathesh.ews.NetworkCall;
import com.wipromail.sathesh.intentservice.PollServerMNS;
import com.wipromail.sathesh.service.data.ExchangeService;
import com.wipromail.sathesh.service.data.FolderId;
import com.wipromail.sathesh.service.data.GetEventsResults;
import com.wipromail.sathesh.service.data.HttpErrorException;
import com.wipromail.sathesh.service.data.PullSubscription;
import com.wipromail.sathesh.service.data.WellKnownFolderName;

public class PullMailNotificationServiceThread_bck
extends Thread implements Constants
{

	private List  folder = new ArrayList();        
	private ExchangeService service = new ExchangeService();

	private GetEventsResults events;
	private Context context;

	private NotificationManager mNM;
	public int newMailNotificationCounter=0;
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
		Log.d(TAG, "retrieving Pull Subscription" + subscription);
		return subscription;
	}

	public static PendingIntent getPendingIntent() {
		return pendingIntent;
	}

	public PullMailNotificationServiceThread_bck(Context context , NotificationManager mNM)
	{
		this.context=context;
		this.mNM = mNM;
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

			Log.d(TAG_MNS, "PullMailNotificationServiceThread -> Sequence initiated. Making a new subscription");


			subscription = NetworkCall.subscribePull(context,service, folder);

			Log.d(TAG, "Setting Pull Subscription" + subscription);

			msg.obj=context.getString(R.string.mns_service_started);
			
			
			if(!repeatingPollAlarmSet){
				setNewRepeatingPoll();
				handler.sendMessage(msg);
				Log.d(TAG_MNS, "PullMailNotificationServiceThread -> Poll Service Alarm Set");
				repeatingPollAlarmSet=true;
			}

			//The current thread will go on a wait state until the pull subscription renewal time expires or a notify is called for the current thread.
			waitUntilNextSubscriptionOrNotify(PULL_SUBSCRIPTION_RENEWAL);
			
			cancelRepeatingPollAlarm();
			run();

		}
		catch(NoUserSignedInException ne){

			Log.e(TAG_MNS, "PullMailNotificationServiceThread -> No User has signed in");
			waitThisThread();
			

		}

		catch (UnknownHostException e) {
			Log.e(TAG_MNS, "PollServerMNS -> " + e.getMessage());
			waitThisThread();

		}

		catch(NoInternetConnectionException nic){
			Log.e(TAG_MNS, "PullMailNotificationServiceThread -> " + nic);
			waitThisThread();


		}
		catch(HttpErrorException e){

			Log.e(TAG_MNS, "PullMailNotificationServiceThread -> " + e.getMessage());

			if(e.getMessage().toLowerCase().contains("Unauthorized".toLowerCase())){
				//unauthorised
				try{
					cancelRepeatingPollAlarm();
					showLoginErrorNotification();
				}
				catch(Exception le){};
				waitThisThread();
			}
			else
			{
				Log.e(TAG_MNS, "PullMailNotificationServiceThread -> " + e.getMessage());
				handleGeneralException(e);
			}
		}

		catch (InterruptedException e) {
			// TODO Auto-generated catch block
			try{
				cancelRepeatingPollAlarm();
			}catch(Exception ae){}
			Log.d(TAG_MNS, "PullMailNotificationServiceThread -> Thread Interupted..PollServer Alarm cancelled. Exiting");
		}
		catch(Exception e){
			Log.e(TAG_MNS, "PullMailNotificationServiceThread -> " + e.getMessage());
			
			handleGeneralException(e);


		}

	}

	private void waitUntilNextSubscriptionOrNotify(long pullDuration) {
		// TODO Auto-generated method stub
		try {
			synchronized(this)  {
		wait(pullDuration);
			}
			run();
			
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			try{
				cancelRepeatingPollAlarm();
			}catch(Exception ae){}
			Log.d(TAG_MNS, "PullMailNotificationServiceThread -> Thread Interupted..PollServer Alarm cancelled. Exiting");
		}
	}

	private  void waitThisThread() {
		// TODO Auto-generated method stub
		try {
			synchronized(this)  {
				wait();
			}
			run();
			
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			try{
				cancelRepeatingPollAlarm();
			}catch(Exception ae){}
			Log.d(TAG_MNS, "PullMailNotificationServiceThread -> Thread Interupted..PollServer Alarm cancelled. Exiting");
		}
	}

	public static void cancelRepeatingPollAlarm() {
		// TODO Auto-generated method stub

		if(null != pendingIntent && null != alarmManager){
			try{
				alarmManager.cancel(pendingIntent);
			}
			catch(Exception e){
				Log.e(TAG, "PullMailNotificationServiceThread -> Error while cancelling alarm");
				e.printStackTrace();
			}
		}
		repeatingPollAlarmSet=false;
	}

	/** This will set a new repeating alarm for polling the server
	 * @throws Exception 
	 * 
	 */
	private void setNewRepeatingPoll() throws Exception {
		// TODO Auto-generated method stub
		intent = new Intent(context, PollServerMNS.class);

		//intent.putExtra("asd", context);
		pendingIntent = PendingIntent.getBroadcast(context, 0,
				intent, PendingIntent.FLAG_CANCEL_CURRENT);
		
		alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),
				MailApplication.getPullFrequency(context), pendingIntent);
	}

	private void handleGeneralException(Exception ne) {
		// TODO Auto-generated method stub
		ne.printStackTrace();
		cancelRepeatingPollAlarm();
		Log.e(TAG_MNS, "PullMailNotificationServiceThread -> Exception " + ne.getMessage());
		waitThisThread();
	}



	private void showLoginErrorNotification() {
		// TODO Auto-generated method stub

		// Set the icon, scrolling text and timestamp
		notification = new Notification(R.drawable.ic_launcher, context.getString(R.string.mns_service_invalidUser_title),
				System.currentTimeMillis());

		MailApplication.setLightNotificationWithPermission(notification);
		MailApplication.setSoundWithPermission(notification);
		MailApplication.setVibrateNotificationWithPermission(notification);

		notification.flags=Notification.FLAG_AUTO_CANCEL;

		// The PendingIntent to launch our activity if the user selects this notification
		contentIntent = PendingIntent.getActivity(context, 0,
				new Intent(context, MailApplication.mainApplicationActivity()), 0);

		mNM.cancelAll();
		// Set the info for the views that show in the notification panel.
		notification.setLatestEventInfo(context,  context.getString(R.string.mns_service_invalidUser_title),
				context.getString(R.string.mns_service_invalidUser_message), contentIntent);
		mNM.notify(0, notification);

	}

	public static boolean isRepeatingPollAlarmSet() {
		return repeatingPollAlarmSet;
	}

	public static void setRepeatingPollAlarmSet(boolean repeatingPollAlarmSet) {
		PullMailNotificationServiceThread_bck.repeatingPollAlarmSet = repeatingPollAlarmSet;
	}


	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if(null != msg && null != msg.obj){
			Notifications.showToast(context, msg.obj.toString(), Toast.LENGTH_SHORT);
			}
		}
		};

}


