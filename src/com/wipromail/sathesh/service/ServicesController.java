package com.wipromail.sathesh.service;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;

public class ServicesController {

	
	public static boolean startMailNotificationService(Object cls){
		//sdf
		((ContextWrapper)cls).startActivity(new Intent((Context) cls,MailNotificationService.class));
		return true;
	}
	
}
