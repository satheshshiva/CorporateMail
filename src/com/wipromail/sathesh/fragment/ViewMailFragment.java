/**
 * 
 */
package com.wipromail.sathesh.fragment;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.wipromail.sathesh.BuildConfig;
import com.wipromail.sathesh.R;
import com.wipromail.sathesh.activity.ComposeActivity;
import com.wipromail.sathesh.activity.MailListViewActivity;
import com.wipromail.sathesh.adapter.ComposeActivityAdapter;
import com.wipromail.sathesh.application.MailApplication;
import com.wipromail.sathesh.application.SharedPreferencesAdapter;
import com.wipromail.sathesh.application.interfaces.ViewMailFragmentDataPasser;
import com.wipromail.sathesh.asynccaller.DeleteMailAsyncCaller;
import com.wipromail.sathesh.constants.Constants;
import com.wipromail.sathesh.customserializable.ContactSerializable;
import com.wipromail.sathesh.ews.MailFunctions;
import com.wipromail.sathesh.ews.MailFunctionsImpl;
import com.wipromail.sathesh.handlers.LoadEmailHandler;
import com.wipromail.sathesh.handlers.runnables.LoadEmailRunnable;
import com.wipromail.sathesh.jsinterfaces.CommonWebChromeClient;
import com.wipromail.sathesh.service.data.EmailAddress;
import com.wipromail.sathesh.service.data.EmailMessage;
import com.wipromail.sathesh.service.data.MessageBody;
import com.wipromail.sathesh.sqlite.db.pojo.vo.CachedMailHeaderVO;
import com.wipromail.sathesh.ui.ProgressDisplayNotificationBar;
import com.wipromail.sathesh.ui.listeners.ViewMailListener;
import com.wipromail.sathesh.web.StandardWebView;

/**
 * @author sathesh
 *
 */
public class ViewMailFragment extends Fragment implements Constants, ViewMailFragmentDataPasser{

	public SherlockFragmentActivity activity ;
	private Context context ;

	private TextView fromIdView ;
	private TextView toIdView ;
	private TextView cCIdView ;
	private TextView dateIdView;
	private Button toShowMoreBtn;
	private Button cCShowMoreBtn;
	private StandardWebView standardWebView ;
	private LinearLayout cc_LinearLayout;
	private WebView webview;
	private ProgressDisplayNotificationBar progressStatusDispBar;

	private CachedMailHeaderVO itemToOpen;

	public enum Status{
		LOADING,	// Started loading body. Network Call for loading body is in progress
		SHOW_BODY,	// Network call made for body and got the body. Refreshe the body in UI
		SHOW_IMG_LOADING_PROGRESSBAR,	// Inline images are present. Show the status bar for downloading images
		DOWNLOADED_AN_IMAGE,	// Triggered each time an image got downloaded. Body gets refreshed so that the newly downloaded image will be displayed
		LOADED,		// Everything loaded. Will call read email network call. after this
		ERROR	// Error
	}
	
	private EmailMessage message;

	private MessageBody msgBody=null;
	private String from="", to="",cc="",bcc="", subject="";
	private Date date;

	private boolean toShowMoreFlag=false;

	private boolean ccShowMoreFlag=false;
	private String[] toReceivers;
	private String[] ccReceivers;
	private String[] bccReceivers;

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


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.fragment_view_mail,
				container, false);

		activity = (SherlockFragmentActivity) getActivity();
		context = (SherlockFragmentActivity) getActivity();
		
		setRetainInstance(true);
		
		progressStatusDispBar = new ProgressDisplayNotificationBar(activity,view);
		//listener for this frament and activity
		viewMailListener = new ViewMailListener(this);
		
		//if(customTitleSupported)
		//	getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, CustomTitleBar.getViewMailTitleBarLayout());
		standardWebView= new StandardWebView();

		fromIdView = (TextView)view.findViewById(R.id.ViewMailFromId);
		toIdView = (TextView)view.findViewById(R.id.ViewMailToId);
		cCIdView = (TextView)view.findViewById(R.id.ViewMailCCId);
		dateIdView = (TextView)view.findViewById(R.id.ViewMailDateId);
		//titleBarSubject = (TextView)findViewById(R.id.titlebar_viewmail_sub) ;

		webview = (WebView)view.findViewById(R.id.view_mail_webview);

		WebSettings webSettings = webview.getSettings();
		webSettings.setAllowFileAccess(true);


		webSettings.setJavaScriptEnabled(true);	//this is important
		webSettings.setSupportZoom(true);
		webSettings.setBuiltInZoomControls(true);

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

		toShowMoreBtn =(Button)view.findViewById(R.id.ViewMailToShowMoreBtn);
		cCShowMoreBtn =(Button)view.findViewById(R.id.ViewMailCCShowMoreBtn);

		toShowMoreBtn.setOnClickListener(viewMailListener);
		cCShowMoreBtn.setOnClickListener(viewMailListener);
		
		cc_LinearLayout =(LinearLayout)view.findViewById(R.id.CC_ViewMail_LinearLayout);
		//load email
		itemToOpen = (CachedMailHeaderVO) activity.getIntent().getSerializableExtra(MailListViewActivity.EXTRA_MESSAGE_CACHED_HEADER);

		if(itemToOpen!=null){
			from = itemToOpen.getMail_from();
			to = itemToOpen.getMail_to();
			cc = itemToOpen.getMail_cc();
			bcc = itemToOpen.getMail_bcc();
			subject = itemToOpen.getMail_subject();
			setMailType(itemToOpen.getMail_type());
		}

		if(BuildConfig.DEBUG){
			Log.d(TAG, "ViewMailFragment -> from " + from + " to " + to  + " cc " + cc + " bcc " + bcc + " subject " + subject);
		}
		try {
			date = itemToOpen.getMail_datetimereceived();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//shows the from, to etc., which we got from the intent extra 
		showDetails();

		loadEmail();
		
		return view;
	}

	/**
	 * 
	 */
	private void loadEmail() {
		// TODO Auto-generated method stub
		LoadEmailHandler loadEmailHandler = new LoadEmailHandler(this);
		if(currentStatus==null){
		Thread t = new Thread(new LoadEmailRunnable(this, loadEmailHandler));
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
		// TODO Auto-generated method stub

		// TODO Auto-generated method stub
		ContactSerializable sContact;
		Bundle toBundle = new Bundle();
		Bundle ccBundle = new Bundle();
		Bundle bccBundle = new Bundle();

		String replyTo="";
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
		Log.d(TAG, "quote in ViewMaik Activiyt " + processedHtml);
		}
		ComposeActivityAdapter.startForward(context, ComposeActivity.PREFILL_TYPE_FORWARD, message.getId(), toBundle, ccBundle, bccBundle, replySubject, replySubject, true, processedHtml);
	}

	@Override
	public void replyMail(boolean replyAll) throws Exception{
		// TODO Auto-generated method stub
		ContactSerializable sContact;
		Bundle toBundle = new Bundle();
		Bundle ccBundle = new Bundle();
		Bundle bccBundle = new Bundle();

		String replyTo="";
		String replySubject="";

		// prefill to
		/*if(isToExist){

			for(String receiver : toReceivers){
				sContact = new ContactSerializable("", receiver, true);
				toBundle.putSerializable(sContact.getEmail(), sContact);
			}
		}*/

		//Prefill To

		EmailAddress sender =message.getSender();

		if(sender != null){
			sContact = new ContactSerializable(sender.getAddress(), sender.getName(), true);
			toBundle.putSerializable(sContact.getEmail(), sContact);
		}
		//Prefill cc
		if(replyAll){
			if(isToExist){
				Log.d(TAG, "toReceivers");
				Log.d(TAG, toReceivers.toString());
				//if there are more than 1 To receivers then add them to CC except the same person
				for(String receiver : toReceivers){
					Log.d(TAG, receiver);
					if(receiver!=null){
						receiver = receiver.trim();
						if(!receiver.equals(MailApplication.getUserDisplayName(context))){
							sContact = new ContactSerializable(receiver, receiver, true);
							Log.d(TAG, "ViewMailFragment -> Adding to as reciever" + sContact.getEmail());
							ccBundle.putSerializable(sContact.getEmail(), sContact);
						}
						else{
							Log.d(TAG, "ViewMailFragment -> Skipped adding the logged in user from To to CC in new mail");
						}
					}
					else{
						Log.e(TAG, "ViewMailFragment -> Receiver "+ receiver + " is null");
					}
				}
			}
			if(isCCExist){

				for(String receiver : ccReceivers){
					if(receiver!=null){
						receiver = receiver.trim();
						if(!receiver.equals(MailApplication.getUserDisplayName(context))){
							sContact = new ContactSerializable(receiver, receiver, true);
							Log.d(TAG, "ViewMailFragment -> Adding cc as cc reciever" + sContact.getEmail());
							ccBundle.putSerializable(sContact.getEmail(), sContact);
						}else{
							Log.d(TAG, "ViewMailFragment -> Skipped adding the logged in user from CC to CC in new mail");
						}

					}
					else{
						Log.e(TAG, "ViewMailFragment -> Receiver "+ receiver + " is null");
					}
				}
			}

			//Prefill bcc
			if(isBCCExist){

				for(String receiver : bccReceivers){
					if(receiver!=null){
						receiver = receiver.trim();
						if(!receiver.equals(MailApplication.getUserDisplayName(context))){
							sContact = new ContactSerializable(receiver, receiver, true);
							bccBundle.putSerializable(sContact.getEmail(), sContact);
						}else{
							Log.d(TAG, "ViewMailFragment -> Skipped adding the logged in user in BCC");
						}
					}
					else{
						Log.e(TAG, "Receiver "+ receiver + " is null");
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

		Log.d(TAG, "quote in ViewMaik Activiyt " + processedHtml);

		if(!(replyAll)){
			ComposeActivityAdapter.startReply(context, ComposeActivity.PREFILL_TYPE_REPLY, message.getId(), toBundle, ccBundle, bccBundle, replySubject, replySubject, true, processedHtml);
		}
		else{
			ComposeActivityAdapter.startReply(context, ComposeActivity.PREFILL_TYPE_REPLY_ALL, message.getId(),  toBundle, ccBundle, bccBundle, replySubject, replySubject, true, processedHtml);
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if(showRateApp){
			//check whether to show rate app dialog
			try {
				doNotRateAppFlag=SharedPreferencesAdapter.getDoNotRateApp(activity);
			} catch (Exception e) {
				// TODO Auto-generated catch block
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
			// TODO Auto-generated catch block
			Log.d(TAG, "Exception while deleting cache" + e.getMessage());
			e.printStackTrace();
		}*/
	}
	
	public void displayEverything(){

		try{
			showDetails();

			//body
			showBody(MessageBody.getStringFromMessageBody(msgBody));

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void showDetails() {
		// TODO Auto-generated method stub

		if(to!=null && !(to.equals(""))){
			isToExist=true;
			toReceivers= to.split(";");

		}
		else{
			isToExist=false;
		}
		if(cc!=null && !(cc.equals(""))){
			isCCExist=true;
			ccReceivers= cc.split(";");
			cc_LinearLayout.setVisibility(View.VISIBLE);
		}
		else{
			isCCExist=false;
		}
		if(bcc!=null && !(bcc.equals(""))){
			isBCCExist=true;
			bccReceivers= bcc.split(";");
			//	BCC_ViewMail_LinearLayout.setVisibility(View.VISIBLE);
		}
		else{
			isBCCExist=false;
		}
		try {
			//subject
			if(subject!=null && !(subject.equals(""))){
				//activity.setTitle(subject);
				displaySubject(subject);
			}
			else{
				//activity.setTitle(VIEW_MAIL_WEBVIEW_NO_SUBJECT);
				displaySubject(VIEW_MAIL_WEBVIEW_NO_SUBJECT);
			}
			//from
			fromIdView.setText(from);

			//to
			if(isToExist){
				//limit the number of receivers in To if there are more
				if(toReceivers.length > MAX_TO_RECEIVERS_TO_DISPLAY){
					//reduce the no. of To: receivers and hide thmem with show more button
					showFewToReceivers();
					toShowMoreBtn.setVisibility(View.VISIBLE);
				}
				else{
					toIdView.setText(to);
				}
			}
			if(isCCExist){
				//cc
				//limit the number of receivers in CC if there are more
				if(ccReceivers.length > MAX_TO_RECEIVERS_TO_DISPLAY){
					//reduce the no. of CC receivers and hide them with show more button
					showFewCCReceivers();
					cCShowMoreBtn.setVisibility(View.VISIBLE);
				}
				else{
					cCIdView.setText(cc);
				}
			}
			//date
			if(date!=null){
				dateIdView.setText((new SimpleDateFormat(VIEW_MAIL_DATE_FORMAT)).format(date.getTime()));
			}
		}catch(Exception e){
			e.printStackTrace();
		}

	}

	private void displaySubject(String viewMailWebviewNoSubject) {
		// TODO Auto-generated method stub
		//titleBarSubject.setText(viewMailWebviewNoSubject);
		activity.getSupportActionBar().setTitle(viewMailWebviewNoSubject);
	}

	public void showBody(String html1){

		if(null!=html1 && !(html1.equals(""))){
			if(BuildConfig.DEBUG)
			Log.d(TAG, "Loading html ");

			//webview.loadData(dispBody, CommonWebChromeClient.MIME_TYPE_HTML,CommonWebChromeClient.ENCODING);
			standardWebView.loadData(webview, html1);

		}
		else{
			//webvew.loadData(VIEW_MAIL_WEBVIEW_BODY_NO_CONTENT, StandardWebView.MIME_TYPE_HTML,StandardWebView.ENCODING);
			standardWebView.loadData(webview, VIEW_MAIL_WEBVIEW_BODY_NO_CONTENT);
		}
	}

	public void showFewCCReceivers(){
		cCIdView.setText(ccReceivers[0] + "; " +ccReceivers[1] + ";...");
	}

	/** Confirmation dialog shown for deleting items from Deleted Items folder
	 * @param activity
	 * @param context
	 */
	@Override
	public void showAlertdialogPermanentDelete(){

		final SherlockFragmentActivity _acivity=activity;

		//build the dialog
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(R.string.dialog_deletemail_title)
		.setMessage(R.string.dialog_deletemail_msg)
		.setPositiveButton(R.string.alertdialog_positive_lbl, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				DeleteMailAsyncCaller deleteCaller = new DeleteMailAsyncCaller(_acivity, message, true);
				deleteCaller.startDeleteMailAsyncTask();
			}
		})
		.setNegativeButton(R.string.alertdialog_negative_lbl, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				dialog.cancel();
			}
		})
		.create();
		AlertDialog alertDialog = builder.create();
		alertDialog.show();
	}
	
	public void showFewToReceivers(){
		toIdView.setText(toReceivers[0] + "; " +toReceivers[1] + ";...");
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

	public Status getCurrentStatus() {
		return currentStatus;
	}

	public void setCurrentStatus(Status currentStatus) {
		this.currentStatus = currentStatus;
	}
	
	@Override
	public EmailMessage getMessage() {
		return message;
	}

	public void setMessage(EmailMessage message) {
		this.message = message;
	}
	
	@Override
	public int getMailType() {
		return mailType;
	}

	public void setMailType(int mailType) {
		this.mailType = mailType;
	}
	
	public MessageBody getMsgBody() {
		return msgBody;
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

	public void setMsgBody(MessageBody msgBody) {
		this.msgBody = msgBody;
	}
	public Button getToShowMoreBtn() {
		return toShowMoreBtn;
	}

	public void setToShowMoreBtn(Button toShowMoreBtn) {
		this.toShowMoreBtn = toShowMoreBtn;
	}

	public Button getcCShowMoreBtn() {
		return cCShowMoreBtn;
	}

	public void setcCShowMoreBtn(Button cCShowMoreBtn) {
		this.cCShowMoreBtn = cCShowMoreBtn;
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

	public boolean isToShowMoreFlag() {
		return toShowMoreFlag;
	}

	public void setToShowMoreFlag(boolean toShowMoreFlag) {
		this.toShowMoreFlag = toShowMoreFlag;
	}

	public boolean isCcShowMoreFlag() {
		return ccShowMoreFlag;
	}

	public void setCcShowMoreFlag(boolean ccShowMoreFlag) {
		this.ccShowMoreFlag = ccShowMoreFlag;
	}
	public CachedMailHeaderVO getItemToOpen() {
		return itemToOpen;
	}

	public void setItemToOpen(CachedMailHeaderVO itemToOpen) {
		this.itemToOpen = itemToOpen;
	}

	public MailFunctions getMailFunctions() {
		return mailFunctions;
	}

	public void setMailFunctions(MailFunctions mailFunctions) {
		this.mailFunctions = mailFunctions;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	
	public TextView getFromIdView() {
		return fromIdView;
	}

	public void setFromIdView(TextView fromIdView) {
		this.fromIdView = fromIdView;
	}

	public TextView getToIdView() {
		return toIdView;
	}

	public void setToIdView(TextView toIdView) {
		this.toIdView = toIdView;
	}

	public TextView getcCIdView() {
		return cCIdView;
	}

	public void setcCIdView(TextView cCIdView) {
		this.cCIdView = cCIdView;
	}

	public TextView getDateIdView() {
		return dateIdView;
	}

	public void setDateIdView(TextView dateIdView) {
		this.dateIdView = dateIdView;
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
}
