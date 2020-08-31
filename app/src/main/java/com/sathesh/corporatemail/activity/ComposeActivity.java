package com.sathesh.corporatemail.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.net.ParseException;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.OpenableColumns;
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

import com.google.android.flexbox.FlexboxLayout;
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
import com.sathesh.corporatemail.customui.AttachmentCardView;
import com.sathesh.corporatemail.customui.Notifications;
import com.sathesh.corporatemail.ews.EWSConnection;
import com.sathesh.corporatemail.ews.NetworkCall;
import com.sathesh.corporatemail.ui.components.ProgressDisplayNotificationBar;
import com.sathesh.corporatemail.ui.components.WarningDisplayNotificationBar;
import com.sathesh.corporatemail.util.Utilities;
import com.sathesh.corporatemail.web.StandardWebView;

import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

import microsoft.exchange.webservices.data.core.ExchangeService;
import microsoft.exchange.webservices.data.core.enumeration.service.ConflictResolutionMode;
import microsoft.exchange.webservices.data.core.exception.http.HttpErrorException;
import microsoft.exchange.webservices.data.core.exception.misc.ArgumentOutOfRangeException;
import microsoft.exchange.webservices.data.core.exception.service.local.ServiceLocalException;
import microsoft.exchange.webservices.data.core.exception.service.remote.ServiceRequestException;
import microsoft.exchange.webservices.data.core.service.item.Contact;
import microsoft.exchange.webservices.data.core.service.item.EmailMessage;
import microsoft.exchange.webservices.data.core.service.response.ResponseMessage;
import microsoft.exchange.webservices.data.misc.NameResolutionCollection;
import microsoft.exchange.webservices.data.property.complex.Attachment;
import microsoft.exchange.webservices.data.property.complex.FileAttachment;
import microsoft.exchange.webservices.data.property.complex.MessageBody;

import static android.content.Intent.EXTRA_ALLOW_MULTIPLE;

public class ComposeActivity extends MyActivity implements Constants,IResolveNames{

    private  static MyActivity activity;
    //making this change
    private  static EditText composeSubject;
    private  static EditText composeBody, composeSignature;
    private  static Collection<ContactSerializable> to;
    private  static Collection<ContactSerializable> cc;
    private  static Collection<ContactSerializable> bcc;
    private  static final String STATUS_SENT="STATUS_SENT";
    private  static final String STATUS_ERROR="STATUS_ERROR";
    private  static ExchangeService service;
    private  static GeneralPreferenceAdapter generalSettings = new GeneralPreferenceAdapter();

    private static Intent intent;
    private static StandardWebView standardWebView ;
    private static CharSequence msgSendingFailedLbl="";
    private static ProgressDialog progressDialog;
    private TextView compose_to_disp;
    private TextView compose_cc_disp;
    private TextView compose_bcc_disp;

    private static String tempStr="";
    private  static Map<Integer, ContactSerializable> actualToReceivers;
    private  static Map<Integer, ContactSerializable> actualCCReceivers;
    private  static Map<Integer, ContactSerializable> actualBCCReceivers;

    private  static int actualToReceiversId;
    private  static int actualCCReceiversId;
    private  static int actualBCCReceiversId;

    private LinearLayout ccLayout;
    private LinearLayout bccLayout;

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

    public final static String   ADD_TYPE_EXTRA="ADD_TYPE_EXTRA";
    public final static String   ADD_TYPE_COLLECTION="ADD_TYPE_COLLECTION";
    public final static int   ADD_RECIPIENT_REQ_CODE=1;
    public final static int   OPEN_DOCUMENT_REQ_CODE=2;

    public final static String   ATTACHMENT_EXTRA="ATTACHMENT_EXTRA";

    private WebView quoteWebview;
    private LinearLayout quotedTextLinearLayout;
    private boolean resolvingNames=false;

    private  static int notResolved=0;
    private  static StringBuffer notResolvedNames = new StringBuffer();

    public static int TOTAL_RESOLVE_NAMES=0;
    public  static int resolvedNames=0;

    private ProgressDisplayNotificationBar progressDispBar;

    private WarningDisplayNotificationBar warningDispBar;


    //The To CC and BCC will expect a bundle of ContactSerializable objects
    private  static int prefill_type;
    private  static String prefill_repl_itemid;
    private  static Bundle prefill_to, prefill_cc, prefill_bcc;
    private  static String prefill_subject, prefill_body, prefill_titlebar,prefill_quoteWebview;

    private static SpannableStringBuilder sBuilder=new SpannableStringBuilder();

    private ArrayList<FileAttach> fileAttachList = new ArrayList<FileAttach>();
    private FlexboxLayout attachmentsLayout;
    private static EmailMessage msg;
    private static ResponseMessage responseMsg;
    private static boolean existingDraft;
    private static ReentrantLock saveDraftLock = new ReentrantLock();
    private static ExecutorService saveDraftExecutorService ;
    private static EmailMessage oldDraftMail;

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
        // Changing to invisible so that users wont experience a layout change on when this disappears.

        quotedTextLinearLayout = (LinearLayout)findViewById(R.id.quoteLinearLayout);
        attachmentsLayout = (FlexboxLayout) findViewById(R.id.compose_attachments_layout);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        try {
            service = EWSConnection.getInstance(this);}
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
        existingDraft=false;
        //prefill data
        try {
            if(getIntent().getBooleanExtra(PREFILL_DATA_EXTRA, false)){
                prefillData();
            }else{
                prefillEmptyData();
            }
        } catch (Exception e) {
            Utilities.generalCatchBlock(e, this);
        }
        saveDraftExecutorService =  Executors.newFixedThreadPool(2);
    }

    private void prefillEmptyData() throws Exception {
        prefill_type=PREFILL_TYPE_COMPOSE;
        prefill_repl_itemid="";
        prefill_to=null; prefill_cc=null; prefill_bcc=null;
        prefill_subject="";
        prefill_body="";
        prefill_titlebar="";
        prefill_quoteWebview="";
        msg= new EmailMessage(service);
    }

    private void prefillData() throws Exception {

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

        //handler for bind msg call
        Handler h = new Handler(Looper.getMainLooper()){
            // simple retry mechanism.
            int retries=0;
            @Override
            public void handleMessage(Message m) {
                if (m.what == 2) { //failure
                    if (retries < 3) {
                        new Thread(bindMsg(this)).start();
                        retries++;
                    } else {
                        try {
                            msg = new EmailMessage(service);
                        } catch (Exception e) {
                            Utilities.generalCatchBlock(e, this);
                        }
                    }
                }else if (m.what == 3) { //show attachments
                    showAttachementsFromMsg(msg);
                }
            }
        };
        //create the msg object which will be binded with the existing message
        new Thread(bindMsg(h)).start();
    }

    private Runnable bindMsg(Handler h){
        return new Runnable() {
            @Override
            public void run() {
                try {
                    h.sendEmptyMessage(0);
                    //Network call
                    msg = NetworkCall.bind(activity, service, prefill_repl_itemid);
                    switch (prefill_type){
                        case PREFILL_TYPE_REPLY:
                            responseMsg = msg.createReply(false);
                            //Network Call
                            msg = responseMsg.save();
                            existingDraft = true;
                            break;
                        case PREFILL_TYPE_REPLY_ALL:
                            responseMsg = msg.createReply(true);
                            //Network Call
                            msg = responseMsg.save();
                            existingDraft = true;
                            break;
                        case PREFILL_TYPE_FORWARD:
                            h.sendEmptyMessage(3);
                            responseMsg = msg.createForward();
                            //Network Call
                            msg = responseMsg.save();
                            existingDraft = true;
                            break;
                    }
                    h.sendEmptyMessage(1);
                }catch(Exception e){
                    Utilities.generalCatchBlock(e, this);
                    h.sendEmptyMessage(2);
                }
            }
        };

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

    public void toAddOnClick(View view){
        //addToRecipient(composeTo.getText().toString(), composeTo.getText().toString());
        startAddRecipientActivity();
    }

    private void startAddRecipientActivity() {
        
        Intent i = new Intent(this, AddRecipientActivity.class);
        startActivityForResult(i, ADD_RECIPIENT_REQ_CODE);
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

    private static String subject="", body="", signature="";

    private void updateAllValues(){
        subject = composeSubject.getText().toString();
        body=Utilities.convertEditableToHTML(composeBody.getText());
        signature = Utilities.convertEditableToHTML(composeSignature.getText());
        to=actualToReceivers.values();
        cc=actualCCReceivers.values();
        bcc=actualBCCReceivers.values();

        if(generalSettings.isComposeSignatureEnabled(activity)){
            body += signature;
        }
    }
    public class Send extends AsyncTask<Void, String, Boolean>{
        @Override
        protected void onPreExecute() {

            try {
                progressDialog = ProgressDialog.show(activity, "",
                        activity.getString(R.string.compose_sending), true);
                updateAllValues();
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(LOG_TAG, "Exception occured on preexecute");
            }
        }

        @Override
        protected Boolean doInBackground(Void... paramArrayOfParams) {

            try {
                saveDraftLock.lock();   //save draft thread should not be run in parallel with sending. so locking and unlocking the save draft lock here.

                if(!isResponseMsg()) {
                    //EWS call
                    //Send
                    NetworkCall.sendMail(activity, msg, to, cc, bcc, subject, body);
                }else{
                    //EWS call
                    //Send
                    NetworkCall.sendResponseMail(activity, responseMsg, msg, to, cc, bcc, subject, body);
                }

                publishProgress(STATUS_SENT, "");

            } catch (URISyntaxException e) {
                
                Log.e(LOG_TAG, "Malformed Webmail URL");
                publishProgress(STATUS_ERROR, "Message not sent!\n\nDetails: Malformed Webmail URL " );
            }
            catch(HttpErrorException  | ServiceRequestException e){
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
            }finally{
                saveDraftLock.unlock();
            }
            return true;
        }

        @Override
        protected void onProgressUpdate(String... progress) {

            if(progress[0].equalsIgnoreCase(STATUS_ERROR)){
                progressDialog.dismiss();
                Notifications.showAlert(activity, progress[1]);
            }else if(progress[0].equalsIgnoreCase(STATUS_SENT)){
                progressDialog.dismiss();
                exitActivity();
                Notifications.showToast(activity, activity.getText(R.string.compose_msg_sent), Toast.LENGTH_SHORT);
            }
        }
    }

    //the start activity for result intent of "AddRecipientActivity" will  be called on clicking the "Add Recipient" buton. This method will be called when the child is finished.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        List<ContactSerializable> contactList;
        if (resultCode == RESULT_OK) {
            switch(requestCode) {
                case ADD_RECIPIENT_REQ_CODE:
                //Add recipient
                if (data.hasExtra(ADD_TYPE_EXTRA) && data.hasExtra(ADD_TYPE_COLLECTION)) {
                    contactList = (ArrayList<ContactSerializable>) data.getSerializableExtra(ADD_TYPE_COLLECTION);
                    int type = data.getIntExtra(ADD_TYPE_EXTRA, 0);

                    if (type == AddRecipientActivity.ADD_TYPE_TO) {
                        for (ContactSerializable contact : contactList) {
                            addToRecipient(contact);
                        }
                    } else if (type == AddRecipientActivity.ADD_TYPE_CC) {
                        for (ContactSerializable contact : contactList) {
                            addCCRecipient(contact);
                        }
                    } else if (type == AddRecipientActivity.ADD_TYPE_BCC) {
                        for (ContactSerializable contact : contactList) {
                            addBCCRecipient(contact);
                        }
                    }
                }
                break;
                case OPEN_DOCUMENT_REQ_CODE:
                //attachments
                    ArrayList<Uri> uriList = new ArrayList<>();
                    // multiple attachments can be selected. Loading it to a arraylist.
                    if (data.getData() != null) {
                        uriList.add(data.getData());
                    } else if (data.getClipData() != null) {
                        for (int i = 0; i < data.getClipData().getItemCount(); i++) {
                            uriList.add(data.getClipData().getItemAt(i).getUri());
                        }
                    }

                    // create AttachmentCardView for all the URIs found in the arraylist.
                    for (Uri uri : uriList) {
                      attachAttachmentToMsg(uri);
                    }
                    saveDraft(false);
                    break;
                default:
                    Log.w(LOG_TAG, "Compose Activity -> unknown activity result code received: " + resultCode);
                }
        }
    }

    /**
     * Creates the card view for the given uri and shows in UI. Also attaces the attachment to the msg
     * @param uri
     */
    private void attachAttachmentToMsg(Uri uri) {
        FileAttach fileAttach = getDocumentData(uri);
        if (fileAttach != null) {
            AttachmentCardView attachmentCardView = new AttachmentCardView(this, null);
            try {
                msg.getAttachments().addFileAttachment(fileAttach.fileName, getContentResolver().openInputStream(uri));
            }catch(Exception e){
                Utilities.generalCatchBlock(e, this);
                Notifications.showToast(activity, getString(R.string.compose_attachment_error));
                return;
            }
            attachmentCardView.setFileName(fileAttach.fileName);
            try {
                attachmentCardView.setSizeOrStatus(android.text.format.Formatter.formatShortFileSize(this, Long.parseLong(fileAttach.size)));
            } catch (ParseException | NumberFormatException e) {
                attachmentCardView.setSizeOrStatus(fileAttach.size);
            }
            attachmentCardView.showRemoveIcon((View v) -> {
                try {
                    for(Attachment attach : msg.getAttachments()){
                        if (attach.getName().equalsIgnoreCase(fileAttach.fileName)){
                            msg.getAttachments().remove(attach);
                            break;
                        }
                    }
                    attachmentsLayout.removeView(attachmentCardView);
                    saveDraft(false);
                }catch(Exception e){
                    Utilities.generalCatchBlock(e, this);
                }
            });
            attachmentsLayout.addView(attachmentCardView);
        }
    }

    private void showAttachementsFromMsg(EmailMessage msg) {
        try {
            for (Attachment attach : msg.getAttachments()) {
                AttachmentCardView attachmentCardView = new AttachmentCardView(this, null);
                attachmentCardView.setFileName(attach.getName());
                attachmentCardView.setSizeOrStatus(android.text.format.Formatter.formatShortFileSize(this, attach.getSize()));

                attachmentCardView.showRemoveIcon((View v) -> {
                    try {
                        msg.getAttachments().remove(attach);
                        attachmentsLayout.removeView(attachmentCardView);
                        saveDraft(false);
                    }catch(Exception e){
                        Utilities.generalCatchBlock(e, this);
                    }
                });

                attachmentsLayout.addView(attachmentCardView);
            }
        }catch (Exception e){
            Utilities.generalCatchBlock(e, this);
        }

    }

    private class FileAttach{
        private String fileName;
        private String size;
        private Uri uri;
        private FileAttach(String fileName, String size, Uri uri){
            this.fileName = fileName;
            this.size = size;
            this.uri = uri;
        }
    }

    /**
     * Code copied from https://developer.android.com/training/data-storage/shared/documents-files#examine-metadata
     * Retrieves the meta data of a file selected from the Storage Access Framework browser.
     * @param uri
     * @return
     */
    private FileAttach getDocumentData(Uri uri) {
        try (Cursor cursor = activity.getContentResolver()
                .query(uri, null, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {

                String displayName = cursor.getString(
                        cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));

                int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                String size = null;
                if (!cursor.isNull(sizeIndex)) {
                    size = cursor.getString(sizeIndex);
                } else {
                    size = getString(R.string.compose_unknown_attachment_size);
                }
                return new FileAttach(displayName, size, uri);
            }
        }catch (Exception e){Utilities.generalCatchBlock(e, this);}
        return null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

            menu.add(getText(R.string.compose_actionbar_attach))
                .setIcon(R.drawable.round_attachment_white_24)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS );

            menu.add(getText(R.string.compose_actionbar_send))
                    .setIcon(R.drawable.round_send_white_24)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS );

             menu.add(getText(R.string.compose_actionbar_save_draft))
                .setIcon(R.drawable.round_save_white_24)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

            menu.add(getText(R.string.compose_actionbar_cancel))
                    .setIcon(R.drawable.outline_cancel_white_24)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (item !=null) {
            if (item.getItemId() == android.R.id.home) {
                saveDraft(true);
            } else if (item.getTitle().equals(getText(R.string.compose_actionbar_send))) {
                sendMail();
            }else if (item.getTitle().equals(getText(R.string.compose_actionbar_save_draft))) {
                saveDraft(false);
            }
            else if (item.getTitle().equals(getText(R.string.compose_actionbar_cancel))) {
                exitActivity();
            }else if (item.getTitle().equals(getText(R.string.compose_actionbar_attach))) {
                Intent intent=null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                    intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.putExtra(EXTRA_ALLOW_MULTIPLE, true);
                   // intent.putExtra(EXTRA_INITIAL_URI, true);

                }else{
                    intent = new Intent(Intent.ACTION_GET_CONTENT);
                }
                    intent.setType("*/*");
                    startActivityForResult(intent, OPEN_DOCUMENT_REQ_CODE);
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        saveDraft(true);
    }

    private void exitActivity(){
        activity.finish();
        ApplyAnimation.setComposeActivityCloseAnim(activity);
    }

    /**
     * User is navigating out of page. Automatically save a draft. Alert the user if the saving draft fails.
     * @param exitPage - exits the page after saving
     */
    private void saveDraft(boolean exitPage) {
        //check if whether wants to exit the activity while already a save draft in progress
        if (exitPage && saveDraftLock.isLocked()){
            new AlertDialog.Builder(activity)
                    //set message, title, and icon
                    .setTitle(getString(R.string.compose_alert_confirm_cancel_title))
                    .setMessage(getString(R.string.compose_alert_confirm_cancel_saving))
                    .setPositiveButton(getString(R.string.alertdialog_positive_lbl), (dialog, whichButton) -> {
                        // User wants to exit the page even after knowing there is a save in progress. So cancel all the save draft runnables and close the page.
                         // This shutdown is crucial because, when the user exits this page and immediately open ComposeActivity again, the previous thread will keep on saving the message and the new page "msg"
                            // object is getting overwritten. So do a clean exit and clear all the runnables from this page.

                        // Currently the shutdownNow is not working as expected. The save draft thread is not getting interrupted as expected.
                        //Bug: https://trello.com/c/LP5lxUXj

                        saveDraftExecutorService.shutdownNow();
                        exitActivity();
                    })
                    .setNegativeButton(getString(R.string.alertdialog_negative_lbl), (dialog, which) -> dialog.dismiss())
                    .create().show();
            return;
        }
        //handler for bind msg call
        Handler h = new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(Message m) {
                switch(m.what){
                    case 0:// save draft started
                        progressDispBar.setText(getString(R.string.compose_saving_draft));
                        progressDispBar.showStatusBar();
                        break;
                    case 1:// save draft success
                        progressDispBar.hideProgressBar();
                        progressDispBar.setText(getString(R.string.compose_statusbar_save_success));
                        if(exitPage) {
                            Notifications.showToast(activity, activity.getString(R.string.compose_save_draft_success));
                            exitActivity();
                        }else{
                            new Handler().postDelayed(()-> {
                                if (!saveDraftLock.isLocked()) {  //this means already a save draft is in progress by another thread.
                                    progressDispBar.hideStatusBar();
                                }else{
                                    Log.d(LOG_TAG, "ComposeActivity -> saveDraftThread - not hiding the status bar because another save draft is running");
                                }

                            },2000);
                        }
                        break;
                    case 2://error
                        progressDispBar.hideProgressBar();
                        progressDispBar.setText(getString(R.string.compose_statusbar_save_failure));
                        if (exitPage) {
                            AlertDialog myConfirmBox = new AlertDialog.Builder(activity)
                                    //set message, title, and icon
                                    .setTitle(getString(R.string.compose_alert_confirm_cancel_title))
                                    .setMessage(getString(R.string.compose_alert_confirm_cancel_msg))
                                    .setPositiveButton(getString(R.string.alertdialog_positive_lbl), (dialog, whichButton) -> exitActivity())
                                    .setNegativeButton(getString(R.string.alertdialog_negative_lbl), (dialog, which) -> dialog.dismiss())
                                    .create();
                            myConfirmBox.show();
                        }else{
                            new Handler().postDelayed(()-> {
                                if (!saveDraftLock.isLocked()) {  //this means already a save draft is in progress by another thread.
                                    progressDispBar.hideStatusBar();
                                }else{
                                    Log.d(LOG_TAG, "ComposeActivity -> saveDraftThread - not hiding the status bar because another save draft is running");
                                }

                            },2000);
                        }
                        break;
                }
            }
        };
        saveDraftExecutorService.submit(getSaveDraftThread(h));
    }
    private Runnable getSaveDraftThread(Handler h){
        updateAllValues();
        return new Runnable() {
            @Override
            public void run() {
                try {
                    Log.d(LOG_TAG, "ComposeActivity -> saveDraftThread -> acquiring lock..");
                    saveDraftLock.lock();
                    Log.d(LOG_TAG, "ComposeActivity -> saveDraftThread -> acquired lock");
                    h.sendEmptyMessage(0);

                    NetworkCall.saveDraft(activity,  msg, existingDraft, to, cc,bcc, subject, body);

                    existingDraft=true; //when saving the draft multiple times, save() or update() will be called correspondingly based on this bool.

                    h.sendEmptyMessage(1);
                }catch(Exception e){
                    Utilities.generalCatchBlock(e, this);
                    h.sendEmptyMessage(2);
                }finally {
                    Log.d(LOG_TAG, "ComposeActivity -> saveDraftThread -> releasing lock");
                    saveDraftLock.unlock();
                }

            }
        };

    }

    @Override
    //called by resolvenames while started resolving a name
    public void handleResolvingNames() {
        TOTAL_RESOLVE_NAMES++;
        resolvingNames=true;
        setSupportProgressBarIndeterminateVisibility(true);
        progressDispBar.showStatusBar();
        progressDispBar.setText(getString(R.string.compose_resolving_names_text));
    }

    @Override
    public void handleResolveNamesOutput(NameResolutionCollection outputCollection, String extra1) {

        try {
            if(outputCollection!= null && outputCollection.getCount()==1){
                Contact contact = outputCollection.nameResolutionCollection(0).getContact();
                String email = outputCollection.nameResolutionCollection(0).getMailbox().getAddress();
                if(BuildConfig.DEBUG) {
                    Log.d(LOG_TAG, "ComposeActivity - > handleResolveNamesOutput() -> Extramessage " + extra1);
                }
                if(extra1!=null && (extra1.length() ==2 || extra1.length() ==3)){
                    int type=Character.getNumericValue(extra1.charAt(0));
                    int id;
                    if(extra1.length() == 2){
                        id=Character.getNumericValue(extra1.charAt(1));
                    }
                    else{
                        id=extra1.charAt(1) + extra1.charAt(2) ;
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
                    else {
                        id= extra1.charAt(1) + extra1.charAt(2);
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

    private boolean isResponseMsg(){
        if (prefill_type == PREFILL_TYPE_REPLY ||
                prefill_type == PREFILL_TYPE_REPLY_ALL ||
                prefill_type == PREFILL_TYPE_FORWARD) {
            return true;
        }
        return false;
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
            progressDispBar.setText(getString(R.string.compose_resolving_names_text));
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

