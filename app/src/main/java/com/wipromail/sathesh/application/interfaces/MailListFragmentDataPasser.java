package com.wipromail.sathesh.application.interfaces;


public interface MailListFragmentDataPasser {

	/** Refresh the List View from network
	 * 
	 */
	public void refreshList();
	
	
	/** refreshes the List View from Local cache
	 * 
	 */
	public void softRefreshList();
}
