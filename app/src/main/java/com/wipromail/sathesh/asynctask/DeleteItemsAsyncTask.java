package com.wipromail.sathesh.asynctask;

import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;

import com.wipromail.sathesh.R;
import com.wipromail.sathesh.asynctask.interfaces.GenericAsyncTask;
import com.wipromail.sathesh.constants.Constants;
import com.wipromail.sathesh.ews.NetworkCall;
import com.wipromail.sathesh.service.data.Item;

import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;

public class DeleteItemsAsyncTask extends AsyncTask<String, String, String> implements Constants{

	public static final String STATUS_DELETING="DELETING";
	public static final String STATUS_COMPLETED="COMPLETED";
	public static final String STATUS_PROGRESS="PROGRESS";
	public static final String STATUS_ERROR="ERROR";

	private ActionBarActivity activity;

	private ArrayList<Item> items;
	private GenericAsyncTask caller;
	private boolean permanentDelete;

	public DeleteItemsAsyncTask(GenericAsyncTask caller,ActionBarActivity activity, Item item, boolean permanentDelete) {
		this.activity = activity;
		this.items =new ArrayList<Item>();
		this.items.add(item);
		this.caller = caller;
		this.permanentDelete=permanentDelete;
	}

	public DeleteItemsAsyncTask(GenericAsyncTask caller,ActionBarActivity activity, ArrayList<Item> items, boolean permanentDelete) {
		this.activity = activity;
		this.items = items;
		this.caller = caller;
		this.permanentDelete=permanentDelete;
	}


	@Override
	protected void onPreExecute() {
		caller.activity_OnPreExecute();

	}

	@Override
	protected String doInBackground(String... str) {

		publishProgress(STATUS_DELETING , "Started Deleting");
		try{
			int i=0;
			if(items !=null){
				Iterator iterate = items.iterator();
				while(iterate.hasNext()){

					Item item = (Item) iterate.next();
					if(!permanentDelete){
						NetworkCall.deleteItem(item);
					}
					else{
						NetworkCall.deleteItemPermanent(item);
					}
					publishProgress(STATUS_PROGRESS , String.valueOf(++i));
				}
				publishProgress(STATUS_COMPLETED , "Deleted all the items");
			}

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
			Log.e(TAG, "DeleteItemAsyncTask -> Error occured: " + e.getMessage());
			e.printStackTrace();
			//publishProgress(STATUS_ERROR, e.getMessage());
			//should not display any url to user
			publishProgress(STATUS_ERROR, e.getMessage());

		}
		return null;

	}

	@Override
	protected void onProgressUpdate(String... progress) {
		super.onProgressUpdate(progress);
		caller.activity_onProgressUpdate(progress);
	}

	@Override
	protected void onPostExecute(String str) {
		caller.activity_OnPostExecute();
	}

}
