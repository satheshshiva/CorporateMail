package com.wipromail.sathesh.handlers;

import android.os.Handler;
import android.os.Message;

import com.wipromail.sathesh.fragment.MailListViewFragment;
import com.wipromail.sathesh.threads.ui.DeleteMultipleMailsThread;

/**
 * Created by sathesh on 1/18/15.
 */
public class DeleteMultipleMailsHandler extends Handler {
    private MailListViewFragment parent;

    public DeleteMultipleMailsHandler(MailListViewFragment parent){
        this.parent=parent;
    }

    @Override
    public void handleMessage(Message msg) {
        DeleteMultipleMailsThread.Status status = (DeleteMultipleMailsThread.Status)msg.getData().getSerializable("state");
        switch(status){

            case  DELETING:
                if(parent!=null){
                    parent.setUndoBarState(MailListViewFragment.UndoBarStatus.DELETING);
                }
                break;

            case COMPLETED:
                if(parent!=null) {
                    parent.setUndoBarState(MailListViewFragment.UndoBarStatus.IDLE);
                    parent.refreshList();
                }
                break;
        }
    }
}
