package com.sathesh.corporatemail.fragment;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;

import com.sathesh.corporatemail.BuildConfig;
import com.sathesh.corporatemail.R;
import com.sathesh.corporatemail.application.MailApplication;
import com.sathesh.corporatemail.application.MyActivity;
import com.sathesh.corporatemail.asynctask.DownloadAndUpdateAppAsyncTask;
import com.sathesh.corporatemail.constants.Constants;
import com.sathesh.corporatemail.customui.Notifications;
import com.sathesh.corporatemail.fragment.datapasser.AboutFragmentDataPasser;
import com.sathesh.corporatemail.ui.listeners.AboutFragmentListener;


public class AboutFragment extends Fragment implements Constants,AboutFragmentDataPasser{

    public MyActivity activity ;
    private ActivityDataPasser activityDataPasser;
    private static AboutFragment fragment;
    private Context context ;
    private ImageButton fbButton;
    private ActionBar myActionBar;
    private final static String ARG_CHECK_FOR_UPDATES="ARG_CHECK_FOR_UPDATES";
    private ActivityDataPasser mListener;

    /** Factory for this fragment
     *
     * @return
     */
    public static AboutFragment newInstance(boolean onLoadCheckForUpdates) {
        fragment = new AboutFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_CHECK_FOR_UPDATES, onLoadCheckForUpdates);
        fragment.setArguments(args);
        return fragment;
    }

    public static AboutFragment getInstance(){
        return fragment;
    }

    public AboutFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (ActivityDataPasser) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement ActivityDataPasser");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //inflate layout for this fragment
        View view = inflater.inflate(R.layout.fragment_about, container, false);

        activity =   (MyActivity)getActivity();
        activityDataPasser =   (ActivityDataPasser)getActivity();
        context =  getActivity();

        AboutFragmentListener listener = new AboutFragmentListener(activity, context, this);

        fbButton = (ImageButton)view.findViewById(R.id.fbButton);

        fbButton.setOnClickListener(listener);

        //Initialize toolbar
        MailApplication.toolbarInitialize(activity, view);

        //action bar initialize
        myActionBar = activity.getSupportActionBar();
        //update mail type in the action bar title
        myActionBar.setTitle(activity.getString(R.string.drawer_menu_about));
        myActionBar.setDisplayHomeAsUpEnabled(true);
        myActionBar.setHomeButtonEnabled(true);

        return view;
    }

    /** ON RESUME **/
    @Override
    public void onResume() {
        super.onResume();

        try {
            activityDataPasser.getmDrawerToggle().syncState();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void downloadAndUpdate() {
        if (BuildConfig.DEBUG){
            new DownloadAndUpdateAppAsyncTask(activity).execute(APPLICATION_APK_DOWNLOAD_URL1_DEV);
        }
        else {
            new DownloadAndUpdateAppAsyncTask(activity).execute(APPLICATION_APK_DOWNLOAD_URL1_REL);
        }
    }

    @Override
    public void checkForUpdates() {

    }

    /** Unused right now
     *
     * @param view
     */
    public void onClickRateApp(View view) {
        if(BuildConfig.DEBUG) {
            Log.d(TAG, "OnClickRate app");
        }
        try {
            MailApplication.openPlayStoreLink(activity);
        }
        catch (ActivityNotFoundException e) {
            Notifications.showToast(activity, getText(R.string.playstore_not_available), Toast.LENGTH_SHORT);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    /** Interface - Fragment Interaction Listener
     *
     */
    public interface ActivityDataPasser {
        androidx.appcompat.app.ActionBarDrawerToggle getmDrawerToggle();
    }

}
