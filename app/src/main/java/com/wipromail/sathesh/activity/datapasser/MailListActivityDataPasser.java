package com.wipromail.sathesh.activity.datapasser;


import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.RecyclerView;

public interface MailListActivityDataPasser {

    DrawerLayout getmDrawerLayout();

    int getDrawerLayoutSelectedPosition();
    int getDrawerLayoutSelectedPosition2();

    void setDrawerLayoutSelectedPosition(int layoutPosition);
    void setDrawerLayoutSelectedPosition2(int layoutPosition);

    void loadMailListViewFragment(int mailType, String mailFolderName, String mailFolderId);

    void loadAboutFragment(boolean checkForUpdates);

    void loadSearchContactFragment();

    String getString(int Resid);

    void setDrawerLayouPage2Open(boolean drawerLayouPage2Open);

    void closeDrawerLayoutPage2();

    RecyclerView getmDrawerListRecyclerView1();
    RecyclerView getmDrawerListRecyclerView2();

}
