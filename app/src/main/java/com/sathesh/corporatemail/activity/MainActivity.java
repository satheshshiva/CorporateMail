package com.sathesh.corporatemail.activity;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.sathesh.corporatemail.R;
import com.sathesh.corporatemail.animation.ApplyAnimation;
import com.sathesh.corporatemail.application.MailApplication;
import com.sathesh.corporatemail.application.MyApplication;
import com.sathesh.corporatemail.constants.Constants;
import com.sathesh.corporatemail.customui.Notifications;

import java.util.HashMap;
import java.util.Map;

import microsoft.exchange.webservices.data.core.service.item.Item;
import microsoft.exchange.webservices.data.search.FindItemsResults;

public class MainActivity extends Activity implements Constants{

	TextView textView1 ;
	public FindItemsResults<Item> findResults ;
	MyApplication application;
	Tracker mTracker;

	int no=0;
	ProgressBar progressBar;
	private String SignedInAccUser=USERNAME_NULL,SignedInAccPassword=PASSWORD_NULL ;
	private Activity activity;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);

		activity=this;
		application = (MyApplication) getApplication();
		mTracker = application.getDefaultTracker();
		mTracker.setScreenName("MainActivity");

	}

	@Override
	public void onStart() {
		super.onStart();
		(new CheckLogin()).execute();
	}
	 @Override
	  public void onStop() {
	    super.onStop();
	  }

	/** ON RESUME **/
	@Override
	public void onResume() {
		super.onResume();
		try {
			mTracker.send(new HitBuilders.ScreenViewBuilder().build());
		} catch (Exception e) {
			e.printStackTrace();
		}
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
					Intent intent = new Intent(MainActivity.this, MailListViewActivity.class);
                    intent.putExtra(MailListViewActivity.MAIL_TYPE_EXTRA, MailType.INBOX);
                    intent.putExtra(MailListViewActivity.FOLDER_ID_EXTRA, "");
                    intent.putExtra(MailListViewActivity.FOLDER_NAME_EXTRA, getString(R.string.drawer_menu_inbox));

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
