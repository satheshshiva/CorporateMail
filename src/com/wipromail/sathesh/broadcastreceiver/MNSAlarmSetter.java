
package com.wipromail.sathesh.broadcastreceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.commonsware.cwac.wakeful.WakefulIntentService;
import com.wipromail.sathesh.constants.Constants;
import com.wipromail.sathesh.intentservice.PollServerMNS;

public class MNSAlarmSetter extends BroadcastReceiver implements Constants{
	

  @Override
  public void onReceive(Context context, Intent intent) {
	WakefulIntentService.sendWakefulWork(context, PollServerMNS.class);
	
	
  }
  
}