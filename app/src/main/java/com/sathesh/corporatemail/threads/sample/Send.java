package com.sathesh.corporatemail.threads.sample;



import android.os.AsyncTask;

import microsoft.exchange.webservices.data.core.ExchangeService;
import microsoft.exchange.webservices.data.core.service.item.EmailMessage;
import microsoft.exchange.webservices.data.property.complex.MessageBody;

public class Send extends AsyncTask<Void, Void, Boolean>{

	private ExchangeService service;
	
	
	@Override
	protected Boolean doInBackground(Void... paramArrayOfParams) {
		// TODO Auto-generated method stub
		
		try {
			String username="";
			String password="";
			//service = EWSConnection.getService(username ,password);
			
			
			EmailMessage msg= new EmailMessage(service);
			msg.setSubject("Test Mail"); 
			msg.setBody(MessageBody.getMessageBodyFromText("Sent using the <b>Android</b> code"));
			msg.getToRecipients().add("sathesh.shiva@wipro.com");
			msg.send();
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return true;
	}

	protected Boolean onPostExecute(boolean bool) {

		return true;
    }

	
}
