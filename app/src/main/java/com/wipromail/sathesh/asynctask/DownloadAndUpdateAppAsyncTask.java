package com.wipromail.sathesh.asynctask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.wipromail.sathesh.R;
import com.wipromail.sathesh.application.MailApplication;
import com.wipromail.sathesh.constants.Constants;
import com.wipromail.sathesh.customui.Notifications;

public class DownloadAndUpdateAppAsyncTask extends AsyncTask<String, String, String> implements Constants {
	private InputStream input;
	private OutputStream output ;
	

	private Activity activity;
	private ProgressDialog mProgressDialog;
	
	private static String STATUS_DOWNLOADING="DOWNLOADING";
	private static String STATUS_INSTALLING="INSTALLING";
	private static String STATUS_ERROR="ERROR";
	private static String STATUS_DOWNLOAD_COMPLETE="COMPLETE";
	
	public DownloadAndUpdateAppAsyncTask(Activity activity) {
		// TODO Auto-generated constructor stub
		this.activity = activity;
		this.mProgressDialog= mProgressDialog;
	}
	
	@Override
	protected String doInBackground(String... sUrl) {

		Log.i(TAG, "initial URL: "+ sUrl[0]  );
		final long fileLength;

		try {
			HttpClient client = new DefaultHttpClient(); 

			HttpGet get = new HttpGet( sUrl[0]);
			HttpResponse responseGet = client.execute(get);  
			HttpEntity resEntityGet = responseGet.getEntity();  

			if (resEntityGet != null) {  
				fileLength = resEntityGet.getContentLength();
				
				activity.runOnUiThread(new Runnable() {
	                public void run() {

	                	mProgressDialog.setMax((int)fileLength/1000);
	                }
	            });

				//do something with the response
				Log.i(TAG, "RESPONSE STATUS "+ responseGet.getStatusLine().getStatusCode());

				//  Log.i(TAG, "GET RESPONSE "+ EntityUtils.toString(resEntityGet));

				input = resEntityGet.getContent();



				String PATH = MailApplication.getExternalStorageDirectory() + APPLICATION_APK_DOWNLOAD_TEMPLOC;
				//String PATH = MailApplication.getDownloadCacheDirectory() + APPLICATION_APK_DOWNLOAD_TEMPLOC;
				File file = new File(PATH);
				file.mkdirs();
				File outputFile = new File(file, APPLICATION_APK_DOWNLOAD_TEMP_FILENAME);
				
				
				output = new FileOutputStream(outputFile);

				byte data[] = new byte[1024];
				long total = 0;
				int count;
				while ((count = input.read(data)) != -1) {
					total += count;
					// publishing the progress....

					publishProgress(STATUS_DOWNLOADING , String.valueOf(total /1000));
					output.write(data, 0, count);
				}

			}
			else {
				Log.i(TAG, "null reponse");
			}


			/*
			URL url = new URL(sUrl[0]);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setInstanceFollowRedirects(false);

			connection.connect();

			//check for redirects
			int responseCode = connection.getResponseCode();
			Log.d(TAG, "ResponseCode " + responseCode);
			System.out.println( "Header " + 	connection.getHeaderFields() );

			if(responseCode == HttpURLConnection.HTTP_MOVED_PERM || responseCode == HttpURLConnection.HTTP_MOVED_TEMP){
				String newLocation = connection.getHeaderField( "Location" );
				Log.i(TAG, "new location " + newLocation);
				url = new URL(newLocation);
				connection = (HttpURLConnection) url.openConnection();
				connection.connect();
			}


			// this will be useful so that you can show a typical 0-100% progress bar
			int fileLength = connection.getContentLength();

			// download the file
			Log.i(TAG, "downloading the file from URL");
			Log.i(TAG, url.toString());
			input = new BufferedInputStream(url.openStream());

			String PATH = Environment.getExternalStorageDirectory() + "/download/";
			File file = new File(PATH);
			file.mkdirs();
			File outputFile = new File(file, "app.apk");

			output = new FileOutputStream(outputFile);

			byte data[] = new byte[1024];
			long total = 0;
			int count;
			while ((count = input.read(data)) != -1) {
				total += count;
				// publishing the progress....
				publishProgress(STATUS_DOWNLOADING , String.valueOf((total * 100) / fileLength));
				output.write(data, 0, count);
			}
			 */
			Log.i(TAG, "Download Complete.");
			publishProgress(STATUS_DOWNLOAD_COMPLETE , "");

			//The following 3 lines will install the app..
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setDataAndType(Uri.fromFile(new File(Environment.getExternalStorageDirectory()
					+ APPLICATION_APK_DOWNLOAD_TEMPLOC 
					+ APPLICATION_APK_DOWNLOAD_TEMP_FILENAME)),
					"application/vnd.android.package-archive");
			activity.startActivity(intent);


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
				Log.e(TAG, "DownloadAndUpdateAppAsyncTask -> Error occured: " + e.getMessage());
				e.printStackTrace();
				//publishProgress(STATUS_ERROR, e.getMessage());
				//should not display any url to user
				publishProgress(STATUS_ERROR, e.getMessage());
				Log.d(TAG, "after publishing");

		}
		finally{
			try {
				if(null!=input)input.close();
				if(null!=output)output.flush();
				if(null!=output)output.close();
			} 
			catch (Exception e) {	e.printStackTrace();}

		}
		return null;
	}


	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		// instantiate it within the onCreate method
				mProgressDialog = new ProgressDialog(activity);
				mProgressDialog.setMessage(activity.getText(R.string.app_updater_downloading));
				mProgressDialog.setIndeterminate(false);
				mProgressDialog.setCancelable(false);
				
				mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);

		mProgressDialog.show();
	}



	@Override
	protected void onProgressUpdate(String... progress) {
		super.onProgressUpdate(progress);
		try{
			if(progress[0].equals(STATUS_DOWNLOADING)){
				mProgressDialog.setProgress(Integer.valueOf(progress[1]));
			}
			else if(progress[0].equals(STATUS_INSTALLING)){

				mProgressDialog.setMessage(activity.getText(R.string.app_updater_installing) );
			}
			else if(progress[0].equals(STATUS_DOWNLOAD_COMPLETE)){
				mProgressDialog.hide();

			}

			else if(progress[0].equals(STATUS_ERROR)){
		System.out.println("error bloc");
		
				mProgressDialog.hide();
				System.out.println("prog hidden");
				Notifications.showAlert(activity, activity.getText(R.string.app_updater_error) + "\nDetails: " + progress[1]);
				System.out.println("after notify");
		     
		
			}
		}
		catch(Exception e){e.printStackTrace();
		}
	}

}