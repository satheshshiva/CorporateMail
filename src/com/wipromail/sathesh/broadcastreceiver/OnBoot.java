
package com.wipromail.sathesh.broadcastreceiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

import com.wipromail.sathesh.application.MailApplication;
import com.wipromail.sathesh.constants.Constants;
import com.wipromail.sathesh.intentservice.PollServerMNS;

public class OnBoot extends BroadcastReceiver implements Constants{

  @Override
  public void onReceive(Context context, Intent intent) {
	Log.i(TAG, "OnBoot BroadCastReceiver -> Invoked ");
	Log.i(TAG, "OnBoot BroadCastReceiver -> Firing up Mail Notification Service ");
    MailApplication.startMNSService(context);
  }
}