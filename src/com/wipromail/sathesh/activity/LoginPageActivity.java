package com.wipromail.sathesh.activity;

import java.net.URISyntaxException;
import java.util.Date;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.google.analytics.tracking.android.EasyTracker;
import com.wipromail.sathesh.R;
import com.wipromail.sathesh.application.MailApplication;
import com.wipromail.sathesh.application.SharedPreferencesAdapter;
import com.wipromail.sathesh.constants.Constants;
import com.wipromail.sathesh.customexceptions.NoInternetConnectionException;
import com.wipromail.sathesh.customui.Notifications;
import com.wipromail.sathesh.ews.EWSConnection;
import com.wipromail.sathesh.ews.NetworkCall;
import com.wipromail.sathesh.service.data.ExchangeService;
import com.wipromail.sathesh.service.data.FindFoldersResults;
import com.wipromail.sathesh.service.data.Folder;
import com.wipromail.sathesh.service.data.HttpErrorException;
import com.wipromail.sathesh.service.data.NameResolution;
import com.wipromail.sathesh.service.data.NameResolutionCollection;
import com.wipromail.sathesh.sqlite.db.dao.CachedMailHeaderDAO;
import com.wipromail.sathesh.sqlite.db.pojo.vo.CachedMailHeaderVO;
import com.wipromail.sathesh.ui.OptionsUIContent;

public class LoginPageActivity extends SherlockActivity implements Constants {

	private String username=USERNAME_NULL, password=PASSWORD_NULL;
	private Intent intent;
	//	private  boolean customTitleSupported = false;
	private Activity activity;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		activity = this;
		// requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.activity_login_page);
		//testingdb(activity);
		/* if(customTitleSupported)
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, CustomTitleBar.getInboxTitleBarLayout());*/
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		//Always Visible menu
		menu.add(ACTIONBAR_SETTINGS)
		.setIcon(OptionsUIContent.getSettingsIcon())
		.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS|MenuItem.SHOW_AS_ACTION_WITH_TEXT);

		menu.add(ACTIONBAR_ABOUT)
		.setIcon(OptionsUIContent.getAboutIcon())
		.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS|MenuItem.SHOW_AS_ACTION_WITH_TEXT);



		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{

		if(item!=null && item.getTitle().equals(ACTIONBAR_SETTINGS)){
			Intent intent = new Intent(this, PreferencesActivity.class);
			startActivity(intent);
		}
		else if(item!=null && item.getTitle().equals(ACTIONBAR_ABOUT)){
			Intent intent = new Intent(this, AboutActivity.class);
			startActivity(intent);
		}

		return super.onOptionsItemSelected(item);
	}


	public void onClickLogin(View view) {
		EditText login_username = (EditText)findViewById(R.id.login_username);
		username = login_username.getText().toString();

		EditText login_passwd = (EditText)findViewById(R.id.login_passwd);
		password = login_passwd.getText().toString();

		new Login().execute(username, password);

	}
	
	public void textView5OnClick(View view) {
		EditText login_username = (EditText)findViewById(R.id.login_username);
		login_username.requestFocus();
	}

	public void textView2OnClick(View view) {
		EditText login_passwd = (EditText)findViewById(R.id.login_passwd);
		login_passwd.requestFocus();
	}
	
	private class Login extends AsyncTask<String, String, Long>{

		private ExchangeService service;
		ProgressDialog dialog;

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub

			try {
				dialog = ProgressDialog.show(LoginPageActivity.this, "Logging in", 
						"", true);

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.e(TAG, "Exception occured on preexecute");
			}

		}

		@Override
		protected Long doInBackground(String... paramArrayOfParams) {
			// TODO Auto-generated method stub

			try {

				publishProgress("2" ,"RUNNING", LOGGING_IN_PROG1_TEXT);

				service = EWSConnection.getService(activity, paramArrayOfParams[0], paramArrayOfParams[1]);

				//get and store userd detials
				//EWS call
				publishProgress("3" ,"RUNNING", LOGGING_IN_PROG2_TEXT);
				try{
					retrieveAndStoreUserDetails(service, paramArrayOfParams[0]);
				}catch(Exception e){e.printStackTrace();}

				//EWScall
				FindFoldersResults findResults = NetworkCall.getInboxFolders(service);
				publishProgress("6" ,"RUNNING", LOGGING_IN_PROG3_TEXT);

				for(Folder folder : findResults.getFolders())
				{     
					Log.i(TAG, "Count======"+folder.getChildFolderCount());                                         		
					Log.i(TAG, "Name======="+folder.getDisplayName());
				}

				publishProgress("10" ,"COMPLETED", "");

			}
			catch(NullPointerException e){
					publishProgress("0" ,"ERROR", "Check your Internet Connection\n\nDetails:NPE"  );
			}
			catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				publishProgress("0" ,"ERROR", MALFORMED_WEBMAIL_URL_TEXT);
			}
			catch(HttpErrorException e){
				if(e.getMessage().toLowerCase().contains("Unauthorized".toLowerCase())){
					publishProgress("0" ,"ERROR", AUTHENICATION_FAILED_TEXT);
				}
				else
				{
					publishProgress("0" ,"ERROR", "Error Occured!\n\nDetails: " + e.getMessage());
				}
			}

			catch (Exception e) {
				// TODO Auto-generated catch block
				publishProgress("0" ,"ERROR", "Error Occured!\n\nDetails: " +e.getMessage());
			}

			return 0l;

		}

		private void retrieveAndStoreUserDetails(ExchangeService service, String username) throws NoInternetConnectionException, Exception {
			// TODO Auto-generated method stub

			//EWS call
			NameResolutionCollection nameResolutions = MailApplication.resolveName(service, username, false);
			String email;

			for(NameResolution nameResolution : nameResolutions)
			{
				if( nameResolution!= null && nameResolution.getMailbox() != null){
					//it might return more values.. so check for the current user name which is signed in
					if(nameResolution.getMailbox().getName().equalsIgnoreCase(username)){

						email = nameResolution.getMailbox().getAddress();
						//have to get contact details
						//EWS call
						nameResolutions = MailApplication.resolveName(service, email, true);

						//the returned should be 1 since we gave the full email address
						if(nameResolutions.getCount() == 1){

							nameResolution = nameResolutions.nameResolutionCollection(0);
							if( nameResolution!= null && nameResolution.getContact() != null ){
								SharedPreferencesAdapter.storeUserDetailDisplayName(activity, nameResolution.getContact().getDisplayName());
							}
							if( nameResolution!= null && nameResolution.getMailbox() != null ){
								SharedPreferencesAdapter.storeUserDetailEmail(activity, nameResolution.getMailbox().getAddress());
							}
						}

						//since we are done already and no need to process the rest of the username looking similarly
						break;
					}
				}

			}
		}

		@Override
		protected void onProgressUpdate(String... progress) {

			if (progress[1].equalsIgnoreCase("RUNNING")){
				dialog.setMessage(progress[2]);
			}

			else if (progress[1].equalsIgnoreCase("COMPLETED")){
				try {
					dialog.dismiss();	//gives this exception "View not attached to window manager" on a mobile
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				try {
					saveCredentials(username, password);
				//	MailApplication.onFirstTimeSuccessfulLogin(activity);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					onProgressUpdate("0" ,"ERROR", "Error Occured!\n\nDetails: " + e.getMessage());
				}

				intent = new Intent(LoginPageActivity.this, HomePageActivity.class);
				startActivity(intent);

				LoginPageActivity.this.finish();
			}

			else if(progress[1].equalsIgnoreCase("ERROR")){
				dialog.dismiss();
				Notifications.showAlert(LoginPageActivity.this, progress[2] );
			}
		}

		private void saveCredentials(String username, String password) throws Exception {
			// TODO Auto-generated method stub
			SharedPreferencesAdapter.storeCredentials(LoginPageActivity.this.getApplicationContext(), username, password);
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
