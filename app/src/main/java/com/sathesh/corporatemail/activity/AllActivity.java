package com.sathesh.corporatemail.activity;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.sathesh.corporatemail.R;
import com.sathesh.corporatemail.constants.Constants;
import com.sathesh.corporatemail.threads.sample.Inbox;

import java.net.URISyntaxException;

import microsoft.exchange.webservices.data.core.ExchangeService;
import microsoft.exchange.webservices.data.core.exception.http.HttpErrorException;
import microsoft.exchange.webservices.data.core.exception.service.remote.ServiceRequestException;
import microsoft.exchange.webservices.data.core.service.item.EmailMessage;
import microsoft.exchange.webservices.data.core.service.item.Item;
import microsoft.exchange.webservices.data.property.complex.MessageBody;
import microsoft.exchange.webservices.data.search.FindItemsResults;

public class AllActivity extends Activity implements Constants{

	TextView textView1 ;
	public FindItemsResults<Item> findResults ;
	int no=0;
	ProgressBar progressBar;
	private SharedPreferences credStorage;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent intent = new Intent(this, LoginPageActivity.class);
		startActivity(intent);
		credStorage = getSharedPreferences(CRED_PREFS_NAME, 0);
		//Log.d(TAG, "Username : " + credStorage.getString(CRED_PREFS_USERNAME, ""));
		//Log.d(TAG, "Password : " + credStorage.getString(CRED_PREFS_PASSWORD,""));
		setContentView(R.layout.activity_main);
		/*  progressBar = (ProgressBar) findViewById(R.id.progressBar1);
        progressBar.setVisibility(View.INVISIBLE);

        textView1 = (TextView) findViewById(R.id.textView1);*/
		// TestRootAccess.chkRoot();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	public void onClickInbox(View view) {
		progressBar.setVisibility(View.VISIBLE);
		textView1.setText("Logging in");
		try{

			Log.d(LOG_TAG, "Starting thread");

			//do
			//{
			Thread t = new Thread(new Inbox(findResults, handlerInbox));
			t.start();
		}catch(Exception e){
			e.printStackTrace();
			progressBar.setVisibility(View.VISIBLE);
			textView1.setText("Error occured");
		}
	}

	public void onClickSend(View view) {

		textView1.setText("Sending");
		try{

			Send sendmail = new Send();
			sendmail.execute();

		}catch(Exception e){
			System.err.println("Exception occured in getting contents");
			e.printStackTrace();
		}
	}


	private Handler handlerInbox = new Handler() {
		@SuppressWarnings("unchecked")
		@Override
		public void handleMessage(Message msg){
			if (msg.arg1 != 9999){		//exception
				StringBuffer buff = new StringBuffer();


				no=0;

				progressBar.setProgress(msg.arg1);
				try {
					if (msg.obj != null){
						findResults = (FindItemsResults<Item>)msg.obj;
						for(Item item : findResults.getItems())
						{
							buff.append("#" + ++no + " " +item.getDisplayTo() + "\n");

							buff.append("Sub:" + item.getSubject()+"\n\n");

							textView1.setText(buff);
							progressBar.setVisibility(View.INVISIBLE);
						}
					}
				} 
				catch (Exception e) {
					e.printStackTrace();
					progressBar.setVisibility(View.INVISIBLE);
					textView1.setText("Error Occured\nDetails: " + e.getMessage());

				}

			}
			else
			{
				progressBar.setVisibility(View.INVISIBLE);
				if (msg.obj != null){
					Exception e = (Exception) msg.obj;
					e.printStackTrace();
					textView1.setText("Error Occured\nDetails: " + e.getMessage());
				}
				else{
					textView1.setText("Error Occured" );
				}


			}  
		}


	};


	private class Send extends AsyncTask<Void, String, Long>{

		private ExchangeService service;

		@Override
		protected void onPreExecute() {

			try {
				progressBar.setVisibility(View.VISIBLE);

			} catch (Exception e) {
				e.printStackTrace();
				Log.e(LOG_TAG, "Exception occured on preexecute");
			}

		}

		@Override
		protected Long doInBackground(Void... paramArrayOfParams) {

			try {

				publishProgress("2" ,"RUNNING", "Logging in");
				String username="";
				String password="";
				//	service = EWSConnection.getService(username ,password);


				publishProgress("3" ,"RUNNING", "Creating");
				EmailMessage msg= new EmailMessage(service);
				msg.setSubject("Test Mail"); 
				msg.setBody(MessageBody.getMessageBodyFromText("Sent using the <b>Android</b>"));
				msg.getToRecipients().add("sathesh.shiva@wipro.com");
				publishProgress("6" ,"RUNNING", "Sending");
				msg.send();
				publishProgress("7" ,"COMPLETED", "Mail Sent!");
			}
			catch (URISyntaxException e) {
				publishProgress("0" ,"ERROR", "Malformed Webmail URL");
			}
			catch(HttpErrorException  | ServiceRequestException e){
				if(e.getMessage().toLowerCase().contains("Unauthorized".toLowerCase())){
					publishProgress("0" ,"ERROR", "Authentication Failed!\nDetails: " + e.getMessage());
				}
				else
				{
					publishProgress("0" ,"ERROR", "Error Occured!\nDetails: " + e.getMessage());
				}
			}

			catch (Exception e) {
				publishProgress("0" ,"ERROR", "Error Occured!\nDetails: " +e.getMessage());
			}

			return 0l;

		}

		@Override
		protected void onProgressUpdate(String... progress) {

			if (progress[1].equalsIgnoreCase("RUNNING")){
				progressBar.setProgress(Integer.valueOf(progress[0]));
				textView1.setText(progress[2]);
			}

			else if (progress[1].equalsIgnoreCase("COMPLETED")){
				progressBar.setProgress(Integer.valueOf(0));
				progressBar.setVisibility(View.INVISIBLE);
				textView1.setText(progress[2]);
			}

			else if(progress[1].equalsIgnoreCase("ERROR")){
				progressBar.setProgress(Integer.valueOf(progress[0]));
				progressBar.setVisibility(View.INVISIBLE);
				textView1.setText(progress[2]);
			}

		}

		@Override
		protected void onPostExecute(Long nl) {

		}



	}

	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	public void onStop() {
		super.onStop();
	}

}
