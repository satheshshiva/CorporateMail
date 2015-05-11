package com.wipromail.sathesh.ui.action;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.View;
import android.view.WindowManager;

import com.wipromail.sathesh.fragment.MailListViewFragment;
import com.wipromail.sathesh.fragment.datapasser.MailListFragmentDataPasser;

/**
 * Created by Sathesh on 3/19/15.
 *
 * When the Navigation drawer is opened or closed this is called.
 */
public class MyActionBarDrawerToggle extends ActionBarDrawerToggle {

    private Activity activity;
    private static boolean opening = true, closing=false;
    private static int existingNavigationBarColor;

    public MyActionBarDrawerToggle(Activity activity, DrawerLayout drawerLayout, int openDrawerContentDescRes, int closeDrawerContentDescRes) {
        super(activity, drawerLayout, openDrawerContentDescRes, closeDrawerContentDescRes);
        this.activity = activity;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
   @Override
    public void onDrawerOpened(View drawerView) {
        super.onDrawerOpened(drawerView);
        activity.invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
        // change the navigation bar color to translucent
        if(Build.VERSION.SDK_INT  >= Build.VERSION_CODES.LOLLIPOP) {
              changeNavigtionBarColor(true);
        }

        //MailListFragment instance
        MailListFragmentDataPasser mailListViewFragment = MailListViewFragment.getCurrentInstance();
        if(mailListViewFragment!=null){
            mailListViewFragment.getFab().hide();
        }
    }

   /* @Override
   // PERFORMANCE ISSUE
    public void onDrawerSlide (View drawerView, float slideOffset) {
        super.onDrawerOpened(drawerView);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            if (opening && slideOffset > 0.2) {
                Log.i("WIp", "SLIDING " + slideOffset);
                // change the navigation bar color to translucent
                changeNavigtionBarColor(true);
                opening = false;
                closing=true;
            }
            else if (closing && slideOffset < 0.2) {
                Log.i("WIp", "SLIDING " + slideOffset);
                // change the navigation bar color to translucent
                changeNavigtionBarColor(false);
                opening=true;
                closing = false;
            }
        }
    }*/

    @Override
    public void onDrawerClosed(View view) {
        super.onDrawerClosed(view);
        activity.invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()\
        // revert the navigation bar color from translucent
        if(Build.VERSION.SDK_INT  >= Build.VERSION_CODES.LOLLIPOP) {
             changeNavigtionBarColor(false);
        }
        //MailListFragment instance
        MailListFragmentDataPasser mailListViewFragment = MailListViewFragment.getCurrentInstance();
        if(mailListViewFragment!=null){
            mailListViewFragment.getFab().show();
        }

    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    /**
     * Changes the navigationBarColor to translucent when the navigation drawer is opnened
     * opening - true to change to translucent. false to revert to old color
     */
    private void changeNavigtionBarColor(boolean opening) {
        if(opening){
            existingNavigationBarColor = activity.getWindow().getNavigationBarColor();
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
        else{
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            activity.getWindow().setNavigationBarColor(existingNavigationBarColor);
        }
    }
}
