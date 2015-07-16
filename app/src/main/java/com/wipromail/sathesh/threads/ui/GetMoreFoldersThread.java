package com.wipromail.sathesh.threads.ui;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.wipromail.sathesh.BuildConfig;
import com.wipromail.sathesh.constants.Constants;
import com.wipromail.sathesh.ews.EWSConnection;
import com.wipromail.sathesh.ews.NetworkCall;
import com.wipromail.sathesh.service.data.ExchangeService;
import com.wipromail.sathesh.service.data.FindFoldersResults;
import com.wipromail.sathesh.service.data.Folder;
import com.wipromail.sathesh.service.data.FolderId;
import com.wipromail.sathesh.service.data.WellKnownFolderName;
import com.wipromail.sathesh.util.Utilities;

/**
 * Created by Sathesh on 7/16/15.
 */
public class GetMoreFoldersThread extends Thread implements Runnable, Constants {

    private Context context;
    private Handler handler;
    private ExchangeService service;

    private enum Status{
        RUNNING,
        FOLDER_RETRIEVED,
        COMPLETED,
        ERROR
    }

    public GetMoreFoldersThread(Context context, Handler handler) {
        this.context = context;
        this.handler = handler;
    }

    @Override
    public void run() {
        try {
            sendHandlerMsg(Status.RUNNING);
            service = EWSConnection.getServiceFromStoredCredentials(this.context);
            // call the recursive folder search from the root
            recursivePopulateFolders(service, FolderId.getFolderIdFromWellKnownFolderName(WellKnownFolderName.MsgFolderRoot), "MsgFolderRoot");

            if(BuildConfig.DEBUG) Log.i(TAG, "RECURSIVE FOLDER PROCESS COMPLETED");
            sendHandlerMsg(Status.COMPLETED);

        } catch (Exception e) {
            sendHandlerMsg(Status.ERROR);
            Utilities.generalCatchBlock(e,this);
        }
    }

    private  void recursivePopulateFolders( ExchangeService service, FolderId folderId, String folderName) throws Exception{

        if(BuildConfig.DEBUG) Log.i(TAG, "PROCESSING FOLDER " + folderName);

        //EWS call
        FindFoldersResults findResults =  NetworkCall.getFolders(service, folderId);

        // prints the folders and their count
        for(Folder folder : findResults.getFolders())
        {
            //Dont consider these folder names in the message root
            if(!( folderName.equals("MsgFolderRoot") &&
                    folder.getDisplayName().equals("Calendar") ||
                    folder.getDisplayName().equals("Contacts") ||
                    folder.getDisplayName().equals("Conversation Action Settings") ||
                    folder.getDisplayName().equals("Deleted Items") ||
                    folder.getDisplayName().equals("Drafts") ||
                    folder.getDisplayName().equals("Inbox") ||
                    folder.getDisplayName().equals("Journal") ||
                    folder.getDisplayName().equals("Notes") ||
                    folder.getDisplayName().equals("Quick Step Settings") ||
                    folder.getDisplayName().equals("RSS Feeds") ||
                    folder.getDisplayName().equals("Sent Items") ||
                    folder.getDisplayName().equals("Sync Issues") ||
                    folder.getDisplayName().equals("Tasks") ) )
            {
                sendHandlerMsg(Status.FOLDER_RETRIEVED);
                //Log.i(TAG, folderId.getFolderName().toString());
                if(BuildConfig.DEBUG) Log.i(TAG, "Count======" + folder.getChildFolderCount());
                if(BuildConfig.DEBUG) Log.i(TAG, "Name=======" + folder.getDisplayName());
                if(BuildConfig.DEBUG) Log.i(TAG, "Folder id=======" + folder.getId().getUniqueId());
            }

        }

        //Go for recursion for each of the folder
        for(Folder folder : findResults.getFolders()){
            //if the folder has more than 0 subfolders
            if(folder.getChildFolderCount() > 0){
                // dont cosider these folders for subfolder retrieval
                if(!(folder.getDisplayName().equals("Calendar") ||
                        folder.getDisplayName().equals("Contacts")  ||
                        folder.getDisplayName().equals("Conversation Action Settings") ||
                        folder.getDisplayName().equals("Conversation History")  ||
                        folder.getDisplayName().equals("Journal")  ||
                        folder.getDisplayName().equals("Notes")  ||
                        folder.getDisplayName().equals("Quick Step Settings")  ||
                        folder.getDisplayName().equals("RSS Feeds")  ||
                        folder.getDisplayName().equals("Sync Issues") ||
                        folder.getDisplayName().equals("Tasks") ) ){

                    //Thread.sleep(500);
                    recursivePopulateFolders(service, folder.getId(), folder.getDisplayName());
                }
            }
        }
    }

    /** Private method for bundling the message and sending it to the handler
     * @param status
     */
    private void sendHandlerMsg(Status status) {

        if (status!=null) {
            Message msgObj = handler.obtainMessage();
            Bundle b = new Bundle();
            b.putSerializable("state", status);
            msgObj.setData(b);
            handler.sendMessage(msgObj);
        }

    }
}

