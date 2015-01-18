package com.wipromail.sathesh.threads;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.wipromail.sathesh.BuildConfig;
import com.wipromail.sathesh.constants.Constants;
import com.wipromail.sathesh.ews.EWSConnection;
import com.wipromail.sathesh.ews.NetworkCall;
import com.wipromail.sathesh.fragment.MailListViewFragment;
import com.wipromail.sathesh.service.data.ExchangeService;

import java.util.ArrayList;

/**
 * Created by sathesh on 1/17/15.
 */
public class DeleteMultipleMailsThread extends Thread implements Runnable, Constants {
    private MailListViewFragment parent;
    private ArrayList<String> itemIds;
    private Handler handler;
    public DeleteMultipleMailsThread(MailListViewFragment parent, ArrayList<String> itemIds, Handler handler)
    {
        this.parent=parent;
        this.itemIds=itemIds;
        this.handler= handler;
    }
    @Override
    public void run() {
        try {
            threadMsg(MailListViewFragment.UndoBarStatus.DELETING);
            if(BuildConfig.DEBUG) {
                Log.d(TAG, "DeleteMultipleMailsThread -> Item count for deletion " + itemIds.size());
            }
            ExchangeService service = EWSConnection.getServiceFromStoredCredentials(parent.getActivity().getApplicationContext());
            NetworkCall.deleteItemIds(service, itemIds);

        } catch (Exception e) {
            e.printStackTrace();
        }

        threadMsg(MailListViewFragment.UndoBarStatus.IDLE);

    }

    /** Private method for bundling the message and sending it to the handler
     * @param status
     */
    private void threadMsg(MailListViewFragment.UndoBarStatus status) {

        if (status!=null) {
            Message msgObj = handler.obtainMessage();
            Bundle b = new Bundle();
            b.putSerializable("state", status);
            msgObj.setData(b);
            handler.sendMessage(msgObj);
        }

    }
}
