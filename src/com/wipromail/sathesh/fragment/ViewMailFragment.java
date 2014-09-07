/**
 * 
 */
package com.wipromail.sathesh.fragment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
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
import com.wipromail.sathesh.cache.CacheDirectories;
import com.wipromail.sathesh.constants.Constants;
import com.wipromail.sathesh.customexceptions.NoUserSignedInException;
import com.wipromail.sathesh.customserializable.ContactSerializable;
import com.wipromail.sathesh.ews.EWSConnection;
import com.wipromail.sathesh.ews.MailFunctions;
import com.wipromail.sathesh.ews.MailFunctionsImpl;
import com.wipromail.sathesh.ews.NetworkCall;
import com.wipromail.sathesh.jsinterfaces.CommonWebChromeClient;
import com.wipromail.sathesh.service.data.Attachment;
import com.wipromail.sathesh.service.data.AttachmentCollection;
import com.wipromail.sathesh.service.data.EmailAddress;
import com.wipromail.sathesh.service.data.EmailMessage;
import com.wipromail.sathesh.service.data.ExchangeService;
import com.wipromail.sathesh.service.data.FileAttachment;
import com.wipromail.sathesh.service.data.ItemId;
import com.wipromail.sathesh.service.data.MessageBody;
import com.wipromail.sathesh.service.data.ServiceLocalException;
import com.wipromail.sathesh.service.data.ServiceVersionException;
import com.wipromail.sathesh.sqlite.db.pojo.vo.CachedMailHeaderVO;
import com.wipromail.sathesh.ui.ProgressDisplayNotificationBar;
import com.wipromail.sathesh.ui.listeners.ViewMailListener;
import com.wipromail.sathesh.util.Utilities;
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

	private static final String STATUS_LOADING="STATUS_LOADING";
	private static final String STATUS_SHOW_TITLE_BODY_B4_IMG="STATUS_SHOW_TITLE_BODY_B4_IMG";
	public static final String STATUS_DOWNLOADED_AN_IMAGE = "STATUS_DOWNLOADED_AN_IMAGE";
	public static final String STATUS_SHOW_BODY_DOWNLOADING_IMG = "STATUS_SHOW_BODY_DOWNLOADING_IMG";
	public static final String STATUS_SHOW_BODY_AFTER_IMG = "STATUS_SHOW_BODY_AFTER_IMG";
	private static final String STATUS_ERROR="STATUS_ERROR";

	private ExchangeService service;
	private EmailMessage message;

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

	private AttachmentCollection attachmentCollection;
	private String path="";
	private File file;
	private FileAttachment fileAttachment;
	private FileOutputStream fos;
	private List<FileAttachment> successfulCachedImages;
	private int totalInlineImages;

	private String processedHtml="";
	private int remainingInlineImages=0;
	private String currentStatus="";
	private ViewMailListener viewMailListener;


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.fragment_view_mail,
				container, false);

		activity = (SherlockFragmentActivity) getActivity();
		context = (SherlockFragmentActivity) getActivity();
		
		setRetainInstance(true);
		
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

		progressStatusDispBar = new ProgressDisplayNotificationBar(activity);

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
			Log.d(TAG, "ViewMailActivity -> from " + from + " to " + to  + " cc " + cc + " bcc " + bcc + " subject " + subject);
		}
		try {
			date = itemToOpen.getMail_datetimereceived();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		showDetails();

		(new LoadEmail()).execute(itemToOpen.getItem_id());

		return view;
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
							Log.d(TAG, "ViewMailActivity -> Adding to as reciever" + sContact.getEmail());
							ccBundle.putSerializable(sContact.getEmail(), sContact);
						}
						else{
							Log.d(TAG, "ViewMailActivity -> Skipped adding the logged in user from To to CC in new mail");
						}
					}
					else{
						Log.e(TAG, "ViewMailActivity -> Receiver "+ receiver + " is null");
					}
				}
			}
			if(isCCExist){

				for(String receiver : ccReceivers){
					if(receiver!=null){
						receiver = receiver.trim();
						if(!receiver.equals(MailApplication.getUserDisplayName(context))){
							sContact = new ContactSerializable(receiver, receiver, true);
							Log.d(TAG, "ViewMailActivity -> Adding cc as cc reciever" + sContact.getEmail());
							ccBundle.putSerializable(sContact.getEmail(), sContact);
						}else{
							Log.d(TAG, "ViewMailActivity -> Skipped adding the logged in user from CC to CC in new mail");
						}

					}
					else{
						Log.e(TAG, "ViewMailActivity -> Receiver "+ receiver + " is null");
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
							Log.d(TAG, "ViewMailActivity -> Skipped adding the logged in user in BCC");
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
	public String getCacheImageDirectory(EmailMessage message) throws ServiceLocalException, Exception{
		return CacheDirectories.getApplicationCacheDirectory(activity)+"/" + CACHE_DIRECTORY_MAILCACHE + "/" + mailFunctions.getItemId(message);
		//return MailApplication.getApplicationCacheDirectory(activity).toString() ;
	}
	public String getCacheImagePath(String directoryLoc, Attachment attachment){
		return directoryLoc+ "/"+attachment.getName();
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

	private void showBody(String html1){

		if(null!=html1 && !(html1.equals(""))){
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
	private class LoadEmail extends AsyncTask<String, String, Void> {

		@Override
		protected void onPreExecute() {
			//show loading progress
			webview.loadUrl(LOADING_HTML_URL);
			publishProgress(STATUS_LOADING, "Loading");

		}
		@Override
		protected void onProgressUpdate(String... progress) {
			// TODO Auto-generated method stub

			if(progress[0].equals(STATUS_LOADING)){
				currentStatus = STATUS_LOADING;
			}
			else if(progress[0].equals(STATUS_SHOW_TITLE_BODY_B4_IMG)){
				currentStatus = STATUS_SHOW_TITLE_BODY_B4_IMG;
				displayEverything();

			}

			else if(progress[0].equals(STATUS_SHOW_BODY_DOWNLOADING_IMG)){
				currentStatus = STATUS_SHOW_BODY_DOWNLOADING_IMG;
				showImgDownloadingNotification(remainingInlineImages, false);

			}
			else if(progress[0].equals(STATUS_DOWNLOADED_AN_IMAGE)){
				//triggered when 1 image got downloaded
				currentStatus = STATUS_DOWNLOADED_AN_IMAGE;
				showBody(progress[1]);
				showImgDownloadingNotification(remainingInlineImages, true);


			}
			else if(progress[0].equals(STATUS_SHOW_BODY_AFTER_IMG)){
				currentStatus = STATUS_SHOW_BODY_AFTER_IMG;
				//Triggered when all the images has been downloaded
				//		showBody(progress[1]);
				//	hideImgDownloadingNotification();

			}
			else if(progress[0].equals(STATUS_ERROR)){
				currentStatus = STATUS_ERROR;
				standardWebView.loadData(webview, VIEW_MAIL_ERROR_HTML);
			}
		}
		private void hideImgDownloadingNotification() {
			// TODO Auto-generated method stub

			progressStatusDispBar.hideStatusBar();
		}
		private void showImgDownloadingNotification(int no, boolean onlyRefreshText) {
			// TODO Auto-generated method stub


			if(no>0){
				if(!onlyRefreshText){

					progressStatusDispBar.showStatusBar();
				}
				if(no == 1){
					progressStatusDispBar.setText(getString(R.string.viewmail_downloading_img,((totalInlineImages+1) - no),totalInlineImages));
				}
				else{
					progressStatusDispBar.setText(getString(R.string.viewmail_downloading_imgs,((totalInlineImages+1) - no),totalInlineImages));
				}
				//	Notifications.showToast(activity, getString(R.string.viewmail_downloading_img,no), Toast.LENGTH_SHORT);
			}
			else{
				hideImgDownloadingNotification();
			}
		}
		@Override
		protected Void doInBackground(String... itemId) {


			List<FileAttachment> successfulCachedImages;
			try {
				service = EWSConnection.getServiceFromStoredCredentials(context);

				//EWS call for loading the message
				message = EmailMessage.bind(service, new ItemId(itemId[0]));

				//performance improvement.. mark the item as read in cache.
				message.setIsRead(true);
				//CODE HAS TO BE WRITTEN HERE
				//	CacheInboxAdapter.writeCacheInboxData(activity, message, true);


				//EWScall for markin item as read
				NetworkCall.markEmailAsRead(activity, message);

				msgBody = mailFunctions.getBody(message);
				processedHtml = MessageBody.getStringFromMessageBody(msgBody);
				from=mailFunctions.getFrom(message);
				to=mailFunctions.getTo(message);
				cc= mailFunctions.getCC(message);
				subject=mailFunctions.getSubject(message);
				date = mailFunctions.getDateTimeReceived(message);

				publishProgress(STATUS_SHOW_TITLE_BODY_B4_IMG, "Body with no image");

				attachmentCollection =message.getAttachments();

				totalInlineImages= getNoOfInlineImgs(attachmentCollection);
				remainingInlineImages= totalInlineImages;
				if(remainingInlineImages > 0){
					publishProgress(STATUS_SHOW_BODY_DOWNLOADING_IMG, "downloading");
					processedHtml = processBodyHTMLWithImages();	//replace all the inline image "cid" tags with "file://" tags
					successfulCachedImages=cacheInlineImages(attachmentCollection);		//caching images is done here. html body will be refreshed after each img download


					if(null!=successfulCachedImages && successfulCachedImages.size()>0){
						publishProgress(STATUS_SHOW_BODY_AFTER_IMG, processedHtml);
					}
					else{
						Log.i(TAG, "No images were cached in this mail to display ");
					}
				}
				else{
					Log.d(TAG, "No inline images in this email. Inline images counter: " +remainingInlineImages);
				}

			} catch(NoUserSignedInException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				publishProgress(STATUS_ERROR, e.getMessage());
			} catch(Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				publishProgress(STATUS_ERROR, e.getMessage());
			}
			return null;


		}
		private String processBodyHTMLWithImages() throws Exception {
			// TODO Auto-generated method stub



			String bodyWithImage=MessageBody.getStringFromMessageBody(msgBody);
			String cid="", directoryPath="", imagePath="", imageHtmlUrl="";
			for(Attachment attachment:  attachmentCollection){

				try {
					if(null != attachment && attachment.getIsInline() && attachment.getContentType()!=null && attachment.getContentType().contains("image")){
						Log.d(TAG, "ViewMailActivity -> processBodyHTMLWithImages() -> Processing attachment " + attachment.getName());
						cid="cid:"+ attachment.getContentId();
						Log.d(TAG, "ViewMailActivity ->cid "+cid);
						directoryPath= getCacheImageDirectory(message);
						imagePath=getCacheImagePath(directoryPath, attachment);

						imageHtmlUrl=Utilities.getHTMLImageUrl(attachment.getContentType(), imagePath);
						Log.d(TAG, "Replacing " + cid + " in body with " + imageHtmlUrl);
						bodyWithImage=bodyWithImage.replaceAll(cid, imageHtmlUrl);
						//Log.d(TAG, "ViewMailActivity -> Body with image "+bodyWithImage);


					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}


			return bodyWithImage;

		}

		private int getNoOfInlineImgs(AttachmentCollection attachmentCollection){
			int no=0;
			try {
				for(Attachment attachment:  attachmentCollection){
					if(attachment.getIsInline() && attachment.getContentType()!=null && attachment.getContentType().contains("image") && !(attachment.getContentType().equalsIgnoreCase("message/rfc822"))){
						no++;
					}
				}
			} catch (ServiceVersionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return no;
		}
		private List<FileAttachment> cacheInlineImages(AttachmentCollection attachmentCollection){
			successfulCachedImages = new ArrayList<FileAttachment>();
			for(Attachment attachment:  attachmentCollection){

				if(attachment!=null && attachment.getContentType()!= null ){

					Log.d(TAG, "ViewMailActivity -> cacheInlineImages() -> Processing attachment: " + attachment.getName() + " Attachment type " + attachment.getContentType());
					if(!(attachment.getContentType().equalsIgnoreCase("message/rfc822")) ){
						fileAttachment=(FileAttachment)attachment;
						/*
				System.out.println("Is Inline " + fa.getIsInline());
				System.out.println("Name " + fa.getName());
				System.out.println("Size " + fa.getSize());
				System.out.println("Content id " + fa.getContentId());
				System.out.println("Id " + fa.getId());
				System.out.println("Content location " + fa.getContentLocation());
				System.out.println("content type " + fa.getContentType());
				System.out.println("\n");
						 */



						try {
							if(fileAttachment.getIsInline() && fileAttachment.getContentType()!=null && fileAttachment.getContentType().contains("image")){

								file = new File(getCacheImageDirectory(message));

								file.mkdirs();
								path=getCacheImagePath(file.getPath(), attachment);


								Log.d(TAG, "Caching image file " +fileAttachment.getName() );
								if(!((new File(path)).exists())){
									//EWS call
									fos = new FileOutputStream(path);
									try{
										NetworkCall.downloadAttachment(fileAttachment, fos);
									}
									catch(Exception e){
										Log.e(TAG, "ViewMailActivity -> Exception while downloading atttachment");
										e.printStackTrace();
									}
								}
								remainingInlineImages--;
								successfulCachedImages.add(fileAttachment);
								publishProgress(STATUS_DOWNLOADED_AN_IMAGE, processedHtml);
							}
							else{
								Log.d(TAG, "ViewMailActivity -> cacheInlineImages() -> Skipping attachment: " + fileAttachment.getFileName() + " as it is not an inline image" );
							}
						} catch (ServiceVersionException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (FileNotFoundException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (ServiceLocalException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

					}
					else{
						Log.d(TAG, "ViewMailActivity -> Skipping message attachment of the content type message/rfc822");

					}
				}
				else{
					Log.e(TAG, "ViewMailActivity -> The attachment or its content type is null. Not processing this attachment!");
				}
			}
			return successfulCachedImages;
		}


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

	public String getCurrentStatus() {
		return currentStatus;
	}

	public void setCurrentStatus(String currentStatus) {
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

}
