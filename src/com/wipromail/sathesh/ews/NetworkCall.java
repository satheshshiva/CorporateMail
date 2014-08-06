package com.wipromail.sathesh.ews;

import java.io.OutputStream;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.util.Log;

import com.wipromail.sathesh.application.Utils;
import com.wipromail.sathesh.constants.Constants;
import com.wipromail.sathesh.customexceptions.NoInternetConnectionException;
import com.wipromail.sathesh.customserializable.ContactSerializable;
import com.wipromail.sathesh.service.data.BasePropertySet;
import com.wipromail.sathesh.service.data.ChangeCollection;
import com.wipromail.sathesh.service.data.ConflictResolutionMode;
import com.wipromail.sathesh.service.data.DeleteMode;
import com.wipromail.sathesh.service.data.EmailMessage;
import com.wipromail.sathesh.service.data.EventType;
import com.wipromail.sathesh.service.data.ExchangeService;
import com.wipromail.sathesh.service.data.FileAttachment;
import com.wipromail.sathesh.service.data.FindFoldersResults;
import com.wipromail.sathesh.service.data.FindItemsResults;
import com.wipromail.sathesh.service.data.FolderId;
import com.wipromail.sathesh.service.data.FolderView;
import com.wipromail.sathesh.service.data.GetEventsResults;
import com.wipromail.sathesh.service.data.Item;
import com.wipromail.sathesh.service.data.ItemChange;
import com.wipromail.sathesh.service.data.ItemEvent;
import com.wipromail.sathesh.service.data.ItemId;
import com.wipromail.sathesh.service.data.ItemSchema;
import com.wipromail.sathesh.service.data.ItemView;
import com.wipromail.sathesh.service.data.MessageBody;
import com.wipromail.sathesh.service.data.NameResolutionCollection;
import com.wipromail.sathesh.service.data.PropertySet;
import com.wipromail.sathesh.service.data.PullSubscription;
import com.wipromail.sathesh.service.data.ResolveNameSearchLocation;
import com.wipromail.sathesh.service.data.ResponseMessage;
import com.wipromail.sathesh.service.data.SearchFilter;
import com.wipromail.sathesh.service.data.SyncFolderItemsScope;
import com.wipromail.sathesh.service.data.WellKnownFolderName;

public class NetworkCall implements Constants{

	//The following method is used for Pull sibscription by Mail Notification Service
	public static PullSubscription subscribePull(Context context, ExchangeService service, List folder) throws NoInternetConnectionException, Exception{
		if(Utils.checkInternetConnection(context)){
			return service.subscribeToPullNotifications(folder,1440,
					//timeOut: the subscription will end if the server is not polled within x minutes.  min:1 max:1440,
					null  /*watermark: null to start a new subscription*/,
					EventType.NewMail);
		}
		else{	throw new NoInternetConnectionException(); }
	}

	//The following method is used for Pull subscription by MailFunctions Update Sync
	public static PullSubscription subscribePullInboxSync(Context context, ExchangeService service, List folder, String watermark) throws NoInternetConnectionException, Exception{
		if(Utils.checkInternetConnection(context)){
			return service.subscribeToPullNotifications(folder,1440,
					//timeOut: the subscription will end if the server is not polled within x minutes.  min:1 max:1440,
					watermark  ,
					EventType.NewMail,EventType.Modified, EventType.Created, EventType.Deleted);
		}
		else{	throw new NoInternetConnectionException(); }
	}

	public static GetEventsResults pullSubscriptionPoll(Context context, PullSubscription subscription) throws NoInternetConnectionException, Exception{
		if(Utils.checkInternetConnection(context)){
			return subscription.getEvents();
		}
		else{	throw new NoInternetConnectionException(); }

	}

	public static Item bindItem(Context context, ExchangeService service, ItemId itemid) throws NoInternetConnectionException, Exception{
		if(Utils.checkInternetConnection(context)){
			return Item.bind(service, itemid);
		}
		else{	throw new NoInternetConnectionException(); }
	}

	public static EmailMessage bindEmailMessage(Context context, ExchangeService service, ItemEvent itemEvent) throws NoInternetConnectionException, Exception{
		if(Utils.checkInternetConnection(context)){
			return EmailMessage.bind(service, itemEvent.getItemId());
		}
		else{	throw new NoInternetConnectionException(); }
	}

	public static void markEmailAsRead(Context context, EmailMessage email) throws NoInternetConnectionException, Exception{
		if(Utils.checkInternetConnection(context)){
			email.setIsRead(true);
			email.update(ConflictResolutionMode.AutoResolve);
		}
		else{	throw new NoInternetConnectionException(); }
	}

	/** Send an Email
	 * @param context
	 * @param service
	 * @param to
	 * @param subject
	 * @param body
	 * @throws NoInternetConnectionException
	 * @throws Exception
	 */
	public static void sendMail(Context context, ExchangeService service, Collection<ContactSerializable> to, Collection<ContactSerializable> cc,Collection<ContactSerializable> bcc,String subject, String body) throws NoInternetConnectionException, Exception{
		if(Utils.checkInternetConnection(context)){

			EmailMessage msg= new EmailMessage(service);
			msg.setSubject(subject); 
			msg.setBody(MessageBody.getMessageBodyFromText(body));
			Log.d(TAG, "To receipients for sending email " + to);
			if( null != to && to.size()>0){
				for(ContactSerializable tempRecipient: to){
					Log.d(TAG, "adding " + tempRecipient);
					msg.getToRecipients().add(tempRecipient.getEmail());
				}

			}
			if( null != cc && cc.size()>0){
				for(ContactSerializable tempRecipient: cc){
					msg.getCcRecipients().add(tempRecipient.getEmail());
				}

			}
			if( null != bcc && bcc.size()>0){
				for(ContactSerializable tempRecipient: bcc){
					msg.getBccRecipients().add(tempRecipient.getEmail());
				}

			}
			if(SEND_EMAIL_SAVE_COPY_IN_SENT){
				msg.sendAndSaveCopy();
			}
			else{
				msg.send();
			}

		}
		else{	throw new NoInternetConnectionException(); }
	}


	/** Reply or Replyall an Email
	 * @param context
	 * @param service
	 * @param itemIdStr the item id of the original email to send reply
	 * @param to
	 * @param subject
	 * @param body
	 * @throws NoInternetConnectionException
	 * @throws Exception
	 */
	public static void replyMail(Context context, ExchangeService service, String itemIdStr, Collection<ContactSerializable> to, Collection<ContactSerializable> cc,Collection<ContactSerializable> bcc,String subject, String body, boolean replyAll) throws NoInternetConnectionException, Exception{
		if(Utils.checkInternetConnection(context)){
			
			Log.d(TAG, "NetworkCall -> replyMail Item id " + itemIdStr);
			ItemId itemId = ItemId.getItemIdFromString(itemIdStr);

			EmailMessage msg = EmailMessage.bind(service, itemId);
			ResponseMessage replyMsg= msg.createReply(replyAll);
			replyMsg.setSubject(subject); 
			replyMsg.setBodyPrefix(MessageBody.getMessageBodyFromText(body));
			Log.d(TAG, "To receipients for sending email " + to);
			if( null != to && to.size()>0){
				for(ContactSerializable tempRecipient: to){
					Log.d(TAG, "adding " + tempRecipient);
					replyMsg.getToRecipients().add(tempRecipient.getEmail());
				}

			}
			if( null != cc && cc.size()>0){
				for(ContactSerializable tempRecipient: cc){
					replyMsg.getCcRecipients().add(tempRecipient.getEmail());
				}

			}
			if( null != bcc && bcc.size()>0){
				for(ContactSerializable tempRecipient: bcc){
					replyMsg.getBccRecipients().add(tempRecipient.getEmail());
				}

			}
			if(SEND_EMAIL_SAVE_COPY_IN_SENT){
				replyMsg.sendAndSaveCopy();
			}
			else{
				replyMsg.send();
			}

		}
		
		
		else{	throw new NoInternetConnectionException(); }
	}

	/** Send an Email
	 * @param context
	 * @param service
	 * @param to
	 * @param subject
	 * @param body
	 * @throws NoInternetConnectionException
	 * @throws Exception
	 */
	public static void forwardMail(Context context, ExchangeService service, String itemIdStr, Collection<ContactSerializable> to, Collection<ContactSerializable> cc,Collection<ContactSerializable> bcc,String subject, String body) throws NoInternetConnectionException, Exception{
		if(Utils.checkInternetConnection(context)){

			Log.d(TAG, "NetworkCall -> forwardMail Item id " + itemIdStr);
			ItemId itemId = ItemId.getItemIdFromString(itemIdStr);

			EmailMessage msg = EmailMessage.bind(service, itemId);
			ResponseMessage forwardMsg= msg.createForward();
			forwardMsg.setSubject(subject); 
			forwardMsg.setBodyPrefix(MessageBody.getMessageBodyFromText(body));
			Log.d(TAG, "To receipients for sending email " + to);
			if( null != to && to.size()>0){
				for(ContactSerializable tempRecipient: to){
					Log.d(TAG, "adding " + tempRecipient);
					forwardMsg.getToRecipients().add(tempRecipient.getEmail());
				}

			}
			if( null != cc && cc.size()>0){
				for(ContactSerializable tempRecipient: cc){
					forwardMsg.getCcRecipients().add(tempRecipient.getEmail());
				}

			}
			if( null != bcc && bcc.size()>0){
				for(ContactSerializable tempRecipient: bcc){
					forwardMsg.getBccRecipients().add(tempRecipient.getEmail());
				}

			}
			if(SEND_EMAIL_SAVE_COPY_IN_SENT){
				forwardMsg.sendAndSaveCopy();
			}
			else{
				forwardMsg.send();
			}

		}
		
		
		else{	throw new NoInternetConnectionException(); }
	}

	public static EmailMessage bindEmailMessage(Context context, ExchangeService service, ItemId itemid) throws NoInternetConnectionException, Exception{
		if(Utils.checkInternetConnection(context)){
			return EmailMessage.bind(service, itemid);
		}
		else{	throw new NoInternetConnectionException(); }
	}

	public static FindFoldersResults getInboxFolders(ExchangeService service) throws Exception{


		FindFoldersResults findResults = service.findFolders(WellKnownFolderName.Inbox,new  FolderView(Integer.MAX_VALUE));
		return findResults;
	}

	//the following method is not used
	public static FindItemsResults<Item> getLatestMail(ExchangeService service, Date dateTime, ItemView latestMailsView) throws Exception{


		FindItemsResults<Item>  findResults = service.findItems(WellKnownFolderName.Inbox, new SearchFilter.IsGreaterThan(ItemSchema.DateTimeReceived,dateTime),

				latestMailsView);
		return findResults;
	}

	public static ChangeCollection<ItemChange> syncFolderItems(ExchangeService service, int pageSize, String syncState) throws Exception{


		ChangeCollection<ItemChange> changeCollection = service.syncFolderItems(new FolderId(WellKnownFolderName.Inbox),
				new PropertySet(BasePropertySet.FirstClassProperties), null, pageSize,
				SyncFolderItemsScope.NormalItems, syncState);

		return changeCollection;
	}

	public static NameResolutionCollection resolveName(ExchangeService service, String username, boolean retrieveContactDetails) throws NoInternetConnectionException, Exception {
		// TODO Auto-generated method stub
		NameResolutionCollection nameResolutions = service.resolveName(username,ResolveNameSearchLocation.DirectoryOnly, retrieveContactDetails);
		return nameResolutions;

	}
	public static NameResolutionCollection resolveName_LocalThenDirectory(ExchangeService service, String username, boolean retrieveContactDetails) throws NoInternetConnectionException, Exception {
		// TODO Auto-generated method stub
		NameResolutionCollection nameResolutions = service.resolveName(username,ResolveNameSearchLocation.ContactsThenDirectory, retrieveContactDetails);
		return nameResolutions;

	}

	/** Downloads the image inside file attachment
	 * @param fileAttachment
	 * @param fos
	 * @throws NoInternetConnectionException
	 * @throws Exception
	 */
	public static void downloadAttachment(FileAttachment fileAttachment, OutputStream fos) throws NoInternetConnectionException, Exception {
		// TODO Auto-generated method stub

		fileAttachment.load(fos);
	}


	public static FindItemsResults<Item> getFirstNItemsFromFolder(WellKnownFolderName folderName, ExchangeService service, int n) throws Exception {
		// TODO Auto-generated method stub
		ItemView view = new ItemView(n);
		FindItemsResults<Item> findResults = null;
		//view.setOffset(1);

		//do
		//{


		findResults = service.findItems(folderName, view);
		return findResults;

	}



	public static FindItemsResults<Item> getFirstNItemsFromFolder(FolderId folderId, ExchangeService service, int n) throws Exception {
		// TODO Auto-generated method stub
		ItemView view = new ItemView(n);
		FindItemsResults<Item> findResults = null;
		//view.setOffset(1);

		//do
		//{

		findResults = service.findItems(folderId, view);
		return findResults;

	}
	public static FindItemsResults<Item> getFirstNItemsFromFolder(String strFolderId, ExchangeService service, int n) throws Exception {
		// TODO Auto-generated method stub
		FolderId folderId = FolderId.getFolderIdFromString(strFolderId);

		return getFirstNItemsFromFolder(folderId, service, n);

	}

	public static FindFoldersResults getFolders(ExchangeService service, FolderId folderId) throws Exception{

		return service.findFolders(folderId,new  FolderView(Integer.MAX_VALUE));

	}

	
	/** This method deletes the particular item whether its mail or anything and sends to deleted items, 
	 * @param service
	 * @param item
	 * @throws Exception
	 */
	public static void deleteItem(Item item) throws Exception{

		item.delete(DeleteMode.MoveToDeletedItems);

	}
	
	/** This method deletes the particular item whether its mail or anything and deletes it permanently, 
	 * @param service
	 * @param item
	 * @throws Exception
	 */
	public static void deleteItemPermanent(Item item) throws Exception{

		item.delete(DeleteMode.HardDelete);

	}

}
