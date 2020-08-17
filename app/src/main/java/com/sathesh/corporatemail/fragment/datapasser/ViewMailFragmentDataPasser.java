/**
 * 
 */
package com.sathesh.corporatemail.fragment.datapasser;

import android.view.View;

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
	void forwardMail() throws Exception;

	/**
	 * @param b
	 * @throws Exception 
	 */
	void replyMail(boolean b) throws Exception;

	/**
	 * @return
	 */
	EmailMessage getMessage();

	/**
	 * @return
	 */
	int getMailType();

    String getItemId();

    ViewMailFragment.Status getCurrentStatus();
    CachedMailHeaderVO getCachedMailHeaderVO();
    void mailAsReadInCache();
    void expandBtnOnClick(View view);

}
