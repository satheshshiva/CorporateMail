package com.wipromail.sathesh.threads.ui;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.wipromail.sathesh.BuildConfig;
import com.wipromail.sathesh.constants.Constants;
import com.wipromail.sathesh.ews.NetworkCall;
import com.wipromail.sathesh.util.Utilities;

import java.util.ArrayList;

/**
 * Created by sathesh on 1/17/15.
 */
public class MarkMailsReadUnreadThread extends Thread implements Runnable, Constants {
    private Context context;
    private ArrayList<String> itemIds;
    private Handler handler;
    private boolean isRead;
    public enum Status{
        CONNECTING,
        COMPLETED,
        ERROR
    }
    public MarkMailsReadUnreadThread(Context context, ArrayList<String> itemIds, boolean isRead, Handler handler)
    {
        this.context=context;
        this.itemIds=itemIds;
        this.handler= handler;
        this.isRead=isRead;
    }
    @Override
    public void run() {
        try {
            threadMsg(Status.CONNECTING);
            if(itemIds!=null && itemIds.size()>0) {
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "MarkMailsReadUnreadThread -> Item count for read/unread " + itemIds.size());
                }
                //loop all the item ids and make each call for each item
                for(String itemId : itemIds) {
                    NetworkCall.markEmailAsReadUnread(context, itemId, isRead);
                }
                threadMsg(Status.COMPLETED);
            }else{
                Log.e(TAG, "MarkMailsReadUnreadThread -> Item count 0 or null");
                threadMsg(Status.ERROR);
            }

        } catch (Exception e) {
            Utilities.generalCatchBlock(e,this);
            threadMsg(Status.ERROR);
        }



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
