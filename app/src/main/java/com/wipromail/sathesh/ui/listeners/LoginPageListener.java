package com.wipromail.sathesh.ui.listeners;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import com.wipromail.sathesh.R;
import com.wipromail.sathesh.activity.LoginPageActivity;
import com.wipromail.sathesh.activity.PreferencesActivity;
import com.wipromail.sathesh.adapter.GeneralPreferenceAdapter;
import com.wipromail.sathesh.application.MailApplication;
import com.wipromail.sathesh.constants.Constants;

/**
 * Created by sathesh on 10/16/14.
 */
public class LoginPageListener implements AdapterView.OnItemSelectedListener, Constants {
    private Context context;
    private GeneralPreferenceAdapter sharedPref = new GeneralPreferenceAdapter();
    private LoginPageActivity parent1;
    private TextView textViewUserName1, textViewUserName2;

    public LoginPageListener(Context context, LoginPageActivity parent){
        this.context=context;
        this.parent1 = parent;
        textViewUserName1 = (TextView) parent.findViewById(R.id.textView5);
        textViewUserName2 = (TextView) parent.findViewById(R.id.textView6);
    }

    /**The OnItemSelected Listener
     *
     * @param parent
     * @param view
     * @param position
     * @param id
     */
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch(parent.getId()){

            /** The OnItemClick listener for the server URL spinner in login page
             *
             */
            case R.id.serverSpinner:
                //if the option  selected is other than the Custom URL option then just save the value in preferences
                if(position!= PreferencesActivity.CUSTOM_URL_POSITION) {
                    // check if its the office 365
                    if(position!= PreferencesActivity.OFFICE365_URL_POSITION) {
                        //update the user name display. Replace "wipro/" with "@wipro.com"
                        textViewUserName1.setVisibility(View.VISIBLE);
                        textViewUserName2.setVisibility(View.GONE);
                    }
                    else{
                        //update the user name display. Replace "@wipro.com" with "wipro/"
                        textViewUserName1.setVisibility(View.GONE);
                        textViewUserName2.setVisibility(View.VISIBLE);
                    }

                    //store the selected URL
                    sharedPref.storeServerURL(context,
                            context.getResources().getStringArray(R.array.preferences_serverlist_values)[position]);

                    //update the URL in text view
                    if(parent1.getUrlDisp()!=null) {
                        parent1.getUrlDisp().setText(sharedPref.getServerURL(context));
                    }
                }
                //if the Custom URL option is selected then show the dialog box to enter the Custom URL
                else{
                    //open the dialog box for Custom URL
                    MailApplication.showCustomServerURLDialog(context, null, parent1.getUrlDisp());
                    //update the user name display. Replace "wipro/" with "@wipro.com"
                    textViewUserName1.setVisibility(View.VISIBLE);
                    textViewUserName2.setVisibility(View.GONE);
                }
            break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }
}
