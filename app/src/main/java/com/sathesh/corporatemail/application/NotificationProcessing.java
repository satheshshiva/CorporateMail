package com.sathesh.corporatemail.application;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.sathesh.corporatemail.R;
import com.sathesh.corporatemail.activity.MailListViewActivity;
import com.sathesh.corporatemail.constants.Constants;

import microsoft.exchange.webservices.data.core.enumeration.property.WellKnownFolderName;

public class NotificationProcessing implements Constants{

	private static PendingIntent pendingIntent;
	private static CharSequence title = "" , message="";

	public static void initNotificationChannels(Context context){
		NotificationChannel channel1, channel2;
		NotificationManagerCompat notificationManager;
			notificationManager = NotificationManagerCompat.from(context);
			// Create the NotificationChannel, but only on API 26+ because
			// the NotificationChannel class is new and not in the support library
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
				// New mail notification channel
				channel1 = new NotificationChannel(NOTIFICATION_CHANNEL_ID_NEW_MAIL,
						context.getString(R.string.notification_channel_new_mail_name),
						NotificationManager.IMPORTANCE_HIGH);
				channel1.setDescription(context.getString(R.string.notification_channel_new_mail_desc));
				channel1.enableLights(true);
				channel1.enableVibration(false);
				notificationManager.createNotificationChannel(channel1);

				// Important alerts notification channel
				channel2 = new NotificationChannel(NOTIFICATION_CHANNEL_ID_LOGIN_ERROR,
						context.getString(R.string.notification_channel_alert_name),
						NotificationManager.IMPORTANCE_HIGH);
				channel1.enableLights(true);
				channel1.enableVibration(false);
				channel2.setDescription(context.getString(R.string.notification_channel_alert_desc));
				// Register the channel with the system
				notificationManager.createNotificationChannel(channel2);
		}
	}

	/** New Mail Notificaion
     *
     * @param context
     * @param totNewMailNotificationCounter
     * @param args
     */
    public static void showNewMailNotification(Context context, int totNewMailNotificationCounter, String... args) {

		NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
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

		Intent pIntent=new Intent(context, MailListViewActivity.class);
		pIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
		pIntent.putExtra(MailListViewActivity.MAIL_TYPE_EXTRA, MailType.INBOX);
        pIntent.putExtra(MailListViewActivity.FOLDER_NAME_EXTRA, WellKnownFolderName.Inbox.toString());
		// The PendingIntent to launch our activity if the user selects this notification
		pendingIntent = PendingIntent.getActivity(context, 0,
				pIntent, PendingIntent.FLAG_CANCEL_CURRENT);

		Bitmap largeIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher);

		Notification notification = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID_NEW_MAIL)
				.setSmallIcon(R.drawable.ic_notification)
				.setLargeIcon(largeIcon)
				.setContentTitle(title)
				.setContentText(message)
				.setColor(Color.parseColor("#2cb3f5"))
				.setPriority(NotificationCompat.PRIORITY_HIGH)
				.setAutoCancel(false)
				.setContentIntent(pendingIntent)
				.build();

		notificationManager.notify(totNewMailNotificationCounter, notification);

	}


    /** Login Error Notification
     *
     * @param context
     */
    public static void showLoginErrorNotification(Context context) {

		NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
		
		// The PendingIntent to launch our activity if the user selects this notification
		pendingIntent = PendingIntent.getActivity(context, 0,
				new Intent(context, MailApplication.mainApplicationActivity()), PendingIntent.FLAG_CANCEL_CURRENT);

		notificationManager.cancelAll();
		// Set the info for the views that show in the notification panel.
		Bitmap largeIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher);
		Notification notification = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID_LOGIN_ERROR )
				.setAutoCancel(false)
				.setContentTitle(context.getString(R.string.mns_service_invalidUser_title))
				.setContentText(context.getString(R.string.mns_service_invalidUser_message))
				.setSmallIcon(R.drawable.ic_notification)
				.setLargeIcon(largeIcon)
				.setColor(Color.parseColor("#FF0000"))
				.setPriority(NotificationCompat.PRIORITY_HIGH)
				.setContentIntent(pendingIntent)
				.setNumber(100)
				.build();


		notificationManager.notify(0, notification);
	}

    /** Cancels all the current notifications
     *
     * @param context
     */
    public static void cancelAllNotifications(Context context){
		NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
		notificationManager.cancelAll();
	}
}
