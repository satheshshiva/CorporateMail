package com.sathesh.corporatemail.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.view.MenuItemCompat;

import com.sathesh.corporatemail.BuildConfig;
import com.sathesh.corporatemail.R;
import com.sathesh.corporatemail.adapter.GeneralPreferenceAdapter;
import com.sathesh.corporatemail.animation.ApplyAnimation;
import com.sathesh.corporatemail.application.MailApplication;
import com.sathesh.corporatemail.application.MyActivity;
import com.sathesh.corporatemail.asynctask.ResolveNamesAsyncTask;
import com.sathesh.corporatemail.asynctask.interfaces.IResolveNames;
import com.sathesh.corporatemail.constants.Constants;
import com.sathesh.corporatemail.customserializable.ContactSerializable;
import com.sathesh.corporatemail.customui.Notifications;
import com.sathesh.corporatemail.ews.EWSConnection;
import com.sathesh.corporatemail.ews.NetworkCall;
import com.sathesh.corporatemail.ui.components.ProgressDisplayNotificationBar;
import com.sathesh.corporatemail.ui.components.WarningDisplayNotificationBar;
import com.sathesh.corporatemail.util.Utilities;
import com.sathesh.corporatemail.web.StandardWebView;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import microsoft.exchange.webservices.data.core.ExchangeService;
import microsoft.exchange.webservices.data.core.exception.http.HttpErrorException;
import microsoft.exchange.webservices.data.core.exception.misc.ArgumentOutOfRangeException;
import microsoft.exchange.webservices.data.core.exception.service.local.ServiceLocalException;
import microsoft.exchange.webservices.data.core.service.item.Contact;
import microsoft.exchange.webservices.data.misc.NameResolutionCollection;

public class ComposeActivity extends MyActivity implements Constants,IResolveNames{

    private  static MyActivity activity;
    //making this change
    private  static EditText composeSubject;
    private  static EditText composeBody, composeSignature;
    private  static Collection<ContactSerializable> to;
    private  static Collection<ContactSerializable> cc;
    private  static Collection<ContactSerializable> bcc;
    private  static String  subject="",body="";
    private  static final String STATUS_SENT="STATUS_SENT";
    private  static final String STATUS_ERROR="STATUS_ERROR";
    private  static ExchangeService service;
    private  static GeneralPreferenceAdapter generalSettings = new GeneralPreferenceAdapter();

    private static Intent intent;
    private static StandardWebView standardWebView ;
    private static CharSequence msgSendingFailedLbl="";
    private static ProgressDialog progressDialog;
    private static TextView compose_to_disp;
    private static TextView compose_cc_disp;
    private static TextView compose_bcc_disp;

    private static String tempStr="";
    private  static Map<Integer, ContactSerializable> actualToReceivers;
    private  static Map<Integer, ContactSerializable> actualCCReceivers;
    private  static Map<Integer, ContactSerializable> actualBCCReceivers;

    private  static int actualToReceiversId;
    private  static int actualCCReceiversId;
    private  static int actualBCCReceiversId;

    private static LinearLayout ccLayout;
    private static LinearLayout bccLayout;

    private static final String RESOLVE_NAME_ALERT_DIALOG_TYPE_TO="RESOLVE_NAME_ALERT_DIALOG_TYPE_TO";
    private static final String RESOLVE_NAME_ALERT_DIALOG_TYPE_CC="RESOLVE_NAME_ALERT_DIALOG_TYPE_CC";
    private static final String RESOLVE_NAME_ALERT_DIALOG_TYPE_BCC="RESOLVE_NAME_ALERT_DIALOG_TYPE_BCC";

    private static final int TYPE_TO=0;
    private static final int TYPE_CC=1;
    private static final int TYPE_BCC=2;

    public static final int PREFILL_TYPE_COMPOSE = 0;
    public static final int PREFILL_TYPE_ADD_RECIPIENT = 1;
    public static final int PREFILL_TYPE_BUGS_SUGGESTIONS = 2;
    public static final int PREFILL_TYPE_REPLY = 3;
    public static final int PREFILL_TYPE_REPLY_ALL = 4;
    public static final int PREFILL_TYPE_FORWARD = 5;
    public static final int PREFILL_TYPE_CONTACT_DETAILS_BTN = 6;
    public static final int PREFILL_TYPE_OTHERS = 7;

    public static final  String PREFILL_DATA_EXTRA="PREFILL_DATA_EXTRA";
    public static final String PREFILL_DATA_TYPE_EXTRA="PREFILL_DATA_TYPE_EXTRA";
    public static final  String PREFILL_DATA_REPL_ITEMID="PREFILL_DATA_REPL_ITEMID";
    public static final String PREFILL_DATA_TO_EXTRA="PREFILL_DATA_TO_EXTRA";
    public static final String PREFILL_DATA_CC_EXTRA="PREFILL_DATA_CC_EXTRA";
    public static final String PREFILL_DATA_BCC_EXTRA="PREFILL_DATA_BCC_EXTRA";
    public static final String PREFILL_DATA_SUBJECT_EXTRA="PREFILL_DATA_SUBJECT_EXTRA";
    public static final String PREFILL_DATA_BODY_EXTRA="PREFILL_DATA_BODY_EXTRA";
    public static final String PREFILL_DATA_TITLEBAR_EXTRA="PREFILL_DATA_TITLEBAR_EXTRA";		//displays this text in the title bar
    public static final String PREFILL_DATA_SETFOCUS_ON_BODY_EXTRA="PREFILL_DATA_SETFOCUS_ON_BODY_EXTRA";
    public static final String PREFILL_DATA_QUOTE_HTML="PREFILL_DATA_QUOTE_HTML";

    private static WebView quoteWebview;
    private static LinearLayout quotedTextLinearLayout;
    private static boolean resolvingNames=false;

    private static LinearLayout titleBar_Progress_linearLayout;
    private  static int notResolved=0;
    private  static StringBuffer notResolvedNames = new StringBuffer();

    public static int TOTAL_RESOLVE_NAMES=0;
    public  static int resolvedNames=0;

    private static ProgressDisplayNotificationBar progressDispBar;

    private static WarningDisplayNotificationBar warningDispBar;


    //The To CC and BCC will expect a bundle of ContactSerializable objects
    private  static int prefill_type;
    private  static String prefill_repl_itemid;
    private  static Bundle prefill_to, prefill_cc, prefill_bcc;
    private  static String prefill_subject, prefill_body, prefill_titlebar,prefill_quoteWebview;

    private static SpannableStringBuilder sBuilder=new SpannableStringBuilder();
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity=this;

        setContentView(R.layout.activity_compose);

        //Initialize toolbar
        MailApplication.toolbarInitialize(this);

        standardWebView = new StandardWebView();

        //composeTo = (EditText)findViewById(R.id.compose_to);
        composeSubject = (EditText)findViewById(R.id.compose_subject);
        composeBody = (EditText)findViewById(R.id.compose_body);

        composeSignature = (EditText)findViewById(R.id.compose_signature);

        compose_to_disp = (TextView)findViewById(R.id.compose_to_disp);
        compose_cc_disp = (TextView)findViewById(R.id.compose_cc_disp);
        compose_bcc_disp = (TextView)findViewById(R.id.compose_bcc_disp);

        ccLayout = (LinearLayout) findViewById(R.id.layout_cc);
        bccLayout = (LinearLayout) findViewById(R.id.layout_bcc);

        msgSendingFailedLbl=activity.getText(R.string.compose_msg_send_failed);

        quoteWebview  = (WebView)findViewById(R.id.quoteWebview);
        progressDispBar = new ProgressDisplayNotificationBar(activity);
        warningDispBar = new WarningDisplayNotificationBar(activity);
        titleBar_Progress_linearLayout = (LinearLayout)findViewById(R.id.titleBar_Progress_linearLayout);

        titleBar_Progress_linearLayout.setVisibility(View.INVISIBLE);	// the default in layout is "GONE".
        // Changing to invisible so that users wont experience a layout change on when this disappears.

        quotedTextLinearLayout = (LinearLayout)findViewById(R.id.quoteLinearLayout);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        try {
            service = EWSConnection.getServiceFromStoredCredentials(this);}
        catch (Exception e) {
            e.printStackTrace();
        }
        actualToReceiversId=-1;
        actualCCReceiversId=-1;
        actualBCCReceiversId=-1;

        actualToReceivers = new HashMap<Integer, ContactSerializable>();
        actualCCReceivers = new HashMap<Integer, ContactSerializable>();
        actualBCCReceivers = new HashMap<Integer, ContactSerializable>();

        TOTAL_RESOLVE_NAMES=0;
        resolvedNames=0;
        notResolved=0;
        notResolvedNames = new StringBuffer();

        setSupportProgressBarIndeterminateVisibility(false);
        //prefill data
        try {
            if(getIntent().getBooleanExtra(PREFILL_DATA_EXTRA, false)){
                prefillData();
            }
            else{
                prefill_type=PREFILL_TYPE_COMPOSE;
                prefill_repl_itemid="";
                prefill_to=null; prefill_cc=null; prefill_bcc=null;
                prefill_subject="";
                prefill_body="";
                prefill_titlebar="";
                prefill_quoteWebview="";

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void prefillData() {

        boolean prefill_setfocus_onbody=false;

        prefill_type = getIntent().getIntExtra(PREFILL_DATA_TYPE_EXTRA, PREFILL_TYPE_COMPOSE);
        prefill_repl_itemid = getIntent().getStringExtra(PREFILL_DATA_REPL_ITEMID);
        prefill_to = getIntent().getBundleExtra(PREFILL_DATA_TO_EXTRA);
        prefill_cc = getIntent().getBundleExtra(PREFILL_DATA_CC_EXTRA);
        prefill_bcc = getIntent().getBundleExtra(PREFILL_DATA_BCC_EXTRA);
        prefill_subject = getIntent().getStringExtra(PREFILL_DATA_SUBJECT_EXTRA);
        prefill_body = getIntent().getStringExtra(PREFILL_DATA_BODY_EXTRA);
        prefill_titlebar = getIntent().getStringExtra(PREFILL_DATA_TITLEBAR_EXTRA);
        prefill_setfocus_onbody = getIntent().getBooleanExtra(PREFILL_DATA_SETFOCUS_ON_BODY_EXTRA,false);
        prefill_quoteWebview = getIntent().getStringExtra(PREFILL_DATA_QUOTE_HTML);

        // Prefills To
        if(null!=prefill_to && prefill_to.size() > 0){
            ContactSerializable prefill_sTo;
            for(String a:prefill_to.keySet()){
                prefill_sTo = (ContactSerializable)prefill_to.get(a);
                addToRecipient(prefill_sTo);
            }
        }

        // Prefills CC
        if(null!=prefill_cc && !(prefill_cc.equals(""))){
            ContactSerializable prefill_sCC;
            for(String a:prefill_cc.keySet()){
                prefill_sCC = (ContactSerializable)prefill_cc.get(a);
                addCCRecipient(prefill_sCC);
            }

        }

        // Prefills BCC
        if(null!=prefill_bcc && !(prefill_bcc.equals(""))){
            ContactSerializable prefill_sBCC;
            for(String a:prefill_bcc.keySet()){
                prefill_sBCC = (ContactSerializable)prefill_bcc.get(a);
                addBCCRecipient(prefill_sBCC);
            }
        }

        // Prefills Subject
        if(null!=prefill_subject && !(prefill_subject.equals(""))){
            composeSubject.setText(prefill_subject);
        }

        // Prefills Body
        if(null!=prefill_body && !(prefill_body.equals(""))){
            composeBody.setText(prefill_body);
        }

        // Prefills titlebar
        if(null!=prefill_titlebar && !(prefill_titlebar.equals(""))){
            setTitle(prefill_titlebar);
        }

        //prefill quote
        if(null != prefill_quoteWebview && !(prefill_quoteWebview.equals(""))){
            quotedTextLinearLayout.setVisibility(View.VISIBLE);
            standardWebView.loadData(quoteWebview,prefill_quoteWebview);
        }

        // sets focus
        if(prefill_setfocus_onbody){
            composeBody.requestFocus();
        }
    }

    @Override
    public void onPause(){
        super.onPause();

        if(generalSettings.isComposeSignatureEnabled(activity)){
            try {

                //store signature
                generalSettings.storeComposeSignature(activity, Utilities.convertEditableToHTML(composeSignature.getText()));
            } catch (Exception e1) {
                
                e1.printStackTrace();
            }
        }
    }

    @Override
    public void onResume(){
        super.onResume();

        if(generalSettings.isComposeSignatureEnabled(activity)){
            try {
                tempStr=generalSettings.getComposeSignature(activity);
                tempStr=Utilities.convertHTMLToText(tempStr);
                //restore signature
                composeSignature.setText(tempStr);
            } catch (Exception e1) {
                
                e1.printStackTrace();
            }
        }
        else{
            composeSignature.setVisibility(View.GONE);
        }
    }

    public void sendBtnOnClick(View view){
        sendMail();
    }

    public void toAddOnClick(View view){
        //addToRecipient(composeTo.getText().toString(), composeTo.getText().toString());
        startAddRecipientActivity();
    }

    private void startAddRecipientActivity() {
        
        Intent i = new Intent(this, AddRecipientActivity.class);
        startActivityForResult(i, 1);
    }

    // Add a To Recipient
    public void addToRecipient( ContactSerializable contact){
        //if(contact!=null && contact.getEmail() !=null && (!(contact.getEmail().equals("")))){
        if(contact!=null ){
            actualToReceiversId++;
            actualToReceivers.put(actualToReceiversId, contact);
            refreshDisplayString(compose_to_disp,actualToReceivers,TYPE_TO);
            if(BuildConfig.DEBUG) {
                Log.d(LOG_TAG, "actualToReceivers after adding a recipient " + actualToReceivers);
            }

            //if the contact is set to resolve names in directory flag then do that now.
            if(contact.isTryResolveNamesInDirectory()){
                StringBuilder extraMessage = new StringBuilder();
                extraMessage.append(TYPE_TO);
                extraMessage.append(actualToReceiversId);
                if(null!= contact.getEmail() && (!(contact.getEmail().equals("")))){
                    new ResolveNamesAsyncTask(this,this,service,contact.getEmail().toString(),false,"",extraMessage.toString()).execute();
                }
                else if (null!= contact.getDisplayName() && (!(contact.getDisplayName().equals("")))){
                    new ResolveNamesAsyncTask(this,this,service,contact.getDisplayName().toString(),false,"",extraMessage.toString()).execute();
                }
            }
        }
        else{
            Log.e(LOG_TAG, "ComposeActivity -> addTorecipient() -> Contact is null. Hence not adding this contact");
        }
    }

    // Remove a To Recipient
    public void removeToRecipient( int contactKey){
        actualToReceivers.remove(contactKey);
        refreshDisplayString(compose_to_disp,actualToReceivers,TYPE_TO);
        if(BuildConfig.DEBUG) {
            Log.d(LOG_TAG, "actualToReceivers after removing a recipient" + actualToReceivers);
        }
    }

    // Add a CC Recipient
    public void addCCRecipient( ContactSerializable contact){
        //if(contact!=null && contact.getEmail() !=null && (!(contact.getEmail().equals("")))){
        if(contact!=null ){
            actualCCReceiversId++;
            ccLayout.setVisibility(View.VISIBLE);
            actualCCReceivers.put(actualCCReceiversId, contact);
            ccLayout.setVisibility(View.VISIBLE);

            refreshDisplayString(compose_cc_disp,actualCCReceivers,TYPE_CC);

            //if the contact is set to resolve names in directory flag then do that now.
            if(contact.isTryResolveNamesInDirectory()){
                StringBuilder extraMessage = new StringBuilder();
                extraMessage.append(TYPE_CC);
                extraMessage.append(actualCCReceiversId);
                if(BuildConfig.DEBUG) {
                    Log.d(LOG_TAG, "ComposeActivity -> addCCrecipient() -> Extramessage " + extraMessage.toString());
                }
                if(null!= contact.getEmail() && (!(contact.getEmail().equals("")))){
                    new ResolveNamesAsyncTask(this,this,service,contact.getEmail().toString(),false,"",extraMessage.toString()).execute();
                }
                else if (null!= contact.getDisplayName() && (!(contact.getDisplayName().equals("")))){
                    new ResolveNamesAsyncTask(this,this,service,contact.getDisplayName().toString(),false,"",extraMessage.toString()).execute();
                }
            }
        }
        else{
            Log.e(LOG_TAG, "ComposeActivity -> addCCrecipient() -> Contact is null. Hence not adding this contact");
        }
    }
    // Remove a CC Recipient
    public void removeCCRecipient( int contactKey){
        actualCCReceivers.remove(contactKey);
        refreshDisplayString(compose_cc_disp,actualCCReceivers,TYPE_CC);
        if(BuildConfig.DEBUG) {
            Log.d(LOG_TAG, "actualCCReceivers after removing a recipient" + actualCCReceivers);
        }
    }

    // Add a BCC Recipient
    public void addBCCRecipient( ContactSerializable contact){
        //if(contact!=null && contact.getEmail() !=null && (!(contact.getEmail().equals("")))){
        if(contact!=null ){
            actualBCCReceiversId++;
            actualBCCReceivers.put(actualBCCReceiversId, contact);

            refreshDisplayString(compose_bcc_disp,actualBCCReceivers,TYPE_BCC);
            bccLayout.setVisibility(View.VISIBLE);

            //if the contact is set to resolve names in directory flag then do that now.
            if(contact.isTryResolveNamesInDirectory()){
                StringBuilder extraMessage = new StringBuilder();
                extraMessage.append(TYPE_BCC);
                extraMessage.append(actualBCCReceiversId);
                if(null!= contact.getEmail() && (!(contact.getEmail().equals("")))){
                    new ResolveNamesAsyncTask(this,this,service,contact.getEmail().toString(),false,"",extraMessage.toString()).execute();
                }
                else if (null!= contact.getDisplayName() && (!(contact.getDisplayName().equals("")))){
                    new ResolveNamesAsyncTask(this,this,service,contact.getDisplayName().toString(),false,"",extraMessage.toString()).execute();
                }
            }
        }
        else{
            Log.e(LOG_TAG, "ComposeActivity -> addBCCrecipient() -> Contact is null. Hence not adding this contact");
        }
    }

    // Remove a BCC Recipient
    public void removeBCCRecipient( int contactKey){
        actualBCCReceivers.remove(contactKey);
        refreshDisplayString(compose_bcc_disp,actualBCCReceivers,TYPE_BCC);
        if(BuildConfig.DEBUG) {
            Log.d(LOG_TAG, "actualBCCReceivers after removing a recipient" + actualBCCReceivers);
        }
    }


    /** refreshes the display of To or CC or BCC
     * @param disp
     * @param actualReceivers
     * @param type
     */
    private void refreshDisplayString(TextView disp,
                                      Map<Integer, ContactSerializable> actualReceivers, int type) {

        if(actualReceivers.size() !=0){
            SpannableStringBuilder sBuilder =buildDisplayString(actualReceivers, type);

            disp.setText(sBuilder);
            disp.setMovementMethod(LinkMovementMethod.getInstance()); 	//this line will make the links to work
        }
        else{
            //if the list is empty then set then set the empty string
            disp.setText(getString(R.string.compose_emptyString));
        }
    }

    /** build the email id display names string for display in To, CC and BCC
     * @param actualReceivers
     * @param type Specify whether Tp or CC or BCC
     * @return
     */
    private SpannableStringBuilder buildDisplayString(Map<Integer,  ContactSerializable> actualReceivers, final int type) {
        sBuilder.clear();
        int initLength=0;
        Set<Integer> contactKeys = actualReceivers.keySet();
        for(final Integer contactKey : contactKeys){
            initLength = sBuilder.length();
            if(actualReceivers.get(contactKey)!=null){
                final ContactSerializable sContact = actualReceivers.get(contactKey);
                sBuilder.append(sContact.getDisplayName()+ EMAIL_DELIMITER_DISP);

                sBuilder.setSpan(new ClickableSpan() {

                    @Override
                    public void onClick(View arg0) {
                        if(BuildConfig.DEBUG) {
                            Log.d(LOG_TAG, contactKey + " clicked");
                        }
                        (new AlertDialog.Builder(ComposeActivity.this))
                                .setTitle(sContact.getDisplayName())
                                .setItems(R.array.compose_email_link, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {

                                        String[] items = getResources().getStringArray(R.array.compose_email_link);

                                        if(which ==0){
                                            //View Contact Details
                                            Intent contactDetailsIntent = new Intent(getBaseContext(), ContactDetailsActivity.class);
                                            contactDetailsIntent.putExtra(ContactDetailsActivity.CONTACT_SERIALIZABLE_EXTRA, sContact);
                                            startActivity(contactDetailsIntent);
                                        }
                                        else if(which ==1){
                                            //Remove Contact

                                            if(type == TYPE_TO){
                                                removeToRecipient(contactKey);
                                            }
                                            else if(type == TYPE_CC){
                                                removeCCRecipient(contactKey);
                                            }
                                            else if(type == TYPE_BCC){
                                                removeBCCRecipient(contactKey);
                                            }
                                        }
                                    }
                                })
                                .setNegativeButton(R.string.alertdialog_negative_lbl, new OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog, int arg1) {
                                        
                                        dialog.dismiss();
                                    }
                                })
                                .show();

                    }
                }, initLength, sBuilder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                sBuilder.append("\n");
            }
            else{
                Log.e(LOG_TAG, "ComposeActivity -> actualReceivers.get(contactKey) is null");
            }
        }
        return sBuilder;
    }

    private void sendMail() {
        if(!resolvingNames){
            if((null != actualToReceivers && (actualToReceivers.size()>0 ))
                    || (( null != actualCCReceivers) && (actualCCReceivers.size() >0))
                    || ((null != actualBCCReceivers) && (actualBCCReceivers.size()>0))){

                alertAndSendMail();
            }
            else{
                //To is null
                Notifications.showToast(activity, activity.getText(R.string.compose_to_no_recep), Toast.LENGTH_LONG);
            }
        }else{
            //Resolving names
            Notifications.showToast(activity, activity.getText(R.string.compose_resolving_names_wait), Toast.LENGTH_LONG);
        }
    }

	/*public void toSearchOnClick(View view){

		if( null!=composeTo.getText() && null!=composeTo.getText().toString() && !(composeTo.getText().toString().equals(""))){

			//EWS Call

			new ResolveNamesAsyncTask(this,this,service,composeTo.getText().toString(),true,getText(R.string.compose_resoleNameDialog_progress).toString(),RESOLVE_NAME_ALERT_DIALOG_TYPE_TO).execute();

		}
	}*/

    private void alertAndSendMail() {
        String alertMsg="";

        if(composeBody.getText().toString().equals("") && composeSubject.getText().toString().equals("")){
            alertMsg=getString(R.string.compose_alert_confirm_nobody_nosub_send);
        }
        else if ( composeSubject.getText().toString().equals("")){
            alertMsg=getString(R.string.compose_alert_confirm_nosub_send);
        }
        else if ( composeBody.getText().toString().equals("")){
            alertMsg=getString(R.string.compose_alert_confirm_nobody_send);
        }
        else{
            alertMsg=getString(R.string.compose_alert_confirm_send);
        }

        buildAlertDialog(this, getString(R.string.compose_alert_confirm_title), alertMsg,getString(R.string.alertdialog_positive_lbl), getString(R.string.alertdialog_negative_lbl));
    }

    public class Send extends AsyncTask<Void, String, Boolean>{

        private ExchangeService service;

        @Override
        protected void onPreExecute() {

            try {
                progressDialog = ProgressDialog.show(activity, "",
                        activity.getString(R.string.compose_sending), true);
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(LOG_TAG, "Exception occured on preexecute");
            }
        }

        @Override
        protected Boolean doInBackground(Void... paramArrayOfParams) {

            try {

                service = EWSConnection.getServiceFromStoredCredentials(activity.getApplicationContext());

                to=actualToReceivers.values();
                cc=actualCCReceivers.values();
                bcc=actualBCCReceivers.values();
                subject=composeSubject.getText().toString();
                body=Utilities.convertEditableToHTML(composeBody.getText());
                if(generalSettings.isComposeSignatureEnabled(activity)){
                    body=appendSignature(body);
                }

                //EWS call
                if(prefill_type == PREFILL_TYPE_REPLY || prefill_type == PREFILL_TYPE_REPLY_ALL ||  prefill_type == PREFILL_TYPE_REPLY_ALL){
                    NetworkCall.replyMail(activity, service, prefill_repl_itemid, to, cc, bcc, subject, body, false);
                    publishProgress(STATUS_SENT, "");
                }
                else if(prefill_type == PREFILL_TYPE_REPLY_ALL){
                    NetworkCall.replyMail(activity, service, prefill_repl_itemid, to, cc, bcc, subject, body, true);
                    publishProgress(STATUS_SENT, "");
                }
                else if(prefill_type == PREFILL_TYPE_FORWARD){
                    NetworkCall.forwardMail(activity, service, prefill_repl_itemid, to, cc, bcc, subject, body);
                    publishProgress(STATUS_SENT, "");
                }
                else{
                    NetworkCall.sendMail(activity, service, to, cc,bcc, subject, body);
                    publishProgress(STATUS_SENT, "");
                }
            } catch (URISyntaxException e) {
                
                Log.e(LOG_TAG, "Malformed Webmail URL");
                publishProgress(STATUS_ERROR, "Message not sent!\n\nDetails: Malformed Webmail URL " );
            }
            catch(HttpErrorException e){
                if(e.getMessage().toLowerCase().contains("Unauthorized".toLowerCase())){
                    Log.e(LOG_TAG, "Authentication Failed!\nDetails: " + e.getMessage());
                    publishProgress(STATUS_ERROR, msgSendingFailedLbl +"\n\nDetails: Authentication Failed ");
                }
                else
                {
                    Log.e(LOG_TAG, "Error Occured!\nDetails:" + e.getMessage());
                    e.printStackTrace();
                    publishProgress(STATUS_ERROR, msgSendingFailedLbl +"\n\nDetails:" + e.getMessage());
                }
            }
            catch (Exception e) {
                Log.e(LOG_TAG, "Error Occured!\nDetails:" + e.getMessage());
                e.printStackTrace();
                publishProgress(STATUS_ERROR, msgSendingFailedLbl + "\n\nDetails:" + e.getMessage());
            }
            return true;
        }

        private String appendSignature(String body) {

            body=body+Utilities.convertEditableToHTML(composeSignature.getText());
            return body;
        }

        @Override
        protected void onProgressUpdate(String... progress) {

            if(progress[0].equalsIgnoreCase(STATUS_ERROR)){
                progressDialog.dismiss();
                Notifications.showAlert(activity, progress[1]);
            }else if(progress[0].equalsIgnoreCase(STATUS_SENT)){
                progressDialog.dismiss();
                onBackPressed();
                Notifications.showToast(activity, activity.getText(R.string.compose_msg_sent), Toast.LENGTH_SHORT);
            }
        }
    }

    //the start activity for result intent of "AddRecipientActivity" will  be called on clicking the "Add Recipient" buton. This method will be called when the child is finished.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        List<ContactSerializable> contactList ;
        if (resultCode == RESULT_OK ) {
            if (data.hasExtra(AddRecipientActivity.ADD_TYPE_EXTRA) && data.hasExtra(AddRecipientActivity.ADD_TYPE_COLLECTION)) {

                contactList = (ArrayList<ContactSerializable>)data.getSerializableExtra(AddRecipientActivity.ADD_TYPE_COLLECTION);
                int type = data.getIntExtra(AddRecipientActivity.ADD_TYPE_EXTRA, 0);

                if(type == AddRecipientActivity.ADD_TYPE_TO){
                    for(ContactSerializable contact  : contactList){
                        addToRecipient(contact);

                    }
                }
                else if(type == AddRecipientActivity.ADD_TYPE_CC){
                    for(ContactSerializable contact  : contactList){

                        addCCRecipient(contact);
                    }
                }
                else if(type == AddRecipientActivity.ADD_TYPE_BCC){
                    for(ContactSerializable contact  : contactList){
                        addBCCRecipient(contact);
                    }
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

            MenuItem menuItem;

            menuItem = menu.add(getText(R.string.compose_actionbar_send));
            MenuItemCompat.setShowAsAction(menuItem,
                    MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);

            menuItem = menu.add(getText(R.string.compose_actionbar_cancel));
            MenuItemCompat.setShowAsAction(menuItem,
                    MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);

		/*
				//Submenu
				SubMenu subMenu = menu.addSubMenu("");

				subMenu
				.add(ACTIONBAR_SETTINGS)
				.setIcon(OptionsUIContent.getSettingsIcon())
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);


				subMenu
				.add(ACTIONBAR_ABOUT)
				.setIcon(OptionsUIContent.getAboutIcon())
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);


				//Overflow submenu icon
				MenuItem subMenuItem = subMenu.getItem();
				subMenuItem.setIcon(OptionsUIContent.getMoreoverFlowIcon());
				subMenuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		 */

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if(item!=null && item.getItemId()==android.R.id.home){
            onBackPressed();
        }
        else if(item!=null && item.getTitle().equals(getText(R.string.compose_actionbar_send))){
            sendMail();
        }
        else if(item!=null && item.getTitle().equals(getText(R.string.compose_actionbar_cancel))){
            cancelPage();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        activity.finish();
        ApplyAnimation.setComposeActivityCloseAnim(activity);
    }

    private void cancelPage() {
        AlertDialog myConfirmBox =new AlertDialog.Builder(this)
                //set message, title, and icon
                .setTitle(getString(R.string.compose_alert_confirm_cancel_title))
                .setMessage(getString(R.string.compose_alert_confirm_cancel_msg) )
                .setPositiveButton(getString(R.string.alertdialog_positive_lbl)	, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        onBackPressed();
                    }
                })
                .setNegativeButton(getString(R.string.alertdialog_negative_lbl), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();
        myConfirmBox.show();
    }

    @Override
    //called by resolvenames while started resolving a name
    public void handleResolvingNames() {
        TOTAL_RESOLVE_NAMES++;
        resolvingNames=true;
        setSupportProgressBarIndeterminateVisibility(true);
        progressDispBar.showStatusBar();
        progressDispBar.setText(getString(R.string.compose_resolving_names_text,(resolvedNames+1),TOTAL_RESOLVE_NAMES));
    }

    @Override
    public void handleResolveNamesOutput(
            NameResolutionCollection outputCollection, String extra1) {

        try {
            if(outputCollection!= null && outputCollection.getCount()==1){
                Contact contact = outputCollection.nameResolutionCollection(0).getContact();
                String email = outputCollection.nameResolutionCollection(0).getMailbox().getAddress();
                if(BuildConfig.DEBUG) {
                    Log.d(LOG_TAG, "ComposeActivity - > handleResolveNamesOutput() -> Extramessage " + extra1);
                }
                if(extra1!=null && (extra1.length() ==2 || extra1.length() ==3)){
                    int type=Character.getNumericValue(extra1.charAt(0));
                    int id=0;
                    if(extra1.length() == 2){
                        id=Character.getNumericValue(extra1.charAt(1));
                    }
                    else if(extra1.length() == 3){
                        id=Integer.valueOf(String.valueOf(extra1.charAt(1)) + String.valueOf(extra1.charAt(2))) ;
                    }
                    if(BuildConfig.DEBUG) {
                        Log.d(LOG_TAG, "type " + type);
                        Log.d(LOG_TAG, "id" + id);
                    }
                    if(type == TYPE_TO){
                        if(BuildConfig.DEBUG) {
                            Log.d(LOG_TAG, "ComposeActivity - > handleResolveNamesOutput() -> TYPE TO; Id:" + id);
                        }
                        actualToReceivers.put(id, ContactSerializable.getContactSerializableFromContact(contact,email ));
                        refreshDisplayString(compose_to_disp,actualToReceivers,TYPE_TO);

                    }
                    if(type== TYPE_CC){
                        if(BuildConfig.DEBUG) {
                            Log.d(LOG_TAG, "ComposeActivity - > handleResolveNamesOutput() -> TYPE CC; Id:" + id);
                        }
                        actualCCReceivers.put(id, ContactSerializable.getContactSerializableFromContact(contact,email ));
                        refreshDisplayString(compose_cc_disp,actualCCReceivers,TYPE_CC);
                    }
                    if(type == TYPE_BCC){
                        if(BuildConfig.DEBUG) {
                            Log.d(LOG_TAG, "ComposeActivity - > handleResolveNamesOutput() -> TYPE BCC; Id:" + id);
                        }
                        actualBCCReceivers.put(id, ContactSerializable.getContactSerializableFromContact(contact,email ));
                        refreshDisplayString(compose_bcc_disp,actualBCCReceivers,TYPE_BCC);
                    }

                }
                else
                {
                    Log.e(LOG_TAG, "ComposeActivity -> Extra message to Resolve Names extra param length is not 2");
                }
            }
            else{
                // the directory returned no or more than one names for the search. so remove that entry from the list and display a notification to the user.
                //NOT RESOLVED EMAIL REMOVING SECTION
                Log.e(LOG_TAG, "ComposeActivity -> Resolve Names output collections is null or output collection count is not 1");
                Log.e(LOG_TAG, "ComposeActivity -> Output collection count:" + ((outputCollection!=null)?String.valueOf(outputCollection.getCount()):"null"));
                if(BuildConfig.DEBUG) {
                    Log.d(LOG_TAG, "Removing recipient");
                }
                int id=0;
                if(extra1!=null && (extra1.length() ==2 || extra1.length() ==3)){
                    int type=Character.getNumericValue(extra1.charAt(0));

                    if(extra1.length() == 2){
                        id=Character.getNumericValue(extra1.charAt(1));
                    }
                    else if(extra1.length() == 3){
                        id=Integer.valueOf(String.valueOf(extra1.charAt(1)) + String.valueOf(extra1.charAt(2))) ;
                    }

                    if(type== TYPE_CC){
                        if(actualCCReceivers.get(id) !=null )
                        {
                            if(actualCCReceivers.get(id).getEmail()!=null){
                                if(!Utilities.isValidEmail(actualCCReceivers.get(id).getEmail())){
                                    Notifications.showToast(this, this.getString(R.string.compose_resolve_names_not_found, actualCCReceivers.get(id).getDisplayName()) , Toast.LENGTH_SHORT);
                                    notResolved++;
                                    notResolvedNames.append(notResolved);
                                    notResolvedNames.append(". ");
                                    notResolvedNames.append(actualCCReceivers.get(id).getDisplayName());
                                    notResolvedNames.append("\n");
                                    actualCCReceivers.remove(id);

                                    refreshDisplayString(compose_cc_disp,actualCCReceivers,TYPE_CC);
                                }
                                else{
                                    Log.d(LOG_TAG, "ComposeActivity -> Contact has an email id which is in directory. so the email may be gmail, yahoo, etc. and s not removed");
                                }
                            }
                            else{
                                Notifications.showToast(this, this.getString(R.string.compose_resolve_names_not_found, actualCCReceivers.get(id).getDisplayName()) , Toast.LENGTH_SHORT);
                                notResolved++;
                                notResolvedNames.append(notResolved);
                                notResolvedNames.append(". ");
                                notResolvedNames.append(actualCCReceivers.get(id).getDisplayName());
                                notResolvedNames.append("\n");
                                actualCCReceivers.remove(id);
                                refreshDisplayString(compose_cc_disp,actualCCReceivers,TYPE_CC);
                            }
                        }
                        else{
                            Log.e(LOG_TAG, "ComposeActivity -> Contact is null");

                        }
                    }
                    if(type == TYPE_BCC){
                        if(actualBCCReceivers.get(id) !=null )
                        {
                            if(actualBCCReceivers.get(id).getEmail()!=null){
                                if(!Utilities.isValidEmail(actualBCCReceivers.get(id).getEmail())){
                                    Notifications.showToast(this, this.getString(R.string.compose_resolve_names_not_found, actualBCCReceivers.get(id).getDisplayName()) , Toast.LENGTH_SHORT);
                                    notResolved++;
                                    notResolvedNames.append(notResolved);
                                    notResolvedNames.append(". ");
                                    notResolvedNames.append(actualBCCReceivers.get(id).getDisplayName());
                                    notResolvedNames.append("\n");
                                    actualBCCReceivers.remove(id);
                                    refreshDisplayString(compose_bcc_disp,actualBCCReceivers,TYPE_BCC);
                                }
                                else{
                                    Log.d(LOG_TAG, "ComposeActivity -> Contact has an email id which is in directory. so the email may be gmail, yahoo, etc. and s not removed");
                                }
                            }
                            else{
                                Notifications.showToast(this, this.getString(R.string.compose_resolve_names_not_found, actualBCCReceivers.get(id).getDisplayName()) , Toast.LENGTH_SHORT);
                                notResolved++;
                                notResolvedNames.append(notResolved);
                                notResolvedNames.append(". ");
                                notResolvedNames.append(actualBCCReceivers.get(id).getDisplayName());
                                notResolvedNames.append("\n");
                                actualBCCReceivers.remove(id);
                                refreshDisplayString(compose_bcc_disp,actualBCCReceivers,TYPE_BCC);
                            }

                        }
                        else{
                            Log.e(LOG_TAG, "ComposeActivity -> Contact is null");

                        }
                    }
                }
            }

            //increased the total resolved names and check with everything got resolved.
            resolvedNames++;
//			Log.d(TAG, "resolvedNames " + resolvedNames);
//			Log.d(TAG, "TOTAL_RESOLVE_NAMES " + TOTAL_RESOLVE_NAMES);

            checkResolvedNames();

        } catch (ArgumentOutOfRangeException e) {
            e.printStackTrace();
        } catch (ServiceLocalException e) {
            e.printStackTrace();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void checkResolvedNames() {
        // Shows the disp bar indicating the status, updates it, hides it or shows a error disp bar when there are unreolved ones.

        if(resolvedNames >= TOTAL_RESOLVE_NAMES){
            resolvingNames=false;
            TOTAL_RESOLVE_NAMES=0;
            setSupportProgressBarIndeterminateVisibility(false);
            progressDispBar.hideStatusBar();

            //if there are names which are not resolved and removed then update the progres notification layout a warning msg
            if(notResolved >0){
                warningDispBar.showStatusBar();
                warningDispBar.setText(getString(R.string.compose_resolve_notfound_warning,notResolvedNames.toString()));
            }
        }
        else{
            resolvingNames=true;
            progressDispBar.setText(getString(R.string.compose_resolving_names_text,(resolvedNames+1),TOTAL_RESOLVE_NAMES));
        }
    }

    @Override
    public void handleResolveNamesOutputError(
            NameResolutionCollection outputCollection, String extra1,
            Exception pE) {

        //setSupportProgressBarIndeterminateVisibility(false);
        resolvedNames++;
        checkResolvedNames();
    }

    public  void buildAlertDialog(Context context, String title, String message, String positiveMsg, String negativeMsg){
        AlertDialog myConfirmBox =new AlertDialog.Builder(context)
                //set message, title, and icon
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(positiveMsg	, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        //your deleting code
                        new ComposeActivity().new Send().execute();
                    }
                })
                .setNegativeButton(negativeMsg, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();

        myConfirmBox.show();
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

