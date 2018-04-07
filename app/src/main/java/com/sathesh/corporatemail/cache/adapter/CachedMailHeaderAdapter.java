/**
 * 
 */
package com.sathesh.corporatemail.cache.adapter;

import android.content.Context;
import android.util.Log;

import com.sathesh.corporatemail.BuildConfig;
import com.sathesh.corporatemail.constants.Constants;
import com.sathesh.corporatemail.ews.MailFunctions;
import com.sathesh.corporatemail.ews.MailFunctionsImpl;
import com.sathesh.corporatemail.service.data.Item;
import com.sathesh.corporatemail.service.data.ServiceLocalException;
import com.sathesh.corporatemail.sqlite.db.cache.dao.CachedMailHeaderDAO;
import com.sathesh.corporatemail.sqlite.db.cache.vo.CachedMailHeaderVO;

import java.util.ArrayList;
import java.util.List;

/**
 * @author sathesh
 *
 */
public class CachedMailHeaderAdapter implements Constants{

	private static MailFunctions mailFunctions;
	private static CachedMailHeaderDAO dao;
	
	public CachedMailHeaderAdapter(Context context){
		if(dao==null){
		dao=new CachedMailHeaderDAO(context);
		}
	}

    /** CREATE QUERIES **/

    /** Inserts 1 item to cache
     * @param item  - The item to write to cache
     * @param mailType - the folder. Depending upon the mail type it will use folder name or folder id
     * @param mailFolderName
     * @param mailFolderId
     */
    public void cacheNewData(Item item, int mailType, String mailFolderName, String mailFolderId)  {
        CachedMailHeaderVO vo = new CachedMailHeaderVO();
        try {

            //convert the Item to List of VO
            vo = covertItemToVO(mailType, mailFolderName, mailFolderId, item);

            //call the DAO for with VO to save
            dao.createOrUpdate(vo);

        } catch (Exception e) {
            if(BuildConfig.DEBUG)
                e.printStackTrace();
        }
    }

	/** Writes the array List of items to cache with or without emptying the previous ones
	 * @param items
	 * @param mailType - the folder. Depending upon the mail type it will use folder name or folder id
	 * @param mailFolderName
	 * @param mailFolderId
	 * @param emptyCache - Empties the cache before writing
	 */
	public void cacheNewData(ArrayList<Item> items, int mailType, String mailFolderName, String mailFolderId, boolean emptyCache)  {
        List<CachedMailHeaderVO> vos = new ArrayList<CachedMailHeaderVO>();
        try {
            //empties the cache before writing new records
			if(emptyCache){
				deleteAll(mailType, mailFolderId);
			}
            //convert the list of Item to List of VO
            for(Item item : items){
                vos.add(covertItemToVO(mailType, mailFolderName, mailFolderId, item));
            }
            //call the DAO for with the list of VOs to save
            dao.createOrUpdate(vos);

		} catch (Exception e) {
			if(BuildConfig.DEBUG)
				e.printStackTrace();
		}	
	}

    /** Writes the  array list of vos to cache
     *
     * @param vos
     */
    public void creteNewData(ArrayList<CachedMailHeaderVO> vos)  {
        try {
            //call the DAO for with the list of VOs to save
            dao.createOrUpdate(vos);

        } catch (Exception e) {
            if(BuildConfig.DEBUG)
                e.printStackTrace();
        }
    }

    /** SELECT QUERIES **/

	/** This method will return all the cached mail header data list of VOs for the particular mail type given
	 * @return
	 */
	public List<CachedMailHeaderVO> getMailHeaders(int mailType, String mailFolderId){
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
			if(BuildConfig.DEBUG)
				e.printStackTrace();
		}
		return mailListHeaderData;

	}

	/** Gets the total number of record mail headers in cache for the particular mail type
	 * @throws Exception 
	 * 
	 */
	public int getRecordsCount(int mailType, String mailFolderId) throws Exception {
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
	public int getUnreadMailCount(int mailType, String mailFolderId) throws Exception {
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

    /** DELETE QUERIES **/

	/** Delete all the cached mail headers for this particular mail type
	 * @throws Exception 
	 * 
	 */
	private void deleteAll(int mailType, String mailFolderId) throws Exception {
		// Pass folder id or folder name to the corresponding folder
		if(mailType!=MailType.FOLDER_WITH_ID && mailType!=MailType.INBOX_SUBFOLDER_WITH_ID){
			dao.deleteAllByMailType(mailType);
		}
		else{
			//by folder id
			dao.deleteAllByFolderId(mailFolderId);
		}
	}

    /** Delete records leaving "n" no of records on top
     *
     * @param mailType
     * @param mailFolderId
     * @param n - no of records to keep on top
     * @throws Exception
     */
	public void deleteN(int mailType, String mailFolderId, int n) throws Exception {
		// Pass folder id or folder name to the corresponding folder
		if(mailType!=MailType.FOLDER_WITH_ID && mailType!=MailType.INBOX_SUBFOLDER_WITH_ID){
			dao.deleteNByMailType(mailType,n);
		}
		else{
			//by folder id
			dao.deleteNByFolderId(mailFolderId,n);
		}
	}

    /** Delete items with item itemid
     *
     * @throws Exception
     */
    public void deleteItems(ArrayList<CachedMailHeaderVO> vos) throws Exception {
        ArrayList<String> itemIds = new ArrayList<>();
        for(CachedMailHeaderVO vo: vos) {
            itemIds.add(vo.getItem_id());
        }
       dao.deleteItems(itemIds);
    }

    /** Delete vo from cachce
     *
     * @throws Exception
     * @param vo mail header vo to delete
     */
    public void deleteItemVo(CachedMailHeaderVO vo) throws Exception {
        if(vo!=null) {
            dao.deleteItemId(vo.getItem_id());
        }
        else{
            Log.e(TAG, "CachedMailHeaderAdapter-> deleteItemVo() -> vo is null" );
        }
    }

    /** UPDATE QUERIES **/

	/** mark mail as read
	 * @throws ServiceLocalException
	 * @throws Exception
	 */
	public synchronized void markMailAsReadUnread(String itemId , boolean isRead) throws Exception {
		//call the DAO for with the list of VOs to save
		dao.markMailAsReadUnread(itemId, isRead);
	}



    /** mark mails as read
     * @throws ServiceLocalException
     * @throws Exception
     */
    public synchronized void markMailsAsReadUnread(ArrayList<CachedMailHeaderVO> vos , boolean isRead) throws Exception {
       ArrayList<String> itemIds = new ArrayList<String>();
        for(CachedMailHeaderVO vo: vos){
            itemIds.add(vo.getItem_id());
       }
        //call the DAO
        dao.markMailsAsReadUnread(itemIds, isRead);
    }


    /** PRIVATE METHODS **/

	/** private function for converting Item to VO
	 * @param mailFolderName 
	 *
	 */
	private CachedMailHeaderVO covertItemToVO(int mailType, String mailFolderName,  String strFolderId, Item item) throws ServiceLocalException, Exception{
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
