package com.sathesh.corporatemail.threads.ui;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.sathesh.corporatemail.constants.Constants;
import com.sathesh.corporatemail.ews.EWSConnection;
import com.sathesh.corporatemail.ews.NetworkCall;
import com.sathesh.corporatemail.service.data.ExchangeService;
import com.sathesh.corporatemail.util.Utilities;

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
            if (itemId != null && !itemId.equals("")) {
                threadMsg(Status.DELETING);

                ExchangeService service = EWSConnection.getServiceFromStoredCredentials(parent.getApplicationContext());
                if (!permanent) {
                    NetworkCall.deleteItemId(service, itemId, false);
                } else {
                    NetworkCall.deleteItemId(service, itemId, true);
                }

                threadMsg(Status.DELETED);
            }else{
                Log.e(TAG, "MarkMailsReadUnreadThread -> Item count 0 or null");
                threadMsg(Status.ERROR);
            }
        }

        catch (Exception e) {
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
