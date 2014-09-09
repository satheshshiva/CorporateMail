/**
 * 
 */
package com.wipromail.sathesh.handlers.runnables;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.wipromail.sathesh.BuildConfig;
import com.wipromail.sathesh.cache.CacheDirectories;
import com.wipromail.sathesh.cache.adapter.CachedMailHeaderCacheAdapter;
import com.wipromail.sathesh.constants.Constants;
import com.wipromail.sathesh.customexceptions.NoUserSignedInException;
import com.wipromail.sathesh.ews.EWSConnection;
import com.wipromail.sathesh.ews.MailFunctions;
import com.wipromail.sathesh.ews.NetworkCall;
import com.wipromail.sathesh.fragment.ViewMailFragment;
import com.wipromail.sathesh.fragment.ViewMailFragment.Status;
import com.wipromail.sathesh.service.data.Attachment;
import com.wipromail.sathesh.service.data.AttachmentCollection;
import com.wipromail.sathesh.service.data.EmailMessage;
import com.wipromail.sathesh.service.data.ExchangeService;
import com.wipromail.sathesh.service.data.FileAttachment;
import com.wipromail.sathesh.service.data.ItemId;
import com.wipromail.sathesh.service.data.MessageBody;
import com.wipromail.sathesh.service.data.ServiceLocalException;
import com.wipromail.sathesh.service.data.ServiceVersionException;
import com.wipromail.sathesh.sqlite.db.pojo.vo.CachedMailHeaderVO;
import com.wipromail.sathesh.util.Utilities;

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
		// TODO Auto-generated method stub

		List<FileAttachment> successfulCachedImages;
		EmailMessage message;
		MessageBody msgBody;
		CachedMailHeaderVO cachedMailHeaderVO;

		try {
			sendHandlerMsg(Status.LOADING);
			mailFunctions = parent.getMailFunctions();
			service = EWSConnection.getServiceFromStoredCredentials(parent.getActivity());
			
			cachedMailHeaderVO= parent.getCachedMailHeader();
			
			//EWS call for loading the message
			message=EmailMessage.bind(service, new ItemId(cachedMailHeaderVO.getItem_id()));

			//performance improvement.. mark the item as read in cache.
			message.setIsRead(true);
			//CODE HAS TO BE WRITTEN HERE
			//	CacheInboxAdapter.writeCacheInboxData(activity, message, true);

			msgBody=mailFunctions.getBody(message);
			parent.setProcessedHtml(MessageBody.getStringFromMessageBody(msgBody));
			parent.setFrom(mailFunctions.getFrom(message));
			parent.setTo(mailFunctions.getTo(message));
			parent.setCc(mailFunctions.getCC(message));
			parent.setSubject(mailFunctions.getSubject(message));
			parent.setDate(mailFunctions.getDateTimeReceived(message));
			parent.setMessage(message);
			parent.setMsgBody(msgBody);
			sendHandlerMsg(Status.SHOW_BODY);	//shows the headers and body 
			attachmentCollection= message.getAttachments();
			parent.setTotalInlineImages(getNoOfInlineImgs(attachmentCollection));
			parent.setRemainingInlineImages(parent.getTotalInlineImages());
			if(parent.getRemainingInlineImages() > 0){
				sendHandlerMsg(Status.SHOW_IMG_LOADING_PROGRESSBAR);
				parent.setProcessedHtml(processBodyHTMLWithImages(attachmentCollection));	//replace all the inline image "cid" tags with "file://" tags
				successfulCachedImages=cacheInlineImages(attachmentCollection);		//caching images is done here. html body will be refreshed after each img download
			}
			else{
				if(BuildConfig.DEBUG) Log.d(TAG, "No inline images in this email. Inline images counter: " +parent.getRemainingInlineImages());
			}
			
			sendHandlerMsg(Status.LOADED, parent.getProcessedHtml());
			
			//Mark the item as read
			//First mark it in the cache
			CachedMailHeaderCacheAdapter mailHeaderAdapter = new CachedMailHeaderCacheAdapter(parent.getActivity());
			mailHeaderAdapter.markMailAsRead(parent.getActivity(), cachedMailHeaderVO.getItem_id());
			// Network call to mark the item as read
			NetworkCall.markEmailAsRead(parent.getActivity(), message);

		} catch(NoUserSignedInException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			sendHandlerMsg(Status.ERROR, e.getMessage());
		} catch(Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			sendHandlerMsg(Status.ERROR, e.getMessage());
		}
	}

	private String processBodyHTMLWithImages(AttachmentCollection attachmentCollection) throws Exception {
		// TODO Auto-generated method stub

		String bodyWithImage=MessageBody.getStringFromMessageBody(parent.getMsgBody());
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
					directoryPath= getCacheImageDirectory(parent.getMessage());
					imagePath=getCacheImagePath(directoryPath, attachment);

					imageHtmlUrl=Utilities.getHTMLImageUrl(attachment.getContentType(), imagePath);
					if(BuildConfig.DEBUG){
						Log.d(TAG, "Replacing " + cid + " in body with " + imageHtmlUrl);
					}
					bodyWithImage=bodyWithImage.replaceAll(cid, imageHtmlUrl);
					//Log.d(TAG, "ViewMailActivity -> Body with image "+bodyWithImage);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return no;
	}

	private List<FileAttachment> cacheInlineImages(AttachmentCollection attachmentCollection){
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

							file = new File(getCacheImageDirectory(parent.getMessage()));

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
									Log.e(TAG, "ViewMailActivity -> Exception while downloading atttachment");
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
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (ServiceLocalException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (Exception e) {
						// TODO Auto-generated catch block
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

	private String getCacheImageDirectory(EmailMessage message) throws ServiceLocalException, Exception{
		return CacheDirectories.getApplicationCacheDirectory(parent.getActivity())+"/" + CACHE_DIRECTORY_MAILCACHE + "/" + mailFunctions.getItemId(message);
		//return MailApplication.getApplicationCacheDirectory(activity).toString() ;
	}
}
