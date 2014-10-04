package com.wipromail.sathesh.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.SubMenu;
import com.google.analytics.tracking.android.EasyTracker;
import com.wipromail.sathesh.R;
import com.wipromail.sathesh.application.interfaces.ViewMailFragmentDataPasser;
import com.wipromail.sathesh.asynccaller.DeleteMailAsyncCaller;
import com.wipromail.sathesh.constants.Constants;
import com.wipromail.sathesh.fragment.ViewMailFragment;
import com.wipromail.sathesh.ui.OptionsUIContent;

public class ViewMailActivity extends SherlockFragmentActivity implements Constants{

    private ViewMailFragmentDataPasser viewMailFragment;
    /** ON CREATE **
     *  Fragment : ViewMailFragment
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_view_mail);

        // declaring the fragment
        viewMailFragment = (ViewMailFragmentDataPasser) getSupportFragmentManager()
                .findFragmentById(R.id.viewMailFragment);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        MenuItem subMenuItem;

        //if the current status is not loading or error states then show the menus
        if(viewMailFragment.getCurrentStatus() != ViewMailFragment.Status.LOADING
                && viewMailFragment.getCurrentStatus() != ViewMailFragment.Status.ERROR) {
            //Reply submenu
            SubMenu subMenuReply = menu.addSubMenu(this.getString(R.string.actionBar_Submenu_Reply_Options));
            subMenuReply
                    .add(this.getString(R.string.actionBar_Reply))
                    .setIcon(OptionsUIContent.getReplyIcon())
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

            subMenuReply
                    .add(this.getString(R.string.actionBar_Reply_All))
                    .setIcon(OptionsUIContent.getReplyAllIcon())
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

            subMenuReply
                    .add(this.getString(R.string.actionBar_Forward))
                    .setIcon(OptionsUIContent.getForwardIcon())
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

            //Setting icons and settings to Reply Submenu
            subMenuItem = subMenuReply.getItem();
            subMenuItem.setIcon(OptionsUIContent.getReplyIcon());
            subMenuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);

            //Adding Delete Icon to Main Menu
            menu.add(this.getString(R.string.actionBar_Delete))
                    .setIcon(OptionsUIContent.getDeleteIcon())
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);

        }
        // Attachment main menu
		/*	menu.add(ACTIONBAR_ATTACHMENT)
		.setIcon(OptionsUIContent.getAttachementIcon())
		.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS|MenuItem.SHOW_AS_ACTION_WITH_TEXT);


        //Overflow submenu
        SubMenu subMenu = menu.addSubMenu(this.getString(R.string.actionBar_Submenu_Others));

        //Overflow  main menu
        subMenuItem = subMenu.getItem();
        subMenuItem.setIcon(OptionsUIContent.getMoreoverFlowIcon());
        subMenuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        */
        return true;
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if(item!=null && item.getItemId()==android.R.id.home){
            finish();
        }

        else if(item!=null && item.getTitle().equals(this.getString(R.string.actionBar_Reply))){
            try {
                viewMailFragment.replyMail(false);
            } catch (Exception e) {
                Log.e(TAG, "ViewMailActivity -> Reply Mail exception");
                e.printStackTrace();
            }
        }
        else if(item!=null && item.getTitle().equals(this.getString(R.string.actionBar_Reply_All))){
            try {
                viewMailFragment.replyMail(true);
            } catch (Exception e) {
                Log.e(TAG, "ViewMailActivity -> Reply Mail exception");
                e.printStackTrace();
            }
        }
        else if(item!=null && item.getTitle().equals(this.getString(R.string.actionBar_Forward))){
            try {
                viewMailFragment.forwardMail();
            } catch (Exception e) {
                Log.e(TAG, "ViewMailActivity -> Forward Mail exception");
                e.printStackTrace();
            }
        }

        else if(item!=null && item.getTitle().equals(this.getString(R.string.actionBar_Delete))){
            if(viewMailFragment.getMailType() != MailType.DELETED_ITEMS){
                DeleteMailAsyncCaller deleteCaller;
                deleteCaller = new DeleteMailAsyncCaller(this, viewMailFragment.getMessage(),viewMailFragment.getItemId(), false);
                deleteCaller.startDeleteMailAsyncTask();
            }else{
                viewMailFragment.showAlertdialogPermanentDelete();
            }
        }

        else if(item!=null && item.getTitle().equals(this.getString(R.string.actionBar_Attachment))){
            //	Notifications.showToast(this.getApplicationContext(),getString(R.string.attachment_feature_not_avaialable) , Toast.LENGTH_SHORT);
        }

        else if(item!=null && item.getTitle().equals(ACTIONBAR_SETTINGS)){
            Intent intent = new Intent(this, PreferencesActivity.class);
            startActivity(intent);
        }
        else if(item!=null && item.getTitle().equals(ACTIONBAR_ABOUT)){
            Intent intent = new Intent(this, AboutActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    // Google Analytics
    @Override
    public void onStart() {
        super.onStart();
        EasyTracker.getInstance().activityStart(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EasyTracker.getInstance().activityStop(this);
    }

}


