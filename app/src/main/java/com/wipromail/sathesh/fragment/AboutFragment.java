package com.wipromail.sathesh.fragment;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;

import com.wipromail.sathesh.BuildConfig;
import com.wipromail.sathesh.R;
import com.wipromail.sathesh.activity.ComposeActivity;
import com.wipromail.sathesh.adapter.ComposeActivityAdapter;
import com.wipromail.sathesh.application.MailApplication;
import com.wipromail.sathesh.application.MyActivity;
import com.wipromail.sathesh.asynctask.DownloadAndUpdateAppAsyncTask;
import com.wipromail.sathesh.constants.Constants;
import com.wipromail.sathesh.customserializable.ContactSerializable;
import com.wipromail.sathesh.customui.Notifications;
import com.wipromail.sathesh.fragment.datapasser.AboutFragmentDataPasser;
import com.wipromail.sathesh.jsinterfaces.AboutActivityJSInterface;
import com.wipromail.sathesh.update.CheckLatestVersion;


public class AboutFragment extends Fragment implements Constants,AboutFragmentDataPasser{

    public MyActivity activity ;
    private ActivityDataPasser activityDataPasser;
    private static AboutFragment fragment;
    private Context context ;
    private Button bugOrSuggestionBtn;
    private WebView wv;
    private ActionBar myActionBar;
    private final static String ARG_CHECK_FOR_UPDATES="ARG_CHECK_FOR_UPDATES";
    private boolean onLoadCheckForUpdates=false;
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

    public static AboutFragment getCurrentInstance(){
        return fragment;
    }

    public AboutFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            this.onLoadCheckForUpdates = getArguments().getBoolean(ARG_CHECK_FOR_UPDATES);
        }
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

        wv = (WebView)view.findViewById(R.id.aboutActivityWebView);

        //the following code will prevent new webview from opening when loading url
        wv.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view1, String url) {

                view1.loadUrl(url);
                return true;
            }});

        WebSettings webSettings = wv.getSettings();
        webSettings.setSavePassword(false);
        webSettings.setSaveFormData(false);
        webSettings.setJavaScriptEnabled(true);	//this is important
        webSettings.setSupportZoom(false);
        //for displaying js alert in console

        //	wv.setWebChromeClient(new CommonWebChromeClient());

        wv.addJavascriptInterface(new AboutActivityJSInterface(this), AboutActivityJSInterface.ABOUT_ACTIVITY_JS_INTERFACE_NAME);

        bugOrSuggestionBtn = (Button)view.findViewById(R.id.about_activity_bugOrSuggestion_btn);
        //hide the Bug/suggestion buttton if no user signed in.
        try {
            if(!(MailApplication.checkUserIfSignedIn(context))){
                bugOrSuggestionBtn.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        //Initialize toolbar
        MailApplication.toolbarInitialize(activity, view);

        //action bar initialize
        myActionBar = activity.getSupportActionBar();
        //update mail type in the action bar title
        myActionBar.setTitle(activity.getString(R.string.drawer_menu_about));
        myActionBar.setDisplayHomeAsUpEnabled(true);
        myActionBar.setHomeButtonEnabled(true);

        if(onLoadCheckForUpdates){
           checkForUpdates();
        }
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

    /*** ON CLICK METHODS ***/

    @Override
    public void onClickChkUpdate(View view) {
        checkForUpdates();
    }

    private void checkForUpdates() {
        new CheckLatestVersion(activity,wv).startAsyncCheck();
    }

    /** open the Compose activity to send email to developer with prefilled developer details
     * @param view
     * @throws PackageManager.NameNotFoundException
     */
    @Override
    public void onBugOrSuggestion(View view) throws PackageManager.NameNotFoundException {

        //create a ContactSerializable to hold the To value of the developer
        Bundle toBundle = new Bundle();
        PackageInfo pInfo ;
        String developerEmail = getText(R.string.bugsOrSuggestion_developer_email).toString();
        ContactSerializable developerContact = new ContactSerializable(developerEmail, developerEmail, true);	//true autoresolves the entry
        //put the ContactSerializable to a bundle
        toBundle.putSerializable(developerEmail, developerContact);	//the key value (developer email) for the bundle is not needed since ComposeActivity concerns only the values

        //get the app version info
        pInfo = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0);

        ComposeActivityAdapter.startPrefilledCompose(activity,
                ComposeActivity.PREFILL_TYPE_BUGS_SUGGESTIONS,
                toBundle, null, null,
                getString(R.string.bugsOrSuggestion_email_subject, pInfo.versionName),
                getString(R.string.bugsOrSuggestion_email_titlebar),
                true);
    }

    /** This will be invoked when the facebook like image is clicked
     * @param view
     */
    @Override
    public void fbOnclick(View view){
        Intent fbIntent;
        try {
            activity.getPackageManager().getPackageInfo("com.facebook.katana", 0);
            fbIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(FB_LIKE_URL_APP));

        } catch (Exception e) {
            fbIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(FB_LIKE_URL_BROWSER));
        }
        try{
            startActivity(fbIntent);
        }catch(Exception e){e.printStackTrace();}
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
        android.support.v7.app.ActionBarDrawerToggle getmDrawerToggle();
    }

}
