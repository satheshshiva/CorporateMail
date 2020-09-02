package com.sathesh.corporatemail.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.sathesh.corporatemail.R;
import com.sathesh.corporatemail.adapter.GeneralPreferenceAdapter;
import com.sathesh.corporatemail.animation.ApplyAnimation;
import com.sathesh.corporatemail.application.MailApplication;
import com.sathesh.corporatemail.application.MyActivity;
import com.sathesh.corporatemail.application.MyApplication;
import com.sathesh.corporatemail.application.SharedPreferencesAdapter;
import com.sathesh.corporatemail.constants.Constants;
import com.sathesh.corporatemail.customexceptions.NoInternetConnectionException;
import com.sathesh.corporatemail.customui.Notifications;
import com.sathesh.corporatemail.ews.EWSConnection;
import com.sathesh.corporatemail.ews.NetworkCall;

import java.net.URISyntaxException;

import microsoft.exchange.webservices.data.core.ExchangeService;
import microsoft.exchange.webservices.data.core.exception.http.HttpErrorException;
import microsoft.exchange.webservices.data.core.exception.service.remote.ServiceRequestException;
import microsoft.exchange.webservices.data.core.service.folder.Folder;
import microsoft.exchange.webservices.data.misc.NameResolution;
import microsoft.exchange.webservices.data.misc.NameResolutionCollection;
import microsoft.exchange.webservices.data.search.FindFoldersResults;

public class LoginPageActivity extends MyActivity implements Constants {

    private String username=USERNAME_NULL, password=PASSWORD_NULL, loginUrl;
    private Intent intent;
    private Activity activity;
    private Context context;
    private EditText login_url, login_username, login_passwd;
    private TextView tncTextView;
    MyApplication application;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = this;
        context=this;
        // requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.activity_login_page);

        application = (MyApplication) getApplication();

        //Initialize toolbar
        MailApplication.toolbarInitialize(this);

        login_url = (EditText) findViewById(R.id.login_url);
        login_username = (EditText)findViewById(R.id.login_username);
        login_passwd = (EditText)findViewById(R.id.login_passwd);

        tncTextView = (TextView) findViewById(R.id.login_tnc);

        if (getSupportActionBar() != null ) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }

        tncTextView.setOnClickListener((v)->{
            Intent intent = new Intent(this, TncActivity.class);
            startActivity(intent);
        });
        //testingdb(activity);
		/* if(customTitleSupported)
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, CustomTitleBar.getInboxTitleBarLayout());*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

     /*   menuItem=menu.add(ACTIONBAR_ABOUT)
                .setIcon(OptionsUIContent.getAboutDarkIcon());
        MenuItemCompat.setShowAsAction(menuItem, MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
*/
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if(item!=null && item.getTitle().equals(ACTIONBAR_LOGIN)){
            loginButtonClicked();
        }
       /* else if(item!=null && item.getTitle().equals(ACTIONBAR_ABOUT)){
            Intent intent = new Intent(this, AboutActivity.class);
            startActivity(intent);
        }*/

        return super.onOptionsItemSelected(item);
    }


    /** Public method for the Button Onclick from layout
     *
     * @param view
     */
    public void loginButtonClicked(View view){
        loginButtonClicked();
    }
    /**Called when the login button is clicked
     *
     */
    private void loginButtonClicked() {
        loginUrl = login_url.getText().toString();
        username = login_username.getText().toString();
        password = login_passwd.getText().toString();

        //validate user name
        if(loginUrl.equalsIgnoreCase("")) {
            //if user name is null then shake the edit text
            login_url.startAnimation(ApplyAnimation.getLoginPageTextViewShakeAnim((MyActivity) activity));
            return;
        }


        //validate user name
        if(username.equalsIgnoreCase("")) {
            //if user name is null then shake the edit text
            login_username.startAnimation(ApplyAnimation.getLoginPageTextViewShakeAnim((MyActivity) activity));
            return;
        }

        //validate password
        if(password.equalsIgnoreCase("")) {
            //if password is null then shake the edit text
            login_passwd.startAnimation(ApplyAnimation.getLoginPageTextViewShakeAnim((MyActivity) activity));
            return;
        }

        //Log.d(TAG, "USERNAME " + username + " PASSWORD " + password);
        //Validation successful. Login
        new Login().execute(loginUrl, username, password);
    }

    private class Login extends AsyncTask<String, String, Long>{

        private ExchangeService service;
        ProgressDialog dialog;

        @Override
        protected void onPreExecute() {

            try {
                dialog = ProgressDialog.show(LoginPageActivity.this, "Logging in",
                        "", true);

            } catch (Exception e) {
                e.printStackTrace();
                Log.e(LOG_TAG, "Exception occured on preexecute");
            }
        }

        @Override
        protected Long doInBackground(String... paramArrayOfParams) {

            try {

                publishProgress("2" ,"RUNNING", LOGGING_IN_PROG1_TEXT);

                service = EWSConnection.getNewService(activity, paramArrayOfParams[0], paramArrayOfParams[1], paramArrayOfParams[2]);

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
                    Log.i(LOG_TAG, "Count======"+folder.getChildFolderCount());
                    Log.i(LOG_TAG, "Name======="+folder.getDisplayName());
                }

                publishProgress("10" ,"COMPLETED", "");

            }
            catch(NullPointerException e){
                publishProgress("0" ,"ERROR", "Check your Internet Connection\n\nDetails:NPE"  );
            }
            catch (URISyntaxException e) {
                publishProgress("0" ,"ERROR", MALFORMED_WEBMAIL_URL_TEXT);
            }
            catch(HttpErrorException  | ServiceRequestException e){
                if(e.getMessage().toLowerCase().contains("Unauthorized".toLowerCase())){
                    publishProgress("0" ,"ERROR", AUTHENICATION_FAILED_TEXT);
                }
                else
                {
                    publishProgress("0" ,"ERROR", "Error Occured!\n\nDetails: " + e.getMessage());
                }
            }

            catch (Exception e) {
                publishProgress("0" ,"ERROR", "Error Occured!\n\nDetails: " +e.getMessage());
            }

            return 0l;

        }

        private void retrieveAndStoreUserDetails(ExchangeService service, String username) throws NoInternetConnectionException, Exception {

            //EWS call
            NameResolutionCollection nameResolutions = MailApplication.resolveName(service, trimUserName(username), false);
            String email;

            for(NameResolution nameResolution : nameResolutions)
            {
                if( nameResolution!= null && nameResolution.getMailbox() != null){
                    //it might return more values.. so check for the current user name which is signed in
                    if(nameResolution.getMailbox().getName().equalsIgnoreCase(trimUserName(username))){

                        email = nameResolution.getMailbox().getAddress();
                        //have to get contact details
                        //EWS call
                        nameResolutions = MailApplication.resolveName(service, email, true);

                        //the returned should be 1 since we gave the full email address
                        if(nameResolutions.getCount() == 1){

                            nameResolution = nameResolutions.nameResolutionCollection(0);
                            if(nameResolution!= null ) {
                                //storing the Display name locally
                                if (nameResolution.getContact() != null){
                                    SharedPreferencesAdapter.storeUserDetailDisplayName(activity, nameResolution.getContact().getDisplayName());
                                }
                                //storing the Company Name locally
                                if ( nameResolution.getMailbox() != null) {
                                    SharedPreferencesAdapter.storeUserDetailEmail(activity, nameResolution.getMailbox().getAddress());
                                    SharedPreferencesAdapter.storeUserDetailsCompanyName(activity, nameResolution.getContact().getCompanyName());
                                }

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
                // successful login
                try {
                    dialog.dismiss();	//gives this exception "View not attached to window manager" on a mobile
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
                try {
                    saveDetails(loginUrl, username, password);
                    //	MailApplication.onFirstTimeSuccessfulLogin(activity);
                } catch (Exception e) {
                    e.printStackTrace();
                    onProgressUpdate("0" ,"ERROR", "Error Occured!\n\nDetails: " + e.getMessage());
                }

                //start the mail list view activity
                intent = new Intent(LoginPageActivity.this, MailListViewActivity.class);
                intent.putExtra(MailListViewActivity.MAIL_TYPE_EXTRA, MailType.INBOX);
                intent.putExtra(MailListViewActivity.FOLDER_ID_EXTRA, "");
                intent.putExtra(MailListViewActivity.FOLDER_NAME_EXTRA, getString(R.string.drawer_menu_inbox));

                startActivity(intent);

                // starts a seperate thread for storing the all folders table
                MailApplication.startGetMoreFoldersThread((MyActivity)activity, new Handler());

                LoginPageActivity.this.finish();
            }

            else if(progress[1].equalsIgnoreCase("ERROR")){
                dialog.dismiss();
                Notifications.showAlert(LoginPageActivity.this, progress[2] );
            }
        }

        private void saveDetails(String url, String username, String password) throws Exception {
            GeneralPreferenceAdapter.storeServerURL(context, url);
            SharedPreferencesAdapter.storeCredentials(LoginPageActivity.this.getApplicationContext(), username, password);
        }

        @Override
        protected void onPostExecute(Long nl) {

        }
    }   //end async task


    private String trimUserName(String username) {
        if(username.contains("@")){
            return username.substring(0, username.indexOf("@"));
        }
        return username;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    /** ON RESUME **/
    @Override
    public void onResume() {
        super.onResume();
    }

}
