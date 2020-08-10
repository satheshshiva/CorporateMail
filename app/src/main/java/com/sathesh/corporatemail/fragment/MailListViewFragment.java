package com.sathesh.corporatemail.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher.ViewFactory;

import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.melnykov.fab.FloatingActionButton;
import com.sathesh.corporatemail.BuildConfig;
import com.sathesh.corporatemail.R;
import com.sathesh.corporatemail.adapter.MailListViewAdapter;
import com.sathesh.corporatemail.animation.ApplyAnimation;
import com.sathesh.corporatemail.application.MailApplication;
import com.sathesh.corporatemail.application.MyActivity;
import com.sathesh.corporatemail.application.NotificationProcessing;
import com.sathesh.corporatemail.cache.adapter.CachedMailHeaderAdapter;
import com.sathesh.corporatemail.constants.Constants;
import com.sathesh.corporatemail.fragment.datapasser.MailListFragmentDataPasser;
import com.sathesh.corporatemail.handlers.GetMoreMailsHandler;
import com.sathesh.corporatemail.handlers.GetNewMailsHandler;
import com.sathesh.corporatemail.sqlite.db.cache.vo.CachedMailHeaderVO;
import com.sathesh.corporatemail.threads.ui.GetMoreMailsThread;
import com.sathesh.corporatemail.threads.ui.GetNewMailsThread;
import com.sathesh.corporatemail.ui.listeners.MailListViewListener;
import com.sathesh.corporatemail.util.Utilities;

import java.util.ArrayList;
import java.util.List;

/**
 * @author sathesh
 *         This fragment is used to load only the MailFunctions.
 */

public class MailListViewFragment extends Fragment implements Constants, MailListFragmentDataPasser {

    private static final String ARG_MAILTYPE = "mailType";
    private static final String ARG_MAIL_FOLDER_NAME = "mailFolderName";
    private static final String ARG_MAIL_FOLDER_ID = "mailFolderId";

    private ActivityDataPasser activityDataPasser;
    public MyActivity activity;
    private Context context;

    private MailListViewAdapter adapter;
    private TextSwitcher textswitcher;

    private int mailType;
    private String mailFolderName;
    private String mailFolderId;

    private ProgressBar circle_progressbar;
    private boolean fabShown=false;
    private ImageView successIcon, failureIcon, readIcon, unreadIcon;
    private FloatingActionButton fab;

    private ActionBar myActionBar;

    private ListView listView;
    private CachedMailHeaderAdapter cacheMailHeaderAdapter;
    private int totalCachedRecords = 0;

    private SwipeRefreshLayout swipeRefreshLayout;
    private Status newMailsThreadState;
    private Status moreMailsThreadState;
    private UndoBarStatus undoBarState;

    private static MailListViewFragment fragment;
    private long totalMailsInFolder = -1;
    private final String STATE_FAB_SHOW="fabShown";

    //contains all the UI listeners for this fragment
    private MailListViewListener listener;
    private ArrayList<CachedMailHeaderVO> cachedHeaderVoList;

    /**
     * @author sathesh
     */

    public static MailListViewFragment newInstance(int mailType, String mailFolderName, String mailFolderId) {
        fragment = new MailListViewFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_MAILTYPE, mailType);
        args.putString(ARG_MAIL_FOLDER_NAME, mailFolderName);
        args.putString(ARG_MAIL_FOLDER_ID, mailFolderId);
        fragment.setArguments(args);
        return fragment;
    }

    public MailListViewFragment() {
        // Required empty public constructor
    }

    public static MailListViewFragment getInstance() {
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            this.mailType = getArguments().getInt(ARG_MAILTYPE);
            this.mailFolderName = getArguments().getString(ARG_MAIL_FOLDER_NAME);
            this.mailFolderId = getArguments().getString(ARG_MAIL_FOLDER_ID);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            activityDataPasser = (ActivityDataPasser) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement ActivityDataPasser");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        activityDataPasser = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mail_list_view,
                container, false);

        activity = (MyActivity) getActivity();
        context = getActivity();
        if (cacheMailHeaderAdapter == null) {
            cacheMailHeaderAdapter = new CachedMailHeaderAdapter(context);
        }
        setRetainInstance(true);

        if (activity != null) {

            try {
                // Initializing the fragmentListener
                if (listener == null) {
                    listener = new MailListViewListener((MyActivity) activityDataPasser, this);
                }

                //Initialize toolbar
                MailApplication.toolbarInitialize(activity, view);

                //List View Initialization
                listView = (ListView) view.findViewById(R.id.listView);

                //Text Switcher Initiliazation
                textswitcher = (TextSwitcher) view.findViewById(R.id.textswitcher);

                //Text Switcher customze with text view
                textswitcher.setFactory(new ViewFactory() {
                    public View makeView() {
                        // Custom text view since the default one auto applies center gravity
                        TextView textView = new TextView(activity);
                        textView.setGravity(Gravity.LEFT);
                        textView.setTextSize(12);
                        return textView;
                    }
                });

                //animation for text switcher
                ApplyAnimation.setTitleInboxStatusTextSwitcher(activity, textswitcher);

                //progress bar initialize
                circle_progressbar = (ProgressBar) view.findViewById(R.id.circle_progressbar);

                //icons initialize
                successIcon = (ImageView) view.findViewById(R.id.success_icon);
                failureIcon = (ImageView) view.findViewById(R.id.failure_icon);
                readIcon = (ImageView) view.findViewById(R.id.read_icon);
                unreadIcon = (ImageView) view.findViewById(R.id.unread_icon);

                //action bar initialize
                myActionBar = activity.getSupportActionBar();

                //update mail type in the action bar title
                myActionBar.setTitle(getMailFolderDisplayName(mailType));
                myActionBar.setDisplayHomeAsUpEnabled(true);
                myActionBar.setHomeButtonEnabled(true);

                //initializes the adapter and associates the listview.
                //this set  of code when placed when placed few lines before wont initialize and is giving empty listview. dont know why.
                //get the cursor

                //if(adapter==null){	//on config change it wont be null
                cachedHeaderVoList = (ArrayList<CachedMailHeaderVO>) cacheMailHeaderAdapter.getMailHeaders(mailType, mailFolderId);
                //initialize the adapter
                adapter = new MailListViewAdapter(context, this, cachedHeaderVoList);
                //}

                // initializes the list view with the adapter. also will place all the cached mails in list view initially
                listView.setAdapter(adapter);

                //FAB attachment
                fab = (FloatingActionButton) view.findViewById(R.id.fab_compose);
                fab.attachToListView(listView);
                fab.setOnClickListener(listener);

                //Initialize SwipeRefreshLayout
                swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);
                // the refresh listener. this would be called when the layout is pulled down
                swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        //refresh list when the SwipeRefresh is pulled
                        refreshList();
                    }
                });
                // sets the colors used in the refresh animation
                int[] resources = MailApplication.getSwipeRefreshLayoutColorResources();
                if (resources.length == 4) {
                    swipeRefreshLayout.setColorSchemeResources(resources[0], resources[1], resources[2], resources[3]);
                }

                //Action
                //if the activity is recreated, and if the thread is already updating then update the UI status
                if (newMailsThreadState == Status.UPDATING) {
                    updatingStatusUIChanges();
                }

                if (moreMailsThreadState == Status.WAITING || moreMailsThreadState == Status.UPDATING) {
                    showMoreLoadingAnimation();
                }
                //make a refresh only once when the screen is loaded first time. make a soft refresh on config change
                if (savedInstanceState!=null) {
                    //config change (Screen rotation)
                    softRefreshList();
                    fabShown = savedInstanceState.getBoolean(STATE_FAB_SHOW);
                } else {
                    refreshList();
                }
            } catch (Exception e) {
                Utilities.generalCatchBlock(e, this);
            }
        }
        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save the user's current state
        savedInstanceState.putBoolean(STATE_FAB_SHOW, fabShown);

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    /**
     * ON RESUME *
     */
    @Override
    public void onResume() {
        super.onResume();

        try {
            // show FAB
            if(!fabShown) {
                fab.show();
                fabShown=true;
            }

            activityDataPasser.getmDrawerToggle().syncState();

            //below 2 lines moved from activity to here
            if (mailType == MailType.INBOX) {
                NotificationProcessing.cancelAllNotifications(activity);
            }
            softRefreshList(); //when returning after a mail view
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Show the more loading text with animation
     */
    public void showMoreLoadingAnimation() {
        int totalRecordsInCache = -1;

        //show the loading animation with the no of mails remaining in the end of listview
        try {
            totalRecordsInCache = cacheMailHeaderAdapter.getRecordsCount(mailType, mailFolderId);

        } catch (Exception e) {
            // if exception in getting the no of mails in cache then just show loading mails symbol
        }
        adapter.showMoreMailsLoadingAnimation(MORE_NO_OF_MAILS, totalRecordsInCache, totalMailsInFolder);
    }

    /* (non-Javadoc)
     * @see android.support.v4.app.Fragment#onActivityCreated(android.os.Bundle)
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);
        listView.setOnScrollListener(listener);
        //ListView -Itemclick Listener
        listView.setOnItemClickListener(listener);
        listView.setMultiChoiceModeListener(listener);
    }

    /**
     * Refreshes the list from network
     */
    @Override
    public void refreshList() {

        if (undoBarState == UndoBarStatus.DISPLAYED || undoBarState == UndoBarStatus.DELETING) {
            //if the undo bar is displayed then dont refresh, hide the swipeRefreshLayout
            swipeRefreshLayout.setRefreshing(false);
        } else if (newMailsThreadState != Status.UPDATING) {
            //network call for getting the new mails and corresponding UI changes
            Handler getNewMailsHandler = new GetNewMailsHandler(this, getActivity());
            Thread t = new GetNewMailsThread(this, getNewMailsHandler);
            t.start();
        }

    }

    /**
     * Refreshes the listview from local cache
     */
    @Override
    public void softRefreshList() {
        if (BuildConfig.DEBUG) {
            Log.d(LOG_TAG, ((Object) this).getClass() + " ->  Called Soft refresh list");
        }
        try {
            //update the list view
            cachedHeaderVoList = (ArrayList<CachedMailHeaderVO>) cacheMailHeaderAdapter.getMailHeaders(mailType, mailFolderId);
            adapter.setMailListVOs(cachedHeaderVoList);
            adapter.updateAndNotify();

            //update the text switcher
            updateTextSwitcherWithMailCount(totalCachedRecords);

        } catch (Exception e) {
            Utilities.generalCatchBlock(e, this);
        }
    }

    /**
     * Gets More mails when the user scrolls down
     */
    public void getMoreMails() {
        if (moreMailsThreadState != Status.UPDATING) {
            showMoreLoadingAnimation();

            //network call for getting the new mails
            Handler getMoreMailsHandler = new GetMoreMailsHandler(this);
            Thread t = new GetMoreMailsThread(this, getMoreMailsHandler);
            t.start();
        }
    }

    /**
     * This method gets the customized Display name for a folder.
     * Well Known Folder Name can be used as is, but some have 2 words without space.
     *
     * @param mailType
     * @return
     */
    private String getMailFolderDisplayName(int mailType) {
        switch (mailType) {
            case (MailType.SENT_ITEMS):
                return (activity.getString(R.string.ActionBarTitle_SentItems));
            case MailType.DELETED_ITEMS:
                return (activity.getString(R.string.ActionBarTitle_DeletedItems));
            case MailType.JUNK_EMAIL:
                return (activity.getString(R.string.ActionBarTitle_JunkEmail));
            case MailType.CONVERSATION_HISTORY:
                return (activity.getString(R.string.ActionBarTitle_ConversationHistory));
            default:
                return (mailFolderName);
        }
    }

    /**
     * Make UI changes for the Updating status
     * which includes progress bar, text switcher, swipe refresh
     */
    public void updatingStatusUIChanges() {
        try {
            //swipe refresh layout - visible
          //  swipeRefreshLayout.setRefreshing(true);
            //if total cached records in the folder is more than 0 then show msg "Checking for new mails" otherwise "Update folder"
            totalCachedRecords = cacheMailHeaderAdapter.getRecordsCount(mailType, mailFolderId);
            if (totalCachedRecords > 0) {
                textswitcher.setText(activity.getString(R.string.folder_updater_checking));
            } else {
                textswitcher.setText(activity.getString(R.string.folder_updater_updating, getMailFolderDisplayName(mailType)));
            }
            //text switcher - refreshing icon
            updateTextSwitcherIcons(View.VISIBLE, View.GONE, View.GONE, View.GONE, View.GONE);
        } catch (Exception e) {
            Utilities.generalCatchBlock(e, this);
        }
    }

    /**
     * Updates the Text Switcher with the unread mail count.
     * usually called after successful update
     */
    public void updateTextSwitcherWithMailCount(int totalCachedRecords) throws Exception {
        //get the unread emails count
        totalCachedRecords = cacheMailHeaderAdapter.getRecordsCount(mailType, mailFolderId);
        int totalUnread = cacheMailHeaderAdapter.getUnreadMailCount(mailType, mailFolderId);
        String successMsg = "";

        //no email
        if (totalCachedRecords < 1) {
            successMsg = getString(R.string.no_mail);
            //update icon
            updateTextSwitcherIcons(View.GONE, View.GONE, View.GONE, View.VISIBLE, View.GONE);
        }
        //more than 1 unread email
        else if (totalUnread > 1) {
            //update text in text switcher. for inbox alone show as "new mail" for other folders show "unread"
            successMsg = (mailType == MailType.INBOX || mailType == MailType.INBOX_SUBFOLDER_WITH_ID) ?
                    getString(R.string.new_mail_x, totalUnread) :
                    getString(R.string.unread_item_x, totalUnread);
            //update icon
            updateTextSwitcherIcons(View.GONE, View.GONE, View.GONE, View.GONE, View.VISIBLE);
        }
        //one unread email
        else if (totalUnread == 1) {
            //update text in text switcher. for inbox alone show as "new mail" for other folders show "unread"
            successMsg = (mailType == MailType.INBOX || mailType == MailType.INBOX_SUBFOLDER_WITH_ID) ?
                    getString(R.string.new_mail_1) :
                    getString(R.string.unread_item_1);
            //update icon
            updateTextSwitcherIcons(View.GONE, View.GONE, View.GONE, View.GONE, View.VISIBLE);
        }
        //no new mail
        else {
            //update text in text switcher
            successMsg = getString(R.string.no_new_mail);
            //update icon
            updateTextSwitcherIcons(View.GONE, View.GONE, View.GONE, View.VISIBLE, View.GONE);
        }
        textswitcher.setText(successMsg);
    }


    /**
     * This method sets the visibility of the icons inside the text switcher
     *
     * @param progressCircleVisibility
     * @param successIconVisibility
     * @param failureIconVisibility
     */
    public void updateTextSwitcherIcons(int progressCircleVisibility, int successIconVisibility, int failureIconVisibility, int readIconVisibility, int unreadIconVisibility) {
        circle_progressbar.setVisibility(progressCircleVisibility);
        successIcon.setVisibility(successIconVisibility);
        failureIcon.setVisibility(failureIconVisibility);
        readIcon.setVisibility(readIconVisibility);
        unreadIcon.setVisibility(unreadIconVisibility);
    }

    /*** INTERFACES ***/

    /**
     * Fragment Interation Listner
     */
    public interface ActivityDataPasser {
        androidx.appcompat.app.ActionBarDrawerToggle getmDrawerToggle();
    }

    /**
     * GETTER SETTER PART **
     */

    public Status getNewMailsThreadState() {
        return newMailsThreadState;
    }

    public void setNewMailsThreadState(Status currentStatus) {
        this.newMailsThreadState = currentStatus;
    }

    public String getMailFolderId() {
        return mailFolderId;
    }

    public void setMailFolderId(String mailFolderId) {
        this.mailFolderId = mailFolderId;
    }

    public String getMailFolderName() {
        return mailFolderName;
    }

    public void setMailFolderName(String mailFolderName) {
        this.mailFolderName = mailFolderName;
    }

    public ProgressBar getCircle_progressbar() {
        return circle_progressbar;
    }

    public void setCircle_progressbar(
            ProgressBar maillist_refresh_progressbar) {
        this.circle_progressbar = maillist_refresh_progressbar;
    }

    public SwipeRefreshLayout getSwipeRefreshLayout() {
        return swipeRefreshLayout;
    }

    public void setSwipeRefreshLayout(SwipeRefreshLayout swipeRefreshLayout) {
        this.swipeRefreshLayout = swipeRefreshLayout;
    }

    public TextSwitcher getTextswitcher() {
        return textswitcher;
    }

    public void setTextswitcher(TextSwitcher textswitcher) {
        this.textswitcher = textswitcher;
    }

    public MailListViewAdapter getAdapter() {
        return adapter;
    }

    public void setAdapter(MailListViewAdapter adapter) {
        this.adapter = adapter;
    }

    public CachedMailHeaderAdapter getMailHeadersCacheAdapter() {
        return cacheMailHeaderAdapter;
    }

    public void setMailHeadersCacheAdapter(CachedMailHeaderAdapter mailHeadersCacheAdapter) {
        this.cacheMailHeaderAdapter = mailHeadersCacheAdapter;
    }

    public int getMailType() {
        return mailType;
    }

    public void setMailType(int mailType) {
        this.mailType = mailType;
    }

    public Status getMoreMailsThreadState() {
        return moreMailsThreadState;
    }

    public void setMoreMailsThreadState(Status moreMailThreadState) {
        this.moreMailsThreadState = moreMailThreadState;
    }

    public long getTotalMailsInFolder() {
        return totalMailsInFolder;
    }

    public void setTotalMailsInFolder(long totalMailsInFolder) {
        this.totalMailsInFolder = totalMailsInFolder;
    }

    public ListView getListView() {
        return listView;
    }

    public void setListView(ListView listView) {
        this.listView = listView;
    }

    public UndoBarStatus getUndoBarState() {
        return undoBarState;
    }

    public void setUndoBarState(UndoBarStatus undoBarState) {
        this.undoBarState = undoBarState;
    }

    public MailListViewListener getListener() {
        return listener;
    }

    public void setListener(MailListViewListener listener) {
        this.listener = listener;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    @Override
    public FloatingActionButton getFab() {
        return fab;
    }

    @Override
    public boolean isFabShown() {
        return fabShown;
    }

    @Override
    public void setFabShown(boolean fabShown) {
        this.fabShown = fabShown;
    }

    @Override
    public ArrayList<CachedMailHeaderVO> getCachedHeaderVoList() {
        return cachedHeaderVoList;
    }
}
