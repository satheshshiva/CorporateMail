package com.wipromail.sathesh.activity;
import java.net.URISyntaxException;

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

import com.google.analytics.tracking.android.EasyTracker;
import com.wipromail.sathesh.R;
import com.wipromail.sathesh.R.layout;
import com.wipromail.sathesh.R.menu;
import com.wipromail.sathesh.constants.Constants;
import com.wipromail.sathesh.ews.EWSConnection;
import com.wipromail.sathesh.service.data.EmailMessage;
import com.wipromail.sathesh.service.data.ExchangeService;
import com.wipromail.sathesh.service.data.FindItemsResults;
import com.wipromail.sathesh.service.data.HttpErrorException;
import com.wipromail.sathesh.service.data.Item;
import com.wipromail.sathesh.service.data.MessageBody;
import com.wipromail.sathesh.threads.Inbox;

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
		Log.d(TAG, "Username : " + credStorage.getString(CRED_PREFS_USERNAME, ""));
		Log.d(TAG, "Password : " + credStorage.getString(CRED_PREFS_PASSWORD,""));
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

			Log.d(TAG, "Starting thread");

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
					// TODO Auto-generated catch block
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
			// TODO Auto-generated method stub

			try {
				progressBar.setVisibility(View.VISIBLE);

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.e(TAG, "Exception occured on preexecute");
			}

		}

		@Override
		protected Long doInBackground(Void... paramArrayOfParams) {
			// TODO Auto-generated method stub

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
				// TODO Auto-generated catch block
				publishProgress("0" ,"ERROR", "Malformed Webmail URL");
			}
			catch(HttpErrorException e){
				if(e.getMessage().toLowerCase().contains("Unauthorized".toLowerCase())){
					publishProgress("0" ,"ERROR", "Authentication Failed!\nDetails: " + e.getMessage());
				}
				else
				{
					publishProgress("0" ,"ERROR", "Error Occured!\nDetails: " + e.getMessage());
				}
			}

			catch (Exception e) {
				// TODO Auto-generated catch block
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



	//Google Analytics
	@Override
	public void onStart() {
		super.onStart();
		EasyTracker.getInstance().activityStart(this); // Add this method.
	}

	@Override
	public void onStop() {
		super.onStop();
		EasyTracker.getInstance().activityStop(this); // Add this method.
	}

}
