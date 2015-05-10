/**
 *
 */
package com.wipromail.sathesh.ui.listeners;

import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;

import com.wipromail.sathesh.R;
import com.wipromail.sathesh.activity.MyPreferencesActivity;
import com.wipromail.sathesh.activity.SearchContactsActivity;
import com.wipromail.sathesh.adapter.DrawerRecyclerViewAdapter;
import com.wipromail.sathesh.application.MyActivity;
import com.wipromail.sathesh.application.interfaces.MailListActivityDataPasser;
import com.wipromail.sathesh.constants.Constants;
import com.wipromail.sathesh.constants.DrawerMenuRowType;
import com.wipromail.sathesh.fragment.AboutFragment;
import com.wipromail.sathesh.fragment.MailListViewFragment;
import com.wipromail.sathesh.service.data.WellKnownFolderName;
import com.wipromail.sathesh.sqlite.db.cache.vo.DrawerMenuVO;

/**
 * @author sathesh
 *
 */
public class MailListViewActivityListener implements  Constants, DrawerRecyclerViewAdapter.OnRecyclerViewClickListener {

    private MailListActivityDataPasser activityDataPasser;
    private MyActivity activity;

    public MailListViewActivityListener(MailListActivityDataPasser activityDataPasser){
        this.activity = (MyActivity)activityDataPasser;
        this.activityDataPasser = activityDataPasser;
    }

    @Override
    public void onDrawerLayoutRecyclerViewClick(View view, int position, DrawerMenuVO drawerMenuVO) {
        Intent intent;
        activityDataPasser.setDrawerLayoutSelectedPosition(position);

        switch(drawerMenuVO.getType()){
            case DrawerMenuRowType.INBOX:
            case DrawerMenuRowType.DRAFTS:
                loadMailListViewFragment( drawerMenuVO.getType(), drawerMenuVO.getMenu_name(), null);
                break;
            case DrawerMenuRowType.SENT_ITEMS:
                loadMailListViewFragment(drawerMenuVO.getType(), WellKnownFolderName.SentItems.toString(), null);
                break;
            case DrawerMenuRowType.DELETED_ITEMS:
                loadMailListViewFragment(drawerMenuVO.getType(), WellKnownFolderName.DeletedItems.toString(), null);
                break;

            case DrawerMenuRowType.FAVOURITE_FOLDERS:
                loadMailListViewFragment(MailType.FOLDER_WITH_ID, drawerMenuVO.getMenu_name(), drawerMenuVO.getFolder_id());
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
                loadAboutFragment();
                break;

            default:
                //usuaully header will come here. do nothing.
                break;
        }
    }

    //(re)loads the MailListViewFragment inside the MailListViewActivity
    private void loadMailListViewFragment(int mailType, String mailFolderName, String mailFolderId) {
        activityDataPasser.getmDrawerLayout().closeDrawers();
        //using fragment transaction, replace the fragment
        FragmentManager fm = activity.getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        MailListViewFragment fragment = MailListViewFragment.newInstance(mailType, mailFolderName, mailFolderId);
        ft.replace(R.id.mailListFragmentLayout, fragment);
        ft.commit();
    }

    //loads the AboutFragment inside the MailListViewActivity
    private void loadAboutFragment() {
        activityDataPasser.getmDrawerLayout().closeDrawers();
        //using fragment transaction, replace the fragment
        FragmentManager fm = activity.getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        AboutFragment fragment = AboutFragment.newInstance("");
        ft.replace(R.id.mailListFragmentLayout, fragment);
        ft.commit();

    }
}