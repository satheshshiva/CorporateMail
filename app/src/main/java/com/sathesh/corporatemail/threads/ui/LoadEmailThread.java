/**
 *
 */
package com.sathesh.corporatemail.threads.ui;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.sathesh.corporatemail.BuildConfig;
import com.sathesh.corporatemail.application.MailApplication;
import com.sathesh.corporatemail.cache.adapter.CachedMailBodyAdapter;
import com.sathesh.corporatemail.constants.Constants;
import com.sathesh.corporatemail.customexceptions.NoUserSignedInException;
import com.sathesh.corporatemail.ews.EWSConnection;
import com.sathesh.corporatemail.ews.MailFunctions;
import com.sathesh.corporatemail.ews.NetworkCall;
import com.sathesh.corporatemail.files.AttachmentsManager;
import com.sathesh.corporatemail.fragment.ViewMailFragment;
import com.sathesh.corporatemail.fragment.ViewMailFragment.Status;
import com.sathesh.corporatemail.sqlite.db.cache.vo.CachedMailBodyVO;
import com.sathesh.corporatemail.sqlite.db.cache.vo.CachedMailHeaderVO;

import java.util.List;

import microsoft.exchange.webservices.data.core.ExchangeService;
import microsoft.exchange.webservices.data.core.service.item.EmailMessage;
import microsoft.exchange.webservices.data.property.complex.AttachmentCollection;
import microsoft.exchange.webservices.data.property.complex.FileAttachment;
import microsoft.exchange.webservices.data.property.complex.ItemId;

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
        CachedMailBodyAdapter cacheMailBodyAdapter;
        String from_delimited="",to_delimited="", cc_delimited="", bcc_delimited="";

        try {
            sendHandlerMsg(Status.LOADING);

            mailFunctions = parent.getMailFunctions();
            service = EWSConnection.getServiceFromStoredCredentials(parent.getContext());

            cachedMailHeaderVO= parent.getMailHeaderVo();
            cacheMailBodyAdapter = new CachedMailBodyAdapter(parent.getContext());

            //Mark the item as read in cache
            parent.mailAsReadInCache();

            // get the cached body items by passing item id
            List<CachedMailBodyVO> bodyVOList = cacheMailBodyAdapter.getMailBody(parent.getItemId());

            //TODO revert this disable cache
            if(bodyVOList!=null && bodyVOList.size()>0 && false){
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
                    Log.d(LOG_TAG, "LoadEmailRunnable -> Making network call for setting mail as read");
                }

                NetworkCall.markEmailAsReadUnread( parent.getContext(), parent.getItemId(), true);
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
                parent.setAttachmentsMeta(AttachmentsManager.convertAttachmentCollection(parent.getContext(), attachmentCollection, this));
                sendHandlerMsg(Status.SHOW_ATTACHMENTS);    //shows the attachments

                parent.setTotalInlineImages(AttachmentsManager.getTotalNoOfInlineImgs(attachmentCollection, this));
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
                    AttachmentsManager.downloadInlineImgs(parent.getContext(), attachmentCollection, parent.getItemId(), bodyWithImg, this, this, false);
                }
                //no inline images
                else {
                    //writing VO to cache
                    cacheMailBodyAdapter.cacheNewData(message, parent.getMailType(), parent.getMailFolderName(), parent.getMailFolderId() );

                    if (BuildConfig.DEBUG) {
                        Log.d(LOG_TAG, "No inline images in this email. Inline images counter: "
                                + parent.getRemainingInlineImages());
                    }
                }

                sendHandlerMsg(Status.LOADED);

                // Network call to mark the item as read
                if(BuildConfig.DEBUG){
                    Log.d(LOG_TAG, "LoadEmailRunnable -> Making network call for setting mail as read");
                }
                NetworkCall.markEmailAsReadUnread(parent.getContext(), message.getId().toString(),true);
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
