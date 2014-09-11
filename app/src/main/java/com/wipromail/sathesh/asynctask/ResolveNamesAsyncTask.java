package com.wipromail.sathesh.asynctask;

import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

import com.wipromail.sathesh.R;
import com.wipromail.sathesh.application.MailApplication;
import com.wipromail.sathesh.asynctask.interfaces.IResolveNames;
import com.wipromail.sathesh.constants.Constants;
import com.wipromail.sathesh.customui.Notifications;
import com.wipromail.sathesh.service.data.ExchangeService;
import com.wipromail.sathesh.service.data.NameResolutionCollection;

/**
 * @author Sathesh
 * 
 * This will will resolve names in screen and build dialog box.. 
 *
 */
public class ResolveNamesAsyncTask extends AsyncTask<String, String, String> implements Constants {
	
	private Activity activity;
	private ExchangeService service;
	private String searchString="";
	private static String STATUS_CHECKING="CHECKING";

	private static String STATUS_DONE="DONE";
	private static String STATUS_ERROR="ERROR";
	
	private IResolveNames caller;
	private NameResolutionCollection outputCollection;
	private String progressDialogString="";
	private String extra1="";
	private Exception pE;
	private boolean showProgressDialog;
	
	public ResolveNamesAsyncTask(IResolveNames caller, Activity activity,ExchangeService service, String searchString,boolean showProgressDialog, String progressDialogString, String extra1) {
		// TODO Auto-generated constructor stub
		this.caller=caller;
		this.activity = activity;
		this.service=service;
		this.searchString=searchString;
		this.progressDialogString=progressDialogString;
		this.extra1=extra1;
		this.showProgressDialog =showProgressDialog;
	}

	ProgressDialog dialog;

	@Override
	protected void onPreExecute() {
		// TODO Auto-generated method stub

		try {
			if(showProgressDialog){
			dialog = ProgressDialog.show(activity, "", 
					progressDialogString, true);
			}
			publishProgress(STATUS_CHECKING, "Checking");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.e(TAG, "Exception occured on preexecute");
		}

	}
	@Override
	protected String doInBackground(String... str) {

		
		

		try {
			outputCollection=MailApplication.resolveName_LocalThenDirectory(service, searchString, true);
			publishProgress(STATUS_DONE,"Done");
		}

		catch (UnknownHostException e) {
			pE=e;
			publishProgress(STATUS_ERROR, activity.getText(R.string.no_internet_connection_error_text).toString());

		}
		catch (SocketException e) {
			pE=e;
			publishProgress(STATUS_ERROR, activity.getText(R.string.no_internet_connection_error_text).toString());
			
		}
		catch (SocketTimeoutException e) {
			pE=e;
			publishProgress(STATUS_ERROR, activity.getText(R.string.no_internet_connection_error_text).toString());
			
		}
		catch (Exception e) {
			Log.e(TAG, "ResolveNamesAsyncTask -> Error occured: " + e.getMessage());
			e.printStackTrace();
			//publishProgress(STATUS_ERROR, e.getMessage());
			//should not display any url to user
			pE=e;
			publishProgress(STATUS_ERROR, e.getMessage());
			
		}
		
		return null;
	}




	@Override
	protected void onProgressUpdate(String... progress) {
		super.onProgressUpdate(progress);
		try{
			if(progress[0].equals(STATUS_CHECKING)){
				if(showProgressDialog){
				dialog.setMessage(progressDialogString);
				}
				caller.handleResolvingNames();
			}
			if(progress[0].equals(STATUS_DONE)){
				if(showProgressDialog){
				dialog.dismiss();
				}
				Log.d(TAG, "Resolve names -> got the output. returning to caller " + caller.getClass().getName());
				caller.handleResolveNamesOutput(outputCollection,extra1);
			}
			else if(progress[0].equals(STATUS_ERROR)){
				System.out.println("dismissing dialog");
				if(showProgressDialog){
				dialog.dismiss();
				}
				System.out.println("after dismissing dialog");
				System.out.println("exception name "+pE);
				System.out.println("exceiption type "+ pE.getMessage());
				Log.d(TAG, "exception name " + pE.getMessage());
				caller.handleResolveNamesOutputError(outputCollection,extra1,pE);
				
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	
	
		

}