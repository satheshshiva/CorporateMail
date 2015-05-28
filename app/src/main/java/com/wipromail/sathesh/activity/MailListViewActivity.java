package com.wipromail.sathesh.activity;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.wipromail.sathesh.BuildConfig;
import com.wipromail.sathesh.R;
import com.wipromail.sathesh.activity.datapasser.MailListActivityDataPasser;
import com.wipromail.sathesh.adapter.DrawerRecyclerViewAdapter;
import com.wipromail.sathesh.application.MailApplication;
import com.wipromail.sathesh.application.MyActivity;
import com.wipromail.sathesh.application.SharedPreferencesAdapter;
import com.wipromail.sathesh.constants.Constants;
import com.wipromail.sathesh.fragment.AboutFragment;
import com.wipromail.sathesh.fragment.MailListViewFragment;
import com.wipromail.sathesh.fragment.SearchContactFragment;
import com.wipromail.sathesh.fragment.datapasser.AboutFragmentDataPasser;
import com.wipromail.sathesh.fragment.datapasser.SearchContactFragmentDataPasser;
import com.wipromail.sathesh.sqlite.db.cache.dao.DrawerMenuDAO;
import com.wipromail.sathesh.sqlite.db.cache.vo.DrawerMenuVO;
import com.wipromail.sathesh.tools.CacheClear;
import com.wipromail.sathesh.ui.action.MyActionBarDrawerToggle;
import com.wipromail.sathesh.ui.listeners.MailListViewActivityListener;

import java.util.List;

/** This Activity is the one which shows the mail list.
 *
 * All the heavy loading is done by the fragment MailListViewFragment
 *
 * @author sathesh
 *
 */
public class MailListViewActivity extends MyActivity implements Constants, MailListActivityDataPasser ,MailListViewFragment.ActivityDataPasser, AboutFragment.ActivityDataPasser, SearchContactFragment.ActivityDataPasser{

    public final static String MAIL_TYPE_EXTRA = "MAIL_TYPE_EXTRA";
    public final static String FOLDER_ID_EXTRA = "FOLDER_ID_EXTRA";
    public final static String FOLDER_NAME_EXTRA = "FOLDER_NAME_EXTRA";
    public final static String SIGN_OUT_EXTRA = "SIGN_OUT_EXTRA";
    public final static String APP_UPDATE_AVAILABLE = "APP_UPDATE_AVAILABLE";

    public static final String EXTRA_MESSAGE_CACHED_HEADER = "cachedMailHeaderToOpen";

    private int mailType ;
    private String mailFolderName;
    private String mailFolderId="";
    private TextView dispName;
    private TextView companyName;
    private static MyActivity activity;
    private Context context;
    private boolean appUpdateAvailble=false;

    private int drawerLayoutSelectedPosition=0;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private  AboutFragmentDataPasser aboutFragment;
    private SearchContactFragmentDataPasser searchContactFragment;
    private Fragment currentlyLoadedFragment;

    private static MailListViewActivity activityInstance;
    private MailListViewActivityListener activityListener;

    private final String STATE_DRAWER_MENU_HIGHTLIGHTED="stateDrawerMenuHighlighted";

    public MailListViewActivity(){
        activityInstance = this;
    }

    public static MailListViewActivity getInstance(){
        return activityInstance;
    }

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
        try {
            //while sign out is clicked, the enitire application will be closed by calling this activity since with clear top since this is the first
            //spawned activity.  if this is happening then we have to finish this first activity for sign out.
            if (getIntent().getBooleanExtra(SIGN_OUT_EXTRA, false)) {
                finish();
            }

            //Initializing Intent Extras
            mailType = getIntent().getIntExtra(MAIL_TYPE_EXTRA, -1);
            mailFolderId = getIntent().getStringExtra(FOLDER_ID_EXTRA);
            mailFolderName = getIntent().getStringExtra(FOLDER_NAME_EXTRA);
            appUpdateAvailble = getIntent().getBooleanExtra(APP_UPDATE_AVAILABLE, false);

            if(mailType == MailType.INBOX) {
                MailApplication.getInstance().onEveryAppOpen(activity, context);
            }
            //note: this will trigger the OnCreateView in the fragment.
            setContentView(R.layout.activity_mail_list_view);

            //Initializing Listener for this activity
            if(activityListener == null){
                activityListener = new MailListViewActivityListener(this);
            }

            if(appUpdateAvailble){
                loadAboutFragment(true);
            }
            else if(savedInstanceState==null){
                if(mailType == -1){    //this is not passed when app opened from debugger or something
                    loadMailListViewFragment(MailType.INBOX, getString(R.string.drawer_menu_inbox), mailFolderId);
                }
                else {
                    loadMailListViewFragment(mailType, mailFolderName, mailFolderId);
                }
            }

            // Initializing the Drawer Layout
            //Navigation Drawer
            DrawerMenuDAO drawerMenuDAO = new DrawerMenuDAO( context);
            List<DrawerMenuVO> drawerMenuList = drawerMenuDAO.getAllRecords();

            RecyclerView mDrawerList = (RecyclerView) activity.findViewById(R.id.recyclerView);
            mDrawerList.setScrollContainer(true);

            mDrawerList.setAdapter(new DrawerRecyclerViewAdapter(this, drawerMenuList, activityListener));
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

            dispName.setText(SharedPreferencesAdapter.getUserDetailsDisplayName(context));
            companyName.setText(SharedPreferencesAdapter.getUserDetailsCompanyName(context));

            if(savedInstanceState != null){
                drawerLayoutSelectedPosition=savedInstanceState.getInt(STATE_DRAWER_MENU_HIGHTLIGHTED);
            }
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

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save the user's current game state
        savedInstanceState.putInt(STATE_DRAWER_MENU_HIGHTLIGHTED, drawerLayoutSelectedPosition);

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
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

    /** OPTION ITEM SELECTED **/
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /** ON RESUME **/
    @Override
    public void onResume() {
        super.onResume();

        try {
            //cancel all the notifications
            // NotificationProcessing.cancelAllNotifications(this);
            // mailListViewFragmentDataPasser.softRefreshList();
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
        //this will close the navigation drawer first if its open
        if(mDrawerLayout.isDrawerOpen(Gravity.LEFT)){
            mDrawerLayout.closeDrawer(Gravity.LEFT);
            return;
        }
        else if(currentlyLoadedFragment instanceof MailListViewFragment){
            if(MailListViewFragment.getInstance()!=null && MailListViewFragment.getInstance().getMailType() == MailType.INBOX) {
                //only when back pressed from inbox, exit out of the app
                super.onBackPressed();
                return;
            }
        }
        // else open the inbox
        loadMailListViewFragment(MailType.INBOX, getString(R.string.drawer_menu_inbox),null);
    }

    //  ON CLICK METHODS

    // Drawer layout header on click will call this. To prevent the underlying element click
    public void emptyClick(View view){
        //do nothing
    }

    /*** FRAGMENT LOADING ***/

    @Override
    //loads the MailListViewFragment inside the MailListViewActivity
    public void loadMailListViewFragment(int mailType, String mailFolderName, String mailFolderId) {
        //using fragment transaction, replace the fragment
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        MailListViewFragment fragment = MailListViewFragment.newInstance(mailType, mailFolderName, mailFolderId);
        ft.replace(R.id.fragmentContainer, fragment);
        ft.commit();
        currentlyLoadedFragment = fragment;
    }

    @Override
    //loads the AboutFragment inside the MailListViewActivity
    public void loadAboutFragment(boolean onLoadCheckForUpdates) {
        //using fragment transaction, replace the fragment
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        AboutFragment fragment = AboutFragment.newInstance(onLoadCheckForUpdates);
        ft.replace(R.id.fragmentContainer, fragment);
        ft.commit();
        currentlyLoadedFragment = fragment;
    }

    @Override
    //loads the SearchContactFrgment inside the MailListViewActivity
    public void loadSearchContactFragment() {
        //using fragment transaction, replace the fragment
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        SearchContactFragment fragment = SearchContactFragment.newInstance();
        ft.replace(R.id.fragmentContainer, fragment);
        ft.commit();
        currentlyLoadedFragment = fragment;
    }

    /** GETTER SETTER PART **/

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
    public int getDrawerLayoutSelectedPosition() {
        return drawerLayoutSelectedPosition;
    }

    public void setDrawerLayoutSelectedPosition(int drawerLayoutSelectedPosition) {
        this.drawerLayoutSelectedPosition = drawerLayoutSelectedPosition;
    }

    public Fragment getCurrentlyLoadedFragment() {
        return currentlyLoadedFragment;
    }

    public void setCurrentlyLoadedFragment(Fragment currentlyLoadedFragment) {
        this.currentlyLoadedFragment = currentlyLoadedFragment;
    }
}
