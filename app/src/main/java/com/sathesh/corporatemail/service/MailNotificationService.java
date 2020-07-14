
package com.sathesh.corporatemail.service;

import android.app.AlarmManager;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.sathesh.corporatemail.R;
import com.sathesh.corporatemail.application.NotificationProcessing;
import com.sathesh.corporatemail.constants.Constants;
import com.sathesh.corporatemail.customui.Notifications;
import com.sathesh.corporatemail.threads.service.PullSubscriptionThread;

/**
 * @author Sathesh
 *
 *This service will create a thread PullSubscriptionThread(MNSThread in DDMS) which subscribes for Pull Subscription and schedules an alarm for Polling the server.
 *The Pull subscription thread is always active and it renews itself for 
 *every 23 hours. The Alarm will call the class PollServerMNS on the interval specified.
 *If the MNSThread is in TIMED_WAIT then it is waiting normally for renewal subscription time elapse, if it is in WAIT state then an exception has made it to wait..
 *it will resume again only when notify() is called..
 *
 *The onStartCommand() is invoked whenever this service wanted to be started. it is called everytime when opening inbox.. when called, ittt will handle bassed on the current state of the worker(PullMailNotificationService) thread
 *
 */
public class MailNotificationService extends Service implements Constants {
    private static PullSubscriptionThread mnsthread ;
    private String mnsThreadState="";
    private AlarmManager am;
  
    public static int newMailNotificationCounter=0;
    /**
     * Class for clients to access.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with
     * IPC.
     */
    public class LocalBinder extends Binder {
        MailNotificationService getService() {
            return MailNotificationService.this;
        }
    }
    
    @Override
    public void onCreate() {
    	Log.d(TAG, "MailNoificationService -> OnCreate called "); 
      //  mnsthread = new StreamMailNotificationServiceThread(this);
    	 mnsthread =createNewNotificationThread();
        // new PullSubscriptionThread(this , mNM);
      
    }

    private PullSubscriptionThread createNewNotificationThread() {
		// TODO Auto-generated method stub
		return (new PullSubscriptionThread(this));
    	//return (new PullSubscriptionThread());
	}

	@Override
    public int onStartCommand(Intent intent, int flags, int startId) {
		
		//will enter here when trying to instantiate teh service.. i.e. logically on appln launch every time..
        Log.i(TAG_MNS, "Received start id " + startId + ": " + intent);
   
       
        if(mnsthread != null){
        	//thread name is just for reference in DDMS. not programatic use.
        	mnsthread.setName("MNSThread");
        	Log.i(TAG_MNS, "MailNotificationService inside start command");
        
        	//The mnsthread manages the pull subscription and creates the alarm for polling server
       mnsThreadState = mnsthread.getState().name();
       
       Log.i(TAG_MNS, "MailNotificationServiceThread State is ==> " + mnsThreadState);
       
       
       
       if(mnsThreadState.equalsIgnoreCase("NEW") ){
    	   mnsthread.start();
    
       }
       else if(mnsThreadState.equalsIgnoreCase("WAITING")){
    	   //will be in wait state when previously executed and paused after a exception
    	   notifyMNSThread();
       } 
       else if(mnsThreadState.equalsIgnoreCase("TIMED_WAITING")){
    	   //thread is alive and will renew after subscription time expire. so do nothing here
   
       }
       else if(mnsThreadState.equalsIgnoreCase("TERMINATED")){
    	   //the thread will be in TERMINATED state when the thread was stopped bcos of usually an auth issue. 
    	   //since service start is called now we try to create a new subscription, alarm etc., Given a new thread name just to identify
    	   
    	   mnsthread=createNewNotificationThread();
    	   mnsthread.setName("NewMNSThread");
    	   mnsthread.start();
    	  
       } 
       
        }
        else
        {
        	Log.e(TAG_MNS, "PullSubscriptionThread is null");
        }
        return START_STICKY;
    }

    /** Calling notify will create a new pull subscription and creates alarm if it is not already set.
     * 
     */
    public static  void notifyMNSThread() {
		// TODO Auto-generated method stub
    	synchronized(mnsthread){
    	mnsthread.notify();
    	}
	}

	@Override
    public void onDestroy() {
        // Cancel the persistent notification.
    	
        NotificationProcessing.cancelAllNotifications(this);

    	
    	cancelRepeatingSubscriptionAndPolling();
        // Tell the user we stopped.
        Notifications.showToast(this, getText(R.string.mns_service_stopped), Toast.LENGTH_SHORT);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    // This is the object that receives interactions from clients.  See
    // RemoteService for a more complete example.
    private final IBinder mBinder = new LocalBinder();

	/** Called when the repeating pollservermns finds a invalid user name/password. This will shutdown both threads of Pull Subscription and Poll server alarm.
	 * 
	 */
	public static void cancelRepeatingSubscriptionAndPolling() {
		// TODO Auto-generated method stub
		
		PullSubscriptionThread.cancelRepeatingPollAlarm();
		mnsthread.interrupt();
		
	}

  
   
}

