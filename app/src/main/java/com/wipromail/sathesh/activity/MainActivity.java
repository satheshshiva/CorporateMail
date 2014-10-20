package com.wipromail.sathesh.activity;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.analytics.tracking.android.EasyTracker;
import com.wipromail.sathesh.R;
import com.wipromail.sathesh.animation.ApplyAnimation;
import com.wipromail.sathesh.application.MailApplication;
import com.wipromail.sathesh.constants.Constants;
import com.wipromail.sathesh.customui.Notifications;
import com.wipromail.sathesh.service.data.FindItemsResults;
import com.wipromail.sathesh.service.data.Item;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends Activity implements Constants{

	TextView textView1 ;
	public FindItemsResults<Item> findResults ;


	int no=0;
	ProgressBar progressBar;
	private String SignedInAccUser=USERNAME_NULL,SignedInAccPassword=PASSWORD_NULL ;
	private Activity activity;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);

		activity=this;

	}

	@Override
	public void onStart() {
		super.onStart();
		(new CheckLogin()).execute();
		EasyTracker.getInstance().activityStart(this); // Add this method.


	}
	 @Override
	  public void onStop() {
	    super.onStop();
	    EasyTracker.getInstance().activityStop(this); 
	  }
	
	private class CheckLogin extends AsyncTask<Void, Void, Void>{

		@Override
		protected Void doInBackground(Void... paramArrayOfParams) {
			try {

				Map<String, String> storedCredentials = new HashMap<String, String>();
				
					storedCredentials = MailApplication.getStoredCredentials(MainActivity.this);
			

				SignedInAccUser = storedCredentials.get("signedInAccUser");
				SignedInAccPassword =  storedCredentials.get("signedInAccPassword");

				//Log.e(TAG,"MainActivity -> throwing exception for test purpose");
				//System.out.println(1/0);
				
				if (null != SignedInAccUser && !(SignedInAccUser.equals(USERNAME_NULL)) && !(SignedInAccPassword.equals(PASSWORD_NULL))){
					//user signed in
					Intent intent = new Intent(MainActivity.this, HomePageActivity.class);
					startActivity(intent);

					ApplyAnimation.setMainActivitySignedInAnim(MainActivity.this);

					

				}
				else{
					//user not signed in
					Thread.sleep(SPLASH_NOT_SIGNED_IN_TIME);
					Intent intent = new Intent(MainActivity.this, LoginPageActivity.class);
					startActivity(intent);
					ApplyAnimation.setMainActivityNotSignedSignedInAnim(MainActivity.this);

				}
				MainActivity.this.finish();
			}
			//catch (final Exception e) {
				catch (final Exception e) {
				MainActivity.this.runOnUiThread(new Runnable() {
					public void run() {
						Notifications.showAlert( MainActivity.this, "Error Occured!\n\nDetails: " +e.getMessage());

					}});
				e.printStackTrace();

			}
			return null;
		}



	}

}
