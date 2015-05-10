package com.wipromail.sathesh.application.interfaces;


import android.support.v4.widget.DrawerLayout;

public interface MailListActivityDataPasser {

    DrawerLayout getmDrawerLayout();

    int getDrawerLayoutSelectedPosition();

    void setDrawerLayoutSelectedPosition(int layoutPosition);

}
