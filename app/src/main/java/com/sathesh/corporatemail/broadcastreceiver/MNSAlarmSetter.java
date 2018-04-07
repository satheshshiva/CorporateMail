
package com.sathesh.corporatemail.broadcastreceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.commonsware.cwac.wakeful.WakefulIntentService;
import com.sathesh.corporatemail.constants.Constants;
import com.sathesh.corporatemail.intentservice.PollServerMNS;

public class MNSAlarmSetter extends BroadcastReceiver implements Constants{
	

  @Override
  public void onReceive(Context context, Intent intent) {
	WakefulIntentService.sendWakefulWork(context, PollServerMNS.class);
	
	
  }
  
}