/**
 *
 */
package com.wipromail.sathesh.ui.listeners;

import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.view.View;

import com.wipromail.sathesh.R;
import com.wipromail.sathesh.activity.AboutActivity;
import com.wipromail.sathesh.activity.MyPreferencesActivity;
import com.wipromail.sathesh.activity.SearchContactsActivity;
import com.wipromail.sathesh.adapter.DrawerRecyclerViewAdapter;
import com.wipromail.sathesh.application.interfaces.MailListActivityDataPasser;
import com.wipromail.sathesh.constants.Constants;
import com.wipromail.sathesh.constants.DrawerMenuRowType;
import com.wipromail.sathesh.fragment.MailListViewFragment;
import com.wipromail.sathesh.service.data.WellKnownFolderName;
import com.wipromail.sathesh.sqlite.db.cache.vo.DrawerMenuVO;

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
    public void onDrawerLayoutRecyclerViewClick(View view, int position, DrawerMenuVO drawerMenuVO) {
        Intent intent;
        activityDataPasser.setDrawerLayoutSelectedPosition(position);

        switch(drawerMenuVO.getType()){
            case DrawerMenuRowType.INBOX:
            case DrawerMenuRowType.DRAFTS:
                activityDataPasser.setMailFolderName(drawerMenuVO.getMenu_name());
                activityDataPasser.setMailType(drawerMenuVO.getType());
                loadMailListViewFragment();
                break;
            case DrawerMenuRowType.SENT_ITEMS:
                activityDataPasser.setMailFolderName( WellKnownFolderName.SentItems.toString());
                activityDataPasser.setMailType(drawerMenuVO.getType());
                loadMailListViewFragment();
                break;
            case DrawerMenuRowType.DELETED_ITEMS:
                activityDataPasser.setMailFolderName( WellKnownFolderName.DeletedItems.toString());
                activityDataPasser.setMailType(drawerMenuVO.getType());
                loadMailListViewFragment();
                break;

            case DrawerMenuRowType.FAVOURITE_FOLDERS:
                activityDataPasser.setMailFolderName(drawerMenuVO.getMenu_name());
                activityDataPasser.setMailType(MailType.FOLDER_WITH_ID);
                activityDataPasser.setMailFolderId(drawerMenuVO.getFolder_id());
                loadMailListViewFragment();
                break;

            case DrawerMenuRowType.SEARCH_CONTACT:
                activityDataPasser.getmDrawerLayout().closeDrawers();
                intent = new Intent(activity, SearchContactsActivity.class);
                activity.startActivity(intent);
                break;

            case DrawerMenuRowType.SETTINGS:
                activityDataPasser.getmDrawerLayout().closeDrawers();
                intent = new Intent(activity, MyPreferencesActivity.class);
                activity.startActivity(intent);
                break;

            case DrawerMenuRowType.ABOUT:
                activityDataPasser.getmDrawerLayout().closeDrawers();
                intent=new Intent(activity, AboutActivity.class);
                activity.startActivity(intent);
                break;

            default:
                //usuaully header will come here. do nothing.
                break;

        }
    }

    //(re)loads the MailListViewFragment inside the MailListViewActivity
    private void loadMailListViewFragment() {
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