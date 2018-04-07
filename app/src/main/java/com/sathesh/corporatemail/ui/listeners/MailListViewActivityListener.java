/**
 *
 */
package com.sathesh.corporatemail.ui.listeners;

import android.content.Intent;
import android.view.View;

import com.sathesh.corporatemail.R;
import com.sathesh.corporatemail.activity.MyPreferencesActivity;
import com.sathesh.corporatemail.activity.datapasser.MailListActivityDataPasser;
import com.sathesh.corporatemail.adapter.DrawerRecyclerViewAdapter;
import com.sathesh.corporatemail.adapter.DrawerRecyclerViewMoreFoldersAdapter;
import com.sathesh.corporatemail.animation.ApplyAnimation;
import com.sathesh.corporatemail.application.MailApplication;
import com.sathesh.corporatemail.application.MyActivity;
import com.sathesh.corporatemail.constants.Constants;
import com.sathesh.corporatemail.constants.DrawerMenuRowType;
import com.sathesh.corporatemail.handlers.GetMoreFoldersHandler;
import com.sathesh.corporatemail.service.data.WellKnownFolderName;
import com.sathesh.corporatemail.sqlite.db.cache.vo.FolderVO;
import com.sathesh.corporatemail.util.Utilities;

/**
 * @author sathesh
 *
 */
public class MailListViewActivityListener implements  Constants, DrawerRecyclerViewAdapter.OnRecyclerViewClickListener, DrawerRecyclerViewMoreFoldersAdapter.OnRecyclerViewClick2Listener, View.OnClickListener {

    private MailListActivityDataPasser activityDataPasser;
    private MyActivity activity;
    private View page1View ;
    private View page2View;

    public MailListViewActivityListener(MailListActivityDataPasser activityDataPasser){
        this.activity = (MyActivity)activityDataPasser;
        this.activityDataPasser = activityDataPasser;
        page1View =  activity.findViewById(R.id.drawerLayoutPage1);
        page2View =  activity.findViewById(R.id.drawerLayoutPage2);
    }

    @Override
    public void onDrawerLayoutRecyclerViewClick(View view, int position, FolderVO folderVO) {
        Intent intent;

        try {
            switch (folderVO.getType()) {
                case DrawerMenuRowType.INBOX:
                case DrawerMenuRowType.DRAFTS:
                    activityDataPasser.getmDrawerLayout().closeDrawers();
                    activityDataPasser.loadMailListViewFragment(folderVO.getType(), folderVO.getName(), null);
                    break;
                case DrawerMenuRowType.SENT_ITEMS:
                    activityDataPasser.getmDrawerLayout().closeDrawers();
                    activityDataPasser.loadMailListViewFragment(folderVO.getType(), WellKnownFolderName.SentItems.toString(), null);
                    break;
                case DrawerMenuRowType.DELETED_ITEMS:
                    activityDataPasser.getmDrawerLayout().closeDrawers();
                    activityDataPasser.loadMailListViewFragment(folderVO.getType(), WellKnownFolderName.DeletedItems.toString(), null);
                    break;
                case DrawerMenuRowType.MORE_FOLDERS:
                    //hide the first recycler view
                    page1View.setVisibility(View.GONE);
                    //show the second recycler view with animation
                    page2View.setAnimation(ApplyAnimation.getDrawerLayoutPage2InAnimation(activity));
                    page2View.setVisibility(View.VISIBLE);

                    // reset the scroll position for the second recycler view if there was a selected item
                    if (!(activityDataPasser.getDrawerLayoutSelectedPosition2() > -1)) {
                        activityDataPasser.getmDrawerListRecyclerView2().scrollToPosition(0);
                    }

                    // starts a seperate thread for storing the all folders table
                    MailApplication.startGetMoreFoldersThread(activity, new GetMoreFoldersHandler((MailListActivityDataPasser) activity));

                    activityDataPasser.setDrawerLayouPage2Open(true);   //flag for use in the back navigation button
                    activityDataPasser.setDrawerLayoutSelectedPosition(-1);
                    if (activityDataPasser.getmDrawerListRecyclerView2().getAdapter() != null) {
                        activityDataPasser.refreshDrawerListRecyclerView2();
                    }

                    break;

                case DrawerMenuRowType.FAVOURITE_FOLDERS:
                    activityDataPasser.getmDrawerLayout().closeDrawers();
                    activityDataPasser.loadMailListViewFragment(MailType.FOLDER_WITH_ID, folderVO.getName(), folderVO.getFolder_id());
                    break;

                case DrawerMenuRowType.SEARCH_CONTACT:
                    activityDataPasser.getmDrawerLayout().closeDrawers();
                    activityDataPasser.loadSearchContactFragment();
                    break;

                case DrawerMenuRowType.SETTINGS:
                    activityDataPasser.getmDrawerLayout().closeDrawers();
                    intent = new Intent(activity, MyPreferencesActivity.class);
                    activity.startActivity(intent);
                    break;

                case DrawerMenuRowType.ABOUT:
                    activityDataPasser.getmDrawerLayout().closeDrawers();
                    activityDataPasser.loadAboutFragment(false);
                    break;

                default:
                    //usuaully header will come here. do nothing.
                    break;
            }

            //update the selected position
            // since setting will open in a seperate activity we are not updating for it
            if (folderVO.getType() != DrawerMenuRowType.SETTINGS && folderVO.getType() != DrawerMenuRowType.MORE_FOLDERS) {
                activityDataPasser.setDrawerLayoutSelectedPosition(position);
                activityDataPasser.setDrawerLayoutSelectedPosition2(-1);
            }
        }catch(Exception e){
            Utilities.generalCatchBlock(e, this);
        }
    }

    @Override
    public void onClick(View v) {
        try {
            switch (v.getId()) {
                ///back button is pressed in the drawer layout
                case R.id.drawer_back_icon:
                case R.id.drawer_back_btn:
                case R.id.drawer_back_layout:
                    //update the datasets
                    ((DrawerRecyclerViewAdapter) activityDataPasser.getmDrawerListRecyclerView1().getAdapter()).updateDataSets();
                    //call notify data set changed. to update if there is any change in the faves
                    activityDataPasser.getmDrawerListRecyclerView1().getAdapter().notifyDataSetChanged();
                    activityDataPasser.closeDrawerLayoutPage2();
                    break;

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Second Recycler View in the Drawer list.. On click events handler
     *
     * @param view
     * @param position
     * @param moreFolderVO
     */
    @Override
    public void onDrawerLayoutRecyclerView2Click(View view, int position, FolderVO moreFolderVO) {
        switch(moreFolderVO.getType()) {

            case DrawerMenuRowType.MoreFolders.HEADER:
                break;
            case DrawerMenuRowType.MoreFolders.FOLDER:
                activityDataPasser.getmDrawerLayout().closeDrawers();
                activityDataPasser.loadMailListViewFragment(MailType.FOLDER_WITH_ID, moreFolderVO.getName(), moreFolderVO.getFolder_id());
                break;
        }
        activityDataPasser.setDrawerLayoutSelectedPosition2(position);

    }
}