/**
 * 
 */
package com.sathesh.corporatemail.fragment.datapasser;

import com.sathesh.corporatemail.fragment.ViewMailFragment;
import com.sathesh.corporatemail.sqlite.db.cache.vo.CachedMailHeaderVO;

import microsoft.exchange.webservices.data.core.service.item.EmailMessage;

/**
 * @author sathesh
 *
 */
public interface ViewMailFragmentDataPasser {

	/**
	 * @throws Exception 
	 * 
	 */
	public void forwardMail() throws Exception;

	/**
	 * @param b
	 * @throws Exception 
	 */
	public void replyMail(boolean b) throws Exception;

	/**
	 * @return
	 */
	public EmailMessage getMessage();

	/**
	 * @return
	 */
	public int getMailType();

    public String getItemId();

    public ViewMailFragment.Status getCurrentStatus();
    public CachedMailHeaderVO getCachedMailHeaderVO();
    public void mailAsReadInCache();

}
