package com.sathesh.corporatemail.handlers;

import android.os.Handler;
import android.os.Message;

import com.sathesh.corporatemail.activity.datapasser.MailListActivityDataPasser;
import com.sathesh.corporatemail.threads.ui.GetMoreFoldersThread;
import com.sathesh.corporatemail.util.Utilities;

/**
 * Created by Sathesh on 7/21/15.
 */
public class GetMoreFoldersHandler extends Handler {

    private MailListActivityDataPasser parent;

    public GetMoreFoldersHandler(MailListActivityDataPasser parent) {
        this.parent = parent;
    }

    @Override
    public void handleMessage(Message msg) {
        GetMoreFoldersThread.Status status = (GetMoreFoldersThread.Status) msg.getData().getSerializable("state");
        switch (status) {

            case RUNNING:
                break;

            case ERROR:
                break;

            // case FOLDER_RETRIEVED:   // commenting since too much updates in list
            case COMPLETED:
                try {
                    parent.refreshDrawerListRecyclerView2();
                } catch (Exception e) {
                    Utilities.generalCatchBlock(e,this);
                }
                break;

        }
    }
}
