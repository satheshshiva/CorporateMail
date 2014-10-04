/**
 * 
 */
package com.wipromail.sathesh.application.interfaces;

import com.wipromail.sathesh.fragment.ViewMailFragment;
import com.wipromail.sathesh.service.data.EmailMessage;

/**
 * @author sathesh
 *
 */
public interface ViewMailFragmentDataPasser {

	/**
	 * 
	 */
	public void showAlertdialogPermanentDelete();

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


}
