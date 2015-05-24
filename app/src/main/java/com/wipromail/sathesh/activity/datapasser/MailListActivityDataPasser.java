package com.wipromail.sathesh.activity.datapasser;


import android.support.v4.widget.DrawerLayout;

public interface MailListActivityDataPasser {

    DrawerLayout getmDrawerLayout();

    int getDrawerLayoutSelectedPosition();

    void setDrawerLayoutSelectedPosition(int layoutPosition);

    void loadMailListViewFragment(int mailType, String mailFolderName, String mailFolderId);

    void loadAboutFragment(boolean checkForUpdates);

    void loadSearchContactFragment();

    String getString(int Resid);

}
