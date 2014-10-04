package com.wipromail.sathesh.asynctask;

import android.os.AsyncTask;
import android.util.Log;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.wipromail.sathesh.R;
import com.wipromail.sathesh.asynctask.interfaces.GenericAsyncTask;
import com.wipromail.sathesh.constants.Constants;
import com.wipromail.sathesh.ews.EWSConnection;
import com.wipromail.sathesh.ews.NetworkCall;
import com.wipromail.sathesh.service.data.ExchangeService;

import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class DeleteItemIdsAsyncTask extends AsyncTask<String, String, String> implements Constants{

    public static final String STATUS_DELETING="DELETING";
    public static final String STATUS_COMPLETED="COMPLETED";
    public static final String STATUS_PROGRESS="PROGRESS";
    public static final String STATUS_ERROR="ERROR";

    private SherlockFragmentActivity activity;

    private ArrayList<String> itemIds;
    private GenericAsyncTask caller;
    private boolean permanentDelete;
    private ExchangeService service;

    public DeleteItemIdsAsyncTask(GenericAsyncTask caller, SherlockFragmentActivity activity, String itemId, boolean permanentDelete) {
        // TODO Auto-generated constructor stub
        this.activity = activity;
        this.itemIds =new ArrayList<String>();
        this.itemIds.add(itemId);
        this.caller = caller;
        this.permanentDelete=permanentDelete;
    }

    public DeleteItemIdsAsyncTask(GenericAsyncTask caller, SherlockFragmentActivity activity, ArrayList<String> itemIds, boolean permanentDelete) {
        // TODO Auto-generated constructor stub
        this.activity = activity;
        this.itemIds = itemIds;
        this.caller = caller;
        this.permanentDelete=permanentDelete;
    }


    @Override
    protected void onPreExecute() {
        // TODO Auto-generated method stub

        caller.activity_OnPreExecute();

    }

    @Override
    protected String doInBackground(String... str) {

        publishProgress(STATUS_DELETING , "Started Deleting");

        try{
            service = EWSConnection.getServiceFromStoredCredentials(activity.getApplicationContext());
            int i=0;
            for(String itemId: itemIds){
                if(!permanentDelete){
                    NetworkCall.deleteItemId(service, itemId);
                }
                else{
                    NetworkCall.deleteItemIdPermanent(service, itemId);
                }
                publishProgress(STATUS_PROGRESS , String.valueOf(++i));
            }
            publishProgress(STATUS_COMPLETED , "Deleted all the items");
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
