package com.wipromail.sathesh.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.google.analytics.tracking.android.EasyTracker;
import com.wipromail.sathesh.R;
import com.wipromail.sathesh.adapter.GeneralPreferenceAdapter;
import com.wipromail.sathesh.application.MailApplication;
import com.wipromail.sathesh.constants.Constants;
import com.wipromail.sathesh.customui.Notifications;
import com.wipromail.sathesh.tools.CacheClear;
import com.wipromail.sathesh.ui.ChangePasswordDialog;

import java.util.Calendar;
//import com.wipromail.sathesh.ui.SignOutAlertDialog;

/** For the general preferncescreen, Use the adapter {@link GeneralPreferenceAdapter}
 * @author Sathesh
 *
 */
public class PreferencesActivity extends SherlockPreferenceActivity implements Constants ,OnSharedPreferenceChangeListener{

    public final static String KEY_WEBMAIL_SERVER="webmail_server";
    public final static String KEY_COMPOSE_SIGNATURE_ENABLE="compose_signature_enable";
    public final static String KEY_NOTIFICATION_ENABLE="notification_enable";
    public final static String KEY_NOTIFICATION_TYPE="notification_type";
    public final static String KEY_PULL_FREQUENCY="notification_pull_frequency";
    public final static String KEY_AUTO_UPDATE_NOTIFY="auto_update_notify_enable";
    public final static String KEY_CHANGE_PASSWORD="change_password";
    public final static String KEY_CLEAR_CACHE="clear_cache";
    public final static String KEY_SIGN_OUT="sign_out";

    //The following are not in the preferences page
    public final static String COMPOSE_SIGNATURE="compose_signature";

    private static ListPreference webMailServer;
    private static CheckBoxPreference notificationEnable,composeSignatureEnable,autoUpdateNotifyEnable;
    private static Preference changePassword, signOut, clearCache;
    private static ListPreference subscrpyionType;
    private static ListPreference pullDuration;
    public static Calendar timeOfLastCustomURL;
    private PreferencesActivity activity;
    private static Context context;

    private GeneralPreferenceAdapter sharedPref = new GeneralPreferenceAdapter();


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activity= this;
        context = this;
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.mainpreferences);
        webMailServer=(ListPreference)getPreferenceScreen().findPreference(
                KEY_WEBMAIL_SERVER);
        composeSignatureEnable=(CheckBoxPreference)getPreferenceScreen().findPreference(
                KEY_COMPOSE_SIGNATURE_ENABLE);
        notificationEnable=(CheckBoxPreference)getPreferenceScreen().findPreference(
                KEY_NOTIFICATION_ENABLE);
        subscrpyionType=(ListPreference)getPreferenceScreen().findPreference(
                KEY_NOTIFICATION_TYPE);
        pullDuration=(ListPreference)getPreferenceScreen().findPreference(
                KEY_PULL_FREQUENCY);
        autoUpdateNotifyEnable = (CheckBoxPreference)getPreferenceScreen().findPreference(
                KEY_AUTO_UPDATE_NOTIFY);
        autoUpdateNotifyEnable = (CheckBoxPreference)getPreferenceScreen().findPreference(
                KEY_AUTO_UPDATE_NOTIFY);
        changePassword = (Preference)getPreferenceScreen().findPreference(
                KEY_CHANGE_PASSWORD);
        clearCache = (Preference)getPreferenceScreen().findPreference(
                KEY_CLEAR_CACHE);
		/*signOut = (Preference)getPreferenceScreen().findPreference(
				KEY_SIGN_OUT);
*/
        //Will call the function to initialize the summary. The summary will change when the option changes
        oncreateWebmailServerFromSummary();
        updateComposeSignatureEnablePrefernceSummary();
        updateNotificationEnablePrefernceSummary();
        updateSubscriptionTypePrefernceSummary();
        updatePullDurationPrefernceSummary();
        updateAutoUpdateNotifySummary();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initializeEventsForAccount();

    }


    /** This functions initializes the click event for the Preferences other than checkboxpreference, list box preference etc.,
     *
     */
    private void initializeEventsForAccount() {

        //initializing the click event for Change Password button
        changePassword.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                ChangePasswordDialog.showAlertdialog(activity, context);
                return true;
            }
        });

        //initializing the click event for Clear Cache button
        clearCache.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                CacheClear.clearFullCacheAndDbDir(activity);
                Notifications.showToast(activity, activity.getString(R.string.preferences_cache_cleared_msg) , Toast.LENGTH_SHORT);
                return true;
            }
        });

        //initializing the click event for Sign Out  button
		/*signOut.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
				SignOutAlertDialog.showAlertdialog(activity, context);
				return true;
			}
		});*/

    }


    private void updatePullDurationPrefernceSummary() {
        // TODO Auto-generated method stub
        if(sharedPref.getNotificationPullFrequency(this)==30000){
            pullDuration.setSummary(getString(R.string.preference_pull_ferequency_30s_summary));
        }
        else if(sharedPref.getNotificationPullFrequency(this)==900000){
            pullDuration.setSummary(getString(R.string.preference_pull_ferequency_15m_summary));
        }
        else if(sharedPref.getNotificationPullFrequency(this)==3600000){
            pullDuration.setSummary(getString(R.string.preference_pull_ferequency_1h_summary));
        }
        else if(sharedPref.getNotificationPullFrequency(this)==14400000){
            pullDuration.setSummary(getString(R.string.preference_pull_ferequency_4h_summary));
        }
        else if(sharedPref.getNotificationPullFrequency(this)==43200000){
            pullDuration.setSummary(getString(R.string.preference_pull_ferequency_12h_summary));
        }
    }

    private void updateSubscriptionTypePrefernceSummary() {
        // TODO Auto-generated method stub
        subscrpyionType.setSummary(getString(R.string.preference_notification_type_summary));
    }

    private void updateNotificationEnablePrefernceSummary() {
        // TODO Auto-generated method stub
        if(sharedPref.isNotificationEnabled(this)){
            notificationEnable.setSummary(getString(R.string.preference_notification_enable_ON_summary));
        }else{
            notificationEnable.setSummary(getString(R.string.preference_notification_enable_OFF_summary));
        }
    }

    private void updateAutoUpdateNotifySummary() {
        // TODO Auto-generated method stub
        if(sharedPref.isAutoUdpateNotifyEnabled(this)){
            autoUpdateNotifyEnable.setSummary(getString(R.string.preferences_updater_ON_autoupdate_notify_summary));
        }else{
            autoUpdateNotifyEnable.setSummary(getString(R.string.preferences_updater_OFF_autoupdate_notify_summary));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);



        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if(item!=null && item.getItemId()==android.R.id.home){
            finish();
        }

        return super.onOptionsItemSelected(item);
    }
    private void updateComposeSignatureEnablePrefernceSummary() {
        // TODO Auto-generated method stub
        if(sharedPref.isComposeSignatureEnabled(this)){
            composeSignatureEnable.setSummary(getString(R.string.preference_compose_signature_enable_ON_summary));
        }else{
            composeSignatureEnable.setSummary(getString(R.string.preference_compose_signature_enable_OFF_summary));
        }


    }
    @Override
    protected void onResume() {
        super.onResume();

        // Set up a listener whenever a key changes
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Unregister the listener whenever a key changes
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences pref, String key) {
        // TODO Auto-generated method stub
        if(key.equals(KEY_WEBMAIL_SERVER)){
            try {
                Calendar now = Calendar.getInstance();
                if( !(PreferencesActivity.timeOfLastCustomURL!=null && ((now.getTimeInMillis() - PreferencesActivity.timeOfLastCustomURL.getTimeInMillis() ) < 2000))){
                    //when the custom url is saved, this event will be invoked again. so to preven that, the last custom url save shld be > than 2secs.
                    updateWebmailServerPrefernceSummary();
                }
                else{
                    Log.i(TAG, "PreferenceActivity -> Last prefernce save was save less than 2 secs. So url changing fn not invoked");
                }


            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        else if(key.equals(KEY_COMPOSE_SIGNATURE_ENABLE)){
            updateComposeSignatureEnablePrefernceSummary();
        }
        else if(key.equals(KEY_NOTIFICATION_ENABLE)){

            if(sharedPref.isNotificationEnabled(this)){
                MailApplication.startMNSService(this);
            }
            else{
                MailApplication.stopMNSService(this);
            }
            updateNotificationEnablePrefernceSummary();

        }
        else if(key.equals(KEY_NOTIFICATION_TYPE)){
            updateSubscriptionTypePrefernceSummary();
        }
        else if(key.equals(KEY_PULL_FREQUENCY)){
            try {
                MailApplication.onChangeMNSResetPullDuration();
                updatePullDurationPrefernceSummary();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        else if(key.equals(KEY_AUTO_UPDATE_NOTIFY)){
            updateAutoUpdateNotifySummary();
        }


    }

    /** called on oncreate. reads the webmail URL from storage
     *
     */
    private void oncreateWebmailServerFromSummary() {
        // TODO Auto-generated method stub
        webMailServer.setSummary(sharedPref.getServerURL(context));
    }

    /** called everytime when this preference is changed
     *
     */
    private void updateWebmailServerPrefernceSummary() {
        // TODO Auto-generated method stub

        String[] serversText = getResources().getStringArray(R.array.preferences_serverlist_text);
        // for the primary and secondary URLs
        for(int i=0; i<serversText.length; i++){
            if(i!=2){	//i=2 means the user clicked Custom URL 3rd option. It is handled in WebmailURLPreference.java
                if(serversText[i].equalsIgnoreCase(webMailServer.getEntry().toString())){
                    webMailServer.setSummary(getResources().getStringArray(R.array.preferences_serverlist_values)[i]);
                }
            }
        }

    }

    public void showCustomServerURLDialog(){

        //build the dialog for change password using the xml layout
        LayoutInflater factory = LayoutInflater.from(context);
        final View textEntryView = factory.inflate(R.layout.dialog_webmail_url, null);

        //update the passowrd field with the old password
        final EditText changeURLEdit = (EditText)textEntryView.findViewById(R.id.url_edit);

        final Context _context = context;

        changeURLEdit.setText(sharedPref.getServerURL(context));

        //build the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.dialog_webmail_url_title)
                .setView(textEntryView)
                .setPositiveButton(R.string.alertdialog_save_lbl, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String _url = changeURLEdit.getText().toString();
                        PreferencesActivity.timeOfLastCustomURL = Calendar.getInstance();		//will prevent from invoking the code inside OnSharedPreferenceChange event again, which will be invoked on next line while saving the shared prefs
                        GeneralPreferenceAdapter.storeServerURL(_context, _url);
                        webMailServer.setSummary(_url);
                    }
                })
                .setNegativeButton(R.string.alertdialog_negative_lbl, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.cancel();
                    }
                })
                .create();
        AlertDialog webmailURL = builder.create();
        webmailURL.show();

    }
    //Google Analytics
    @Override
    public void onStart() {
        super.onStart();
        EasyTracker.getInstance().activityStart(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EasyTracker.getInstance().activityStop(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}
