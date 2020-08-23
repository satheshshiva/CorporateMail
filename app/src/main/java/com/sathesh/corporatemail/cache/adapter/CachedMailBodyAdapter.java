/**
 *
 */
package com.sathesh.corporatemail.cache.adapter;

import android.content.Context;

import com.sathesh.corporatemail.BuildConfig;
import com.sathesh.corporatemail.application.MailApplication;
import com.sathesh.corporatemail.constants.Constants.MailType;
import com.sathesh.corporatemail.ews.MailFunctions;
import com.sathesh.corporatemail.ews.MailFunctionsImpl;
import com.sathesh.corporatemail.sqlite.db.cache.dao.CachedMailBodyDAO;
import com.sathesh.corporatemail.sqlite.db.cache.vo.CachedMailBodyVO;

import java.util.List;

import microsoft.exchange.webservices.data.core.exception.service.local.ServiceLocalException;
import microsoft.exchange.webservices.data.core.service.item.EmailMessage;
import microsoft.exchange.webservices.data.core.service.item.Item;

/**
 * @author sathesh
 *
 */
public class CachedMailBodyAdapter {

    private static MailFunctions mailFunctions;
    private static CachedMailBodyDAO dao;

    public CachedMailBodyAdapter(Context context){
        if(dao==null){
            dao= new CachedMailBodyDAO(context);
        }
    }

    /*** CREATE QUERIES ***/

    /** Writes the Item to body cache
     *
     * @param message
     * @param mailType - the folder. Depending upon the mail type it will use folder name or folder id
     * @param mailFolderName
     * @param mailFolderId
     */
    public void cacheNewData(EmailMessage message, int mailType, String mailFolderName, String mailFolderId)  {
        try {
            String from_delimited = MailApplication.getDelimitedAddressString(message.getFrom());
            String to_delimited = MailApplication.getDelimitedAddressString(message.getToRecipients());
            String cc_delimited = MailApplication.getDelimitedAddressString(message.getCcRecipients());
            String bcc_delimited = MailApplication.getDelimitedAddressString(message.getBccRecipients());

            CachedMailBodyVO vo = covertItemToVO(mailType, message, mailFolderName, mailFolderId, from_delimited,
                    to_delimited,
                    cc_delimited,
                    bcc_delimited);
            dao.createOrUpdate(vo);
        } catch (Exception e) {
            if(BuildConfig.DEBUG)
                e.printStackTrace();
        }
    }

    /** Writes the body record to cache. Receives custom body html instead of taking from the Item
     *
     * @param message
     * @param body - custom body apart from body get from the item
     * @param mailType - the folder. Depending upon the mail type it will use folder name or folder id
     * @param mailFolderName
     * @param mailFolderId
     */
    public void cacheNewData(EmailMessage message, String body, int mailType, String mailFolderName, String mailFolderId)  {
        try {
            String from_delimited = MailApplication.getDelimitedAddressString(message.getFrom());
            String to_delimited = MailApplication.getDelimitedAddressString(message.getToRecipients());
            String cc_delimited = MailApplication.getDelimitedAddressString(message.getCcRecipients());
            String bcc_delimited = MailApplication.getDelimitedAddressString(message.getBccRecipients());

            CachedMailBodyVO vo = covertItemToVO(mailType, message, mailFolderName, mailFolderId, from_delimited,
                    to_delimited,
                    cc_delimited,
                    bcc_delimited);
            vo.setMail_body(body);  // provided body
            dao.createOrUpdate(vo);
        } catch (Exception e) {
            if(BuildConfig.DEBUG)
                e.printStackTrace();
        }
    }

    /*** SELECT QUERIES ***/

    /** This method will return all the cached mail body data list of VOs for the particular mail type given
     * @return
     */
    public List<CachedMailBodyVO> getMailBody(String itemId){
        List<CachedMailBodyVO> mailListHeaderData=null;
        try {
            //get the list of records for the item id. it will be 1 since the database will replace record
            mailListHeaderData = dao.getAllRecordsByItemId(itemId);
        } catch (Exception e) {
            if(BuildConfig.DEBUG)
                e.printStackTrace();
        }
        return mailListHeaderData;
    }

    /*** DELETE QUERIES ***/

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
            dao.deleteNByMailType(mailType, n);
        }
        else{
            //by folder id
            dao.deleteNByFolderId(mailFolderId, n);
        }
    }

    /*** PRIVATE METHODS ***/

    /** private method for converting Item to VO
     * @param mailFolderName
     *
     */
    private CachedMailBodyVO covertItemToVO(int mailType, Item item, String mailFolderName, String strFolderId, String from_delimited, String to_delimited, String cc_delimited, String bcc_delimited) throws ServiceLocalException, Exception{
        CachedMailBodyVO vo = new CachedMailBodyVO();
        mailFunctions = MailFunctionsImpl.getInstance();

        vo.setItem_id(mailFunctions.getItemId(item));
        vo.setFolder_name(mailFolderName);
        vo.setFolder_id(strFolderId);
        vo.setMail_type(mailType);
        vo.setMail_body(mailFunctions.getBody(item));
        vo.setMail_from_delimited(from_delimited);
        vo.setMail_to_delimited(to_delimited);
        vo.setMail_cc_delimited(cc_delimited);
        vo.setMail_bcc_delimited(bcc_delimited);
        return vo;
    }


}
