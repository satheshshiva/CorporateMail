/**
 *
 */
package com.sathesh.corporatemail.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.google.android.flexbox.FlexboxLayout;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.sathesh.corporatemail.BuildConfig;
import com.sathesh.corporatemail.R;
import com.sathesh.corporatemail.activity.ComposeActivity;
import com.sathesh.corporatemail.activity.ContactDetailsActivity;
import com.sathesh.corporatemail.adapter.ComposeActivityAdapter;
import com.sathesh.corporatemail.application.MailApplication;
import com.sathesh.corporatemail.application.MyActivity;
import com.sathesh.corporatemail.application.SharedPreferencesAdapter;
import com.sathesh.corporatemail.cache.adapter.CachedMailHeaderAdapter;
import com.sathesh.corporatemail.constants.Constants;
import com.sathesh.corporatemail.customserializable.ContactSerializable;
import com.sathesh.corporatemail.customui.AttachmentCardView;
import com.sathesh.corporatemail.customui.Notifications;
import com.sathesh.corporatemail.datamodels.FileAttachmentMeta;
import com.sathesh.corporatemail.ews.MailFunctions;
import com.sathesh.corporatemail.ews.MailFunctionsImpl;
import com.sathesh.corporatemail.files.AttachmentsManager;
import com.sathesh.corporatemail.fragment.datapasser.ViewMailFragmentDataPasser;
import com.sathesh.corporatemail.handlers.LoadEmailHandler;
import com.sathesh.corporatemail.jsinterfaces.CommonWebChromeClient;
import com.sathesh.corporatemail.sqlite.db.cache.vo.CachedMailHeaderVO;
import com.sathesh.corporatemail.threads.ui.LoadEmailThread;
import com.sathesh.corporatemail.ui.components.ProgressDisplayNotificationBar;
import com.sathesh.corporatemail.ui.util.UIutilities;
import com.sathesh.corporatemail.util.Utilities;
import com.sathesh.corporatemail.web.StandardWebView;

import java.io.File;
import java.io.FileOutputStream;
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

    private TextView subjectIdView, expandedDateIdView, collapsedDateIdView, toLbl, ccLbl ;
    private StandardWebView standardWebView ;
    private WebView webview;
    private ProgressDisplayNotificationBar progressStatusDispBar;
    private EmailMessage message;
    private ConstraintLayout moreHeadersLayout;

    private CachedMailHeaderVO mailHeaderVo;

    private List<FileAttachmentMeta> attachmentsMeta;
    private boolean expanded;

    public enum Status{
        LOADING,	// Started loading body. Network Call for loading body is in progress
        SHOW_BODY,	// Network call made for body and got the body. Refreshe the body in UI
        SHOW_ATTACHMENTS, //Displays the attachments card view
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
    private CachedMailHeaderAdapter cachedMailHeaderAdapter;
    private ImageButton expandBtn;
    private ChipGroup collapsedfromChipGrp, expandedFromChipGrp, expandedToChipGrp, expandedCcChipGrp ;
    private FlexboxLayout attachmentsLayout;
    private View view;


    public ViewMailFragment(CachedMailHeaderVO mailHeaderVo){
        this.mailHeaderVo = mailHeaderVo;
    }
    private boolean zooming;
    @SuppressLint({"SetJavaScriptEnabled", "ClickableViewAccessibility"})
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

        progressStatusDispBar = new ProgressDisplayNotificationBar(activity,view);

        //if(customTitleSupported)
        //	getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, CustomTitleBar.getViewMailTitleBarLayout());
        standardWebView= new StandardWebView();

        subjectIdView = (TextView)view.findViewById(R.id.subject);
        collapsedfromChipGrp = (ChipGroup) view.findViewById(R.id.collapsedFromChipGrp);
        expandedFromChipGrp = (ChipGroup)view.findViewById(R.id.expandedFromChipGrp);
        expandedToChipGrp = (ChipGroup)view.findViewById(R.id.expandedToChipGrp);
        expandedCcChipGrp = (ChipGroup)view.findViewById(R.id.expandedCcChipGrp);
        toLbl = (TextView)view.findViewById(R.id.expandedToLbl);
        ccLbl = (TextView)view.findViewById(R.id.expandedCcLbl);
        expandedDateIdView = (TextView)view.findViewById(R.id.expandedDate);
        collapsedDateIdView = (TextView)view.findViewById(R.id.collapsedDate);
        moreHeadersLayout = (ConstraintLayout) view.findViewById(R.id.moreHeaders);
        //titleBarSubject = (TextView)findViewById(R.id.titlebar_viewmail_sub) ;
        expandBtn = (ImageButton) view.findViewById(R.id.expandBtn);
        attachmentsLayout =(FlexboxLayout) view.findViewById(R.id.view_mail_attachments_layout) ;

        moreHeadersLayout.setVisibility(View.GONE);

        webview = (WebView)view.findViewById(R.id.webview);
        WebSettings webSettings = webview.getSettings();
        webSettings.setAllowFileAccess(true);

        webSettings.setJavaScriptEnabled(true);	//this is important
        // The Android system will hanldle image caches for the webview
        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);

        /*nestedScrollview.setOnTouchListener((View view1, MotionEvent ev)->{
            mScaleDetector= new ScaleGestureDetector(context, new ScaleListener());
            // Let the ScaleGestureDetector inspect all events.
            mScaleDetector.onTouchEvent(ev);
            return false;
        });*/

        webview.setOnTouchListener((View v, MotionEvent event) -> {
            int action = event.getActionMasked();

            // Setting on Touch Listener for handling the touch inside ScrollView
                // Disallow the touch request for parent scroll on touch of child view
            if (action == MotionEvent.ACTION_POINTER_DOWN ) {
                zooming = true;
                return false;
            }

            if (action == MotionEvent.ACTION_POINTER_UP || action == MotionEvent.ACTION_CANCEL) {
                zooming = false;
            }

            if (zooming) {
                v.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            }

            return false;

        });

        webview.setWebChromeClient(new CommonWebChromeClient());

        if(mailHeaderVo !=null){
            from = mailHeaderVo.getMail_from();
            to = mailHeaderVo.getMail_to();
            cc = mailHeaderVo.getMail_cc();
            bcc = mailHeaderVo.getMail_bcc();
            subject = mailHeaderVo.getMail_subject();
            mailType= mailHeaderVo.getMail_type();
            mailFolderName= mailHeaderVo.getFolder_name();
            mailFolderId= mailHeaderVo.getFolder_id();
            itemId= mailHeaderVo.getItem_id();
        }

        if(BuildConfig.DEBUG){
            Log.d(LOG_TAG, "ViewMailFragment -> from " + from + " to " + to  + " cc " + cc + " bcc " + bcc + " subject " + subject);
        }
        try {
            date = mailHeaderVo.getMail_datetimereceived();
        } catch (Exception e) {
            e.printStackTrace();
        }

        //shows the from, to etc., which we got from the intent extra
        showHeaders();

        loadEmail();

        if (MailApplication.getInstance().isViewMailTransitionEnabled()) {
            //resume the enter activity transition which was postponed in the ViewMailActivity:onCreate()
            // the pause and resume was done so that the shared objects will be available.
            subjectIdView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    // Tell the framework to start.
                    subjectIdView.getViewTreeObserver().removeOnPreDrawListener(this);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && activity != null) {
                        // This code is tied to ViewMailActivity.java -> onCreate -> postponeEnterTransition()
                        activity.startPostponedEnterTransition();
                    }
                    return true;
                }
            });
        }
        this.view=view;
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
            cachedMailHeaderAdapter.markMailAsReadUnread(mailHeaderVo.getItem_id(), true);
        } catch (Exception e) {
            Utilities.generalCatchBlock(e, this);
        }
    }

    @Override
    public void expandBtnOnClick(View view) {
        expandBtn.setVisibility(View.GONE);
        moreHeadersLayout.setVisibility(View.VISIBLE);
        collapsedfromChipGrp.setVisibility(View.GONE);
        collapsedDateIdView.setVisibility(View.GONE);
        expanded=true;
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

    /**
     * Displays the attachments card view from the attachments meta
     */
    @Override
    public void showAttachments() {
        if(attachmentsLayout!=null) {
            for (FileAttachmentMeta attachmentMeta : attachmentsMeta) {
                AttachmentCardView attachmentCardView = new AttachmentCardView(context, null);
                attachmentCardView.setFileName(attachmentMeta.getFileName());
                attachmentCardView.setSizeOrStatus(attachmentMeta.getHumanReadableSize());
                attachmentCardView.setOnClickListener((View v)->{
                    //handler
                    Handler handler = new Handler(Looper.getMainLooper()){
                        @Override
                        public void handleMessage(Message msg) {
                            switch(msg.what){
                                case AttachmentsManager.DownloadAttachmentThread.DOWNLOAD_STARTED:
                                    attachmentCardView.showProgressBar();
                                    break;
                                case AttachmentsManager.DownloadAttachmentThread.DOWNLOAD_SUCCESS:
                                    attachmentCardView.hideProgressBar();
                                    File f = new File(msg.obj.toString());
                                    if (f.exists()) {
                                        try {
                                            Utilities.openFile(context, f);
                                        }catch(Exception e ){
                                            Notifications.showSnackBarShort(view, getString(R.string.attachment_open_error));
                                            Utilities.generalCatchBlock(e, this);
                                        }
                                    }else{
                                        Notifications.showSnackBarShort(view, getString(R.string.attachment_download_error));
                                    }
                                    break;
                                case AttachmentsManager.DownloadAttachmentThread.DOWNLOAD_ERROR:
                                    attachmentCardView.hideProgressBar();
                                    Notifications.showSnackBarShort(view, getString(R.string.attachment_download_error));
                                    break;
                                default:
                                    attachmentCardView.hideProgressBar();
                                    super.handleMessage(msg);
                            }
                        }
                    };
                    new AttachmentsManager.DownloadAttachmentThread(context, attachmentMeta, handler).start();
                });

                attachmentsLayout.addView(attachmentCardView);
            }
        }else{
            Log.e(LOG_TAG, "ViewMailFragment -> attachmentsLayout is null");
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
        expandBtn.setVisibility(expanded?View.GONE:View.VISIBLE);       //for a bug fix. Without this, when the expand btn once hidden by clicking it, when you open contact details from email and then when going back
                                                                                            //to the View mails page, this button again shows up for no reason.
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

    @Override
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
                subjectIdView.setText(R.string.noSubjectDisplay);
            }
            // From
            if (fromReceivers.size() > 0) {
                buildContactChips(expandedFromChipGrp, fromReceivers);
                buildContactChips(collapsedfromChipGrp, fromReceivers);
            }else{
                expandedFromChipGrp.removeAllViews();
            }

            // To
            if(isToExist){
                //show all To contacts
                buildContactChips(expandedToChipGrp, toReceivers);
            }else{
                expandedCcChipGrp.setVisibility(View.GONE);
                toLbl.setVisibility(View.GONE);
            }

            //CC
            if(isCCExist){
                buildContactChips(expandedCcChipGrp, ccReceivers);
            }else{
                expandedCcChipGrp.setVisibility(View.GONE);
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

    private void buildContactChips(ChipGroup chipGrp, List<ContactSerializable> fromReceivers) {
        Chip chip;
        chipGrp.removeAllViews();   // remove all the existing chips. This fn will be called once before loading and once after loading the email. So we should not create duplicate chips
        for(ContactSerializable contact: fromReceivers) {
            chip = new Chip(context);
            chip.setText(contact.getDisplayName());
            chip.setFocusable(true);
            ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT,
                    (int)activity.getResources().getDimension(R.dimen.view_mail_contact_chip_height));
            chip.setLayoutParams(params);
            chipGrp.addView(chip);
            //onclick listener for this chip.
            chip.setOnClickListener((View v)->{
                Intent contactDetailsIntent = new Intent(context, ContactDetailsActivity.class);
                contactDetailsIntent.putExtra(ContactDetailsActivity.CONTACT_SERIALIZABLE_EXTRA, contact);
                startActivity(contactDetailsIntent);
            });
        }
    }

    @Override
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

    /*** GETTER SETTER ***/

    @Override
    public MyActivity getMyActivity(){
        return activity;
    }
    public String getProcessedHtml() {
        return processedHtml;
    }

    public void setProcessedHtml(String processedHtml) {
        this.processedHtml = processedHtml;
    }

    @Override
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

    @Override
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

    public void setCc(String cc) {
        this.cc = cc;
    }

    public void setBcc(String bcc) {
        this.bcc = bcc;
    }
    @Override
    public CachedMailHeaderVO getCachedMailHeaderVO() {
        return mailHeaderVo;
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
    @Override
    public ProgressDisplayNotificationBar getProgressStatusDispBar() {
        return progressStatusDispBar;
    }

    @Override
    public StandardWebView getStandardWebView() {
        return standardWebView;
    }

    @Override
    public WebView getWebview() {
        return webview;
    }

    public void setWebview(WebView webview) {
        this.webview = webview;
    }
    @Override
    public int getTotalInlineImages() {
        return totalInlineImages;
    }

    public void setTotalInlineImages(int totalInlineImages) {
        this.totalInlineImages = totalInlineImages;
    }

    public CachedMailHeaderVO getMailHeaderVo() {
        return mailHeaderVo;
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

    @Override
    public void setAttachmentsMeta(List<FileAttachmentMeta> attachmentsMeta) {
        this.attachmentsMeta = attachmentsMeta;
    }
}