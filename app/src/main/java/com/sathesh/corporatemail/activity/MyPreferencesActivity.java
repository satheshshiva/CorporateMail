package com.sathesh.corporatemail.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.sathesh.corporatemail.R;
import com.sathesh.corporatemail.adapter.GeneralPreferenceAdapter;
import com.sathesh.corporatemail.application.MailApplication;
import com.sathesh.corporatemail.constants.Constants;
import com.sathesh.corporatemail.customui.Notifications;
import com.sathesh.corporatemail.tools.CacheClear;
import com.sathesh.corporatemail.ui.components.ChangePasswordDialog;
import com.sathesh.corporatemail.ui.components.SignOutAlertDialog;

import java.util.Calendar;
//import com.wipromail.sathesh.ui.components.SignOutAlertDialog;

/** For the general preferncescreen, Use the adapter {@link GeneralPreferenceAdapter}
 * @author Sathesh
 *
 */
public class MyPreferencesActivity extends PreferenceActivity implements Constants ,OnSharedPreferenceChangeListener{

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
    private MyPreferencesActivity activity;
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
		signOut = (Preference)getPreferenceScreen().findPreference(
				KEY_SIGN_OUT);

        //Will call the function to initialize the summary. The summary will change when the option changes
        oncreateWebmailServerFromSummary();
        updateComposeSignatureEnablePrefernceSummary();
        updateNotificationEnablePrefernceSummary();
        updateSubscriptionTypePrefernceSummary();
        updatePullDurationPrefernceSummary();
        updateAutoUpdateNotifySummary();
        // TODO Set the action bar as up enabled
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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
        subscrpyionType.setSummary(getString(R.string.preference_notification_type_summary));
    }

    private void updateNotificationEnablePrefernceSummary() {
        if(sharedPref.isNotificationEnabled(this)){
            notificationEnable.setSummary(getString(R.string.preference_notification_enable_ON_summary));
        }else{
            notificationEnable.setSummary(getString(R.string.preference_notification_enable_OFF_summary));
        }
    }

    private void updateAutoUpdateNotifySummary() {
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
        switch (key) {
            case KEY_COMPOSE_SIGNATURE_ENABLE:
                updateComposeSignatureEnablePrefernceSummary();
                break;
            case KEY_NOTIFICATION_ENABLE:

                if (sharedPref.isNotificationEnabled(this)) {
                    MailApplication.startMNSService(this);
                } else {
                    MailApplication.stopMNSService(this);
                }
                updateNotificationEnablePrefernceSummary();

                break;
            case KEY_NOTIFICATION_TYPE:
                updateSubscriptionTypePrefernceSummary();
                break;
            case KEY_PULL_FREQUENCY:
                try {
                    MailApplication.onChangeMNSResetPullDuration();
                    updatePullDurationPrefernceSummary();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case KEY_AUTO_UPDATE_NOTIFY:
                updateAutoUpdateNotifySummary();
                break;
        }
    }

    /** called on oncreate. reads the webmail URL from storage
     *
     */
    private void oncreateWebmailServerFromSummary() {
        webMailServer.setSummary(sharedPref.getServerURL(context));
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