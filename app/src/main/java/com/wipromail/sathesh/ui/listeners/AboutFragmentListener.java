package com.wipromail.sathesh.ui.listeners;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.wipromail.sathesh.R;
import com.wipromail.sathesh.activity.ComposeActivity;
import com.wipromail.sathesh.adapter.ComposeActivityAdapter;
import com.wipromail.sathesh.application.MyActivity;
import com.wipromail.sathesh.constants.Constants;
import com.wipromail.sathesh.customserializable.ContactSerializable;
import com.wipromail.sathesh.fragment.datapasser.AboutFragmentDataPasser;

/**
 * Created by Sathesh on 5/28/15.
 */
public class AboutFragmentListener implements Constants, OnClickListener{

    private MyActivity activity;
    private Context context;
    private AboutFragmentDataPasser fragment;

    public AboutFragmentListener(MyActivity activity, Context context, AboutFragmentDataPasser fragment) {
        this.activity = activity;
        this.context = context;
        this.fragment = fragment;
    }

    /*** ON CLICK METHODS ***/

    private void onClickChkUpdate(View view) {
        fragment.checkForUpdates();
    }

    /** open the Compose activity to send email to developer with prefilled developer details
     * @param view
     * @throws PackageManager.NameNotFoundException
     */

    private void onBugOrSuggestion(View view) throws PackageManager.NameNotFoundException {

        //create a ContactSerializable to hold the To value of the developer
        Bundle toBundle = new Bundle();
        PackageInfo pInfo ;
        String developerEmail = activity.getText(R.string.bugsOrSuggestion_developer_email).toString();
        ContactSerializable developerContact = new ContactSerializable(developerEmail, developerEmail, true);	//true autoresolves the entry
        //put the ContactSerializable to a bundle
        toBundle.putSerializable(developerEmail, developerContact);	//the key value (developer email) for the bundle is not needed since ComposeActivity concerns only the values

        //get the app version info
        pInfo = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0);

        ComposeActivityAdapter.startPrefilledCompose(activity,
                ComposeActivity.PREFILL_TYPE_BUGS_SUGGESTIONS,
                toBundle, null, null,
                context.getString(R.string.bugsOrSuggestion_email_subject, pInfo.versionName),
                context.getString(R.string.bugsOrSuggestion_email_titlebar),
                true);
    }

    /** This will be invoked when the facebook like image is clicked
     * @param view
     */
    private void fbOnclick(View view){
        Intent fbIntent;
        try {
            activity.getPackageManager().getPackageInfo("com.facebook.katana", 0);
            fbIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(FB_LIKE_URL_APP));

        } catch (Exception e) {
            fbIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(FB_LIKE_URL_BROWSER));
        }
        try{
            context.startActivity(fbIntent);
        }catch(Exception e){e.printStackTrace();}
    }

    /** On Click Listener **
     *
     * @param view
     */
    @Override
    public void onClick(View view) {
        try {
            switch (view.getId()) {

                case R.id.about_activity_check_update_btn:
                    onClickChkUpdate(view);
                    break;

                case R.id.about_activity_bugOrSuggestion_btn:
                    onBugOrSuggestion(view);
                    break;

                case R.id.fbButton:
                    fbOnclick(view);
                    break;

            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }
}
