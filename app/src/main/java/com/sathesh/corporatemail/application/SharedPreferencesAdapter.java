package com.sathesh.corporatemail.application;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.sathesh.corporatemail.constants.Constants;
import com.sathesh.corporatemail.util.Utilities;

public class SharedPreferencesAdapter implements Constants{
	private  static SharedPreferences sharedPreferences;

	public static String getSignedInUsername(Context context) throws Exception{
		sharedPreferences = context.getSharedPreferences(CRED_PREFS_NAME, 0);
		String username= sharedPreferences.getString(CRED_PREFS_USERNAME, USERNAME_NULL);
		if(IS_USERNAME_ENCRYPTION_ENABLED && !(username.equals(USERNAME_NULL))){
		username=Utilities.decrypt(username);
		}
		return username;
	}


	public static String getSignedInPassword(Context context) throws Exception{
		sharedPreferences = context.getSharedPreferences(CRED_PREFS_NAME, 0);
		String password= sharedPreferences.getString(CRED_PREFS_PASSWORD, PASSWORD_NULL);
		if(IS_PASSWORD_ENCRYPTION_ENABLED && !(password.equals(PASSWORD_NULL))){
		password=Utilities.decrypt(password);
		}
		return password;
	}


	/** stores the credentials
	 * @param context
	 * @param username
	 * @param password
	 * @throws Exception 
	 */
	public synchronized static void storeCredentials(Context context, String username, String password) throws Exception{ 

		sharedPreferences = context.getSharedPreferences(CRED_PREFS_NAME, 0);
		if(IS_USERNAME_ENCRYPTION_ENABLED){
			username=Utilities.encrypt(username);
		}
		if(IS_PASSWORD_ENCRYPTION_ENABLED){
			password=Utilities.encrypt(password);
		}
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putString(CRED_PREFS_USERNAME, username);
		editor.putString(CRED_PREFS_PASSWORD, password);

		// Commit the edits!
		editor.apply();

	}
	
	/** Stores or updates the password
	 * @param context
	 * @param password
	 * @throws Exception
	 */
	public synchronized static void storeCredentialsPassword(Context context, String password) throws Exception{ 

		sharedPreferences = context.getSharedPreferences(CRED_PREFS_NAME, 0);

		if(IS_PASSWORD_ENCRYPTION_ENABLED){
			password=Utilities.encrypt(password);
		}
		
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putString(CRED_PREFS_PASSWORD, password);

		// Commit the edits!
		editor.apply();

	}

	/** gets sync state variable which will we get from EWS during sync
	 * @param context
	 * @return
	 * @throws Exception
	 */
	public static String getSyncState(Context context) throws Exception{
		sharedPreferences = context.getSharedPreferences(SYSTEM_VARIABLES, 0);
		Log.i(TAG, "Getting  Sync State " + sharedPreferences.getString(EWS_SYNC_STATE, ""));
		return sharedPreferences.getString(EWS_SYNC_STATE, "");
	}


	public synchronized static void storeSyncState(Context context, String syncState) throws Exception{ 

		Log.d(TAG, "Storing Sync State " + syncState);
		sharedPreferences = context.getSharedPreferences(SYSTEM_VARIABLES, 0);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putString(EWS_SYNC_STATE, syncState);

		editor.apply();

	}

	
	/** counter for storing the number of times emails are viewed(to open the dialog for rating app)
	 * @param context
	 * @return
	 * @throws Exception
	 */
	public static int getCounterOpenedEmails(Context context) throws Exception{
		sharedPreferences = context.getSharedPreferences(USER_ACCT_DETAILS, 0);
		return sharedPreferences.getInt(COUNTER_OPENED_MAILS, 0);
	}


	public synchronized static void storeCounterOpenedMails(Context context, int count) throws Exception{ 

		sharedPreferences = context.getSharedPreferences(USER_ACCT_DETAILS, 0);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putInt(COUNTER_OPENED_MAILS, count);

		editor.apply();

	}
	
	/** counter for storing the number of times emails are viewed(to open the dialog for rating app)
	 * @param context
	 * @return
	 * @throws Exception
	 */
	public static boolean getDoNotRateApp(Context context) throws Exception{
		sharedPreferences = context.getSharedPreferences(USER_ACCT_DETAILS, 0);
		return sharedPreferences.getBoolean(DO_NOT_RATE_APP, false);
	}


	public synchronized static void storeDoNotRateApp(Context context, boolean flag) throws Exception{ 

		sharedPreferences = context.getSharedPreferences(USER_ACCT_DETAILS, 0);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putBoolean(DO_NOT_RATE_APP, flag);

		editor.apply();

	}

	/** gets inbox sync using pull subscription watermark
	 * @param context
	 * @return
	 * @throws Exception
	 */
	public static String getSyncPullMethodWatermark(Context context) throws Exception{
		sharedPreferences = context.getSharedPreferences(SYSTEM_VARIABLES, 0);
		Log.i(TAG, "Getting  Sync State watermark " + sharedPreferences.getString(EWS_SYNC_STATE, ""));
		return sharedPreferences.getString(INBOX_SYNC_PULL_WATERMARK, "");
	}


	public synchronized static void storeSyncPullMethodWatermark(Context context, String watermark) throws Exception{ 

		Log.d(TAG, "Storing Sync State watermark " + watermark);
		sharedPreferences = context.getSharedPreferences(SYSTEM_VARIABLES, 0);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putString(INBOX_SYNC_PULL_WATERMARK, watermark);

		editor.apply();

	}

	/** gets last successful inbox update date time
	 * @param context
	 * @return
	 * @throws Exception
	 */
	public static String getLastSuccessfulInboxUpdate(Context context) throws Exception{
		sharedPreferences = context.getSharedPreferences(SYSTEM_VARIABLES, 0);
		return sharedPreferences.getString(LAST_SUCCESSFUL_INBOX_UPDATE, "");
	}


	public synchronized static void storeLastSuccessfulInboxUpdate(Context context, String date) throws Exception{ 

		sharedPreferences = context.getSharedPreferences(SYSTEM_VARIABLES, 0);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putString(LAST_SUCCESSFUL_INBOX_UPDATE, date.toString());

		editor.apply();

	}

	/** gets last successful auto update check
	 * @param context
	 * @return
	 * @throws Exception
	 */
	public static String getLastautoUpdateCheck(Context context) throws Exception{
		sharedPreferences = context.getSharedPreferences(SYSTEM_VARIABLES, 0);
		return sharedPreferences.getString(LAST_SUCCESSFUL_AUTO_UPDATE_CHECK, "");
	}


	public synchronized static void storeLastSuccessfulAutoUpdateCheck(Context context, String date) throws Exception{ 

		sharedPreferences = context.getSharedPreferences(SYSTEM_VARIABLES, 0);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putString(LAST_SUCCESSFUL_AUTO_UPDATE_CHECK, date.toString());

		editor.apply();

	}
	
	/** Stores Yes  or No(or empty) to indicate whether the Inital Sync Done
	 * @param context
	 * @return
	 * @throws Exception
	 */
	public static String getIsInboxInitialSyncDone(Context context) throws Exception{
		sharedPreferences = context.getSharedPreferences(SYSTEM_VARIABLES, 0);
		return sharedPreferences.getString(IS_INBOX_INITIAL_SYNC_DONE, "");

	}

	public synchronized static void storeIsInboxInitialSyncDone(Context context, String value) throws Exception{ 

		sharedPreferences = context.getSharedPreferences(SYSTEM_VARIABLES, 0);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putString(IS_INBOX_INITIAL_SYNC_DONE, value);

		editor.apply();

	}

	/** Stores the user account details DisplayName
	 * @param context
	 * @return
	 * @throws Exception
	 */
	public static String getUserDetailsDisplayName(Context context) throws Exception{
		sharedPreferences = context.getSharedPreferences(USER_ACCT_DETAILS, 0);
		return sharedPreferences.getString(SIGNED_IN_USER_DISP_NAME, "");

	}

	public synchronized static void storeUserDetailDisplayName(Context context, String value) throws Exception{ 

		sharedPreferences = context.getSharedPreferences(USER_ACCT_DETAILS, 0);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putString(SIGNED_IN_USER_DISP_NAME, value);

		editor.apply();

	}

    /** Stores the user account details Company Name
     * @param context
     * @return
     * @throws Exception
     */
    public static String getUserDetailsCompanyName(Context context) throws Exception{
        sharedPreferences = context.getSharedPreferences(USER_ACCT_DETAILS, 0);
        return sharedPreferences.getString(SIGNED_IN_USER_COMP_NAME, "");

    }

    public synchronized static void storeUserDetailsCompanyName(Context context, String value) throws Exception{

        sharedPreferences = context.getSharedPreferences(USER_ACCT_DETAILS, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SIGNED_IN_USER_COMP_NAME, value);

        editor.apply();

    }

	/** Stores the user account details Email
	 * @param context
	 * @return
	 * @throws Exception
	 */
	public static String getUserDetailsEmail(Context context) throws Exception{
		sharedPreferences = context.getSharedPreferences(USER_ACCT_DETAILS, 0);
		return sharedPreferences.getString(SIGNED_IN_USER_EMAIL, "");

	}

	public synchronized static void storeUserDetailEmail(Context context, String value) throws Exception{ 

		sharedPreferences = context.getSharedPreferences(USER_ACCT_DETAILS, 0);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putString(SIGNED_IN_USER_EMAIL, value);

		editor.apply();

	}
}
