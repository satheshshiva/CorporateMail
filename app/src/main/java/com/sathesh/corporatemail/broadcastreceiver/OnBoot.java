
package com.sathesh.corporatemail.broadcastreceiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

import com.sathesh.corporatemail.application.MailApplication;
import com.sathesh.corporatemail.constants.Constants;
import com.sathesh.corporatemail.intentservice.PollServerMNS;

public class OnBoot extends BroadcastReceiver implements Constants{

  @Override
  public void onReceive(Context context, Intent intent) {
	Log.i(TAG, "OnBoot BroadCastReceiver -> Invoked ");
	Log.i(TAG, "OnBoot BroadCastReceiver -> Firing up Mail Notification Service ");
    MailApplication.startMNSService(context);
  }
}