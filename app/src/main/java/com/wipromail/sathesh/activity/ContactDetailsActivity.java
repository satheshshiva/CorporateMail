package com.wipromail.sathesh.activity;

import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.wipromail.sathesh.R;
import com.wipromail.sathesh.application.MailApplication;
import com.wipromail.sathesh.application.MyActivity;
import com.wipromail.sathesh.asynctask.ResolveNamesAsyncTask;
import com.wipromail.sathesh.asynctask.interfaces.IResolveNames;
import com.wipromail.sathesh.constants.Constants;
import com.wipromail.sathesh.customserializable.ContactSerializable;
import com.wipromail.sathesh.ews.EWSConnection;
import com.wipromail.sathesh.service.data.Contact;
import com.wipromail.sathesh.service.data.ExchangeService;
import com.wipromail.sathesh.service.data.NameResolutionCollection;
import com.wipromail.sathesh.util.Utilities;

public class ContactDetailsActivity extends MyActivity implements Constants,IResolveNames {
    //test msg
    public static final String CONTACT_SERIALIZABLE_EXTRA ="CONTACT_SERIALIZABLE_EXTRA";
    private TextView displayName;
    private TextView department;
    private TextView companyName;
    private TextView designation;
    private TextView email;
    private TextView workphone;
    private TextView mobilephone;
    private TextView fax;
    private TextView officeLocation;

    private ContactSerializable sContact;
    public static final String SHOW_SENDMAIL_BTN_EXTRA="SHOW_SENDMAIL_BTN_EXTRA";
    private boolean showSendMailBtn =false;

    public enum Status{
        IDLE,
        LOADING,
        LOADED,
        ERROR
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_contact_details);

        //Initialize toolbar
        MailApplication.toolbarInitialize(this);

        displayName = (TextView) findViewById(R.id.displayName);
        department = (TextView) findViewById(R.id.dept);
        companyName = (TextView) findViewById(R.id.companyName);
        designation = (TextView) findViewById(R.id.designation);
        email = (TextView) findViewById(R.id.email);
        workphone = (TextView) findViewById(R.id.workPhone);
        mobilephone = (TextView) findViewById(R.id.mobilePhone);
        fax = (TextView) findViewById(R.id.fax);
        officeLocation = (TextView) findViewById(R.id.officeLoc);

        //get the ContactSerializable object from the intent
        sContact = (ContactSerializable)getIntent().getSerializableExtra(CONTACT_SERIALIZABLE_EXTRA);

        showSendMailBtn = getIntent().getBooleanExtra(SHOW_SENDMAIL_BTN_EXTRA, false);	//displays send mail button

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if(sContact!= null ){
            getSupportActionBar().setSubtitle(getCustomDisplayName(sContact));
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        ExchangeService service;
        // resolve the contact if it is set in the flag
        if(sContact!=null){
            //if the resolveOnLoad flag is set to true then resolve the ontact
            if(sContact.isResolveOnLoad() ) {
                try {
                    //make the network call by calling the async task
                    service = EWSConnection.getServiceFromStoredCredentials(this);
                    //if the email is not empty then resolve the name with the email
                    if(sContact.getEmail()!=null && !sContact.getEmail().equals("")) {
                        new ResolveNamesAsyncTask(this, this, service, sContact.getEmail().toString(), false, "", "").execute();
                    }
                    // if the display name is not null then resolve with the display name
                    // Bummer: For some persons there may be 2 or more emails ids with the same display name
                    //          At that time it just prints error in the log and does not resolve. But mostly
                    //          we will send the email

                    else if(sContact.getDisplayName()!=null && !sContact.getDisplayName().equals("")){
                        new ResolveNamesAsyncTask(this, this, service, sContact.getDisplayName().toString(), false, "", "").execute();
                    }
                }
                catch (Exception e) {
                    Utilities.generalCatchBlock(e,this);
                }
            }
            //refresh the UI with the ContactSerializable
            loadUI(sContact);    //refreshes the text fields
        }
        else{
            Log.e(TAG, "ContactDetailsActivity => sContact is null");
        }

    }

    /** Refreshes the UI with the ContactSerializable object
     *
     */
    private void loadUI(ContactSerializable sContact) {
        StringBuilder officeLocationBuilder = new StringBuilder();

        displayName.setText(getCustomDisplayName(sContact));
        department.setText(sContact.getDepartment());
        companyName.setText(sContact.getCompanyName());
        designation.setText(sContact.getDesignation());
        email.setText(sContact.getEmail());
        workphone.setText(sContact.getWorkphone());
        mobilephone.setText(sContact.getMobilephone());
        fax.setText(sContact.getFax());

        if(sContact.getOfficeLocation_street() != null && !(sContact.getOfficeLocation_street().equals(""))){
            officeLocationBuilder.append(sContact.getOfficeLocation_street());
            officeLocationBuilder.append("\n");
        }
        if(sContact.getOfficeLocation_city() != null && !(sContact.getOfficeLocation_city().equals(""))){
            officeLocationBuilder.append(sContact.getOfficeLocation_city());
            officeLocationBuilder.append("\n");
        }
        if(sContact.getOfficeLocation_state() != null && !(sContact.getOfficeLocation_state().equals(""))){
            officeLocationBuilder.append(sContact.getOfficeLocation_state());
            officeLocationBuilder.append("\n");
        }
        if(sContact.getOfficeLocation_countryOrRegion() != null && !(sContact.getOfficeLocation_countryOrRegion().equals(""))){
            officeLocationBuilder.append(sContact.getOfficeLocation_countryOrRegion());
            officeLocationBuilder.append("\n");
        }
        if(sContact.getOfficeLocation_postalCode() != null && !(sContact.getOfficeLocation_postalCode().equals(""))){
            officeLocationBuilder.append(sContact.getOfficeLocation_postalCode());
        }
        officeLocation.setText(officeLocationBuilder.toString());
    }

    //if the dispaly name is null then use the email
    private String getCustomDisplayName(ContactSerializable sContact) {
        if(sContact.getDisplayName() ==null || sContact.getDisplayName().equals("")){
            return sContact.getEmail();
        }
        return sContact.getDisplayName() ;
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        if(showSendMailBtn){
            //Always Visible menu
            MenuItem menuItem;
            menuItem=menu.add(this.getString(R.string.actionBar_Send_Mail));
            MenuItemCompat.setShowAsAction(menuItem,
                     MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if(item!=null && item.getItemId()==android.R.id.home){
            finish();
        }
        else if(item!=null && item.getTitle().equals(this.getString(R.string.actionBar_Send_Mail)))
        {
            if(sContact!= null ){
                MailApplication.composeEmailForContact(this, ComposeActivity.PREFILL_TYPE_CONTACT_DETAILS_BTN, sContact);
            }

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void handleResolvingNames() {
        setProgressBarIndeterminateVisibility(true);
    }

    @Override
    public void handleResolveNamesOutput(NameResolutionCollection outputCollection, String extra1) {
        setProgressBarIndeterminateVisibility(false);
        Contact contact;

        try {
            //check whether only one contact is returned
            if(outputCollection!= null && outputCollection.getCount()==1){

                contact = outputCollection.nameResolutionCollection(0).getContact();
                //get the ContactSerializable object from the contact
                sContact = sContact.getContactSerializableFromContact(contact, sContact.getEmail());
            }
            else{
                //if more than 1 contact is returned then do nothing
                Log.e(TAG, "ContactDetailsActivity -> outputCollection is null or more than 1.");
                Log.e(TAG , "outputCollection Count " + ((outputCollection!=null)?outputCollection.getCount() : outputCollection).toString());
            }
        } catch (Exception e) {
            Utilities.generalCatchBlock(e, this);
        }
        loadUI(sContact);
    }

    @Override
    public void handleResolveNamesOutputError(NameResolutionCollection outputCollection, String extra1, Exception pE) {
        setProgressBarIndeterminateVisibility(false);

    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }
}
