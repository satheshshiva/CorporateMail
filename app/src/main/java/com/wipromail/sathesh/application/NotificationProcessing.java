package com.wipromail.sathesh.application;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.wipromail.sathesh.R;
import com.wipromail.sathesh.activity.MailListViewActivity;
import com.wipromail.sathesh.constants.Constants;
import com.wipromail.sathesh.service.MailNotificationService;
import com.wipromail.sathesh.service.data.WellKnownFolderName;

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
		
		notification = new Notification(R.drawable.ic_launcher, 
				getScrollingText(),
				System.currentTimeMillis());
		
		Intent pIntent=new Intent(context, MailListViewActivity.class);
		pIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
		pIntent.putExtra(MailListViewActivity.MAIL_TYPE_EXTRA, MailType.INBOX);
        pIntent.putExtra(MailListViewActivity.FOLDER_NAME_EXTRA, WellKnownFolderName.Inbox.toString());
		// The PendingIntent to launch our activity if the user selects this notification
		pendingIntent = PendingIntent.getActivity(context, 0,
				pIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        // Set the info for the views that show in the notification panel.
		notification.setLatestEventInfo(context, title,
                message, pendingIntent);

		MailApplication.setLightNotificationWithPermission(notification);
		MailApplication.setSoundWithPermission(notification);
		MailApplication.setVibrateNotificationWithPermission(notification);
		
		//notification.flags |= Notification.FLAG_AUTO_CANCEL;
		//may display a badge of unread notification
		//notification.number = thisnewMailCounter;	//when getting multiple mails on  a single poll it says 1,2,3 instead of 3

		// Send the notification.
		mNM.notify(totNewMailNotificationCounter, notification);
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
		
		// Set the icon, scrolling text and timestamp
		notification = new Notification(R.drawable.ic_launcher, context.getString(R.string.mns_service_invalidUser_title),
				System.currentTimeMillis());
		
		MailApplication.setLightNotificationWithPermission(notification);
		MailApplication.setSoundWithPermission(notification);
		MailApplication.setVibrateNotificationWithPermission(notification);
		
		notification.flags|=Notification.FLAG_ONGOING_EVENT;
		
		// The PendingIntent to launch our activity if the user selects this notification
		pendingIntent = PendingIntent.getActivity(context, 0,
				new Intent(context, MailApplication.mainApplicationActivity()), PendingIntent.FLAG_CANCEL_CURRENT);
		
		mNM.cancelAll();
		// Set the info for the views that show in the notification panel.
		notification.setLatestEventInfo(context,  context.getString(R.string.mns_service_invalidUser_title),
				context.getString(R.string.mns_service_invalidUser_message), pendingIntent);
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
