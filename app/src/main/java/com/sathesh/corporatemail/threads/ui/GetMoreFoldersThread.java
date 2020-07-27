package com.sathesh.corporatemail.threads.ui;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.sathesh.corporatemail.BuildConfig;
import com.sathesh.corporatemail.R;
import com.sathesh.corporatemail.constants.Constants;
import com.sathesh.corporatemail.constants.DrawerMenuRowType;
import com.sathesh.corporatemail.ews.EWSConnection;
import com.sathesh.corporatemail.ews.NetworkCall;
import com.sathesh.corporatemail.sqlite.db.cache.dao.MoreFoldersDAO;
import com.sathesh.corporatemail.sqlite.db.cache.vo.FolderVO;
import com.sathesh.corporatemail.util.Utilities;

import java.util.ArrayList;
import java.util.List;

import microsoft.exchange.webservices.data.core.ExchangeService;
import microsoft.exchange.webservices.data.core.enumeration.property.WellKnownFolderName;
import microsoft.exchange.webservices.data.core.service.folder.Folder;
import microsoft.exchange.webservices.data.property.complex.FolderId;
import microsoft.exchange.webservices.data.search.FindFoldersResults;

/**
 * Created by Sathesh on 7/16/15.
 */
public class GetMoreFoldersThread extends Thread implements Runnable, Constants {

    private Context context;
    private Handler handler;
    private ExchangeService service;
    private MoreFoldersDAO dao;
    private List<FolderVO> listVos;
    private Status currentStatus;

    public enum Status{
        RUNNING,
        FOLDER_RETRIEVED,
        COMPLETED,
        ERROR
    }

    public GetMoreFoldersThread(Context context, Handler handler) {
        this.context = context;
        this.handler = handler;
        listVos = new ArrayList<>();
        this.dao = new MoreFoldersDAO(context);
    }

    @Override
    public void run() {
        FolderVO vo;
        try {
            sendHandlerMsg(Status.RUNNING);
            listVos.clear();    //clear the vo list on every run
            this.currentStatus = Status.RUNNING;
            service = EWSConnection.getServiceFromStoredCredentials(this.context);

            // call the recursive folder search from the root
            recursivePopulateFolders(service, FolderId.getFolderIdFromWellKnownFolderName(WellKnownFolderName.MsgFolderRoot), "MsgFolderRoot");

            // one empty row since last folder might hide inside the navigation bar
            vo = new FolderVO();
            vo.setType(DrawerMenuRowType.MoreFolders.EMPTY_ROW);
            listVos.add(vo);

            //clear the table before populating anything
            dao.deleteAllRecords();
            dao.createOrUpdate(listVos);

            if (BuildConfig.DEBUG) Log.i(LOG_TAG, "RECURSIVE FOLDER PROCESS COMPLETED");
            sendHandlerMsg(Status.COMPLETED);
            this.currentStatus = Status.COMPLETED;

        } catch (Exception e) {
            sendHandlerMsg(Status.ERROR);
            this.currentStatus = Status.ERROR;
            Utilities.generalCatchBlock(e, this);
        }
    }

    private  void recursivePopulateFolders( ExchangeService service, FolderId folderId, String folderName) throws Exception{
        FolderVO vo;
        String subFolderDispName;

        if(BuildConfig.DEBUG) Log.i(LOG_TAG, "PROCESSING FOLDER " + folderName);
        vo = new FolderVO();
        vo.setType(DrawerMenuRowType.MoreFolders.HEADER);
        vo.setName(folderName);
        vo.setFolder_id(folderId.getUniqueId());
        listVos.add(vo);

        //EWS call
        FindFoldersResults findResults =  NetworkCall.getFolders(service, folderId);

        // prints the folders and their count
        for(Folder subFolder : findResults.getFolders())
        {
            subFolderDispName = subFolder.getDisplayName();
            //Dont consider these folder names in the message root
            if(!( folderName.equals("MsgFolderRoot") &&
                    subFolderDispName.equals("Calendar") ||
                    subFolderDispName.equals("Contacts") ||
                    subFolderDispName.equals("Conversation Action Settings") ||
                    subFolderDispName.equals("Deleted Items") ||
                    subFolderDispName.equals("Drafts") ||
                    subFolderDispName.equals("Inbox") ||
                    subFolderDispName.equals("Journal") ||
                    subFolderDispName.equals("Notes") ||
                    subFolderDispName.equals("Quick Step Settings") ||
                    subFolderDispName.equals("RSS Feeds") ||
                    subFolderDispName.equals("Sent Items") ||
                    subFolderDispName.equals("Sync Issues") ||
                    subFolderDispName.equals("Tasks") ) )
            {
                sendHandlerMsg(Status.FOLDER_RETRIEVED);
                //Log.i(TAG, folderId.getFolderName().toString());
                if(BuildConfig.DEBUG) Log.i(LOG_TAG, "Count======" + subFolder.getChildFolderCount());
                if(BuildConfig.DEBUG) Log.i(LOG_TAG, "Name=======" + subFolderDispName);
                if(BuildConfig.DEBUG) Log.i(LOG_TAG, "Folder id=======" + subFolder.getId().getUniqueId());

                vo = new FolderVO();
                vo.setType(DrawerMenuRowType.MoreFolders.FOLDER);
                vo.setName(subFolderDispName);
                vo.setFont_icon(getFontIcon(subFolderDispName, folderName));
                vo.setFolder_id(subFolder.getId().getUniqueId());
                vo.setParent_name(folderName);
                listVos.add(vo);

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

    /** private method for getting the font icon for the folder
      *
     * @param folderDispName
     */
    private String getFontIcon(String folderDispName, String parentFolderName) {

        //assign the inbox icon for the subfolders also

        if(folderDispName.equalsIgnoreCase("Junk E-mail"))
            return context.getString(R.string.fontIcon_drawer_junk_email);
        if(folderDispName.equalsIgnoreCase("Clutter"))
            return context.getString(R.string.fontIcon_drawer_clutter);
        if(folderDispName.equalsIgnoreCase("Outbox"))
            return context.getString(R.string.fontIcon_drawer_outbox);

        if(parentFolderName.equalsIgnoreCase("Inbox"))
            return context.getString(R.string.fontIcon_drawer_inbox);
        if(parentFolderName.equalsIgnoreCase("MsgFolderRoot"))
            return context.getString(R.string.fontIcon_drawer_inbox);
        //default more folder icon
        return context.getString(R.string.fontIcon_drawer_folder);

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

    /** GETTER SETTER PART **/
    public Status getCurrentStatus() {
        return currentStatus;
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }
}

