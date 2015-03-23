package com.wipromail.sathesh.application.interfaces;


import android.support.v4.widget.DrawerLayout;

import com.wipromail.sathesh.ui.listeners.MailListViewListener;

public interface MailListActivityDataPasser {

	public int getMailType();

	public String getStrFolderId();

	public String getMailFolderName();

    public DrawerLayout getmDrawerLayout();

    public android.support.v7.app.ActionBarDrawerToggle getmDrawerToggle();

    public MailListViewListener getFragmentListener() ;

    public int getDrawerLayoutSelectedPosition();

    void setDrawerLayoutSelectedPosition(int layoutPosition);

    public void setMailListViewFragmentDataPasser(MailListFragmentDataPasser mailListViewFragment);

    public void setFragmentListener(MailListViewListener fragmentListener);

}
