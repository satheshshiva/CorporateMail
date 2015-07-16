package com.wipromail.sathesh.activity;

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
import android.widget.Spinner;
import android.widget.TextView;

import com.wipromail.sathesh.R;
import com.wipromail.sathesh.adapter.GeneralPreferenceAdapter;
import com.wipromail.sathesh.animation.ApplyAnimation;
import com.wipromail.sathesh.application.MailApplication;
import com.wipromail.sathesh.application.MyActivity;
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
import com.wipromail.sathesh.threads.ui.GetMoreFoldersThread;
import com.wipromail.sathesh.ui.listeners.LoginPageListener;

import java.net.URISyntaxException;

public class LoginPageActivity extends MyActivity implements Constants {

    private String username=USERNAME_NULL, password=PASSWORD_NULL;
    private Intent intent;
    //	private  boolean customTitleSupported = false;
    private Activity activity;
    private Context context;
    private EditText login_username;
    private EditText login_passwd;
    private Spinner serverSpinner;
    private GeneralPreferenceAdapter sharedPref = new GeneralPreferenceAdapter();
    private TextView urlDisp;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = this;
        context=this;
        // requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.activity_login_page);

        //Initialize toolbar
        MailApplication.toolbarInitialize(this);

        login_username = (EditText)findViewById(R.id.login_username);
        login_passwd = (EditText)findViewById(R.id.login_passwd);
        serverSpinner = (Spinner)findViewById(R.id.serverSpinner);
        serverSpinner.setOnItemSelectedListener(new LoginPageListener(activity, this));
        urlDisp = (TextView) findViewById(R.id.serverURLDisp);

        //Reset the URL preference to default one. Since we have to manually update the spinner if it ws changed
        sharedPref.storeServerURL(context, MailApplication.getDefaultWebmailURL(context));

        //show the current url in display
        urlDisp.setText(sharedPref.getServerURL(context));

        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        //testingdb(activity);
		/* if(customTitleSupported)
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, CustomTitleBar.getInboxTitleBarLayout());*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        MenuItem menuItem;

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
        username = login_username.getText().toString();
        password = login_passwd.getText().toString();

        //validate user name
        if(username==null || username.equalsIgnoreCase("")) {
            //if user name is null then shake the edit text
            login_username.startAnimation(ApplyAnimation.getLoginPageTextViewShakeAnim(activity));
            return;
        }

        //validate password
        if(password==null || password.equalsIgnoreCase("")) {
            //if password is null then shake the edit text
            login_passwd.startAnimation(ApplyAnimation.getLoginPageTextViewShakeAnim(activity));
            return;
        }

        // for office 365 URL append "@wipro.com" to the username
        if(serverSpinner.getSelectedItemPosition() == MyPreferencesActivity.OFFICE365_URL_POSITION){
            username+=getString(R.string.webmail_365_username_append);
        }
        //Log.d(TAG, "USERNAME " + username + " PASSWORD " + password);
        //Validation successful. Login
        new Login().execute(username, password);
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
                Log.e(TAG, "Exception occured on preexecute");
            }
        }

        @Override
        protected Long doInBackground(String... paramArrayOfParams) {

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
                    saveCredentials(username, password);
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
                Thread t = new GetMoreFoldersThread(activity, new Handler());
                t.start();

                LoginPageActivity.this.finish();
            }

            else if(progress[1].equalsIgnoreCase("ERROR")){
                dialog.dismiss();
                Notifications.showAlert(LoginPageActivity.this, progress[2] );
            }
        }

        private void saveCredentials(String username, String password) throws Exception {
            SharedPreferencesAdapter.storeCredentials(LoginPageActivity.this.getApplicationContext(), username, password);
        }

        @Override
        protected void onPostExecute(Long nl) {

        }
    }   //end async task



    /** Private method which updates the Server Spinner selection based on the stored preference value
     * For Custom URL it wont switch to Custom URL because when Custom URL option is selcted it wont trigger the dialog
     *
     */
    private void updateServerSpinnerSelection() {
        String[] serversText = getResources().getStringArray(R.array.preferences_serverlist_values);
        String storedServerURL=sharedPref.getServerURL(activity);
        // for the primary and secondary URLs
        for(int i=0; i<serversText.length; i++){
            if(serversText[i].equalsIgnoreCase(storedServerURL)){	//i=3 means the user clicked Custom URL 4th option. It is handled in WebmailURLPreference.java
                serverSpinner.setSelection(i);
            }
        }
    }
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

    /** GETTER AND SETTER **/
    public TextView getUrlDisp() {
        return urlDisp;
    }

    public void setUrlDisp(TextView urlDisp) {
        this.urlDisp = urlDisp;
    }

}
