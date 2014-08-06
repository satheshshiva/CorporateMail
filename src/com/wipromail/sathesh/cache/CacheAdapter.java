package com.wipromail.sathesh.cache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.util.Log;

import com.wipromail.sathesh.BuildConfig;
import com.wipromail.sathesh.constants.Constants;
import com.wipromail.sathesh.customexceptions.NoInternetConnectionException;
import com.wipromail.sathesh.ews.EWSConnection;
import com.wipromail.sathesh.ews.MailFunctions;
import com.wipromail.sathesh.ews.MailFunctionsImpl;
import com.wipromail.sathesh.ews.NetworkCall;
import com.wipromail.sathesh.service.data.ExchangeService;
import com.wipromail.sathesh.service.data.Item;
import com.wipromail.sathesh.service.data.ItemId;
import com.wipromail.sathesh.service.data.ServiceLocalException;
import com.wipromail.sathesh.sqlite.db.dao.CachedMailHeaderDAO;
import com.wipromail.sathesh.sqlite.db.pojo.vo.CachedMailHeaderVO;

public class CacheAdapter implements  Constants{

	private static MailFunctions mailFunctions;

	//CODE HAS TO BE UPDATE FOR
	//VIEW MAIL ACTIVITY
	//POLLSERVERMNS.java
	/** writes the given arraylist of exchange items to cache
	 * @param context
	 * @param strFolderId 
	 * @param mailFolderName 
	 * @param MailType 
	 * @param items 
	 * @throws ServiceLocalException
	 * @throws Exception
	 */
	public static synchronized void writeCacheData(Context context, int mailType, String mailFolderName, String strFolderId, ArrayList<Item> items ) throws ServiceLocalException, Exception {
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
	private static CachedMailHeaderVO covertItemToVO(int mailType, String mailFolderName,  String strFolderId, Item item) throws ServiceLocalException, Exception{
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
