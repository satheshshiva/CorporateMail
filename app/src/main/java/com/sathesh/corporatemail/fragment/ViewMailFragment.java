/**
 *
 */
package com.sathesh.corporatemail.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.sathesh.corporatemail.BuildConfig;
import com.sathesh.corporatemail.R;
import com.sathesh.corporatemail.activity.ComposeActivity;
import com.sathesh.corporatemail.activity.ContactDetailsActivity;
import com.sathesh.corporatemail.activity.MailListViewActivity;
import com.sathesh.corporatemail.adapter.ComposeActivityAdapter;
import com.sathesh.corporatemail.application.MailApplication;
import com.sathesh.corporatemail.application.MyActivity;
import com.sathesh.corporatemail.application.SharedPreferencesAdapter;
import com.sathesh.corporatemail.cache.adapter.CachedMailHeaderAdapter;
import com.sathesh.corporatemail.constants.Constants;
import com.sathesh.corporatemail.customserializable.ContactSerializable;
import com.sathesh.corporatemail.ews.MailFunctions;
import com.sathesh.corporatemail.ews.MailFunctionsImpl;
import com.sathesh.corporatemail.fragment.datapasser.ViewMailFragmentDataPasser;
import com.sathesh.corporatemail.handlers.LoadEmailHandler;
import com.sathesh.corporatemail.jsinterfaces.CommonWebChromeClient;
import com.sathesh.corporatemail.sqlite.db.cache.vo.CachedMailHeaderVO;
import com.sathesh.corporatemail.threads.ui.LoadEmailThread;
import com.sathesh.corporatemail.ui.components.ProgressDisplayNotificationBar;
import com.sathesh.corporatemail.ui.listeners.ViewMailListener;
import com.sathesh.corporatemail.util.Utilities;
import com.sathesh.corporatemail.web.StandardWebView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import microsoft.exchange.webservices.data.core.service.item.EmailMessage;

/**
 * @author sathesh
 *
 */
public class ViewMailFragment extends Fragment implements Constants, ViewMailFragmentDataPasser{

    public MyActivity activity ;
    private Context context ;

    private TextView subjectIdView, collapsedFromIdView,expandedFromIdView, toIdView, ccIdView, expandedDateIdView, collapsedDateIdView, ccLbl ;
    private StandardWebView standardWebView ;
    private WebView webview;
    private ProgressDisplayNotificationBar progressStatusDispBar;
    private EmailMessage message;

    private CachedMailHeaderVO mailHeader;

    public enum Status{
        LOADING,	// Started loading body. Network Call for loading body is in progress
        SHOW_BODY,	// Network call made for body and got the body. Refreshe the body in UI
        SHOW_IMG_LOADING_PROGRESSBAR,	// Inline images are present. Show the status bar for downloading images
        DOWNLOADED_AN_IMAGE,	// Triggered each time an image got downloaded. Body gets refreshed so that the newly downloaded image will be displayed
        LOADED,		// Everything loaded. Will call read email network call. after this
        ERROR	// Error
    }

    private String from="", to="",cc="",bcc="", subject="", itemId="";
    private String mailFolderName="";
    private String mailFolderId="";
    private Date date;

    private boolean toShowMoreFlag=false;

    private boolean ccShowMoreFlag=false;
    private List<ContactSerializable> fromReceivers;
    private List<ContactSerializable> toReceivers;
    private List<ContactSerializable> ccReceivers;
    private List<ContactSerializable> bccReceivers;

    private boolean isToExist=false;
    private boolean isCCExist=false;
    private boolean isBCCExist=false;

    private int mailType;
    private MailFunctions mailFunctions = new MailFunctionsImpl();

    private int totalInlineImages;

    private String processedHtml="";
    private int remainingInlineImages=0;
    private Status currentStatus;
    private ViewMailListener viewMailListener;
    private CachedMailHeaderAdapter cachedMailHeaderAdapter;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_view_mail,
                container, false);

        activity = (MyActivity) getActivity();
        context = getActivity();

        setRetainInstance(true);

        if(cachedMailHeaderAdapter==null){
            cachedMailHeaderAdapter = new CachedMailHeaderAdapter(context);
        }
        //Initialize toolbar
        MailApplication.toolbarInitialize(activity, view);

        progressStatusDispBar = new ProgressDisplayNotificationBar(activity,view);
        //TODO see whether this works fine

        //listener for this frament and activity
        viewMailListener = new ViewMailListener(this);

        //if(customTitleSupported)
        //	getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, CustomTitleBar.getViewMailTitleBarLayout());
        standardWebView= new StandardWebView();

        subjectIdView = (TextView)view.findViewById(R.id.subject);
        collapsedFromIdView = (TextView)view.findViewById(R.id.collapsedFrom);
        expandedFromIdView = (TextView)view.findViewById(R.id.expandedFrom);
        toIdView = (TextView)view.findViewById(R.id.expandedTo);
        ccIdView = (TextView)view.findViewById(R.id.expandedCc);
        ccLbl = (TextView)view.findViewById(R.id.expandedCcLbl);
        expandedDateIdView = (TextView)view.findViewById(R.id.expandedDate);
        collapsedDateIdView = (TextView)view.findViewById(R.id.collapsedDate);
        //titleBarSubject = (TextView)findViewById(R.id.titlebar_viewmail_sub) ;

        webview = (WebView)view.findViewById(R.id.webview);

        WebSettings webSettings = webview.getSettings();
        webSettings.setAllowFileAccess(true);

        webSettings.setJavaScriptEnabled(true);	//this is important
        // The Android system will hanldle image caches for the webview
        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);
        //webSettings.setLoadWithOverviewMode(true);
        //webSettings.setUseWideViewPort(true);

        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //  webview.loadDataWithBaseURL("fake:///ghj/", "", "text/html", "utf-8", null);

        webview.setWebChromeClient(new CommonWebChromeClient());
        //		webview.setWebViewClient(new WebViewClient(){
        //
        //			@Override
        //			public void onLoadResource (WebView view, String url){
        //				//Log.i(TAG, "View MailAcivity -> URL Loading " +url);
        //
        //			}
        //		});

        //load email
        mailHeader = (CachedMailHeaderVO) activity.getIntent().getSerializableExtra(MailListViewActivity.EXTRA_MESSAGE_CACHED_HEADER);

        if(mailHeader !=null){
            from = mailHeader.getMail_from();
            to = mailHeader.getMail_to();
            cc = mailHeader.getMail_cc();
            bcc = mailHeader.getMail_bcc();
            subject = mailHeader.getMail_subject();
            mailType=mailHeader.getMail_type();
            mailFolderName= mailHeader.getFolder_name();
            mailFolderId=mailHeader.getFolder_id();
            itemId=mailHeader.getItem_id();
        }

        if(BuildConfig.DEBUG){
            Log.d(LOG_TAG, "ViewMailFragment -> from " + from + " to " + to  + " cc " + cc + " bcc " + bcc + " subject " + subject);
        }
        try {
            date = mailHeader.getMail_datetimereceived();
        } catch (Exception e) {
            e.printStackTrace();
        }

        //shows the from, to etc., which we got from the intent extra
        showHeaders();

        loadEmail();

        return view;
    }

    @Override
    public void onDetach()  {
        super.onDetach();
    }

    /** This will mark this mail as read in cache
     *
     */
    public void mailAsReadInCache(){
        //Mark the item as read
        try {
            cachedMailHeaderAdapter.markMailAsReadUnread(mailHeader.getItem_id(), true);
        } catch (Exception e) {
            Utilities.generalCatchBlock(e, this);
        }
    }
    /**
     *
     */
    private void loadEmail() {
        LoadEmailHandler loadEmailHandler = new LoadEmailHandler(this);
        if(currentStatus==null){
            Thread t = new LoadEmailThread(this, loadEmailHandler);
            t.start();
        }
        //when config change (Screen rotation , the activity will be recreated and the UI have to be updated based on previous activity status
        else if(currentStatus==Status.LOADING){
            //it wont be null when there is a config change(Screen rotation)
            webview.loadUrl(LOADING_HTML_URL);
        }
        else if(currentStatus==Status.SHOW_IMG_LOADING_PROGRESSBAR
                || currentStatus==Status.DOWNLOADED_AN_IMAGE
                ){
            progressStatusDispBar.showStatusBar();
            loadEmailHandler.updateProgressBarLabel(remainingInlineImages,totalInlineImages);
            showBody(processedHtml);
        }
        else if(currentStatus==Status.LOADED){
            showBody(processedHtml);
        }
        else if (currentStatus==Status.ERROR){
            standardWebView.loadData(webview, VIEW_MAIL_ERROR_HTML);
        }
    }

    @Override
    public void forwardMail()  throws Exception{
        Bundle toBundle = new Bundle();
        Bundle ccBundle = new Bundle();
        Bundle bccBundle = new Bundle();

        String replySubject="";

        //subject
        if(subject != null ){
            if(subject.toUpperCase().startsWith(getString(R.string.compose_forward_subject_prefix).toUpperCase())){
                //subject already starts with "FW:"
                replySubject = subject;

            }
            else if(subject.toUpperCase().startsWith(getString(R.string.compose_reply_subject_prefix).toUpperCase())){
                //if subject starts with FW: then replace with RE:
                replySubject =getString(R.string.compose_forward_subject_prefix) + subject.substring(3); //will eliminate the RE: text since this is reply
            }
            else{
                //prefixing FW: to subject
                replySubject =getString(R.string.compose_forward_subject_prefix) + " "  + subject ;
            }
        }
        if(BuildConfig.DEBUG){
            Log.d(LOG_TAG, "quote in ViewMailFragment " + processedHtml);
        }
        ComposeActivityAdapter.startForward(context, ComposeActivity.PREFILL_TYPE_FORWARD, itemId, toBundle, ccBundle, bccBundle, replySubject, replySubject, true, processedHtml);
    }

    @Override
    public void replyMail(boolean replyAll) throws Exception{
        Bundle toBundle = new Bundle();
        Bundle ccBundle = new Bundle();
        Bundle bccBundle = new Bundle();
        String replySubject="", displayName="";

        //Prefill To
        ContactSerializable fromContact=null;
        //prepare the toBundle for reply with the "from" String
        List<ContactSerializable> fromList = MailApplication.getContactsFromDelimitedString(from);
        if(fromList.size() > 0 ) {
            fromContact = fromList.get(0);
        }
        toBundle.putSerializable(fromContact.getEmail(), fromContact);  //key,value

        //Prefill CC with to
        if(replyAll){
            if(isToExist){
                //prepare the ccBundle for reply with the "to" String
                if(BuildConfig.DEBUG) {
                    Log.d(LOG_TAG, "toReceivers");
                    Log.d(LOG_TAG, toReceivers.toString());
                }
                //if there are more than 1 To receivers then add them to CC except the same person
                for(ContactSerializable toContact : toReceivers){
                    displayName = toContact.getDisplayName();
                    if(BuildConfig.DEBUG) {
                        Log.d(LOG_TAG, "ViewMailFragment -> Receiver " + displayName);
                    }
                    if(displayName!=null){
                        displayName = displayName.trim();
                        //if the "to" has the same logged in person then skip it
                        if(!displayName.equals(MailApplication.getUserDisplayName(context))){
                            ccBundle.putSerializable(toContact.getEmail(), toContact);  //key,value
                        }
                        else{
                            if(BuildConfig.DEBUG) {
                                Log.d(LOG_TAG, "ViewMailFragment -> Skipped adding the logged in user from To to CC in new mail");
                            }
                        }
                    }
                    else{
                        Log.e(LOG_TAG, "ViewMailFragment -> Receiver " + displayName + " is null");
                    }
                }
            }

            //Prefill CC with cc
            //add ccBundle from String "cc"
            if(isCCExist){
                for(ContactSerializable ccContact : ccReceivers){
                    displayName = ccContact.getDisplayName();
                    if(displayName!=null){
                        displayName = displayName.trim();
                        if(!displayName.equals(MailApplication.getUserDisplayName(context))){
                            ccBundle.putSerializable(ccContact.getEmail(), ccContact);  //key,value
                        }else{
                            if(BuildConfig.DEBUG) {
                                Log.d(LOG_TAG, "ViewMailFragment -> Skipped adding the logged in user from CC to CC in new mail");
                            }
                        }
                    }
                    else{
                        Log.e(LOG_TAG, "ViewMailFragment -> Receiver "+ displayName + " is null");
                    }
                }
            }

            //Prefill BCC
            //prepare bccBundle from string "bcc"
            if(isBCCExist){
                for(ContactSerializable bccContact : bccReceivers){
                    displayName = bccContact.getDisplayName();
                    if(displayName!=null){
                        displayName = displayName.trim();
                        if(!displayName.equals(MailApplication.getUserDisplayName(context))){
                            bccBundle.putSerializable(displayName, bccContact);
                        }else{
                            if(BuildConfig.DEBUG) {
                                Log.d(LOG_TAG, "ViewMailFragment -> Skipped adding the logged in user in BCC");
                            }
                        }
                    }
                    else{
                        Log.e(LOG_TAG, "ViewMailFragment -> Receiver "+ displayName + " is null");
                    }
                }
            }
        }
        //subject
        if(subject != null ){
            if(!(subject.toUpperCase().startsWith(getString(R.string.compose_reply_subject_prefix).toUpperCase()))){
                //if not subject starts with "RE:"
                replySubject =getString(R.string.compose_reply_subject_prefix) + " "  + subject ;
            }
            else{
                //subject starts with "RE:"
                replySubject = subject;
            }

            if(subject.toUpperCase().startsWith(getString(R.string.compose_forward_subject_prefix).toUpperCase())){
                //if subject starts with FW: then replace with RE:
                replySubject =getString(R.string.compose_reply_subject_prefix) + subject.substring(3); //will eliminate the FW: text since this is reply
            }
        }

        if(BuildConfig.DEBUG) {
            Log.d(LOG_TAG, "quote in ViewMailFragment  " + processedHtml);
        }

        if(!(replyAll)){
            ComposeActivityAdapter.startReply(context, ComposeActivity.PREFILL_TYPE_REPLY, itemId, toBundle, ccBundle, bccBundle, replySubject, replySubject, true, processedHtml);
        }
        else{
            ComposeActivityAdapter.startReply(context, ComposeActivity.PREFILL_TYPE_REPLY_ALL, itemId,  toBundle, ccBundle, bccBundle, replySubject, replySubject, true, processedHtml);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        boolean doNotRateAppFlag=false;
        int counterOpenedMails=0;
        //increase the counter to check the rate app
        try {
            //increase the opened mail counter by 1.
            counterOpenedMails=SharedPreferencesAdapter.getCounterOpenedEmails(context);
            counterOpenedMails++;
            SharedPreferencesAdapter.storeCounterOpenedMails(context, counterOpenedMails);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if(showRateApp){
            //check whether to show rate app dialog
            try {
                doNotRateAppFlag=SharedPreferencesAdapter.getDoNotRateApp(activity);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if(!doNotRateAppFlag){
                //show the rate app dialog for every 20 emails opened
                if(counterOpenedMails % RATE_APP_DIALOG_OPEN_MAIL_FREQUENCY==0){
                    MailApplication.showRateAppDialog(activity);
                }
            }
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
		/*boolean success=false;
		try {
			File cacheDir = new File(getCacheImageDirectory(message));
			if(cacheDir.exists()){
				success=Utilities.deleteDirectory(cacheDir);
				Log.d(TAG, "Deleting image cache directory " +((success)?"successful":"failed"));
			}
		} catch (Exception e) {
			Log.d(TAG, "Exception while deleting cache" + e.getMessage());
			e.printStackTrace();
		}*/


    }

    public void displayHeadersAndBody(){

        try{
            showHeaders();

            //body
            showBody(processedHtml);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Private method - Displays the Headers from, to,etc.,
     *
     */
    private void showHeaders() {

        try {
            //convert "from" delimitered text to from list of ContactSerializable
            if(from!=null && !(from.equals(""))){
                isToExist=true;
                fromReceivers= MailApplication.getContactsFromDelimitedString(from);
            }

            //convert "to" delimitered text to "to" list of ContactSerializable
            if(to!=null && !(to.equals(""))){
                isToExist=true;
                toReceivers= MailApplication.getContactsFromDelimitedString(to);
            }
            else{
                isToExist=false;
            }

            //convert "cc" delimitered text to "cc" list of ContactSerializable
            if(cc!=null && !(cc.equals(""))){
                isCCExist=true;
                ccReceivers= MailApplication.getContactsFromDelimitedString(cc);
            }
            else{
                isCCExist=false;
            }
            if(bcc!=null && !(bcc.equals(""))){
                isBCCExist=true;
                bccReceivers= MailApplication.getContactsFromDelimitedString(bcc);
                //	BCC_ViewMail_LinearLayout.setVisibility(View.VISIBLE);
            }
            else{
                isBCCExist=false;
            }

            //subject
            if(subject!=null && !(subject.equals(""))){
                subjectIdView.setText(subject);
            }
            else{
                subjectIdView.setText("");
            }
            // From
            if (fromReceivers.size() > 0) {
                buildHeaderText(expandedFromIdView, fromReceivers, null);
                buildHeaderText(collapsedFromIdView, fromReceivers, null);
            }else{
                collapsedFromIdView.setText("");
                expandedFromIdView.setText("");
            }

            // To
            if(isToExist){
                //show all To contacts
                buildHeaderText(toIdView, toReceivers, null);
            }else{
                toIdView.setText("");
            }

            //CC
            if(isCCExist){
                buildHeaderText(ccIdView, ccReceivers, null);
            }else{
                ccIdView.setVisibility(View.GONE);
                ccLbl.setVisibility(View.GONE);
            }
            //date
            if(date!=null){
                expandedDateIdView.setText((new SimpleDateFormat(VIEW_MAIL_DATE_FORMAT)).format(date.getTime()));
                collapsedDateIdView.setText(MailApplication.getShortDate(date));
            }else{
                expandedDateIdView.setText("");
                collapsedDateIdView.setText("");
            }
        }catch(Exception e){
            Utilities.generalCatchBlock(e, this);
        }

    }

    public void showBody(String html1){

        if(null!=html1 && !(html1.equals(""))){
            if(BuildConfig.DEBUG) {
                Log.d(LOG_TAG, "Loading html " + html1);
            }

            //webview.loadData(dispBody, CommonWebChromeClient.MIME_TYPE_HTML,CommonWebChromeClient.ENCODING);
            standardWebView.loadData(webview, html1);

        }
        else{
            //webvew.loadData(VIEW_MAIL_WEBVIEW_BODY_NO_CONTENT, StandardWebView.MIME_TYPE_HTML,StandardWebView.ENCODING);
            standardWebView.loadData(webview, VIEW_MAIL_WEBVIEW_BODY_NO_CONTENT);
        }
    }

    /** Build the headers display with links
     *
     * @param textView  - TextView
     * @param contacts  - list of ContactSerializables to display
     * @param max   - the no of entries to display
     */
    public void buildHeaderText(TextView textView, List<ContactSerializable> contacts, Integer max) {
        SpannableStringBuilder sBuilder = new SpannableStringBuilder();
        int initLength=0;
        int counter = 0;
        String nonLinkText="";
        //get the current status whether the mails is in loading state
        boolean statusLoadedMail= (currentStatus!=null &&
                    currentStatus != Status.LOADING
                    && currentStatus != Status.ERROR);

        //loop thorugh each contact
        while(counter<contacts.size()){
            final ContactSerializable contact = contacts.get(counter);
            initLength = sBuilder.length();

            //if the mail is loaded then build the link for the contact
            if(statusLoadedMail) {
                sBuilder.append(contact.getDisplayName() + EMAIL_DELIMITER_DISP);

                //click action to perform when clicked
                sBuilder.setSpan(new ClickableSpan() {
                    //on click of the link open the Contacts display
                    @Override
                    public void onClick(View arg0) {
                        if (BuildConfig.DEBUG) {
                            Log.d(LOG_TAG, contact + " clicked");
                        }
                        Intent contactDetailsIntent = new Intent(context, ContactDetailsActivity.class);
                        contactDetailsIntent.putExtra(ContactDetailsActivity.CONTACT_SERIALIZABLE_EXTRA, contact);
                        startActivity(contactDetailsIntent);
                    }
                }, initLength, sBuilder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                   //length determines the length of string to make as clickables
            }
            //if the mail is in loading or error state then just display the contact witout link
            else{
                nonLinkText = contact.getDisplayName() + EMAIL_DELIMITER_DISP;
            }

            counter++;

            //if the max no of contacts is given then break out of the loop
            if (max != null && counter >= max) {
                break;
            }
        }   //end while loop
        //set the textview with the StringBuilder

        //loads the text link in to textview
        if(statusLoadedMail) {
           textView.setText(sBuilder);
           textView.setMovementMethod(LinkMovementMethod.getInstance());
        }
        //loads the text in to textview
        else{
           textView.setText(nonLinkText);
        }
    }

    /*** GETTER SETTER ***/

    public String getProcessedHtml() {
        return processedHtml;
    }

    public void setProcessedHtml(String processedHtml) {
        this.processedHtml = processedHtml;
    }

    public int getRemainingInlineImages() {
        return remainingInlineImages;
    }

    public void setRemainingInlineImages(int remainingInlineImages) {
        this.remainingInlineImages = remainingInlineImages;
    }

    @Override
    public Status getCurrentStatus() {
        return currentStatus;
    }

    public void setCurrentStatus(Status currentStatus) {
        this.currentStatus = currentStatus;
    }

    @Override
    public int getMailType() {
        return mailType;
    }

    public void setMailType(int mailType) {
        this.mailType = mailType;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getCc() {
        return cc;
    }

    public void setCc(String cc) {
        this.cc = cc;
    }

    public String getBcc() {
        return bcc;
    }

    public void setBcc(String bcc) {
        this.bcc = bcc;
    }
    @Override
    public CachedMailHeaderVO getCachedMailHeaderVO() {
        return mailHeader;
    }


    public MailFunctions getMailFunctions() {
        return mailFunctions;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }


    public TextView getCollapsedFromIdView() {
        return collapsedFromIdView;
    }

    public void setCollapsedFromIdView(TextView collapsedFromIdView) {
        this.collapsedFromIdView = collapsedFromIdView;
    }

    public TextView getToIdView() {
        return toIdView;
    }

    public void setToIdView(TextView toIdView) {
        this.toIdView = toIdView;
    }

    public TextView getCcIdView() {
        return ccIdView;
    }

    public void setCcIdView(TextView ccIdView) {
        this.ccIdView = ccIdView;
    }
    public ProgressDisplayNotificationBar getProgressStatusDispBar() {
        return progressStatusDispBar;
    }

    public void setProgressStatusDispBar(
            ProgressDisplayNotificationBar progressStatusDispBar) {
        this.progressStatusDispBar = progressStatusDispBar;
    }
    public StandardWebView getStandardWebView() {
        return standardWebView;
    }

    public void setStandardWebView(StandardWebView standardWebView) {
        this.standardWebView = standardWebView;
    }

    public WebView getWebview() {
        return webview;
    }

    public void setWebview(WebView webview) {
        this.webview = webview;
    }
    public int getTotalInlineImages() {
        return totalInlineImages;
    }

    public void setTotalInlineImages(int totalInlineImages) {
        this.totalInlineImages = totalInlineImages;
    }

    public CachedMailHeaderVO getMailHeader() {
        return mailHeader;
    }

    public void setMailHeader(CachedMailHeaderVO mailHeader) {
        this.mailHeader = mailHeader;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
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

    @Override
    public EmailMessage getMessage() {
        return message;
    }

    public void setMessage(EmailMessage message) {
        this.message = message;
    }

    @Override
    public String getItemId() {
        return itemId;
    }
    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public List<ContactSerializable> getToReceivers() {
        return toReceivers;
    }

    public void setToReceivers(List<ContactSerializable> toReceivers) {
        this.toReceivers = toReceivers;
    }

    public List<ContactSerializable> getCcReceivers() {
        return ccReceivers;
    }

    public void setCcReceivers(List<ContactSerializable> ccReceivers) {
        this.ccReceivers = ccReceivers;
    }

    public List<ContactSerializable> getBccReceivers() {
        return bccReceivers;
    }

    public void setBccReceivers(List<ContactSerializable> bccReceivers) {
        this.bccReceivers = bccReceivers;
    }

    public List<ContactSerializable> getFromReceivers() {
        return fromReceivers;
    }

    public void setFromReceivers(List<ContactSerializable> fromReceivers) {
        this.fromReceivers = fromReceivers;
    }
}