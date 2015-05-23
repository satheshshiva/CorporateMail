package com.wipromail.sathesh.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.widget.Toast;

import com.wipromail.sathesh.R;
import com.wipromail.sathesh.adapter.GeneralPreferenceAdapter;
import com.wipromail.sathesh.application.MailApplication;
import com.wipromail.sathesh.application.MyActivity;
import com.wipromail.sathesh.constants.Constants;
import com.wipromail.sathesh.customui.Notifications;
import com.wipromail.sathesh.tools.CacheClear;
import com.wipromail.sathesh.ui.components.ChangePasswordDialog;
import com.wipromail.sathesh.ui.components.SignOutAlertDialog;

import java.util.Calendar;
//import com.wipromail.sathesh.ui.components.SignOutAlertDialog;

/** For the general preferncescreen, Use the adapter {@link GeneralPreferenceAdapter}
 * @author Sathesh
 *
 */
public class PreferencesFragment extends PreferenceFragment implements Constants ,OnSharedPreferenceChangeListener{

    public static int OFFICE365_URL_POSITION = 0;
    public static int CUSTOM_URL_POSITION = 3;

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
    private MyActivity activity;
    private static Context context;
    private ActionBar myActionBar;

    private GeneralPreferenceAdapter sharedPref = new GeneralPreferenceAdapter();
    private static PreferencesFragment preferencesFragment;

    public static PreferencesFragment newInstance() {
        preferencesFragment = new PreferencesFragment();
        Bundle args = new Bundle();
        preferencesFragment.setArguments(args);
        return preferencesFragment;
    }

    public static PreferencesFragment getCurrentInstance(){
        return preferencesFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activity= (MyActivity) this.getActivity();
        context = activity.getApplicationContext();
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
		signOut = (Preference)getPreferenceScreen().findPreference(
				KEY_SIGN_OUT);

        //Will call the function to initialize the summary. The summary will change when the option changes
        oncreateWebmailServerFromSummary();
        updateComposeSignatureEnablePrefernceSummary();
        updateNotificationEnablePrefernceSummary();
        updateSubscriptionTypePrefernceSummary();
        updatePullDurationPrefernceSummary();
        updateAutoUpdateNotifySummary();
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Initialize toolbar
        /*MailApplication.toolbarInitialize(activity, this.getView());

        //action bar initialize
        myActionBar = activity.getSupportActionBar();
        //update mail type in the action bar title
        myActionBar.setTitle(activity.getString(R.string.drawer_menu_search_contact));
        myActionBar.setDisplayHomeAsUpEnabled(true);
        myActionBar.setHomeButtonEnabled(true);*/

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
		signOut.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                SignOutAlertDialog.showAlertdialog(activity, context);
                return true;
            }
        });
    }

    private void updatePullDurationPrefernceSummary() {
        if(sharedPref.getNotificationPullFrequency(context)==30000){
            pullDuration.setSummary(getString(R.string.preference_pull_ferequency_30s_summary));
        }
        else if(sharedPref.getNotificationPullFrequency(context)==900000){
            pullDuration.setSummary(getString(R.string.preference_pull_ferequency_15m_summary));
        }
        else if(sharedPref.getNotificationPullFrequency(context)==3600000){
            pullDuration.setSummary(getString(R.string.preference_pull_ferequency_1h_summary));
        }
        else if(sharedPref.getNotificationPullFrequency(context)==14400000){
            pullDuration.setSummary(getString(R.string.preference_pull_ferequency_4h_summary));
        }
        else if(sharedPref.getNotificationPullFrequency(context)==43200000){
            pullDuration.setSummary(getString(R.string.preference_pull_ferequency_12h_summary));
        }
    }

    private void updateSubscriptionTypePrefernceSummary() {
        subscrpyionType.setSummary(getString(R.string.preference_notification_type_summary));
    }

    private void updateNotificationEnablePrefernceSummary() {
        if(sharedPref.isNotificationEnabled(context)){
            notificationEnable.setSummary(getString(R.string.preference_notification_enable_ON_summary));
        }else{
            notificationEnable.setSummary(getString(R.string.preference_notification_enable_OFF_summary));
        }
    }

    private void updateAutoUpdateNotifySummary() {
        if(sharedPref.isAutoUdpateNotifyEnabled(context)){
            autoUpdateNotifyEnable.setSummary(getString(R.string.preferences_updater_ON_autoupdate_notify_summary));
        }else{
            autoUpdateNotifyEnable.setSummary(getString(R.string.preferences_updater_OFF_autoupdate_notify_summary));
        }
    }

    private void updateComposeSignatureEnablePrefernceSummary() {
        if(sharedPref.isComposeSignatureEnabled(context)){
            composeSignatureEnable.setSummary(getString(R.string.preference_compose_signature_enable_ON_summary));
        }else{
            composeSignatureEnable.setSummary(getString(R.string.preference_compose_signature_enable_OFF_summary));
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // Set up a listener whenever a key changes
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();

        // Unregister the listener whenever a key changes
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences pref, String key) {
        if(key.equals(KEY_WEBMAIL_SERVER)){
            try {
                Calendar now = Calendar.getInstance();
                if( !(PreferencesFragment.timeOfLastCustomURL!=null && ((now.getTimeInMillis() - PreferencesFragment.timeOfLastCustomURL.getTimeInMillis() ) < 2000))){
                    //when the custom url is saved, this event will be invoked again. so to preven that, the last custom url save shld be > than 2secs.
                    updateWebmailServerPrefernceSummary();
                }
                else{
                    Log.i(TAG, "PreferenceActivity -> Last prefernce save was save less than 2 secs. So url changing fn not invoked");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else if(key.equals(KEY_COMPOSE_SIGNATURE_ENABLE)){
            updateComposeSignatureEnablePrefernceSummary();
        }
        else if(key.equals(KEY_NOTIFICATION_ENABLE)){

            if(sharedPref.isNotificationEnabled(context)){
                MailApplication.startMNSService(context);
            }
            else{
                MailApplication.stopMNSService(context);
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
        webMailServer.setSummary(sharedPref.getServerURL(context));
    }

    /** called everytime when this preference is changed
     *
     */
    private void updateWebmailServerPrefernceSummary() {

        String[] serversText = getResources().getStringArray(R.array.preferences_serverlist_text);
        // for the primary and secondary URLs
        for(int i=0; i<serversText.length; i++){
            if(i!=CUSTOM_URL_POSITION){	//i=3 means the user clicked Custom URL 4th option. It is handled in WebmailURLPreference.java
                if(serversText[i].equalsIgnoreCase(webMailServer.getEntry().toString())){
                    webMailServer.setSummary(getResources().getStringArray(R.array.preferences_serverlist_values)[i]);
                }
            }
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

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}