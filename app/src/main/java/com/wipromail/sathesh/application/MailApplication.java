package com.wipromail.sathesh.application;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.ListPreference;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.wipromail.sathesh.BuildConfig;
import com.wipromail.sathesh.R;
import com.wipromail.sathesh.activity.MainActivity;
import com.wipromail.sathesh.activity.MyPreferencesActivity;
import com.wipromail.sathesh.adapter.ComposeActivityAdapter;
import com.wipromail.sathesh.adapter.GeneralPreferenceAdapter;
import com.wipromail.sathesh.cache.CacheDirectories;
import com.wipromail.sathesh.constants.Constants;
import com.wipromail.sathesh.customexceptions.NoInternetConnectionException;
import com.wipromail.sathesh.customexceptions.StoredDateIsNullException;
import com.wipromail.sathesh.customserializable.ContactSerializable;
import com.wipromail.sathesh.customui.Notifications;
import com.wipromail.sathesh.ews.NetworkCall;
import com.wipromail.sathesh.fragment.ViewMailFragment;
import com.wipromail.sathesh.handlers.runnables.LoadEmailRunnable;
import com.wipromail.sathesh.service.MailNotificationService;
import com.wipromail.sathesh.service.data.Attachment;
import com.wipromail.sathesh.service.data.AttachmentCollection;
import com.wipromail.sathesh.service.data.EmailAddress;
import com.wipromail.sathesh.service.data.EmailAddressCollection;
import com.wipromail.sathesh.service.data.ExchangeService;
import com.wipromail.sathesh.service.data.FileAttachment;
import com.wipromail.sathesh.service.data.NameResolutionCollection;
import com.wipromail.sathesh.service.data.ServiceVersionException;
import com.wipromail.sathesh.threads.PullMailNotificationServiceThread;
import com.wipromail.sathesh.ui.ChangePasswordDialog;
import com.wipromail.sathesh.update.AutoUpdater;
import com.wipromail.sathesh.util.Utilities;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author sathesh
 *
 */


public class MailApplication implements Constants {


    private static String signedInAccUser,signedInAccPassword ;
    private static HashMap<String, String> cred;
    public static GeneralPreferenceAdapter generalSettings = new GeneralPreferenceAdapter();
    private static MailApplication mailApplication=null;
    public static Toolbar toolbar;
    private boolean isWrongPwd = false;

    /** All activities will call this method in the OnCreate to initialize the actionbar toolbar
     *
     */
    public static void toolbarInitialize(ActionBarActivity activity) {
        toolbar = (Toolbar) activity.findViewById(R.id.actionbar_toolbar);
        activity.setSupportActionBar(toolbar);
    }

    /** All fragments will call this method in the OnCreate or similarr to initialize the actionbar toolbar
     *
     */
    public static void toolbarInitialize( ActionBarActivity activity ,View view) {
        toolbar = (Toolbar) view.findViewById(R.id.actionbar_toolbar);
        activity.setSupportActionBar(toolbar);
    }

    public static String getWebmailURL(Context context){
        return generalSettings.getServerURL(context) + context.getString(R.string.webmail_url_EWS_extension);
    }
    /**
     * @param context
     * @return returns the stored credentials who successfully signed in
     * @throws Exception
     */
    public static Map<String, String> getStoredCredentials(Context context) throws Exception{
        cred= new HashMap<String, String>();
        signedInAccUser=USERNAME_NULL;
        signedInAccPassword=PASSWORD_NULL;

        signedInAccUser = SharedPreferencesAdapter.getSignedInUsername(context);
        signedInAccPassword = SharedPreferencesAdapter.getSignedInPassword(context);

        cred.put("signedInAccUser", signedInAccUser);
        cred.put("signedInAccPassword", signedInAccPassword) ;
        return cred;

    }

    public static boolean checkUserIfSignedIn(Context context) throws Exception{
        Map<String, String> storedCredentials = new HashMap<String, String>();

        storedCredentials = getStoredCredentials(context);


        String SignedInAccUser = storedCredentials.get("signedInAccUser");
        String SignedInAccPassword =  storedCredentials.get("signedInAccPassword");


        if (null != SignedInAccUser && !(SignedInAccUser.equals(USERNAME_NULL)) && !(SignedInAccPassword.equals(PASSWORD_NULL))){
            return  true;
        }
        return false;
    }

    /**
     * @param context
     * @return returns the stored credentials who successfully signed in
     * @throws Exception
     */
    public static Long getPullFrequency(Context context) {

        return generalSettings.getNotificationPullFrequency(context);

    }

    /** gets the boolean value whether the auto update notify is enabled
     * @param context
     * @return
     */
    public static boolean isAutoUdpateNotifyEnabled(Context context){
        return generalSettings.isAutoUdpateNotifyEnabled(context);
    }

    /**
     * @param context
     * @return the drawable icon for the Mail Unread
     */
    public static Drawable getMailViewUnreadIcon(Context context){
        if(null != context && null != context.getResources()){
            Drawable drawable=context.getResources().getDrawable(R.drawable.ic_menu_mail_unread);
            return drawable;
        }
        else
            return null;
    }


    /** Gets the external storage directory
     * @return
     */
    public static File getExternalStorageDirectory(){
        //this does not retrieve the directory of SD card alone. it will return internal storage
        return Environment.getExternalStorageDirectory();
    }

    /**
     * @param context
     * @return the drawable icon for the Mail Read
     */
    public static Drawable getMailViewReadIcon(Context context){
        if(null != context && null != context.getResources()){
            Drawable drawable=context.getResources().getDrawable(R.drawable.ic_menu_mail_read);
            return drawable;
        }
        else
            return null;
    }



    /**
     * @param context
     * @return Version code of the app mentioned in  manifest
     * @throws NameNotFoundException
     */
    public static int getAppVersionCode(Context context) throws NameNotFoundException{
        return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
    }

    /**
     * @param from
     * @return Customized From field string to be displayed in inbox
     */
    public static CharSequence getCustomizedInboxFrom(String from) {

        //return from;

        //for the inbox, the From field text will be filtered out after "(WT01" text
        return ((null != from) && (from.lastIndexOf(INBOX_FILTER_TEXT_FROM)>0)) ? from.substring(0, from.lastIndexOf(INBOX_FILTER_TEXT_FROM)):from;
    }

    /**
     * @return Customized Date field string to be displayed in mail list view  for mails
     */
    public static CharSequence getCustomizedInboxDate(Date dDate) {

        String strDate="";

        if (null != dDate){

            long dateDiff = 0;
            dateDiff= Utilities.getNumberOfDaysFromToday(dDate) ;

            if (dateDiff <= 0){
                strDate = (new SimpleDateFormat(INBOX_TEXT_DATE_TIME)).format(dDate.getTime());
            }
            else
            {
                strDate = (new SimpleDateFormat(INBOX_TEXT_DATE)).format(dDate.getTime());
            }
        }
        return strDate;
    }

    /**
     * @return Customized Date field string to be displayed in mail list view for Header
     */
    public static List<String> getCustomizedInboxDateHeader(Context context, Date dDate) {

        List<String> returnStr=null;
        if (null != dDate){
            returnStr= new ArrayList<String>();

            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            today.set(Calendar.MILLISECOND, 0);

            Calendar cDate = Calendar.getInstance();
            cDate.setTime(dDate);
            cDate.set(Calendar.HOUR_OF_DAY, 0);
            cDate.set(Calendar.MINUTE, 0);
            cDate.set(Calendar.SECOND, 0);
            cDate.set(Calendar.MILLISECOND, 0);

            long dateDiff = (today.getTimeInMillis() - cDate.getTimeInMillis()) / (1000 * 60 * 60 * 24) ;
            String date=(new SimpleDateFormat(INBOX_TEXT_DATE_HEADER)).format(dDate.getTime());
            if (dateDiff <= 0){
                //Today
                returnStr.add(context.getString(R.string.mailListView_today));
                returnStr.add(date);
            }
            else if (dateDiff == 1){
                //Yesterday
                returnStr.add(context.getString(R.string.mailListView_yesterday));
                returnStr.add(date);
            }

            else if (dateDiff > 1 && dateDiff <= 6)
            {
                //This week
                returnStr.add((new SimpleDateFormat(INBOX_TEXT_DATE_DAY_HEADER)).format(dDate.getTime()));
                returnStr.add(date);
            }
            else if(dateDiff > 6 )
            {
                //this year
                if( today.get(Calendar.YEAR) == cDate.get(Calendar.YEAR)){
                    //this month
                    if(today.get(Calendar.MONTH) == cDate.get(Calendar.MONTH)){
                        returnStr.add(context.getString(R.string.mailListView_same_month));
                    }
                    else{
                        //this year not this month
                        returnStr.add((new SimpleDateFormat(INBOX_TEXT_MONTH_HEADER)).format(dDate.getTime()));
                    }

                }
                else{
                    //not this year
                    if((today.get(Calendar.YEAR)-cDate.get(Calendar.YEAR)) ==1){
                        returnStr.add(context.getString(R.string.mailListView_1_year));
                    }else{
                        returnStr.add(String.valueOf(cDate.get(Calendar.YEAR)));
                    }
                }
            }

            else
            {
                returnStr.add((new SimpleDateFormat(INBOX_TEXT_DATE_DAY_HEADER)).format(dDate.getTime()));
                returnStr.add((new SimpleDateFormat(INBOX_TEXT_DATE_HEADER)).format(dDate.getTime()));
            }

        }
        return returnStr;
    }

    public static boolean isSatisfyingMinInterval(Date date)
            throws StoredDateIsNullException
    {
        if (date != null)
        {
            Calendar now = Calendar.getInstance();
            Calendar calDate = Calendar.getInstance();

            calDate.setTime(date);
            if (!( ((now.getTimeInMillis() - calDate.getTimeInMillis()) )  > (INBOX_UPDATER_MIN_INTERVAL)))
            {
                return false;
            }
            return true;

        }
        else
        {
            throw new StoredDateIsNullException();
        }

    }

    /** this will be called on clicking a notification
     * @return
     */
    public static Class mainApplicationActivity(){
        return MainActivity.class;
    }

    /** check user settings and decide to give vibrate for notification
     * @param notification
     */
    public static void setVibrateNotificationWithPermission(Notification notification){
        notification.defaults|= Notification.DEFAULT_VIBRATE;
    }

    /** check user settings and decide to give sound for notification
     * @param notification
     */
    public static void setSoundWithPermission(Notification notification){
        notification.defaults|= Notification.DEFAULT_SOUND;
    }

    /** check user settings and decide to give light for notification
     * @param notification
     */
    public static void setLightNotificationWithPermission(Notification notification){
        notification.flags |= Notification.FLAG_SHOW_LIGHTS;
        notification.ledARGB = 0xff00ff00;
        notification.ledOnMS = 300;
        notification.ledOffMS = 1000;
    }

    /** Should call this whenever you need to start or resume the MNS service.
     * This will usually call the OnStartCommand in the service which will handle the thread regarding the current status of the thread.
     * It will check for the status of the thread.
     * @param context
     */
    public static void startMNSService(final Context context) {

        if(generalSettings.isNotificationEnabled(context)){
            //not creating a log here since this iscalled everytime when opening inbox
            context.startService(new Intent(context,MailNotificationService.class));
        }
        else{
            Log.e(TAG, "Notification not enabled in prefernce. Hence cant start Mail Notification Service.");
        }
    }

    /** This method will stop the MNS service, the alarm for polling and subscription and clear any notification messages
     * @param context
     */
    public static void stopMNSService(final Context context) {

        Log.d(TAG, "Stopping service");
        context.stopService(new Intent(context,MailNotificationService.class));
    }

    /** When pull duration for PullMNS is changed, call this to update the time in thread
     * @throws Exception
     */
    public static void onChangeMNSResetPullDuration() throws Exception {
        PullMailNotificationServiceThread.resetAlarm();
    }

    public static NameResolutionCollection resolveName(ExchangeService service, String username, boolean retrieveContactDetails) throws NoInternetConnectionException, Exception{
        return NetworkCall.resolveName(service, username, retrieveContactDetails);
    }

    public static NameResolutionCollection resolveName_LocalThenDirectory(ExchangeService service, String username, boolean retrieveContactDetails) throws NoInternetConnectionException, Exception{
        return NetworkCall.resolveName_LocalThenDirectory(service, username, retrieveContactDetails);
    }

    /** gets the logged in user diaplay name
     * @throws Exception
     *
     */
    public static String getUserDisplayName(Context context) throws Exception{

        return SharedPreferencesAdapter.getUserDetailsDisplayName(context);
    }

    public static void openPlayStoreLink(Context context) throws Exception{
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse("market://details?id=" + context.getPackageName()));
        context.startActivity(i);
    }

    public static void showRateAppDialog(final Activity activity) {
        // if the counter is greater than a limit then show a dialog to rate the app
        //Building Rate App Dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        // Add the buttons
        builder.setPositiveButton(R.string.rate_app_dialog_rate_now, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button
                try {
                    MailApplication.openPlayStoreLink(activity);
                }
                catch (ActivityNotFoundException e) {
                    Notifications.showToast(activity, activity.getText(R.string.playstore_not_available), Toast.LENGTH_SHORT);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        builder.setNegativeButton(R.string.rate_app_dialog_not_now, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
                //automatically dismissed
            }
        });
        builder.setNeutralButton(R.string.rate_app_dialog_never, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
                //automatically dismissed
                try {
                    SharedPreferencesAdapter.storeDoNotRateApp(activity, true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        builder.setCancelable(true);
        builder.setTitle(R.string.rate_app_dialog_title);
        builder.setMessage(R.string.rate_app_msg);

        AlertDialog alert = builder.create();

        alert.show();
    }

    /** starts composing email for the given email id
     */
    public static void composeEmailForContact(Context context, int type, ContactSerializable sContact){
        //create a ContactSerializable to hold the To value of the developer
        if(sContact!= null){
            Bundle toBundle = new Bundle();
            sContact.setTryResolveNamesInDirectory(false);
            //put the ContactSerializable to a bundle
            toBundle.putSerializable(sContact.getEmail(), sContact);	//the key value (developer email) for the bundle is not needed since ComposeActivity concerns only the values


            ComposeActivityAdapter.startPrefilledCompose(context, type, toBundle, null, null, "", "", false);

        }
        else{
            Log.e(TAG, "MailApplication-> composeEmailForContact() => ContactSerializable is null");
        }
    }

    /** will be true when the the password is wrong which is set by (NotificationProcessing.showLoginErrorNotification()). This will be set back to false when the user saves a
     * new passoword in ChangePasswordDialog
     * @return
     */
    public boolean isWrongPwd() {
        return isWrongPwd;
    }
    public void setWrongPwd(boolean isWrongPwd) {
        this.isWrongPwd = isWrongPwd;
    }

    protected MailApplication(){
        // Exists only to defeat instantiation.
    }
    public static MailApplication getInstance(){
        if(mailApplication==null){
            mailApplication = new MailApplication();
        }
        return mailApplication;
    }

    public void onEveryAppOpen(ActionBarActivity activity, Context context) {
        //checking updates
        AutoUpdater.autoCheckForUpdates(activity);
        if(getInstance().isWrongPwd()){
            ChangePasswordDialog.showAlertdialog(activity, context);
        }
    }

    /** Gets SwipeRefreshLayout color resources
     * @return
     * @throws Exception
     */
    public static int[] getSwipeRefreshLayoutColorResources() throws Exception{
        return new int[]{R.color.Holo_Bright,
                R.color.Pallete_Red,
                R.color.Pallete_Yellow,
                R.color.Pallete_Violet};
    }

    /** Returns the list of ContactSerializables from the given string which was separated by delimiters
     * The list of From, To etc., address are stored in cache as delimited strings
     *
     * @param str
     * @return
     */
    public static List<ContactSerializable> getContactsFromDelimitedString(String str){
        List<ContactSerializable> list = new ArrayList<ContactSerializable>();
        String[] nameEmailArray, addressArray;
        ContactSerializable contact ;

        //split the email addesses sperator (;) and put in array
        addressArray = str.split(EMAIL_DELIMITER_DISP);
        for(String address : addressArray){
            // check whether each entry has name and email seprated with delim(#$%)
            if(address.contains(EMAIL_NAMEEMAIL_STORAGE_DELIM)){
                nameEmailArray=address.split(EMAIL_NAMEEMAIL_STORAGE_DELIM);
                contact = new ContactSerializable();
                if(nameEmailArray.length>0) {
                    contact.setDisplayName(nameEmailArray[0]);
                    //if the email is present
                    if(nameEmailArray.length>1) {
                        contact.setEmail(nameEmailArray[1]);
                        contact.setResolveOnLoad(true); // on load of the contact load the full contact
                    }
                    contact.setTryResolveNamesInDirectory(false);
                }
            }
            //if the delim (#%!) is not there then only name is present for the entry
            else{
                contact = new ContactSerializable();
                contact.setDisplayName(address);
                contact.setTryResolveNamesInDirectory(true);
            }
            //add to the return list
            list.add(contact);
        }
        return list;
    }

    /** Gives the delimited String from EmailAddressCollection which can be stored in the cache db
     *
     * @param recipients -EmailAddressCollection obj
     * @return - delimited String
     */
    public static String getDelimitedAddressString(EmailAddressCollection recipients) {
        StringBuffer str=new StringBuffer();
        for(EmailAddress recipient : recipients){
            if(recipient!=null) {
                str.append(recipient.getName())
                        .append(EMAIL_NAMEEMAIL_STORAGE_DELIM)
                        .append(recipient.getAddress())
                        .append(EMAIL_STORAGE_DELIM);
            }
        }
        return str.toString();
    }

    /** Gives the delimited String from EmailAddress which can be stored in the cache db
     *
     * @param recipient -EmailAddress obj
     * @return - delimited String
     */
    public static String getDelimitedAddressString(EmailAddress recipient) {
        StringBuffer str=new StringBuffer();
        if(recipient!=null) {
            str.append(recipient.getName())
                    .append(EMAIL_NAMEEMAIL_STORAGE_DELIM)
                    .append(recipient.getAddress())
                    .append(EMAIL_STORAGE_DELIM);
        }
        return str.toString();
    }

    /** Replaces the body String "cid:contentid" wih "file:filename". Does only the string change.
     *
     * @param context
     * @param body - body string to make the change
     * @param attachmentCollection
     * @param itemId
     * @param thisClass - for writing in exception
     * @return
     * @throws Exception
     */
    public static String getBodyWithImgHtml(Context context, String body, AttachmentCollection attachmentCollection, String itemId, Object thisClass) throws Exception {

        String bodyWithImage=body;
        String cid="", imagePath="", imageHtmlUrl="";
        for(Attachment attachment:  attachmentCollection){

            try {
                //if(null != attachment && attachment.getIsInline() && attachment.getContentType()!=null && attachment.getContentType().contains("image")){
                if(null != attachment && attachment.getIsInline()){
                    if(BuildConfig.DEBUG){
                        Log.d(TAG, "ViewMailActivity -> processBodyHTMLWithImages() -> Processing attachment " + attachment.getName());
                    }
                    cid="cid:"+ attachment.getContentId();
                    if(BuildConfig.DEBUG){
                        Log.d(TAG, "ViewMailActivity ->cid "+cid);
                    }
                    imagePath=CacheDirectories.getMailCacheImageDirectory(context) + "/" + itemId + "/"+attachment.getName();

                    imageHtmlUrl=Utilities.getHTMLImageUrl(attachment.getContentType(), imagePath);
                    if(BuildConfig.DEBUG){
                        Log.d(TAG, "Replacing " + cid + " in body with " + imageHtmlUrl);
                    }
                    bodyWithImage=bodyWithImage.replaceAll(cid, imageHtmlUrl);
                }
            } catch (Exception e) {
                Utilities.generalCatchBlock(e, thisClass);
            }
        }

        return bodyWithImage;
    }

    /** Returns the no of inline images
     *
     * @param attachmentCollection
     * @return
     */
    public static int getTotalNoOfInlineImgs(AttachmentCollection attachmentCollection, Object thisClass){
        int no=0;
        try {
            for(Attachment attachment:  attachmentCollection){
                //if(attachment.getIsInline() && attachment.getContentType()!=null && attachment.getContentType().contains("image") && !(attachment.getContentType().equalsIgnoreCase("message/rfc822"))){
                if(attachment.getIsInline() && !(attachment.getContentType()!=null && attachment.getContentType().equalsIgnoreCase("message/rfc822"))){
                    no++;
                }
            }
        } catch (ServiceVersionException e) {
            Utilities.generalCatchBlock(e, thisClass);
        }
        return no;
    }


    public static void cacheInlineImages(Context context, AttachmentCollection attachmentCollection, String itemId, String body, LoadEmailRunnable loadEmailRunnable, Object thisClass){
        String path="";
        File file;
        FileAttachment fileAttachment;
        FileOutputStream fos;

        for(Attachment attachment:  attachmentCollection){

            if(attachment!=null ){
                if(BuildConfig.DEBUG){
                    Log.d(TAG, "LoadEmailRunnable -> cacheInlineImages() -> Processing attachment: " + attachment.getName() + " Attachment type " + attachment.getContentType());
                }
                //if(!(attachment.getContentType().equalsIgnoreCase("message/rfc822")) ){
                if(!(attachment.getContentType()!=null && attachment.getContentType().equalsIgnoreCase("message/rfc822")) ){
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
                        //if(fileAttachment.getIsInline() && fileAttachment.getContentType()!=null && fileAttachment.getContentType().contains("image")){
                        if(fileAttachment.getIsInline()){
                            file = new File(CacheDirectories.getMailCacheImageDirectory(context) + "/" + itemId);

                            file.mkdirs();
                            path=file.getPath() + "/" + attachment.getName();

                            if(BuildConfig.DEBUG){
                                Log.d(TAG, "Caching image file " +fileAttachment.getName() );
                            }
                            if(!((new File(path)).exists())){
                                //EWS call
                                fos = new FileOutputStream(path);
                                try{
                                    NetworkCall.downloadAttachment(fileAttachment, fos);
                                }
                                catch(Exception e){
                                    Log.e(TAG, "ViewMailActivity -> Exception while downloading attachment");
                                    e.printStackTrace();
                                }
                            }
                            if(loadEmailRunnable!=null ) {
                                loadEmailRunnable.getParent().setRemainingInlineImages(loadEmailRunnable.getParent().getRemainingInlineImages() - 1);
                                loadEmailRunnable.sendHandlerMsg(ViewMailFragment.Status.DOWNLOADED_AN_IMAGE, body);
                            }
                        }
                        else{
                            Log.d(TAG, "ViewMailActivity -> cacheInlineImages() -> Skipping attachment: " + fileAttachment.getFileName() + " as it is not an inline image" );
                        }
                    } catch (Exception e) {
                        Utilities.generalCatchBlock(e, thisClass);
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
    }

    /**Builds and shows a CustomServer URL dialog for entering a manual URL.
     * Called by WebMailURLPreference and LoginPageListener
     *
     * @param context
     * @param listPreference - Component of PreferenceActivity. Pass null for other activities
     * @param textView - Component of LoginPageActivity. Pass null for other activities
     */
    public static void showCustomServerURLDialog(final Context context , final ListPreference listPreference, final TextView textView){

        //build the dialog for change password using the xml layout
        LayoutInflater factory = LayoutInflater.from(context);
        final View textEntryView = factory.inflate(R.layout.dialog_webmail_url, null);

        //update the passowrd field with the old password
        final EditText changeURLEdit = (EditText)textEntryView.findViewById(R.id.url_edit);

        changeURLEdit.setText(generalSettings.getServerURL(context));

        //build the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.dialog_webmail_url_title)
                .setView(textEntryView)
                .setPositiveButton(R.string.alertdialog_save_lbl, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String _url = changeURLEdit.getText().toString();
                        MyPreferencesActivity.timeOfLastCustomURL = Calendar.getInstance();        //will prevent from invoking the code inside OnSharedPreferenceChange event again, which will be invoked on next line while saving the shared prefs
                        GeneralPreferenceAdapter.storeServerURL(context, _url);
                        if (listPreference !=null){             //component of PreferenceActivity
                            listPreference.setSummary(_url);
                        }
                        if(textView!=null){                     //component of LoginPageActivity
                            textView.setText(_url);
                        }
                    }
                })
                .setNegativeButton(R.string.alertdialog_negative_lbl, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.cancel();
                    }
                })
                .create();
        AlertDialog webmailURL = builder.create();
        webmailURL.show();
    }

    /**Returns the default webmail URL
     *
     * @return
     */
    public static String getDefaultWebmailURL(Context context){
        return context.getString(R.string.webmail1_url);
    }

    /**Returns the Office 365 Server URL
     *
     * @return
     */
    public static String getOffice365URL(Context context){
        return context.getString(R.string.webmail_365_url);
    }

}
