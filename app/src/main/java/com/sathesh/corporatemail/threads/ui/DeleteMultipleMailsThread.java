package com.sathesh.corporatemail.threads.ui;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.sathesh.corporatemail.BuildConfig;
import com.sathesh.corporatemail.constants.Constants;
import com.sathesh.corporatemail.ews.EWSConnection;
import com.sathesh.corporatemail.ews.NetworkCall;
import com.sathesh.corporatemail.util.Utilities;

import java.util.ArrayList;

import microsoft.exchange.webservices.data.core.ExchangeService;

/**
 * Created by sathesh on 1/17/15.
 */
public class DeleteMultipleMailsThread extends Thread implements Runnable, Constants {
    private Context context;
    private ArrayList<String> itemIds;
    private Handler handler;
    private boolean permanent;
    public enum Status{
        DELETING,
        COMPLETED
    }
    public DeleteMultipleMailsThread(Context context, ArrayList<String> itemIds, boolean permanent, Handler handler)
    {
        this.context=context;
        this.itemIds=itemIds;
        this.handler= handler;
        this.permanent = permanent;
    }
    @Override
    public void run() {
        try {
            threadMsg(Status.DELETING);
            if(BuildConfig.DEBUG) {
                Log.d(LOG_TAG, "DeleteMultipleMailsThread -> Item count for deletion " + itemIds.size());
            }
            ExchangeService service = EWSConnection.getInstance(context);
            NetworkCall.deleteItemIds(service, itemIds, permanent);

        } catch (Exception e) {
            Utilities.generalCatchBlock(e,this);
        }

        threadMsg(Status.COMPLETED);

    }

    /** Private method for bundling the message and sending it to the handler
     * @param status
     */
    private void threadMsg(Status status) {

        if (handler!=null && status!=null) {
            Message msgObj = handler.obtainMessage();
            Bundle b = new Bundle();
            b.putSerializable("state", status);
            msgObj.setData(b);
            handler.sendMessage(msgObj);
        }

    }
}
