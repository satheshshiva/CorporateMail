package com.wipromail.sathesh.application.interfaces;


import android.support.v4.widget.DrawerLayout;

public interface MailListFragmentDataPasser {

	/** Refresh the List View from network
	 * 
	 */
	public void refreshList();
	
	
	/** refreshes the List View from Local cache
	 * 
	 */
	public void softRefreshList();

    public android.support.v7.app.ActionBarDrawerToggle getmDrawerToggle();
    public DrawerLayout getmDrawerLayout();
}
