package com.sathesh.corporatemail.adapter;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import com.sathesh.corporatemail.R;
import com.sathesh.corporatemail.constants.Constants;
import com.sathesh.corporatemail.fragment.SettingsFragment;

/**
 * @author Sathesh
 *
 */
public class GeneralPreferenceAdapter implements Constants{
	private SharedPreferences sharedPreferences ;
	
	private void loadPreference(Context context){
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
	}
	/** Webmail Server URL
	 * @param context
	 * @return
	 */
	public String getServerURL(Context context){
		loadPreference(context);
		//the second param specifies the value which will be the default when the fetch to the shared preference fails.
		return sharedPreferences.getString(SettingsFragment.KEY_WEBMAIL_SERVER, "");
	}

	/** This will store the given URL to use as the webmail url in the application. Warning: calling this function will trigger the OnSharedPreferenceschangeListener in the PrefernecesActivity
	 * @param context
	 * @param url
	 */
	public static void storeServerURL(Context context, String url){
		SharedPreferences _sharedPreferences ;
		_sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		//the second param specifies the value which will be the default when the fetch to the shared preference fails.
		SharedPreferences.Editor editor =_sharedPreferences.edit();
		editor.putString(SettingsFragment.KEY_WEBMAIL_SERVER, url);
		editor.commit();
	}
	
	/** gets signature
	 * @param context
	 * @return
	 * @throws Exception
	 */
	public String getComposeSignature(Context context) throws Exception{
		loadPreference(context);
		return sharedPreferences.getString(SettingsFragment.COMPOSE_SIGNATURE, context.getString(R.string.compose_signature_default));
	}


	/** store signatre
	 * @param context
	 * @throws Exception
	 */
	public synchronized void storeComposeSignature(Context context, String text) throws Exception{ 

		SharedPreferences _sharedPreferences ;
		_sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = _sharedPreferences.edit();
		editor.putString(SettingsFragment.COMPOSE_SIGNATURE, text);

		editor.commit();

	}
	
	/** gets whether notification is enabled
	 * @param context
	 * @return
	 */
	public boolean isNotificationEnabled(Context context){
		loadPreference(context);
		return sharedPreferences.getBoolean(SettingsFragment.KEY_NOTIFICATION_ENABLE, true);
	}
	
	/** gets whether Compose Signature is enabled
	 * @param context
	 * @return
	 */
	public boolean isComposeSignatureEnabled(Context context){
		loadPreference(context);
		return sharedPreferences.getBoolean(SettingsFragment.KEY_COMPOSE_SIGNATURE_ENABLE, true);
	}
	/** notification type
	 * @param context
	 * @return
	 */
	public String getNotificationType(Context context){
		loadPreference(context);
		return sharedPreferences.getString(SettingsFragment.KEY_NOTIFICATION_TYPE, "pull");
	}
	
	/** pull frequency
	 * @param context
	 * @return
	 */
	@Deprecated
	public Long getNotificationPullFrequency(Context context){
		loadPreference(context);
		return Long.valueOf(sharedPreferences.getString(SettingsFragment.KEY_PULL_FREQUENCY, "900000"));
	}
}
