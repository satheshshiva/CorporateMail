package com.wipromail.sathesh.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.TextView;

import com.wipromail.sathesh.BuildConfig;
import com.wipromail.sathesh.R;
import com.wipromail.sathesh.adapter.DrawerRecyclerViewAdapter;
import com.wipromail.sathesh.application.MailApplication;
import com.wipromail.sathesh.application.MyActivity;
import com.wipromail.sathesh.application.NotificationProcessing;
import com.wipromail.sathesh.application.SharedPreferencesAdapter;
import com.wipromail.sathesh.application.interfaces.MailListActivityDataPasser;
import com.wipromail.sathesh.application.interfaces.MailListFragmentDataPasser;
import com.wipromail.sathesh.constants.Constants;
import com.wipromail.sathesh.fragment.MailListViewFragment;
import com.wipromail.sathesh.tools.CacheClear;
import com.wipromail.sathesh.ui.action.MyActionBarDrawerToggle;
import com.wipromail.sathesh.ui.listeners.MailListViewActivityListener;
import com.wipromail.sathesh.ui.listeners.MailListViewListener;
import com.wipromail.sathesh.ui.util.OptionsUIContent;

/** This Activity is the one which shows the mail list.
 *
 * All the heavy loading is done by the fragment MailListViewFragment
 *
 * @author sathesh
 *
 */
public class MailListViewActivity extends MyActivity implements Constants,MailListActivityDataPasser{

    private MailListFragmentDataPasser mailListViewFragmentDataPasser;

    public final static String MAIL_TYPE_EXTRA = "MAIL_TYPE_EXTRA";
    public final static String FOLDER_ID_EXTRA = "FOLDER_ID_EXTRA";
    public final static String FOLDER_NAME_EXTRA = "FOLDER_NAME_EXTRA";

    public static final String EXTRA_MESSAGE_CACHED_HEADER = "cachedMailHeaderToOpen";

    private int mailType ;
    private String mailFolderName;
    private String mailFolderId="";
    private TextView dispName;
    private TextView companyName;
    private Activity activity;
    private Context context;

    private int drawerLayoutSelectedPosition=0;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;

    private MailListViewListener fragmentListener;
    private MailListViewActivityListener activityListener;

    /** ON CREATE **
     *  Fragment : MailListViewFragment
     * Gets the mailType folder id and folder name from the intent params.
     * Makes it ready by getters so that fragment can make use of
     * (non-Javadoc)
     * @see android.support.v4.app.FragmentActivity#onCreate(android.os.Bundle)
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activity = this;
        context = this;

        mailType = getIntent().getIntExtra(MAIL_TYPE_EXTRA,0);
        mailFolderId = getIntent().getStringExtra(FOLDER_ID_EXTRA);
        mailFolderName = getIntent().getStringExtra(FOLDER_NAME_EXTRA);

        //note: this will trigger the OnCreateView in the fragment.
        setContentView(R.layout.activity_mail_list_view);

        //Initializing the fragment MailListViewFragment
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        if(mailListViewFragmentDataPasser == null ) {
            mailListViewFragmentDataPasser = new MailListViewFragment();
        }
        ft.replace(R.id.mailListFragmentLayout, (android.support.v4.app.Fragment) mailListViewFragmentDataPasser);
        ft.commit();

        if(activityListener == null){
            activityListener = new MailListViewActivityListener(this);
        }

        // Initializing the fragmentListener
        if(fragmentListener ==null) {
            fragmentListener = new MailListViewListener(this, mailListViewFragmentDataPasser);
        }

        // Initializing the Drawer Layout
        //Navigation Drawer
        String[] mailfolderNames = context.getResources().getStringArray(R.array.drawerMailFolderNames);
        String[] mailfolderIcons = context.getResources().getStringArray(R.array.drawerMailFolderIcons);

        RecyclerView mDrawerList = (RecyclerView) activity.findViewById(R.id.recyclerView);
        mDrawerList.setScrollContainer(true);

        mDrawerList.setAdapter(new DrawerRecyclerViewAdapter(this, mailfolderNames, mailfolderIcons, activityListener));
        mDrawerList.setLayoutManager(new LinearLayoutManager(context));
        mDrawerLayout = (DrawerLayout)activity.findViewById(R.id.drawer_layout);

        //Navigation Drawer Slider Listener

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = new MyActionBarDrawerToggle(
                this,
                mDrawerLayout,
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
        );

        mDrawerLayout.setDrawerListener(mDrawerToggle);

        // Showing the display name and company name in Drawer Layout
        dispName = (TextView) findViewById(R.id.dispName);
        companyName = (TextView) findViewById(R.id.companyName);

        try {
            dispName.setText(SharedPreferencesAdapter.getUserDetailsDisplayName(context));
            companyName.setText(SharedPreferencesAdapter.getUserDetailsCompanyName(context));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /** ON START **
     * Starts the MNS service
     * (non-Javadoc)
     * @see android.support.v4.app.FragmentActivity#onStart()
     */
    @Override
    public void onStart() {
        super.onStart();
        //Notification Service
        if(BuildConfig.DEBUG)
            Log.i(TAG, "MailListViewActivity -> Starting MNS Service");
        MailApplication.startMNSService(this);
    }

    /** ON STOP  **
     * Google Analytics
     *  (non-Javadoc)
     */
    @Override
    public void onStop() {
        super.onStop();
    }

    /** ON DESTROY **
     * Delete the cached images
     * (non-Javadoc)
     */
    @Override
    public void onDestroy() {
        super.onDestroy();

        try {
            //clears cache destined for this actvity exit. clears the inline imgs, keeps the top 100 mail headers and body
            CacheClear cacheClear = new CacheClear();
            cacheClear.mailListViewClearCache(this,mailType, mailFolderId);

        } catch (Exception e) {
            if(BuildConfig.DEBUG){
                Log.d(TAG, "MailListViewActivity -> Exception while deleting cache" + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /** OPTION ITEMS **/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        MenuItem menuItem;

        //Always Visible menu
        menuItem=menu.add(ACTIONBAR_COMPOSE)
                .setIcon(OptionsUIContent.getComposeIcon());
        MenuItemCompat.setShowAsAction(menuItem,MenuItem.SHOW_AS_ACTION_ALWAYS|MenuItem.SHOW_AS_ACTION_WITH_TEXT);

        //Submenu
        SubMenu subMenu = menu.addSubMenu(ACTIONBAR_OVERFLOW).setIcon(OptionsUIContent.getMoreoverFlowIcon());
        //Overflow submenu icon
        menuItem = subMenu.getItem();
        MenuItemCompat.setShowAsAction(menuItem,MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);

        //Refresh Submenu
        menuItem=subMenu.add(ACTIONBAR_REFRESH)
                .setIcon(OptionsUIContent.getRefreshIcon());
        MenuItemCompat.setShowAsAction(menuItem,MenuItem.SHOW_AS_ACTION_IF_ROOM);

        //Settings Submenu
        menuItem=subMenu
                .add(ACTIONBAR_SETTINGS)
                .setIcon(OptionsUIContent.getSettingsIcon());
        MenuItemCompat.setShowAsAction(menuItem,MenuItem.SHOW_AS_ACTION_IF_ROOM);

        //About Submenu
        menuItem=subMenu
                .add(ACTIONBAR_ABOUT)
                .setIcon(OptionsUIContent.getAboutIcon());
        MenuItemCompat.setShowAsAction(menuItem,MenuItem.SHOW_AS_ACTION_IF_ROOM);

        return true;
    }

    /** OPTION ITEM SELECTED **/
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        if(item!=null && item.getTitle()!=null) {
            if (item != null && item.getTitle().equals(ACTIONBAR_REFRESH)) {
                mailListViewFragmentDataPasser.refreshList();
            } else if ( item.getTitle().equals(ACTIONBAR_COMPOSE)) {
                Intent intent = new Intent(this, ComposeActivity.class);
                startActivity(intent);
            } else if (item.getTitle().equals(ACTIONBAR_SETTINGS)) {
                Intent intent = new Intent(this, MyPreferencesActivity.class);
                startActivity(intent);
            } else if (item.getTitle().equals(ACTIONBAR_ABOUT)) {
                Intent intent = new Intent(this, AboutActivity.class);
                startActivity(intent);
            }
        }
        return super.onOptionsItemSelected(item);
    }

    /** ON RESUME **/
    @Override
    public void onResume() {
        super.onResume();

        try {
            //cancel all the notifications
            NotificationProcessing.cancelAllNotifications(this);
            mailListViewFragmentDataPasser.softRefreshList();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

 /*   @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }*/

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onBackPressed() {
        //this will close the navigation drawer first when its open
        if(mDrawerLayout.isDrawerOpen(Gravity.LEFT)){
            mDrawerLayout.closeDrawer(Gravity.LEFT);
        }else{
            super.onBackPressed();
        }
    }

    // Drawer layout header on click will call this. To prevent the underlying element click
    public void emptyClick(View view){
        //do nothing

    }

    /** GETTER SETTER PART **/
    @Override
    public int getMailType() {
        return mailType;
    }

    @Override
    public void setMailType(int mailType) {
        this.mailType = mailType;
    }

    @Override
    public void setMailFolderName(String mailFolderName) {
        this.mailFolderName = mailFolderName;
    }

    @Override
    public String getMailFolderId() {
        return mailFolderId;
    }

    @Override
    public void setMailFolderId(String mailFolderId) {
        this.mailFolderId = mailFolderId;
    }

    @Override
    public String getStrFolderId() {
        return mailFolderId;
    }

    @Override
    public String getMailFolderName() {
        return mailFolderName;
    }

    public DrawerLayout getmDrawerLayout() {
        return mDrawerLayout;
    }

    public void setmDrawerLayout(DrawerLayout mDrawerLayout) {
        this.mDrawerLayout = mDrawerLayout;
    }

    public ActionBarDrawerToggle getmDrawerToggle() {
        return mDrawerToggle;
    }

    public void setmDrawerToggle(ActionBarDrawerToggle mDrawerToggle) {
        this.mDrawerToggle = mDrawerToggle;
    }

    public MailListViewListener getFragmentListener() {
        return fragmentListener;
    }

    @Override
    public void setFragmentListener(MailListViewListener fragmentListener) {
        this.fragmentListener = fragmentListener;
    }

    public int getDrawerLayoutSelectedPosition() {
        return drawerLayoutSelectedPosition;
    }

    public void setDrawerLayoutSelectedPosition(int drawerLayoutSelectedPosition) {
        this.drawerLayoutSelectedPosition = drawerLayoutSelectedPosition;
    }

    public MailListFragmentDataPasser getMailListViewFragmentDataPasser() {
        return mailListViewFragmentDataPasser;
    }

    @Override
    public void setMailListViewFragmentDataPasser(MailListFragmentDataPasser mailListViewFragmentDataPasser) {
        this.mailListViewFragmentDataPasser = mailListViewFragmentDataPasser;
    }


}
