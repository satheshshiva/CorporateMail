/**
 *
 */
package com.wipromail.sathesh.ui.listeners;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.view.View;

import com.wipromail.sathesh.R;
import com.wipromail.sathesh.adapter.DrawerRecyclerViewAdapter;
import com.wipromail.sathesh.application.interfaces.MailListActivityDataPasser;
import com.wipromail.sathesh.constants.Constants;
import com.wipromail.sathesh.fragment.MailListViewFragment;

/**
 * @author sathesh
 *
 */
public class MailListViewActivityListener implements  Constants, DrawerRecyclerViewAdapter.OnRecyclerViewClickListener {

    private MailListActivityDataPasser activityDataPasser;
    private ActionBarActivity activity;

    public MailListViewActivityListener(MailListActivityDataPasser activityDataPasser){
        this.activity = (ActionBarActivity)activityDataPasser;
        this.activityDataPasser = activityDataPasser;
    }

    @Override
    public void onDrawerLayoutRecyclerViewClick(View view, int position) {
        activityDataPasser.getmDrawerLayout().closeDrawers();

        //using fragment transaction, replace the fragment
        FragmentManager fm = activity.getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        MailListViewFragment _mailListViewFragment = new MailListViewFragment();
        ft.replace(R.id.mailListFragmentLayout,  _mailListViewFragment);
        ft.commit();

        // replace the newly created fragment object
        activityDataPasser.setMailListViewFragmentDataPasser(_mailListViewFragment);

        //Setting the Mail type, folder name, id
       // activityDataPasser.setMailType(MailType.DRAFTS);
       // activityDataPasser.setMailFolderName(WellKnownFolderName.Drafts.toString());

        // create a new listener for the fragment
        MailListViewListener fragmentListener = new MailListViewListener(activityDataPasser, _mailListViewFragment);
        activityDataPasser.setFragmentListener(fragmentListener);
    }
}