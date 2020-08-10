package com.sathesh.corporatemail.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;

import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuItemCompat;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.sathesh.corporatemail.R;
import com.sathesh.corporatemail.adapter.ViewMailPagerAdapter;
import com.sathesh.corporatemail.application.MailApplication;
import com.sathesh.corporatemail.application.MyActivity;
import com.sathesh.corporatemail.cache.adapter.CachedMailHeaderAdapter;
import com.sathesh.corporatemail.constants.Constants;
import com.sathesh.corporatemail.fragment.ViewMailFragment;
import com.sathesh.corporatemail.fragment.datapasser.ViewMailFragmentDataPasser;
import com.sathesh.corporatemail.sqlite.db.cache.vo.CachedMailHeaderVO;
import com.sathesh.corporatemail.ui.components.MailDeleteDialog;
import com.sathesh.corporatemail.ui.util.OptionsUIContent;
import com.sathesh.corporatemail.util.Utilities;

import java.util.ArrayList;

public class ViewMailActivity extends MyActivity implements Constants{

    private CachedMailHeaderAdapter mailHeaderAdapter;
    private ViewPager2 viewPager;
    private FragmentStateAdapter pagerAdapter;
    private ArrayList<CachedMailHeaderVO> cachedHeaderVoList;
    /** ON CREATE **
     *  Fragment : ViewMailFragment
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //note: this will trigger OnCreateView in fragment
        setContentView(R.layout.activity_view_mail);

        cachedHeaderVoList = (ArrayList<CachedMailHeaderVO>) getIntent().getSerializableExtra(MailListViewActivity.EXTRA_MESSAGE_CACHED_ALL_MAIL_HEADERS);
        int position = getIntent().getIntExtra(MailListViewActivity.EXTRA_MESSAGE_POSITION, 0);

        viewPager = (ViewPager2) findViewById(R.id.pager);
        pagerAdapter = new ViewMailPagerAdapter(this, cachedHeaderVoList);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setCurrentItem(position);
        // declaring the fragment
        /*viewMailFragment = (ViewMailFragmentDataPasser) getSupportFragmentManager()
                .findFragmentById(R.id.viewMailFragment);*/
        mailHeaderAdapter = new CachedMailHeaderAdapter(this);
        MailApplication.toolbarInitialize(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        MenuItem menuItem;
        MenuItem subMenuItem;

        //TODO uncomment this
        //if the current status is not loading or error states then show the menus
       /* if(viewMailFragment.getCurrentStatus() != null
                && viewMailFragment.getCurrentStatus() != ViewMailFragment.Status.LOADING
                && viewMailFragment.getCurrentStatus() != ViewMailFragment.Status.ERROR) {
            //Reply submenu
            SubMenu subMenuReply = menu.addSubMenu(this.getString(R.string.actionBar_Submenu_Reply_Options));
            menuItem=subMenuReply
                    .add(this.getString(R.string.actionBar_Reply))
                    .setIcon(OptionsUIContent.getReplyIcon());
            MenuItemCompat.setShowAsAction(menuItem, MenuItem.SHOW_AS_ACTION_IF_ROOM);

            menuItem=subMenuReply
                    .add(this.getString(R.string.actionBar_Reply_All))
                    .setIcon(OptionsUIContent.getReplyAllIcon());
            MenuItemCompat.setShowAsAction(menuItem, MenuItem.SHOW_AS_ACTION_IF_ROOM);

            menuItem=subMenuReply
                    .add(this.getString(R.string.actionBar_Forward))
                    .setIcon(OptionsUIContent.getForwardIcon());
            MenuItemCompat.setShowAsAction(menuItem, MenuItem.SHOW_AS_ACTION_IF_ROOM);

            //Setting icons and settings to Reply Submenu
            subMenuItem = subMenuReply.getItem();
            subMenuItem.setIcon(OptionsUIContent.getReplyIcon());
            MenuItemCompat.setShowAsAction(subMenuItem, MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);

            //Adding Delete Icon to Main Menu
            menuItem=menu.add(this.getString(R.string.actionBar_Delete))
                    .setIcon(OptionsUIContent.getDeleteIcon());
            MenuItemCompat.setShowAsAction(menuItem, MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);

        }*/
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
    //TODO uncomment this
    //if the current status is not loading or error states then show the menus
    {
        /*
        if(item!=null && item.getItemId()==android.R.id.home){
            //Mark the item as read
            //this is done here because when the mail listview network
            // refresh happens after it getting overriden
            viewMailFragment.mailAsReadInCache();
            finish();
        }

        else if(item!=null && item.getTitle().equals(this.getString(R.string.actionBar_Reply))){
            try {
                viewMailFragment.replyMail(false);
            } catch (Exception e) {
                Log.e(LOG_TAG, "ViewMailActivity -> Reply Mail exception");
                e.printStackTrace();
            }
        }
        else if(item!=null && item.getTitle().equals(this.getString(R.string.actionBar_Reply_All))){
            try {
                viewMailFragment.replyMail(true);
            } catch (Exception e) {
                Log.e(LOG_TAG, "ViewMailActivity -> Reply Mail exception");
                e.printStackTrace();
            }
        }
        else if(item!=null && item.getTitle().equals(this.getString(R.string.actionBar_Forward))){
            try {
                viewMailFragment.forwardMail();
            } catch (Exception e) {
                Log.e(LOG_TAG, "ViewMailActivity -> Forward Mail exception");
                e.printStackTrace();
            }
        }

        else if(item!=null && item.getTitle().equals(this.getString(R.string.actionBar_Delete))){
            try {

                // Delete button is clicked
                if (viewMailFragment.getMailType() != MailType.DELETED_ITEMS) {

                    MailDeleteDialog dialog = new MailDeleteDialog();
                    dialog.mailDeleteDialog(this, mailHeaderAdapter, viewMailFragment.getCachedMailHeaderVO());

                } else {
                    //if in Deleted Items folder show a dialog saying it will permanently delete
                    MailDeleteDialog dialog = new MailDeleteDialog();
                    dialog.mailPermanentDelete(this, mailHeaderAdapter, viewMailFragment.getCachedMailHeaderVO());
                }
            } catch (Exception e) {
                Utilities.generalCatchBlock(e,this);
            }
        }
        */
          return super.onOptionsItemSelected(item);

    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onBackPressed(){
        //Mark the item as read
        //this is done here because when the mail listview network
        // refresh happens after it getting overriden

        //TODO uncomment this
        //viewMailFragment.mailAsReadInCache();
        super.onBackPressed();
    }

}