package com.wipromail.sathesh.threads.ui;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.wipromail.sathesh.constants.Constants;
import com.wipromail.sathesh.ews.EWSConnection;
import com.wipromail.sathesh.ews.NetworkCall;
import com.wipromail.sathesh.service.data.ExchangeService;
import com.wipromail.sathesh.util.Utilities;

/** Thread which deletes a single mail
 * Created by sathesh on 1/18/15.
 */
public class DeleteMailThread extends Thread implements Runnable, Constants {

    private Activity parent;
    private Handler handler;
    private String itemId;
    private boolean permanent;

    public enum Status{
        DELETING,
        DELETED,
        ERROR
    }
    public DeleteMailThread(Activity parent, String itemId, boolean permanent,  Handler handler){
        this.parent=parent;
        this.handler=handler;
        this.itemId=itemId;
        this.permanent=permanent;
    }
    @Override
    public void run() {
        try {
            threadMsg(Status.DELETING);
            ExchangeService service = EWSConnection.getServiceFromStoredCredentials(parent.getApplicationContext());
            if(!permanent) {
                NetworkCall.deleteItemId(service, itemId);
            }
                else{
                NetworkCall.deleteItemIdPermanent(service, itemId);
            }
            threadMsg(Status.DELETED);
        } catch (Exception e) {
            Utilities.generalCatchBlock(e,this);
            threadMsg(Status.ERROR);
        }
    }

    /** Private method for bundling the message and sending it to the handler
     * @param status
     */
    private void threadMsg(Status status) {

        if (status!=null) {
            Message msgObj = handler.obtainMessage();
            Bundle b = new Bundle();
            b.putSerializable("state", status);
            msgObj.setData(b);
            handler.sendMessage(msgObj);
        }

    }
}
