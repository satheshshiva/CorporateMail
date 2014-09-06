/**
 * 
 */
package com.wipromail.sathesh.cache.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.wipromail.sathesh.BuildConfig;
import com.wipromail.sathesh.constants.Constants.MailType;
import com.wipromail.sathesh.ews.MailFunctions;
import com.wipromail.sathesh.ews.MailFunctionsImpl;
import com.wipromail.sathesh.service.data.Item;
import com.wipromail.sathesh.service.data.ServiceLocalException;
import com.wipromail.sathesh.sqlite.db.dao.CachedMailHeaderDAO;
import com.wipromail.sathesh.sqlite.db.pojo.vo.CachedMailHeaderVO;

/**
 * @author sathesh
 *
 */
public class CachedMailHeaderCacheAdapter {

	private static MailFunctions mailFunctions;
	private static CachedMailHeaderDAO dao;
	
	public CachedMailHeaderCacheAdapter(Context context){
		if(dao==null){
		dao=new CachedMailHeaderDAO(context);
		}
	}

	//CODE HAS TO BE UPDATE FOR
	//VIEW MAIL ACTIVITY
	//POLLSERVERMNS.java
	
	/** Writes the array List of items to cache
	 * @param context
	 * @param items
	 * @param mailType - the folder. Depending upon the mail type it will use folder name or folder id
	 * @param mailFolderName
	 * @param mailFolderId
	 * @param emptyCache - Empties the cache before writing
	 */
	public void cacheNewData(Context context, ArrayList<Item> items, int mailType, String mailFolderName, String mailFolderId, boolean emptyCache)  {
		// TODO Auto-generated method stub
		try {
			if(emptyCache){
				deleteAll(mailType,mailFolderName, mailFolderId);
			}
			writeMailHeader(context, mailType, mailFolderName, mailFolderId, items);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			if(BuildConfig.DEBUG)
				e.printStackTrace();
		}	
	}
	
	/** This method will return all the cached mail header data list of VOs for the particular mail type given
	 * @return
	 */
	public List<CachedMailHeaderVO> getMailHeaders(int mailType, String mailFolderName, String mailFolderId){
		List<CachedMailHeaderVO> mailListHeaderData=null;
		try {
			// mail type 8 and 9 have folder id. The rest can be determined by the mailType
			if(mailType!=MailType.FOLDER_WITH_ID && mailType!=MailType.INBOX_SUBFOLDER_WITH_ID){
				mailListHeaderData = dao.getAllRecordsByMailType(mailType);
			}
			else{
				//by folder id
				mailListHeaderData = dao.getAllRecordsByFolderId(mailFolderId);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			if(BuildConfig.DEBUG)
				e.printStackTrace();
		}
		return mailListHeaderData;

	}

	/** Gets the total number of record mail headers in cache for the particular mail type
	 * @throws Exception 
	 * 
	 */
	public int getRecordsCount(int mailType, String mailFolderName, String mailFolderId) throws Exception {
		// TODO Auto-generated method stub
		int totalCachedRecords;
		if(mailType!=MailType.FOLDER_WITH_ID && mailType!=MailType.INBOX_SUBFOLDER_WITH_ID){
			totalCachedRecords=dao.getRecordsCountByMailType(mailType);
		}
		else{
			//by folder id
			totalCachedRecords=dao.getRecordsCountByFolderId(mailFolderId);
		}
		return totalCachedRecords;
	}

	/** Gets the total number of record mail headers in cache for the particular mail type given
	 * @throws Exception 
	 * 
	 */
	public int getUnreadMailCount(int mailType, String mailFolderName, String mailFolderId) throws Exception {
		// TODO Auto-generated method stub
		int totalUnread;
		if(mailType!=MailType.FOLDER_WITH_ID && mailType!=MailType.INBOX_SUBFOLDER_WITH_ID){
			totalUnread=dao.getUnreadByMailType(mailType);
		}
		else{
			//by folder id
			totalUnread=dao.getUnreadCountByFolderId(mailFolderId);
		}
		return totalUnread;
	}

	/** Delete all the cached mail headers for this particular mail type
	 * @throws Exception 
	 * 
	 */
	private void deleteAll(int mailType, String mailFolderName, String mailFolderId) throws Exception {
		// TODO Auto-generated method stub
		// Pass folder id or folder name to the corresponding folder
		if(mailType!=MailType.FOLDER_WITH_ID && mailType!=MailType.INBOX_SUBFOLDER_WITH_ID){
			dao.deleteAllByMailType(mailType);
		}
		else{
			//by folder id
			dao.deleteAllByFolderId(mailFolderId);
		}
	}
	
	/** Delete all the cached mail headers for this particular mail type
	 * @throws Exception 
	 * 
	 */
	public void deleteN(int mailType, String mailFolderName, String mailFolderId, int n) throws Exception {
		// TODO Auto-generated method stub
		// Pass folder id or folder name to the corresponding folder
		if(mailType!=MailType.FOLDER_WITH_ID && mailType!=MailType.INBOX_SUBFOLDER_WITH_ID){
			dao.deleteNByMailType(mailType,n);
		}
		else{
			//by folder id
			dao.deleteNByFolderId(mailFolderId,n);
		}
	}
	
	/** writes the given arraylist of exchange items to cache
	 * @param context
	 * @param strFolderId 
	 * @param mailFolderName 
	 * @param MailType 
	 * @param items 
	 * @throws ServiceLocalException
	 * @throws Exception
	 */
	public synchronized void writeMailHeader(Context context, int mailType, String mailFolderName, String strFolderId, ArrayList<Item> items ) throws ServiceLocalException, Exception {
		// TODO Auto-generated method stub

		List<CachedMailHeaderVO> vos = new ArrayList<CachedMailHeaderVO>();
		
		//convert the list of Item to List of VO
		for(Item item : items){
			vos.add(covertItemToVO(mailType, mailFolderName, strFolderId, item));
		}
		CachedMailHeaderDAO dao = new CachedMailHeaderDAO(context);
		//call the DAO for with the list of VOs to save
		dao.createOrUpdate(vos);
	}

	/** private function for converting Item to VO
	 * @param mailFolderName 
	 * @param MailType 
	 * 
	 */
	private CachedMailHeaderVO covertItemToVO(int mailType, String mailFolderName,  String strFolderId, Item item) throws ServiceLocalException, Exception{
		// TODO Auto-generated method stub
		CachedMailHeaderVO vo = new CachedMailHeaderVO();
		mailFunctions = MailFunctionsImpl.getInbox();
		
		vo.setItem_id(mailFunctions.getItemId(item));
		vo.setFolder_name(mailFolderName);
		vo.setFolder_id(strFolderId);
		vo.setMail_type(mailType);
		vo.setMail_from(mailFunctions.getFrom(item));
		vo.setMail_to(mailFunctions.getTo(item));
		vo.setMail_cc(mailFunctions.getCC(item));
		vo.setMail_subject(mailFunctions.getSubject(item));
		vo.setMail_datetimereceived(mailFunctions.getDateTimeReceived(item));
		vo.setMail_isread(mailFunctions.getIsRead(item));
		vo.setMail_has_attachments(mailFunctions.hasAttachments(item));
		return vo;
	}
	
	
}
