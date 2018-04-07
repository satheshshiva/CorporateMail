package com.sathesh.corporatemail.application;

import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ProgressBar;

import com.google.analytics.tracking.android.EasyTracker;
import com.sathesh.corporatemail.R;

/**
 * Created by sathesh on 11/2/14.
 *
 * All activities in the application will override this activity
 */
public class MyActivity extends AppCompatActivity {

    /** The setSupportProgressBarIndeterminateVisibility() method is NA in toolbar
     * Since it was used across the appln in Actionbar have to override and call a custom progressbar created
     * in the toolbar
     * @param visibility
     */
    public void setSupportProgressBarIndeterminateVisibility(boolean visibility){
        ProgressBar progressBar=(ProgressBar)findViewById(R.id.indeterminate_progress);
        if(visibility)
        progressBar.setVisibility(View.VISIBLE);
        else
        progressBar.setVisibility(View.GONE);
    }

    //to be called from fragment
    public void setSupportProgressBarIndeterminateVisibility(View view, boolean visibility){
        ProgressBar progressBar=(ProgressBar)view.findViewById(R.id.indeterminate_progress);
        if(visibility)
            progressBar.setVisibility(View.VISIBLE);
        else
            progressBar.setVisibility(View.GONE);
    }

    //Google Analytics
    @Override
    public void onStart() {
        super.onStart();
        EasyTracker.getInstance(this).activityStart(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EasyTracker.getInstance(this).activityStop(this);
    }
}
