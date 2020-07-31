package com.sathesh.corporatemail.ui.action;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.view.View;
import android.view.WindowManager;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.drawerlayout.widget.DrawerLayout;

import com.sathesh.corporatemail.activity.MailListViewActivity;
import com.sathesh.corporatemail.activity.datapasser.MailListActivityDataPasser;
import com.sathesh.corporatemail.fragment.MailListViewFragment;
import com.sathesh.corporatemail.fragment.SearchContactFragment;
import com.sathesh.corporatemail.fragment.datapasser.MailListFragmentDataPasser;
import com.sathesh.corporatemail.ui.util.UIutilities;
import com.sathesh.corporatemail.util.Utilities;

/**
 * Created by Sathesh on 3/19/15.
 *
 * When the Navigation drawer is opened or closed this is called.
 */
public class MyActionBarDrawerToggle extends ActionBarDrawerToggle {

    private Activity activity;
    private static int existingNavigationBarColor;
    private MailListActivityDataPasser activityDataPasser;

    public MyActionBarDrawerToggle(Activity activity, MailListActivityDataPasser activityDataPasser, DrawerLayout drawerLayout, int openDrawerContentDescRes, int closeDrawerContentDescRes) {
        super(activity, drawerLayout, openDrawerContentDescRes, closeDrawerContentDescRes);
        this.activity = activity;
        this.activityDataPasser = activityDataPasser;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
   @Override
    public void onDrawerOpened(View drawerView) {
        super.onDrawerOpened(drawerView);

        try {
            activity.invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            // change the navigation bar color to translucent
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                changeNavigtionBarColor(true);
            }

            //refresh the list (may be favourites got changed
            activityDataPasser.refreshDrawerListRecyclerView();

            // Some Actions based on the current fragment like hiding FAB, keyboard etc.,
            //check the currently loaded fragment in the MailListViewActivity
            if (MailListViewActivity.getInstance() != null
                    && MailListViewActivity.getInstance().getCurrentlyLoadedFragment() != null) {
                if ( MailListViewActivity.getInstance().getCurrentlyLoadedFragment() instanceof MailListViewFragment) {

                    //MailListFragment instance
                    MailListFragmentDataPasser mailListViewFragment = MailListViewFragment.getInstance();
                    if (mailListViewFragment != null) {
                        //hide the Floating Action Button
                        mailListViewFragment.getFab().hide();
                    }
                }
                // Seach Fragment
                else if(MailListViewActivity.getInstance().getCurrentlyLoadedFragment() instanceof SearchContactFragment){
                    //hide keyboard
                    SearchContactFragment searchContactFragment = SearchContactFragment.getInstance();
                    UIutilities.hideKeyBoard(activity, searchContactFragment.getContactSearch());
                }
            }
        } catch (Exception e) {
            Utilities.generalCatchBlock(e,this);
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
        MailListFragmentDataPasser mailListViewFragment = MailListViewFragment.getInstance();
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
