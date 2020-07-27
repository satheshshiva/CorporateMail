package com.sathesh.corporatemail.asynctask;

import android.os.AsyncTask;
import android.util.Log;

import com.sathesh.corporatemail.BuildConfig;
import com.sathesh.corporatemail.R;
import com.sathesh.corporatemail.application.MyActivity;
import com.sathesh.corporatemail.asynctask.interfaces.GenericAsyncTask;
import com.sathesh.corporatemail.constants.Constants;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Properties;

public class UpdateCheckerAsyncTask extends AsyncTask<String, String, Void> implements Constants{

	public static final String STATUS_CHECKING="CHECKING";
	public static final String STATUS_UPDATE_AVAILABLE="STATUS_UPDATE_AVAILABLE";
	public static final String STATUS_NO_UPDATE="STATUS_NO_UPDATE";

	public static final String STATUS_ERROR="ERROR";
	private GenericAsyncTask caller;
	private MyActivity activity;
	private InputStream input;

	private String LatestVersionName="";
	private String LatestVersionCode="";
	private int currentVersionCode=0;


	HttpURLConnection connection ;


	public UpdateCheckerAsyncTask(GenericAsyncTask caller, MyActivity activity,int currentVersionCode) {
		this.caller=caller;
		this.activity = activity;
		this.currentVersionCode=currentVersionCode;
	}

	@Override
	protected void onPreExecute() {
		caller.activity_OnPreExecute();

	}

	@Override
	protected Void doInBackground(String... str) {
		
		String url ="";
		//determining whether dev or release URL
		if (BuildConfig.DEBUG){
			url = APPLICATION_LATEST_VERSION_PROP_URL_DEV;
		}
		else {
			url = APPLICATION_LATEST_VERSION_PROP_URL_REL;	

		}
		Log.i(LOG_TAG, "UpdateCheckerAsyncTask -> URL to get version details: "+ url  );

		publishProgress(STATUS_CHECKING , activity.getString(R.string.app_updater_checking));
		try{
			checkUpdate( url);
		}

		catch (UnknownHostException e) {
			publishProgress(STATUS_ERROR, activity.getText(R.string.no_internet_connection_error_text).toString());

		}
		catch (SocketException e) {
			publishProgress(STATUS_ERROR, activity.getText(R.string.no_internet_connection_error_text).toString());

		}
		catch (SocketTimeoutException e) {
			publishProgress(STATUS_ERROR, activity.getText(R.string.no_internet_connection_error_text).toString());

		}
		catch (Exception e) {
			Log.e(LOG_TAG, "UpdateCheckerAsyncTask -> Error occured: " + e.getMessage());
			e.printStackTrace();
			//publishProgress(STATUS_ERROR, e.getMessage());
			//should not display any url to user
			publishProgress(STATUS_ERROR, e.getMessage());

		}
		finally{
			try {
				input.close();
			} catch (IOException e) {	}
			catch (Exception e) {	}

		}
		
		return null;
	}




	@Override
	protected void onProgressUpdate(String... progress) {
		super.onProgressUpdate(progress);
		caller.activity_onProgressUpdate(progress);
	}

	public void checkUpdate(String url) throws Exception{

		URL _url = new URL(url);
		connection = (HttpURLConnection) _url.openConnection();
		connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.8; rv:28.0) Gecko/20100101 Firefox/28.0");

		if (connection.getResponseCode() ==200) {

			//do something with the response
			Log.i(LOG_TAG, "UpdateCheckerAsyncTask -> LATEST VER DETAILS HTTP RESPONSE STATUS "+ connection.getResponseCode());

			//  Log.i(TAG, "GET RESPONSE "+ EntityUtils.toString(resEntityGet));

			input = connection.getInputStream();

			Properties prop = new Properties();
			prop.load(input);

			LatestVersionName=prop.getProperty(LATEST_VERSION_NAME);

			LatestVersionCode=prop.getProperty(LATEST_VERSION_CODE);

			int latestVerCode = Integer.parseInt(LatestVersionCode);
			Log.i(LOG_TAG, "UpdateCheckerAsyncTask -> Currrent Version: " + currentVersionCode);
			Log.i(LOG_TAG, "UpdateCheckerAsyncTask -> Latest Version: " + latestVerCode);

			//check current version is lesser than ltest verssion
			if(currentVersionCode < latestVerCode){
				Log.i(LOG_TAG, "UpdateCheckerAsyncTask -> update available");
				publishProgress(STATUS_UPDATE_AVAILABLE , activity.getString(R.string.app_updater_checking));
			}
			else{
				Log.i(LOG_TAG, "UpdateCheckerAsyncTask -> No app updates");
				publishProgress(STATUS_NO_UPDATE , activity.getString(R.string.app_updater_checking));
			}

		}
		else {
			Log.e(LOG_TAG, "UpdateChecker -> connection not 200 response");
		}
	}
}
