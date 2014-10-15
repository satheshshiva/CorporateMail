/**
 *
 */
package com.wipromail.sathesh.handlers.runnables;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.wipromail.sathesh.BuildConfig;
import com.wipromail.sathesh.cache.CacheDirectories;
import com.wipromail.sathesh.cache.adapter.CachedMailBodyAdapter;
import com.wipromail.sathesh.cache.adapter.CachedMailHeaderAdapter;
import com.wipromail.sathesh.constants.Constants;
import com.wipromail.sathesh.customexceptions.NoUserSignedInException;
import com.wipromail.sathesh.ews.EWSConnection;
import com.wipromail.sathesh.ews.MailFunctions;
import com.wipromail.sathesh.ews.NetworkCall;
import com.wipromail.sathesh.fragment.ViewMailFragment;
import com.wipromail.sathesh.fragment.ViewMailFragment.Status;
import com.wipromail.sathesh.service.data.Attachment;
import com.wipromail.sathesh.service.data.AttachmentCollection;
import com.wipromail.sathesh.service.data.EmailAddress;
import com.wipromail.sathesh.service.data.EmailAddressCollection;
import com.wipromail.sathesh.service.data.EmailMessage;
import com.wipromail.sathesh.service.data.ExchangeService;
import com.wipromail.sathesh.service.data.FileAttachment;
import com.wipromail.sathesh.service.data.ItemId;
import com.wipromail.sathesh.service.data.ServiceLocalException;
import com.wipromail.sathesh.service.data.ServiceVersionException;
import com.wipromail.sathesh.sqlite.db.cache.vo.CachedMailBodyVO;
import com.wipromail.sathesh.sqlite.db.cache.vo.CachedMailHeaderVO;
import com.wipromail.sathesh.util.Utilities;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author sathesh
 *
 */
public class LoadEmailRunnable implements Runnable, Constants{

    private ViewMailFragment parent;
    private Handler handler;
    private ExchangeService service;
    private AttachmentCollection attachmentCollection;
    private String path="";
    private File file;
    private FileAttachment fileAttachment;
    private FileOutputStream fos;
    private List<FileAttachment> successfulCachedImages;
    private MailFunctions mailFunctions;

    public LoadEmailRunnable(ViewMailFragment viewMailFragment, Handler handler){
        this.parent = viewMailFragment;
        this.handler= handler;
    }

    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {

        List<FileAttachment> successfulCachedImages;
        EmailMessage message;
        CachedMailHeaderVO cachedMailHeaderVO;
        CachedMailHeaderAdapter cacheMailHeaderAdapter;
        CachedMailBodyAdapter cacheMailBodyAdapter;
        String from_delimited="",to_delimited="", cc_delimited="", bcc_delimited="";

        try {
            sendHandlerMsg(Status.LOADING);

            mailFunctions = parent.getMailFunctions();
            service = EWSConnection.getServiceFromStoredCredentials(parent.getContext());

            cachedMailHeaderVO= parent.getMailHeader();
            cacheMailHeaderAdapter = new CachedMailHeaderAdapter(parent.getContext());
            cacheMailBodyAdapter = new CachedMailBodyAdapter(parent.getContext());

            //Mark the item as read
            //First mark it in the cache
            cacheMailHeaderAdapter.markMailAsRead(cachedMailHeaderVO.getItem_id());

            // get the cached body items by passing item id
            List<CachedMailBodyVO> bodyVOList = cacheMailBodyAdapter.getMailBody(parent.getItemId());

            if(bodyVOList!=null && bodyVOList.size()>0){
            //cache exist
                //get the first body record from the list of body for the item id. there must be only one record
                CachedMailBodyVO bodyVO = bodyVOList.get(0);
                // restoring the body, from, to etc., from the cache
                parent.setProcessedHtml(bodyVO.getMail_body()); //body
                parent.setTo(bodyVO.getMail_to_delimited());
                parent.setFrom(bodyVO.getMail_from_delimited());
                parent.setCc(bodyVO.getMail_cc_delimited());
                parent.setBcc(bodyVO.getMail_bcc_delimited());

                // the  image cache are handled by the Android System Webview Cache

                sendHandlerMsg(Status.SHOW_BODY);	//shows the headers and body
                sendHandlerMsg(Status.LOADED);      //sets the status message to mail loaded completely

                // Network call to mark the item as read
                if(BuildConfig.DEBUG){
                    Log.d(TAG, "LoadEmailRunnable -> Making network call for setting mail as read");
                }

                NetworkCall.markEmailAsRead(service, parent.getContext(), parent.getItemId());
            }
            else {
            //cache does NOT exist
                //EWS Call - Load the mail from EWS
                message = EmailMessage.bind(service, new ItemId(cachedMailHeaderVO.getItem_id()));

                parent.setProcessedHtml(mailFunctions.getBody(message));

                from_delimited =getAddressString(message.getFrom());
                to_delimited =getAddressString(message.getToRecipients());
                cc_delimited =getAddressString(message.getCcRecipients());
                bcc_delimited =getAddressString(message.getBccRecipients());

                parent.setFrom(from_delimited);
                parent.setTo(to_delimited);
                parent.setCc(cc_delimited);
                parent.setBcc(bcc_delimited);

                sendHandlerMsg(Status.SHOW_BODY);    //shows the headers and body
                attachmentCollection = message.getAttachments();
                parent.setTotalInlineImages(getNoOfInlineImgs(attachmentCollection));
                parent.setRemainingInlineImages(parent.getTotalInlineImages());

                //creating a CachedMailBodyVO to write to cache db
                CachedMailBodyVO vo = new CachedMailBodyVO();
                vo.setItem_id(parent.getItemId());
                vo.setFolder_name(parent.getMailFolderName());
                vo.setFolder_id(parent.getMailFolderId());
                vo.setMail_type(parent.getMailType());
                vo.setMail_from_delimited(from_delimited);
                vo.setMail_to_delimited(to_delimited);
                vo.setMail_cc_delimited(cc_delimited);
                vo.setMail_bcc_delimited(bcc_delimited);

                //if inline images present
                if (parent.getRemainingInlineImages() > 0) {
                    //replace all the inline image "cid" tags with "file://" tags
                    String bodyWithImg = processBodyHTMLWithImages(attachmentCollection, cachedMailHeaderVO);

                    //setting the processed html in the VO cache
                    vo.setMail_body(bodyWithImg);
                    //writing VO to cache
                    cacheMailBodyAdapter.cacheNewData(vo);

                    sendHandlerMsg(Status.SHOW_IMG_LOADING_PROGRESSBAR);
                    parent.setProcessedHtml(bodyWithImg);

                    // download and cache images. html body will be refreshed after each img download to show the imgs
                    successfulCachedImages = cacheInlineImages(attachmentCollection, cachedMailHeaderVO);
                }
                //no inline images
                else {
                    vo.setMail_body(parent.getProcessedHtml()); //this is actually the normal html that comes with the item
                    //Caching the downloaded mail body
                    cacheMailBodyAdapter.cacheNewData(vo);

                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, "No inline images in this email. Inline images counter: "
                                + parent.getRemainingInlineImages());
                    }
                }

                sendHandlerMsg(Status.LOADED);

                // Network call to mark the item as read
                if(BuildConfig.DEBUG){
                    Log.d(TAG, "LoadEmailRunnable -> Making network call for setting mail as read");
                }
                NetworkCall.markEmailAsRead(parent.getContext(), message);
            } // end else cache NOT exist

        } catch(NoUserSignedInException e) {
            e.printStackTrace();
            sendHandlerMsg(Status.ERROR, e.getMessage());
        } catch(Exception e) {
            e.printStackTrace();
            sendHandlerMsg(Status.ERROR, e.getMessage());
        }
    }

    /** Gives the delimited String from EmailAddressCollection which can be stored in the cache db
     *
     * @param recipients -EmailAddressCollection obj
     * @return - delimited String
     */
    private String getAddressString(EmailAddressCollection recipients) {
        StringBuffer str=new StringBuffer();
        for(EmailAddress recipient : recipients){
            if(recipient!=null) {
                str.append(recipient.getName())
                        .append(EMAIL_NAMEEMAIL_STORAGE_DELIM)
                        .append(recipient.getAddress())
                        .append(EMAIL_STORAGE_DELIM);
            }
        }
        return str.toString();
    }

    /** Gives the delimited String from EmailAddress which can be stored in the cache db
     *
     * @param recipient -EmailAddress obj
     * @return - delimited String
     */
    private String getAddressString(EmailAddress recipient) {
        StringBuffer str=new StringBuffer();
        if(recipient!=null) {
            str.append(recipient.getName())
                    .append(EMAIL_NAMEEMAIL_STORAGE_DELIM)
                    .append(recipient.getAddress())
                    .append(EMAIL_STORAGE_DELIM);
        }
        return str.toString();
    }

    /** Replaces the body String "cid:contentid" wih "file:filename". Does only the string change.
     *
     * @param attachmentCollection
     * @return
     * @throws Exception
     */
    private String processBodyHTMLWithImages(AttachmentCollection attachmentCollection, CachedMailHeaderVO cachedMailHeaderVO) throws Exception {

        String bodyWithImage=parent.getProcessedHtml();
        String cid="", directoryPath="", imagePath="", imageHtmlUrl="";
        for(Attachment attachment:  attachmentCollection){

            try {
                if(null != attachment && attachment.getIsInline() && attachment.getContentType()!=null && attachment.getContentType().contains("image")){
                    if(BuildConfig.DEBUG){
                        Log.d(TAG, "ViewMailActivity -> processBodyHTMLWithImages() -> Processing attachment " + attachment.getName());
                    }
                    cid="cid:"+ attachment.getContentId();
                    if(BuildConfig.DEBUG){
                        Log.d(TAG, "ViewMailActivity ->cid "+cid);
                    }
                    directoryPath= getCacheImageDirectory(cachedMailHeaderVO.getItem_id());
                    imagePath=getCacheImagePath(directoryPath, attachment);

                    imageHtmlUrl=Utilities.getHTMLImageUrl(attachment.getContentType(), imagePath);
                    if(BuildConfig.DEBUG){
                        Log.d(TAG, "Replacing " + cid + " in body with " + imageHtmlUrl);
                    }
                    bodyWithImage=bodyWithImage.replaceAll(cid, imageHtmlUrl);
                    //Log.d(TAG, "ViewMailActivity -> Body with image "+bodyWithImage);
                }
            } catch (Exception e) {
                Utilities.generalCatchBlock(e, this.getClass());
            }
        }

        return bodyWithImage;
    }

    private int getNoOfInlineImgs(AttachmentCollection attachmentCollection){
        int no=0;
        try {
            for(Attachment attachment:  attachmentCollection){
                if(attachment.getIsInline() && attachment.getContentType()!=null && attachment.getContentType().contains("image") && !(attachment.getContentType().equalsIgnoreCase("message/rfc822"))){
                    no++;
                }
            }
        } catch (ServiceVersionException e) {
            e.printStackTrace();
        }
        return no;
    }

    private List<FileAttachment> cacheInlineImages(AttachmentCollection attachmentCollection, CachedMailHeaderVO cachedMailHeaderVO){
        successfulCachedImages = new ArrayList<FileAttachment>();
        for(Attachment attachment:  attachmentCollection){

            if(attachment!=null && attachment.getContentType()!= null ){
                if(BuildConfig.DEBUG){
                    Log.d(TAG, "LoadEmailRunnable -> cacheInlineImages() -> Processing attachment: " + attachment.getName() + " Attachment type " + attachment.getContentType());
                }
                if(!(attachment.getContentType().equalsIgnoreCase("message/rfc822")) ){
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
                        if(fileAttachment.getIsInline() && fileAttachment.getContentType()!=null && fileAttachment.getContentType().contains("image")){

                            file = new File(getCacheImageDirectory(cachedMailHeaderVO.getItem_id()));

                            file.mkdirs();
                            path=getCacheImagePath(file.getPath(), attachment);

                            if(BuildConfig.DEBUG){
                                Log.d(TAG, "Caching image file " +fileAttachment.getName() );
                            }
                            if(!((new File(path)).exists())){
                                //EWS call
                                fos = new FileOutputStream(path);
                                try{
                                    NetworkCall.downloadAttachment(fileAttachment, fos);
                                }
                                catch(Exception e){
                                    Log.e(TAG, "ViewMailActivity -> Exception while downloading attachment");
                                    e.printStackTrace();
                                }
                            }
                            parent.setRemainingInlineImages(parent.getRemainingInlineImages()-1);
                            successfulCachedImages.add(fileAttachment);
                            sendHandlerMsg(Status.DOWNLOADED_AN_IMAGE, parent.getProcessedHtml());
                        }
                        else{
                            Log.d(TAG, "ViewMailActivity -> cacheInlineImages() -> Skipping attachment: " + fileAttachment.getFileName() + " as it is not an inline image" );
                        }
                    } catch (ServiceVersionException e) {
                        e.printStackTrace();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (ServiceLocalException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
                else{
                    Log.d(TAG, "ViewMailActivity -> Skipping message attachment of the content type message/rfc822");

                }
            }
            else{
                Log.e(TAG, "ViewMailActivity -> The attachment or its content type is null. Not processing this attachment!");
            }
        }
        return successfulCachedImages;
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
    /** Private method for bundling the message and sending it to the handler
     * @param status
     */
    private void sendHandlerMsg(Status status, String msg) {
        if (status!=null) {
            Message msgObj = handler.obtainMessage();
            Bundle b = new Bundle();
            b.putSerializable("state", status);
            b.putString("message", msg);
            msgObj.setData(b);
            handler.sendMessage(msgObj);
        }
    }

    private String getCacheImagePath(String directoryLoc, Attachment attachment){
        return directoryLoc+ "/"+attachment.getName();
    }

    private String getCacheImageDirectory(String itemId) throws ServiceLocalException, Exception{
        return CacheDirectories.getMailCacheImageDirectory(parent.getContext()) + "/" + itemId;
    }
}
