package com.wipromail.sathesh.handlers;

import android.os.Handler;
import android.os.Message;

import com.wipromail.sathesh.fragment.MailListViewFragment;

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
        MailListViewFragment.UndoBarStatus status = (MailListViewFragment.UndoBarStatus)msg.getData().getSerializable("state");
        switch(status){

            case  DELETING:
                parent.setUndoBarState(MailListViewFragment.UndoBarStatus.DELETING);
                break;

            case IDLE:
                parent.setUndoBarState(MailListViewFragment.UndoBarStatus.IDLE);
                parent.refreshList();
                break;

        }
    }
}
