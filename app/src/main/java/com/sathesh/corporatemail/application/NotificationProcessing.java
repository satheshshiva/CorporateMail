package com.sathesh.corporatemail.application;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;

import com.sathesh.corporatemail.R;
import com.sathesh.corporatemail.activity.MailListViewActivity;
import com.sathesh.corporatemail.constants.Constants;
import com.sathesh.corporatemail.service.MailNotificationService;

import microsoft.exchange.webservices.data.core.enumeration.property.WellKnownFolderName;

public class NotificationProcessing implements Constants{

	private static Notification notification;
	private static PendingIntent pendingIntent;
	private static CharSequence title = "" , message="";


	/** New Mail Notificaion
     *
     * @param context
     * @param thisnewMailCounter
     * @param totNewMailNotificationCounter
     * @param args
     */
    public static void showNewMailNotification(Context context, int thisnewMailCounter, int totNewMailNotificationCounter, String... args) {

		NotificationManager mNM  = (NotificationManager)context.getSystemService(context.NOTIFICATION_SERVICE);

		switch(args.length){
		case 1:
		{
			title = args[0];
			break;
		}
		case 2:
		{
			title = args[0];
			message = args[1];
			break;
		}

		}
		// Set the icon, scrolling text and timestamp
		/*notification = new Notification(R.drawable.ic_launcher, 
				String.format(context.getString(R.string.mnsServiceNewMailNotification), thisnewMailCounter),
				System.currentTimeMillis());
*/
		

		Intent pIntent=new Intent(context, MailListViewActivity.class);
		pIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
		pIntent.putExtra(MailListViewActivity.MAIL_TYPE_EXTRA, MailType.INBOX);
        pIntent.putExtra(MailListViewActivity.FOLDER_NAME_EXTRA, WellKnownFolderName.Inbox.toString());
		// The PendingIntent to launch our activity if the user selects this notification
		pendingIntent = PendingIntent.getActivity(context, 0,
				pIntent, PendingIntent.FLAG_CANCEL_CURRENT);


/*		MailApplication.setLightNotificationWithPermission(notification);
		MailApplication.setSoundWithPermission(notification);
		MailApplication.setVibrateNotificationWithPermission(notification);*/




		Notification notification = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_NEW_MAIL)
				.setSmallIcon(R.drawable.ic_launcher)
				.setContentTitle(title)
				.setContentText(message)
				.setAutoCancel(false)
				.setContentIntent(pendingIntent)
				.build();


		//builder.addAction(R.drawable.cm_hi_res_icon, "SDSDFSDFS", pendingIntent);

	//	builder.setContentIntent(pendingIntent);
//		builder.setAutoCancel(false);
//		builder.setOngoing(true);
//		builder.setNumber(100);
		//builder.setOngoing(true);
		//builder.setSubText("This is subtext...");   //API level 16
		//builder.build();

		mNM.notify(totNewMailNotificationCounter, notification);
		

		//notification.flags |= Notification.FLAG_AUTO_CANCEL;
		//may display a badge of unread notification
		//notification.number = thisnewMailCounter;	//when getting multiple mails on  a single poll it says 1,2,3 instead of 3

	}
	
	private static CharSequence getScrollingText() {
		return title.toString() +"\n"+ message.toString();
	}

    /** Login Error Notification
     *
     * @param context
     */
    public static void showLoginErrorNotification(Context context) {

		MailApplication mailApplication = MailApplication.getInstance();
		/*will be true when the the password is wrong which is set by (NotificationProcessing.showLoginErrorNotification()). This will be set back to false when the user saves a 
		 new password in ChangePasswordDialog*/
		mailApplication.setWrongPwd(true);
		
		NotificationManager mNM  = (NotificationManager)context.getSystemService(context.NOTIFICATION_SERVICE);
		
		/*MailApplication.setLightNotificationWithPermission(notification);
		MailApplication.setSoundWithPermission(notification);
		MailApplication.setVibrateNotificationWithPermission(notification);
		*/
		/*notification.flags|=Notification.FLAG_ONGOING_EVENT;*/
		
		// The PendingIntent to launch our activity if the user selects this notification
		pendingIntent = PendingIntent.getActivity(context, 0,
				new Intent(context, MailApplication.mainApplicationActivity()), PendingIntent.FLAG_CANCEL_CURRENT);
		
		mNM.cancelAll();
		// Set the info for the views that show in the notification panel.

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			// Create the NotificationChannel, but only on API 26+ because
			// the NotificationChannel class is new and not in the support library
			NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_LOGIN_ERROR, context.getString(R.string.app_name), NotificationManager.IMPORTANCE_HIGH);
			channel.setDescription(NOTIFICATION_CHANNEL_LOGIN_ERROR_DESC);
			// Register the channel with the system
			mNM.createNotificationChannel(channel);
		}

		Notification notification = new NotificationCompat.Builder(context,NOTIFICATION_CHANNEL_LOGIN_ERROR )
				.setAutoCancel(false)
				.setContentTitle(context.getString(R.string.mns_service_invalidUser_title))
				.setContentText(context.getString(R.string.mns_service_invalidUser_message))
				.setSmallIcon(R.drawable.ic_launcher)
				.setContentIntent(pendingIntent)
				.setNumber(100)
				.build();


		mNM.notify(0, notification);
	}

    /** Cancels all the current notifications
     *
     * @param context
     */
    public static void cancelAllNotifications(Context context){
		MailNotificationService.newMailNotificationCounter=0;
		NotificationManager mNM = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
		mNM.cancelAll();
	}
}
