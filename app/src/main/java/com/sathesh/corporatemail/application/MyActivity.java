package com.sathesh.corporatemail.application;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

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
    @Override
    public void setSupportProgressBarIndeterminateVisibility(boolean visibility){
        ProgressBar progressBar=(ProgressBar)findViewById(R.id.indeterminate_progress);
        progressBar.setVisibility(visibility ? View.VISIBLE: View.GONE);
    }

    //to be called from fragment
    public void setSupportProgressBarIndeterminateVisibility(MyActivity activity, boolean visibility){
        ProgressBar progressBar=(ProgressBar)activity.findViewById(R.id.indeterminate_progress);
        progressBar.setVisibility(visibility?  View.VISIBLE: View.GONE);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}
