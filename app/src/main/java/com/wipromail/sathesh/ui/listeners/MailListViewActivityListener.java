/**
 *
 */
package com.wipromail.sathesh.ui.listeners;

import android.content.Intent;
import android.view.View;

import com.wipromail.sathesh.R;
import com.wipromail.sathesh.activity.MyPreferencesActivity;
import com.wipromail.sathesh.activity.datapasser.MailListActivityDataPasser;
import com.wipromail.sathesh.adapter.DrawerRecyclerViewAdapter;
import com.wipromail.sathesh.animation.ApplyAnimation;
import com.wipromail.sathesh.application.MyActivity;
import com.wipromail.sathesh.constants.Constants;
import com.wipromail.sathesh.constants.DrawerMenuRowType;
import com.wipromail.sathesh.service.data.WellKnownFolderName;
import com.wipromail.sathesh.sqlite.db.cache.vo.DrawerMenuVO;

/**
 * @author sathesh
 *
 */
public class MailListViewActivityListener implements  Constants, DrawerRecyclerViewAdapter.OnRecyclerViewClickListener, View.OnClickListener {

    private MailListActivityDataPasser activityDataPasser;
    private MyActivity activity;
    private DrawerMenuVO drawerMenuVO;
    private View page1View ;
    private View page2View;

    public MailListViewActivityListener(MailListActivityDataPasser activityDataPasser){
        this.activity = (MyActivity)activityDataPasser;
        this.activityDataPasser = activityDataPasser;
        page1View =  activity.findViewById(R.id.drawerLayoutPage1);
        page2View =  activity.findViewById(R.id.drawerLayoutPage2);
    }

    @Override
    public void onDrawerLayoutRecyclerViewClick(View view, int position, DrawerMenuVO drawerMenuVO) {
        Intent intent;

        this.drawerMenuVO = drawerMenuVO;

        switch(drawerMenuVO.getType()){
            case DrawerMenuRowType.INBOX:
            case DrawerMenuRowType.DRAFTS:
                activityDataPasser.getmDrawerLayout().closeDrawers();
                activityDataPasser.loadMailListViewFragment(drawerMenuVO.getType(), drawerMenuVO.getMenu_name(), null);
                break;
            case DrawerMenuRowType.SENT_ITEMS:
                activityDataPasser.getmDrawerLayout().closeDrawers();
                activityDataPasser.loadMailListViewFragment(drawerMenuVO.getType(), WellKnownFolderName.SentItems.toString(), null);
                break;
            case DrawerMenuRowType.DELETED_ITEMS:
                activityDataPasser.getmDrawerLayout().closeDrawers();
                activityDataPasser.loadMailListViewFragment(drawerMenuVO.getType(), WellKnownFolderName.DeletedItems.toString(), null);
                break;
            case DrawerMenuRowType.MORE_FOLDERS:
                page1View.setVisibility(View.GONE);
                page2View.setAnimation(ApplyAnimation.getDrawerLayoutPage2InAnimation(activity));
                page2View.setVisibility(View.VISIBLE);
                activityDataPasser.setDrawerLayouPage2Open(true);   //flag for use in the back navigation button
                break;

            case DrawerMenuRowType.FAVOURITE_FOLDERS:
                activityDataPasser.getmDrawerLayout().closeDrawers();
                activityDataPasser.loadMailListViewFragment(MailType.FOLDER_WITH_ID, drawerMenuVO.getMenu_name(), drawerMenuVO.getFolder_id());
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
        // for setting we should nothightlight the row
        if(drawerMenuVO.getType() != DrawerMenuRowType.SETTINGS){
            activityDataPasser.setDrawerLayoutSelectedPosition(position);
        }
    }

    public DrawerMenuVO getDrawerMenuVO() {
        return drawerMenuVO;
    }

    public void setDrawerMenuVO(DrawerMenuVO drawerMenuVO) {
        this.drawerMenuVO = drawerMenuVO;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.drawerBackButton:
                page1View.setAnimation(ApplyAnimation.getDrawerLayoutPage1InAnimation(activity));
                page1View.setVisibility(View.VISIBLE);
                page2View.setVisibility(View.GONE);
                activityDataPasser.setDrawerLayouPage2Open(false); //flag for use in the back navigation button
                break;

        }
    }
}