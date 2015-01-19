/**
 *
 */
package com.wipromail.sathesh.threads.ui;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.wipromail.sathesh.BuildConfig;
import com.wipromail.sathesh.application.MailApplication;
import com.wipromail.sathesh.cache.adapter.CachedMailBodyAdapter;
import com.wipromail.sathesh.cache.adapter.CachedMailHeaderAdapter;
import com.wipromail.sathesh.constants.Constants;
import com.wipromail.sathesh.customexceptions.NoUserSignedInException;
import com.wipromail.sathesh.ews.EWSConnection;
import com.wipromail.sathesh.ews.MailFunctions;
import com.wipromail.sathesh.ews.NetworkCall;
import com.wipromail.sathesh.fragment.ViewMailFragment;
import com.wipromail.sathesh.fragment.ViewMailFragment.Status;
import com.wipromail.sathesh.service.data.AttachmentCollection;
import com.wipromail.sathesh.service.data.EmailMessage;
import com.wipromail.sathesh.service.data.ExchangeService;
import com.wipromail.sathesh.service.data.FileAttachment;
import com.wipromail.sathesh.service.data.ItemId;
import com.wipromail.sathesh.sqlite.db.cache.vo.CachedMailBodyVO;
import com.wipromail.sathesh.sqlite.db.cache.vo.CachedMailHeaderVO;

import java.util.List;

/**
 * @author sathesh
 *
 */
public class LoadEmailThread extends Thread implements Runnable, Constants{

    private ViewMailFragment parent;
    private Handler handler;
    private ExchangeService service;
    private AttachmentCollection attachmentCollection;
    private MailFunctions mailFunctions;

    public LoadEmailThread(ViewMailFragment viewMailFragment, Handler handler){
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

                from_delimited = MailApplication.getDelimitedAddressString(message.getFrom());
                to_delimited = MailApplication.getDelimitedAddressString(message.getToRecipients());
                cc_delimited = MailApplication.getDelimitedAddressString(message.getCcRecipients());
                bcc_delimited = MailApplication.getDelimitedAddressString(message.getBccRecipients());

                parent.setFrom(from_delimited);
                parent.setTo(to_delimited);
                parent.setCc(cc_delimited);
                parent.setBcc(bcc_delimited);

                sendHandlerMsg(Status.SHOW_BODY);    //shows the headers and body
                attachmentCollection = message.getAttachments();
                parent.setTotalInlineImages(MailApplication.getTotalNoOfInlineImgs(attachmentCollection, this));
                parent.setRemainingInlineImages(parent.getTotalInlineImages());

                //if inline images present
                if (parent.getRemainingInlineImages() > 0) {
                    //replace all the inline image "cid" tags with "file://" tags
                    String bodyWithImg = MailApplication.getBodyWithImgHtml(parent.getContext(), parent.getProcessedHtml(), attachmentCollection, parent.getItemId(), this);

                    //writing VO to cache with the custom body
                    cacheMailBodyAdapter.cacheNewData(message, bodyWithImg, parent.getMailType(), parent.getMailFolderName(), parent.getMailFolderId());

                    sendHandlerMsg(Status.SHOW_IMG_LOADING_PROGRESSBAR);
                    parent.setProcessedHtml(bodyWithImg);

                    // download and cache images. html body will be refreshed after each img download to show the imgs
                    MailApplication.cacheInlineImages(parent.getContext(), attachmentCollection, parent.getItemId(), bodyWithImg, this, this);
                }
                //no inline images
                else {
                    //writing VO to cache
                    cacheMailBodyAdapter.cacheNewData(message, parent.getMailType(), parent.getMailFolderName(), parent.getMailFolderId() );

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
    public void sendHandlerMsg(Status status, String msg) {
        if (status!=null) {
            Message msgObj = handler.obtainMessage();
            Bundle b = new Bundle();
            b.putSerializable("state", status);
            b.putString("message", msg);
            msgObj.setData(b);
            handler.sendMessage(msgObj);
        }
    }

    public ViewMailFragment getParent() {
        return parent;
    }

    public void setParent(ViewMailFragment parent) {
        this.parent = parent;
    }
}
