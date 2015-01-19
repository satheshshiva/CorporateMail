package com.wipromail.sathesh.handlers;

import android.os.Handler;
import android.os.Message;

import com.wipromail.sathesh.activity.ViewMailActivity;
import com.wipromail.sathesh.threads.ui.DeleteMailThread;

/**
 * Created by sathesh on 1/18/15.
 */
public class DeleteMailHandler extends Handler {
    private ViewMailActivity parent;

    public DeleteMailHandler(ViewMailActivity parent) {
        this.parent = parent;
    }

    @Override
    public void handleMessage(Message msg) {
        DeleteMailThread.Status status = (DeleteMailThread.Status) msg.getData().getSerializable("state");
        switch (status) {

            case DELETING:
                break;

            case DELETED:
                break;

            case ERROR:
                break;

        }
    }
}