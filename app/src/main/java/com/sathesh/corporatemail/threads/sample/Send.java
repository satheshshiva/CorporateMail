package com.sathesh.corporatemail.threads.sample;


import com.sathesh.corporatemail.ews.EWSConnection;
import com.sathesh.corporatemail.service.data.EmailMessage;
import com.sathesh.corporatemail.service.data.ExchangeService;
import com.sathesh.corporatemail.service.data.MessageBody;

import android.os.AsyncTask;

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
