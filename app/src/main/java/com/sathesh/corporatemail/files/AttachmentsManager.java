package com.sathesh.corporatemail.files;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import com.sathesh.corporatemail.BuildConfig;
import com.sathesh.corporatemail.cache.CacheDirectories;
import com.sathesh.corporatemail.constants.Constants;
import com.sathesh.corporatemail.customexceptions.NoInternetConnectionException;
import com.sathesh.corporatemail.datamodels.FileAttachmentMeta;
import com.sathesh.corporatemail.ews.NetworkCall;
import com.sathesh.corporatemail.fragment.ViewMailFragment;
import com.sathesh.corporatemail.threads.ui.LoadEmailThread;
import com.sathesh.corporatemail.util.Utilities;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import microsoft.exchange.webservices.data.core.exception.service.local.ServiceVersionException;
import microsoft.exchange.webservices.data.property.complex.Attachment;
import microsoft.exchange.webservices.data.property.complex.AttachmentCollection;
import microsoft.exchange.webservices.data.property.complex.FileAttachment;

public class AttachmentsManager implements Constants {

    public static List<FileAttachmentMeta> convertAttachmentCollection(Context context, AttachmentCollection attachmentCollection, Object thisClass){
        List<FileAttachmentMeta> attachments = new ArrayList<>();
        FileAttachmentMeta attachment;
        try {
            for(Attachment fAttach:  attachmentCollection){
                //if(attachment.getIsInline() && attachment.getContentType()!=null && attachment.getContentType().contains("image") && !(attachment.getContentType().equalsIgnoreCase("message/rfc822"))){
                if (!fAttach.getIsInline() && !(fAttach.getContentType() != null && fAttach.getContentType().equalsIgnoreCase("message/rfc822"))){
                    attachment = new FileAttachmentMeta();
                    attachment.setFileName(fAttach.getName());
                    attachment.setContentType(fAttach.getContentType());
                    attachment.setSize(fAttach.getSize());
                    attachment.setHumanReadableSize(android.text.format.Formatter.formatShortFileSize(context, fAttach.getSize()));
                    //setting the attachment id
                    attachment.setId(fAttach.getId());
                    attachments.add(attachment);
                }
            }
        } catch (ServiceVersionException e) {
            Utilities.generalCatchBlock(e, thisClass);
        }
        return attachments;
    }

    /** Returns the no of inline images
     *
     * @param attachmentCollection
     * @return
     */
    public static int getTotalNoOfInlineImgs(AttachmentCollection attachmentCollection, Object thisClass){
        int no=0;
        try {
            for(Attachment attachment:  attachmentCollection){
                //if(attachment.getIsInline() && attachment.getContentType()!=null && attachment.getContentType().contains("image") && !(attachment.getContentType().equalsIgnoreCase("message/rfc822"))){
                if(attachment.getIsInline() && !(attachment.getContentType()!=null && attachment.getContentType().equalsIgnoreCase("message/rfc822"))){
                    no++;
                }
            }
        } catch (ServiceVersionException e) {
            Utilities.generalCatchBlock(e, thisClass);
        }
        return no;
    }

    /**
     * Thread for downloading an attachments given its file attachment meta with the attachment id
     */
    public static class DownloadAttachmentThread extends Thread{
        public static final int CREATING_DIRS = 0;
        public static final int DOWNLOAD_STARTED = 1;
        public static final int DOWNLOAD_SUCCESS = 2;
        public static final int DOWNLOAD_ERROR = 3;

        private Context context;
        private FileAttachmentMeta fileAttachmentMeta;
        private Handler handler;

        /***
         *
         * @param context
         * @param fileAttachmentMeta
         * @param handler
         */
        public DownloadAttachmentThread(@NonNull Context context, @NonNull FileAttachmentMeta fileAttachmentMeta, @NonNull Handler handler){
            this.context = context;
            this.fileAttachmentMeta = fileAttachmentMeta;
            this.handler = handler;
        }
        @Override
        public void run() {
                FileOutputStream fos = null;
                String dir = "";
                String actualFilePath=null;
                try {
                    handler.sendEmptyMessage(CREATING_DIRS);
                    dir = CacheDirectories.getAttachmentsCacheDirectory(context) + "/" + fileAttachmentMeta.getId();
                    new File(dir).mkdirs();
                    actualFilePath = dir + "/" + fileAttachmentMeta.getFileName();
                    fos = new FileOutputStream(actualFilePath);
                    handler.sendEmptyMessage(DOWNLOAD_STARTED);
                    //Network call
                    NetworkCall.downloadAttachment(context, fileAttachmentMeta.getId(), fos);
                    handler.sendMessage(handler.obtainMessage(DOWNLOAD_SUCCESS, actualFilePath));
                } catch (Exception e) {
                    e.printStackTrace();
                    handler.sendEmptyMessage(DOWNLOAD_ERROR);
                    if (actualFilePath!=null) {
                        new File(actualFilePath).delete();
                    }
                } finally {
                    try {fos.flush();} catch (Exception ignored) {}
                    try {fos.close();} catch (Exception ignored) {}
                }
        }
    }


    public static void downloadInlineImgs(Context context, AttachmentCollection attachmentCollection, String itemId, String body, LoadEmailThread loadEmailThread, Object thisClass, boolean hardReDownload){
        String path="";
        File file;
        FileAttachment fileAttachment;
        FileOutputStream fos;

        for(Attachment attachment:  attachmentCollection){

            if(attachment!=null ){
                if(BuildConfig.DEBUG){
                    Log.d(LOG_TAG, "LoadEmailRunnable -> cacheInlineImages() -> Processing attachment: " + attachment.getName() + " Attachment type " + attachment.getContentType());
                }
                //if(!(attachment.getContentType().equalsIgnoreCase("message/rfc822")) ){
                if(!(attachment.getContentType()!=null && attachment.getContentType().equalsIgnoreCase("message/rfc822")) ){
                    fileAttachment=(FileAttachment)attachment;
					/*
			System.out.println("Is Inline " + fa.getIsInline());
			System.out.println("Name " + fa.getName());
			System.out.println("Size " + fa.getSize());
			System.out.println("Content id " + fa.getContentId());
			System.out.println("Id " + fa.getId());
			System.out.println("Content location " + fa.getContentLocation());
			System.out.println("content type " + fa.getContentType());
			System.out.println("\n");
					 */

                    try {
                        //if(fileAttachment.getIsInline() && fileAttachment.getContentType()!=null && fileAttachment.getContentType().contains("image")){
                        if(fileAttachment.getIsInline()){
                            file = new File(CacheDirectories.getMailCacheImageDirectory(context) + "/" + itemId);

                            file.mkdirs();
                            path=file.getPath() + "/" + attachment.getName();

                            if(BuildConfig.DEBUG){
                                Log.d(LOG_TAG, "Caching image file " +fileAttachment.getName() );
                            }
                            if(hardReDownload || !(new File(path)).exists()){
                                //EWS call
                                fos = new FileOutputStream(path);
                                try{
                                    NetworkCall.downloadAttachment(fileAttachment, fos);
                                }
                                catch(Exception e){
                                    Log.e(LOG_TAG, "ViewMailActivity -> Exception while downloading attachment");
                                    new File(path).delete();
                                    e.printStackTrace();
                                }
                            }
                            if(loadEmailThread !=null ) {
                                loadEmailThread.getParent().setRemainingInlineImages(loadEmailThread.getParent().getRemainingInlineImages() - 1);
                                loadEmailThread.sendHandlerMsg(ViewMailFragment.Status.DOWNLOADED_AN_IMAGE, body);
                            }
                        }
                        else{
                            Log.d(LOG_TAG, "ViewMailActivity -> cacheInlineImages() -> Skipping attachment: File name:" + fileAttachment.getFileName() + " as it is not an inline image" );
                        }
                    } catch (Exception e) {
                        Utilities.generalCatchBlock(e, thisClass);
                    }

                }
                else{
                    Log.d(LOG_TAG, "ViewMailActivity -> Skipping message attachment of the content type message/rfc822");
                }
            }
            else{
                Log.e(LOG_TAG, "ViewMailActivity -> The attachment or its content type is null. Not processing this attachment!");
            }
        }
    }
}
