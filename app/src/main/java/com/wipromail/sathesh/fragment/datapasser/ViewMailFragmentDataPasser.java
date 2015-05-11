/**
 * 
 */
package com.wipromail.sathesh.fragment.datapasser;

import com.wipromail.sathesh.fragment.ViewMailFragment;
import com.wipromail.sathesh.service.data.EmailMessage;
import com.wipromail.sathesh.sqlite.db.cache.vo.CachedMailHeaderVO;

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
