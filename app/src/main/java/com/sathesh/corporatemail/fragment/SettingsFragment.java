package com.sathesh.corporatemail.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import com.sathesh.corporatemail.R;
import com.sathesh.corporatemail.adapter.GeneralPreferenceAdapter;
import com.sathesh.corporatemail.application.MailApplication;
import com.sathesh.corporatemail.application.MyActivity;
import com.sathesh.corporatemail.constants.Constants;
import com.sathesh.corporatemail.customui.Notifications;
import com.sathesh.corporatemail.tools.CacheClear;
import com.sathesh.corporatemail.ui.components.ChangePasswordDialog;
import com.sathesh.corporatemail.ui.components.SignOutAlertDialog;


public class SettingsFragment extends PreferenceFragmentCompat implements Constants{

    public MyActivity activity ;
    private static SettingsFragment fragment;
    private SettingsFragment.ActivityDataPasser mListener;
    private SettingsFragment.ActivityDataPasser activityDataPasser;

    public final static String KEY_WEBMAIL_SERVER="webmail_server";
    public final static String KEY_COMPOSE_SIGNATURE_ENABLE="compose_signature_enable";
    public final static String KEY_NOTIFICATION_ENABLE="notification_enable";
    public final static String KEY_NOTIFICATION_TYPE="notification_type";
    public final static String KEY_NOTIFICATION_ADV="notification_advanced";
    public final static String KEY_PULL_FREQUENCY="notification_pull_frequency";
    private final static String KEY_CHANGE_PASSWORD="change_password";
    private final static String KEY_CLEAR_CACHE="clear_cache";
    private final static String KEY_SIGN_OUT="sign_out";

    //The following are not in the preferences page
    public final static String COMPOSE_SIGNATURE="compose_signature";

    private EditTextPreference webMailServer;
    private SwitchPreferenceCompat notificationEnable,composeSignatureEnable;
    private Preference changePassword, signOut, clearCache, notificationAdv;
    private ListPreference subscrpyionType;
    @Deprecated
    private ListPreference pullDuration;

    private GeneralPreferenceAdapter sharedPref = new GeneralPreferenceAdapter();

    /** Factory for this fragment
     *
     * @return
     */
    public static SettingsFragment newInstance() {
        fragment = new SettingsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public static SettingsFragment getInstance(){
        return fragment;
    }

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity =   (MyActivity)getActivity();
        activityDataPasser =   (SettingsFragment.ActivityDataPasser)getActivity();
        Context context = getActivity();

        webMailServer=getPreferenceScreen().findPreference(
                KEY_WEBMAIL_SERVER);
        composeSignatureEnable=(SwitchPreferenceCompat) getPreferenceScreen().findPreference(
                KEY_COMPOSE_SIGNATURE_ENABLE);
        notificationEnable=(SwitchPreferenceCompat)getPreferenceScreen().findPreference(
                KEY_NOTIFICATION_ENABLE);
        subscrpyionType=(ListPreference)getPreferenceScreen().findPreference(
                KEY_NOTIFICATION_TYPE);
        notificationAdv = (Preference)getPreferenceScreen().findPreference(
                KEY_NOTIFICATION_ADV);
        pullDuration=(ListPreference)getPreferenceScreen().findPreference(
                KEY_PULL_FREQUENCY);
        pullDuration.setVisible(false);
        changePassword = (Preference)getPreferenceScreen().findPreference(
                KEY_CHANGE_PASSWORD);
        clearCache = (Preference)getPreferenceScreen().findPreference(
                KEY_CLEAR_CACHE);
        signOut = (Preference)getPreferenceScreen().findPreference(
                KEY_SIGN_OUT);

        webMailServer.setText(sharedPref.getServerURL(context));
        webMailServer.setSummary(sharedPref.getServerURL(context));
        //Will call the function to initialize the summary. The summary will change when the option changes
        updateComposeSignatureEnablePrefernceSummary(context, sharedPref.isComposeSignatureEnabled(context));
        updateNotificationEnablePrefernceSummary(context, sharedPref.isNotificationEnabled(context));
        updateSubscriptionTypePrefernceSummary(context);
        updatePullDurationPrefernceSummary(context, sharedPref.getNotificationPullFrequency(context) );
        initializeEventsForAccount(context);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings, rootKey);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (ActivityDataPasser) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement ActivityDataPasser");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /** ON RESUME **/
    @Override
    public void onResume() {
        super.onResume();

        try {
            activityDataPasser.getmDrawerToggle().syncState();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /** Interface - Fragment Interaction Listener
     *
     */
    public interface ActivityDataPasser {
        androidx.appcompat.app.ActionBarDrawerToggle getmDrawerToggle();
    }


    /** This functions initializes the click event for the Preferences other than checkboxpreference, list box preference etc.,
     *
     */
    private void initializeEventsForAccount(Context context) {

        webMailServer.setOnPreferenceChangeListener((Preference preference, Object newValue) -> {
            webMailServer.setSummary(newValue.toString());
            return true;
        });
        //initializing the click event for Change Password button
        changePassword.setOnPreferenceClickListener((Preference preference) ->{
                    ChangePasswordDialog.showAlertdialog(activity, context);
                    return true;
                }
        );

        //initializing the click event for Clear Cache button
        clearCache.setOnPreferenceClickListener((Preference preference) ->{
                    CacheClear.clearFullCacheAndDbDir(activity);
                    Notifications.showToast(activity, activity.getString(R.string.preferences_cache_cleared_msg) , Toast.LENGTH_SHORT);
                    return true;
                }
        );

        //initializing the click event for Sign Out  button
        signOut.setOnPreferenceClickListener((Preference preference) ->{
                    SignOutAlertDialog.showAlertdialog(activity, context);
                    return true;
                }
        );

        pullDuration.setOnPreferenceChangeListener( (p,value) -> {
            updatePullDurationPrefernceSummary(context, Long.parseLong(value.toString()));
            try {
                MailApplication.onChangeMNSResetPullDuration(Long.parseLong(value.toString()));
            }catch(Exception e){
                e.printStackTrace();
            }
            return true;
        });

        composeSignatureEnable.setOnPreferenceChangeListener((p,value) -> {
            updateComposeSignatureEnablePrefernceSummary(context, Boolean.parseBoolean(value.toString()));
            return true;
                }

        );

        notificationEnable.setOnPreferenceChangeListener((p,value) -> {
            updateNotificationEnablePrefernceSummary(context, Boolean.parseBoolean(value.toString()));
            return true;
        });

        notificationAdv.setOnPreferenceClickListener(preference -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Intent intent = new Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS);
                intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.getPackageName());
                intent.putExtra(Settings.EXTRA_CHANNEL_ID, NotificationConstants.channelIdNewEmail);
                startActivity(intent);
            }else{
                Notifications.showToast(context, context.getString(R.string.api_version_low));
            }
            return true;
        });
    }

    @Deprecated
    private void updatePullDurationPrefernceSummary(Context context, long value) {
        if(value==30000){
            pullDuration.setSummary(getString(R.string.preference_pull_ferequency_30s_summary));
        }
        else if(value==900000){
            pullDuration.setSummary(getString(R.string.preference_pull_ferequency_15m_summary));
        }
        else if(value==3600000){
            pullDuration.setSummary(getString(R.string.preference_pull_ferequency_1h_summary));
        }
        else if(value==14400000){
            pullDuration.setSummary(getString(R.string.preference_pull_ferequency_4h_summary));
        }
        else if(value==43200000){
            pullDuration.setSummary(getString(R.string.preference_pull_ferequency_12h_summary));
        }
    }

    private void updateSubscriptionTypePrefernceSummary(Context context) {
        subscrpyionType.setSummary(getString(R.string.preference_notification_type_summary));
    }

    private void updateNotificationEnablePrefernceSummary(Context context, boolean value) {
        if(value){
            notificationEnable.setSummary(getString(R.string.preference_notification_enable_ON_summary));
            MailApplication.startMNWorker(context);
        }else{
            notificationEnable.setSummary(getString(R.string.preference_notification_enable_OFF_summary));
            MailApplication.stopMNWorker(context);
        }
    }
    private void updateComposeSignatureEnablePrefernceSummary(Context context, boolean value) {
        if(value){
            composeSignatureEnable.setSummary(getString(R.string.preference_compose_signature_enable_ON_summary));
        }else{
            composeSignatureEnable.setSummary(getString(R.string.preference_compose_signature_enable_OFF_summary));
        }
    }

}
