package com.wipromail.sathesh.application.interfaces;


import android.support.v4.widget.DrawerLayout;

public interface MailListActivityDataPasser {

	public int getMailType();

	public String getStrFolderId();

	public String getMailFolderName();

    public DrawerLayout getmDrawerLayout();

    public android.support.v7.app.ActionBarDrawerToggle getmDrawerToggle();

    public int getDrawerLayoutSelectedPosition();

    void setDrawerLayoutSelectedPosition(int layoutPosition);

    public void setMailListViewFragmentDataPasser(MailListFragmentDataPasser mailListViewFragment);

    public void setMailType(int mailType) ;

    public void setMailFolderName(String mailFolderName) ;

    public String getMailFolderId() ;

    public void setMailFolderId(String mailFolderId) ;

}
