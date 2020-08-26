package com.sathesh.corporatemail.ews;

import android.content.Context;
import android.util.Log;

import com.sathesh.corporatemail.BuildConfig;
import com.sathesh.corporatemail.application.Utils;
import com.sathesh.corporatemail.constants.Constants;
import com.sathesh.corporatemail.customexceptions.NoInternetConnectionException;
import com.sathesh.corporatemail.customserializable.ContactSerializable;

import org.apache.commons.lang3.StringUtils;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import microsoft.exchange.webservices.data.autodiscover.IAutodiscoverRedirectionUrl;
import microsoft.exchange.webservices.data.core.ExchangeService;
import microsoft.exchange.webservices.data.core.PropertySet;
import microsoft.exchange.webservices.data.core.enumeration.notification.EventType;
import microsoft.exchange.webservices.data.core.enumeration.property.BasePropertySet;
import microsoft.exchange.webservices.data.core.enumeration.property.WellKnownFolderName;
import microsoft.exchange.webservices.data.core.enumeration.search.ResolveNameSearchLocation;
import microsoft.exchange.webservices.data.core.enumeration.service.ConflictResolutionMode;
import microsoft.exchange.webservices.data.core.enumeration.service.DeleteMode;
import microsoft.exchange.webservices.data.core.enumeration.service.SyncFolderItemsScope;
import microsoft.exchange.webservices.data.core.service.item.EmailMessage;
import microsoft.exchange.webservices.data.core.service.item.Item;
import microsoft.exchange.webservices.data.core.service.response.ResponseMessage;
import microsoft.exchange.webservices.data.core.service.schema.ItemSchema;
import microsoft.exchange.webservices.data.misc.NameResolutionCollection;
import microsoft.exchange.webservices.data.notification.GetEventsResults;
import microsoft.exchange.webservices.data.notification.ItemEvent;
import microsoft.exchange.webservices.data.notification.PullSubscription;
import microsoft.exchange.webservices.data.property.complex.FileAttachment;
import microsoft.exchange.webservices.data.property.complex.FolderId;
import microsoft.exchange.webservices.data.property.complex.ItemId;
import microsoft.exchange.webservices.data.property.complex.MessageBody;
import microsoft.exchange.webservices.data.search.FindFoldersResults;
import microsoft.exchange.webservices.data.search.FindItemsResults;
import microsoft.exchange.webservices.data.search.FolderView;
import microsoft.exchange.webservices.data.search.ItemView;
import microsoft.exchange.webservices.data.search.filter.SearchFilter;
import microsoft.exchange.webservices.data.sync.ChangeCollection;
import microsoft.exchange.webservices.data.sync.ItemChange;

public class NetworkCall implements Constants{

	//The following method is used for Pull sibscription by Mail Notification Service
	public static PullSubscription subscribePull(Context context, ExchangeService service, List folder) throws NoInternetConnectionException, Exception{
		if(Utils.checkInternetConnection(context)){
			return service.subscribeToPullNotifications(folder,PULL_SUBSCRIPTION_TIMEOUT,
					//timeOut: the subscription will end if the server is not polled within x minutes.  min:1 max:1440,
					null  /*watermark: null to start a new subscription*/,
					EventType.NewMail);
		}
		else{	throw new NoInternetConnectionException(); }
	}

	public static GetEventsResults pullSubscriptionPoll(Context context, PullSubscription subscription) throws NoInternetConnectionException, Exception{
		if(Utils.checkInternetConnection(context)){
			return subscription.getEvents();
		}
		else{	throw new NoInternetConnectionException(); }

	}


	//the following method is not used (not working)
	public static String autoDiscover(String email, final boolean followRedirect) throws Exception{
		ExchangeService service = new ExchangeService();
		try {
			service.autodiscoverUrl(email, new IAutodiscoverRedirectionUrl() {
				@Override
				public boolean autodiscoverRedirectionUrlValidationCallback(
						String redirectionUrl) {
					return followRedirect;
				}
			});

			if(service.getUrl() != null ){
				return service.getUrl().toString();
			}
		}catch (Exception e){
			Log.e(LOG_TAG, "Could not auto discover with the given email:: " + e.getMessage());
			return null;
		}

		Log.e(LOG_TAG, "Could not auto discover with the given email:: returning null");
		return null;
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

    /** Mark an itemId as Read/Unread
     *
     * @param context
     * @param itemId
     * @param isRead
     * @throws NoInternetConnectionException
     * @throws Exception
     */
    public static void markEmailAsReadUnread(Context context, String itemId, boolean isRead) throws NoInternetConnectionException, Exception{
        ExchangeService  service = EWSConnection.getInstance(context);
        if(Utils.checkInternetConnection(context)){
            ItemId _itemId = ItemId.getItemIdFromString(itemId);
            EmailMessage item=(EmailMessage)Item.bind(service, _itemId);
            item.setIsRead(isRead);
            item.update(ConflictResolutionMode.AutoResolve);
        }
        else{	throw new NoInternetConnectionException(); }
    }



	/** Send an Email
	 * @param context
	 * @param to
	 * @param subject
	 * @param body
	 * @throws NoInternetConnectionException
	 * @throws Exception
	 */
	public static void sendMail(Context context, EmailMessage msg, Collection<ContactSerializable> to, Collection<ContactSerializable> cc,Collection<ContactSerializable> bcc,String subject, String body) throws NoInternetConnectionException, Exception{
		if(Utils.checkInternetConnection(context)){
			msg.setSubject(subject);
			msg.setBody(MessageBody.getMessageBodyFromText(body));

			//since save draft can be called multiple times, clear the variables for fresh insertion from the ui parameters
			msg.getToRecipients().clear();
			msg.getCcRecipients().clear();
			msg.getBccRecipients().clear();

			if(BuildConfig.DEBUG) {
                Log.d(LOG_TAG, "To receipients for sending email " + to);
            }
			if( null != to && to.size()>0){
				for(ContactSerializable tempRecipient: to){
					Log.d(LOG_TAG, "adding " + tempRecipient);
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
	 * @param to
	 * @param subject
	 * @param body
	 * @throws NoInternetConnectionException
	 * @throws Exception
	 */
	public static void replyMail(Context context, EmailMessage msg, Collection<ContactSerializable> to, Collection<ContactSerializable> cc,Collection<ContactSerializable> bcc,String subject, String body, boolean replyAll) throws NoInternetConnectionException, Exception{
		if(Utils.checkInternetConnection(context)){
			ResponseMessage replyMsg= msg.createReply(replyAll);
			replyMsg.setSubject(subject); 
			replyMsg.setBodyPrefix(MessageBody.getMessageBodyFromText(body));

			//since save draft can be called multiple times, clear the variables for fresh insertion from the ui parameters
			replyMsg.getToRecipients().clear();
			replyMsg.getCcRecipients().clear();
			replyMsg.getBccRecipients().clear();

			Log.d(LOG_TAG, "To receipients for sending email " + to);
			if( null != to && to.size()>0){
				for(ContactSerializable tempRecipient: to){
					Log.d(LOG_TAG, "adding " + tempRecipient);
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

	public static EmailMessage bind(Context context, ExchangeService service, String itemId) throws NoInternetConnectionException, Exception {
		if(Utils.checkInternetConnection(context)) {
			return EmailMessage.bind(service, ItemId.getItemIdFromString(itemId));
		}else{	throw new NoInternetConnectionException(); }
	}


	/** Forward Email
	 * @param context
	 * @param to
	 * @param subject
	 * @param body
	 * @throws NoInternetConnectionException
	 * @throws Exception
	 */
	public static void forwardMail(Context context, EmailMessage msg, Collection<ContactSerializable> to, Collection<ContactSerializable> cc,Collection<ContactSerializable> bcc,String subject, String body) throws NoInternetConnectionException, Exception{
		if(Utils.checkInternetConnection(context)){

			ResponseMessage forwardMsg= msg.createForward();
			forwardMsg.setSubject(subject); 
			forwardMsg.setBodyPrefix(MessageBody.getMessageBodyFromText(body));

			//since save draft can be called multiple times, clear the variables for fresh insertion from the ui parameters
			forwardMsg.getToRecipients().clear();
			forwardMsg.getCcRecipients().clear();
			forwardMsg.getBccRecipients().clear();

			Log.d(LOG_TAG, "To receipients for sending email " + to);
			if( null != to && to.size()>0){
				for(ContactSerializable tempRecipient: to){
					Log.d(LOG_TAG, "adding " + tempRecipient);
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

	public static void saveDraft(Context context, EmailMessage msg, boolean existingDraft, Collection<ContactSerializable> to, Collection<ContactSerializable> cc,Collection<ContactSerializable> bcc,String subject, String body) throws Exception{
		if(Utils.checkInternetConnection(context)) {
			msg.setSubject(subject);
			msg.setBody(MessageBody.getMessageBodyFromText(body));
			//since save draft can be called multiple times, clear the variables for fresh insertion from the ui parameters
			msg.getToRecipients().clear();
			msg.getCcRecipients().clear();
			msg.getBccRecipients().clear();

			if( null != to && to.size()>0){
				for(ContactSerializable tempRecipient: to){
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
			if (!existingDraft) { //new draft
				msg.save();
			}else{
				msg.update(ConflictResolutionMode.AutoResolve);
			}

		}else{	throw new NoInternetConnectionException(); }
	}

	public static FindFoldersResults getInboxFolders(ExchangeService service) throws Exception{


		FindFoldersResults findResults = service.findFolders(WellKnownFolderName.Inbox,new FolderView(Integer.MAX_VALUE));
		return findResults;
	}

	//the following method is not used
	public static FindItemsResults<Item> getLatestMail(ExchangeService service, Date dateTime, ItemView latestMailsView) throws Exception{


		FindItemsResults<Item> findResults = service.findItems(WellKnownFolderName.Inbox, new SearchFilter.IsGreaterThan(ItemSchema.DateTimeReceived,dateTime),

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
		NameResolutionCollection nameResolutions = service.resolveName(username, ResolveNameSearchLocation.DirectoryOnly, retrieveContactDetails);
		return nameResolutions;

	}
	public static NameResolutionCollection resolveName_LocalThenDirectory(ExchangeService service, String username, boolean retrieveContactDetails) throws NoInternetConnectionException, Exception {
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
		fileAttachment.load(fos);
	}

	/** Downloads the image inside file attachment. Uncoventional way to recreate the fileattachment obj from the id.
	 * @param id - Attachment id
	 * @param fos
	 * @throws NoInternetConnectionException
	 * @throws Exception
	 */
	public static void downloadAttachment(Context context, String id, OutputStream fos) throws NoInternetConnectionException, Exception {
		ExchangeService  service = EWSConnection.getInstance(context);
		if (!StringUtils.isEmpty(id)) {
			if(fos!=null) {

					FileAttachment fileAttachment = new FileAttachment(new Item(service));
					fileAttachment.setId(id);
					fileAttachment.load(fos);

			}else{
				Log.e(LOG_TAG, "NetworkCall -> downloadAttachment -> FileOutputStream is null. Cannot proceed with attachment download");
			}
		}else{
			Log.e(LOG_TAG, "NetworkCall -> downloadAttachment -> Given id is null. Cannot proceed with attachment download");
		}

	}


	/** Gets the N items from the WellKnownFolderName specified
	 * @param folderName
	 * @param service
	 * @param offset - No of mails to leave on top. pass 0 to fetch from first mail
	 * @param n - number of mails to fetch
	 * @return
	 * @throws Exception
	 */
	public static FindItemsResults<Item> getNItemsFromFolder(WellKnownFolderName folderName, ExchangeService service, int offset, int n) throws Exception {
		ItemView view = new ItemView(n);
		FindItemsResults<Item> findResults = null;
		if(offset>0){
			view.setOffset(offset);
		}
		findResults = service.findItems(folderName, view);
		return findResults;

	}

	/** Gets the number of items specified in the specified folder id
	 * @param folderId
	 * @param service
	 * @param offset - No of mails to leave on top. pass 0 to fetch from first mail 
	 * @param n - number of mails to fetch
	 * @return
	 * @throws Exception
	 */
	public static FindItemsResults<Item> getNItemsFromFolder(FolderId folderId, ExchangeService service, int offset, int n) throws Exception {
		ItemView view = new ItemView(n);
		FindItemsResults<Item> findResults = null;
		if(offset>0){
			view.setOffset(offset);
		}
		findResults = service.findItems(folderId, view);
		return findResults;

	}
	/** Overrides the getFirstNItemsFromFolder with String folderId
	 * @param strFolderId
	 * @param service
	 * @param offset - No of mails to leave on top. pass 0 to fetch from first mail
	 * @param n - number of mails to fetch
	 * @return
	 * @throws Exception
	 */
	public static FindItemsResults<Item> getNItemsFromFolder(String strFolderId, ExchangeService service, int offset, int n) throws Exception {
		FolderId folderId = FolderId.getFolderIdFromString(strFolderId);
		return getNItemsFromFolder(folderId, service,offset, n);
	}

	public static FindFoldersResults getFolders(ExchangeService service, FolderId folderId) throws Exception{
		return service.findFolders(folderId,new  FolderView(Integer.MAX_VALUE));
	}

    /** This method deletes the particular item whether its mail or anything and sends to deleted items,
     * @param itemId - String
     * @throws Exception
     */
    public static void deleteItemId(ExchangeService service, String itemId, boolean permanent) throws Exception{
        ItemId _itemId = ItemId.getItemIdFromString(itemId);
        Item item=Item.bind(service, _itemId);
        item.delete(DeleteMode.MoveToDeletedItems);
        if(!permanent) {
            item.delete(DeleteMode.MoveToDeletedItems);
        }else{
            item.delete(DeleteMode.HardDelete);
        }
    }

    /** This method deletes list of itemIds
     * @param itemIds - Array list of item ids to delete
     * @throws Exception
     */
    public static void deleteItemIds(ExchangeService service, ArrayList<String> itemIds, boolean permanent) throws Exception{
        ItemId _itemId;
        ArrayList<ItemId> _itemIds = new ArrayList<>();

        for(String itemId:itemIds){
            _itemId = ItemId.getItemIdFromString(itemId);
            _itemIds.add(_itemId);
        }
        if(!permanent) {
            service.deleteItems((Iterable) _itemIds, DeleteMode.MoveToDeletedItems, null, null);
        }else{
            service.deleteItems((Iterable) _itemIds, DeleteMode.HardDelete, null, null);
        }
    }

}
