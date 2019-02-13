package com.sathesh.corporatemail.ews;

import android.content.Context;

import com.sathesh.corporatemail.application.MailApplication;
import com.sathesh.corporatemail.constants.Constants;
import com.sathesh.corporatemail.customexceptions.NoUserSignedInException;

import java.net.URI;
import java.util.Map;

import microsoft.exchange.webservices.data.core.ExchangeService;
import microsoft.exchange.webservices.data.credential.WebCredentials;

public class EWSConnection implements Constants{

	private static ExchangeService service;

	private static String SignedInAccUser=USERNAME_NULL,SignedInAccPassword=PASSWORD_NULL ;


	public static ExchangeService getService(Context context, String username, String password) throws Exception{
		service = new ExchangeService();

		try {

			service.setCredentials(new WebCredentials(username,	password));
			//System.out.println("Username " + username + "\nPassword " + password);
			service.setUrl(new URI(MailApplication.getWebmailURL(context)));
			//service.setUrl(new URI("https://mail.cognizant.com/EWS/Exchange.asmx"));
			//service.setTraceEnabled(true);
		} catch (Exception e) {
			throw e;
		}
		return service;
	}


	public static ExchangeService getServiceFromStoredCredentials(Context context) throws NoUserSignedInException, Exception{

		Map<String, String> storedCredentials;


		try{
			service = new ExchangeService();

			storedCredentials = MailApplication.getStoredCredentials(context);

			SignedInAccUser = storedCredentials.get("signedInAccUser");
			SignedInAccPassword = storedCredentials.get("signedInAccPassword");
			//FOR TEST PURPOSE. NEVER LEAVE THIS LINE ON PROD
			// 	Log.d(TAG, "EWSConnection ->" + SignedInAccUser + " PWD " + SignedInAccPassword);
			if ( SignedInAccUser.equals(USERNAME_NULL) && SignedInAccPassword.equals(PASSWORD_NULL)){
				throw new NoUserSignedInException();
			}
			else{
				service.setCredentials(new WebCredentials(SignedInAccUser,	SignedInAccPassword));

				service.setUrl(new URI(MailApplication.getWebmailURL(context)));
				//service.setUrl(new URI("https://mail.cognizant.com/EWS/Exchange.asmx"));
				//service.setTraceEnabled(true);

			}
		}
		catch(Exception e){
			throw e;
		}
		return service;
	}

}
