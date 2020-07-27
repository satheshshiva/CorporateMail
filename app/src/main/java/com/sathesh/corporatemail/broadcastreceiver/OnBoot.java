
package com.sathesh.corporatemail.broadcastreceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.sathesh.corporatemail.application.MailApplication;
import com.sathesh.corporatemail.constants.Constants;

public class OnBoot extends BroadcastReceiver implements Constants{

  @Override
  public void onReceive(Context context, Intent intent) {
	Log.i(LOG_TAG, "OnBoot BroadCastReceiver -> Invoked ");
	Log.i(LOG_TAG, "OnBoot BroadCastReceiver -> Firing up Mail Notification Service ");
    MailApplication.startMNWorker(context);
  }
}