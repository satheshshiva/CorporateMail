package com.sathesh.corporatemail.handlers;

import android.os.Handler;
import android.os.Message;

import com.sathesh.corporatemail.fragment.datapasser.MailListFragmentDataPasser;
import com.sathesh.corporatemail.fragment.MailListViewFragment;
import com.sathesh.corporatemail.threads.ui.DeleteMultipleMailsThread;

/**
 * Created by sathesh on 1/18/15.
 */
public class DeleteMultipleMailsHandler extends Handler {
    private MailListFragmentDataPasser fragment;

    public DeleteMultipleMailsHandler(MailListFragmentDataPasser fragment){
        this.fragment = fragment;
    }

    @Override
    public void handleMessage(Message msg) {
        DeleteMultipleMailsThread.Status status = (DeleteMultipleMailsThread.Status)msg.getData().getSerializable("state");
        switch(status){

            case  DELETING:
                if(fragment !=null){
                    fragment.setUndoBarState(MailListViewFragment.UndoBarStatus.DELETING);
                }
                break;

            case COMPLETED:
                if(fragment !=null) {
                    fragment.setUndoBarState(MailListViewFragment.UndoBarStatus.IDLE);
                    fragment.refreshList();
                }
                break;
        }
    }
}
