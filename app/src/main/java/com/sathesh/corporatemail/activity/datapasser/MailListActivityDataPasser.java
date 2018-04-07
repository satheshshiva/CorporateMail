package com.sathesh.corporatemail.activity.datapasser;


import android.content.Context;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.RecyclerView;

public interface MailListActivityDataPasser {

    DrawerLayout getmDrawerLayout();

    Context getApplicationContext();
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

    void refreshDrawerListRecyclerView() throws Exception;
    void refreshDrawerListRecyclerView2() throws Exception;

}
