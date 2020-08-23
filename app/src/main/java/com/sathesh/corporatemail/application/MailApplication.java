package com.sathesh.corporatemail.application;

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
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.google.common.util.concurrent.ListenableFuture;
import com.sathesh.corporatemail.BuildConfig;
import com.sathesh.corporatemail.R;
import com.sathesh.corporatemail.activity.MainActivity;
import com.sathesh.corporatemail.adapter.ComposeActivityAdapter;
import com.sathesh.corporatemail.adapter.GeneralPreferenceAdapter;
import com.sathesh.corporatemail.cache.CacheDirectories;
import com.sathesh.corporatemail.constants.Constants;
import com.sathesh.corporatemail.customexceptions.NoInternetConnectionException;
import com.sathesh.corporatemail.customexceptions.StoredDateIsNullException;
import com.sathesh.corporatemail.customserializable.ContactSerializable;
import com.sathesh.corporatemail.customui.Notifications;
import com.sathesh.corporatemail.ews.NetworkCall;
import com.sathesh.corporatemail.fragment.ViewMailFragment;
import com.sathesh.corporatemail.threads.ui.GetMoreFoldersThread;
import com.sathesh.corporatemail.threads.ui.LoadEmailThread;
import com.sathesh.corporatemail.ui.components.ChangePasswordDialog;
import com.sathesh.corporatemail.util.Utilities;
import com.sathesh.corporatemail.worker.PullMnWorker;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import microsoft.exchange.webservices.data.core.ExchangeService;
import microsoft.exchange.webservices.data.core.exception.service.local.ServiceVersionException;
import microsoft.exchange.webservices.data.misc.NameResolutionCollection;
import microsoft.exchange.webservices.data.property.complex.Attachment;
import microsoft.exchange.webservices.data.property.complex.AttachmentCollection;
import microsoft.exchange.webservices.data.property.complex.EmailAddress;
import microsoft.exchange.webservices.data.property.complex.EmailAddressCollection;
import microsoft.exchange.webservices.data.property.complex.FileAttachment;

import static android.content.Context.POWER_SERVICE;
import static com.sathesh.corporatemail.constants.Constants.MailType.WORKER_TAG_PULL_MN;

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
    private static GetMoreFoldersThread getMoreFoldersThread;
    private boolean isNotificationChannelInitialized;
    private boolean isViewMailTransitionEnabled = true; // toggle for enabling or disabling the view mail transition animation with shared elements. Disable if too intrusive.

    public void onEveryAppOpen(Activity activity, Context context){

        //Check wrong password dialog
        MailApplication app = getInstance();
        if(app.isWrongPwd())
        {
            ChangePasswordDialog.showAlertdialog(activity, context);
        }
        new Thread( () -> {

            //Notification Service initialization
            // triggering this everytime because after an application upgrade (tried the run button from IDE), the workinfo status is showing enqueued, but the job
            // is not actually queued when checked with `adb shell dumpsys jobscheduler`
            MailApplication.startMNWorker(context);

            // Battery optimization question dialog
            if(Build.VERSION.SDK_INT >=Build.VERSION_CODES.M)

            {
                Intent intent = new Intent();
                String packageName = context.getPackageName();
                PowerManager pm = (PowerManager) context.getSystemService(POWER_SERVICE);
                if (pm != null && !pm.isIgnoringBatteryOptimizations(packageName)) {
                    Log.e(LOG_TAG, "Permission Always run in background: NOT ENABLED");
                    intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                    intent.setData(Uri.parse("package:" + packageName));
                    context.startActivity(intent);
                } else {
                    Log.d(LOG_TAG, "Permission Always run in background: ENABLED");
                }
            }

            if(!app.isNotificationChannelInitialized()) {
                // Initialize all the notification channels
                NotificationProcessing.initNotificationChannels(context);
                app.setNotificationChannelInitialized(true);

            }
        }).start();
    }

    /** All activities will call this method in the OnCreate to initialize the actionbar toolbar
     *
     */
    public static Toolbar toolbarInitialize(MyActivity activity) {
        toolbar = (Toolbar) activity.findViewById(R.id.actionbar_toolbar);
        activity.setSupportActionBar(toolbar);
        return toolbar;
    }

    public static String getWebmailURL(Context context, String url){
        return url + context.getString(R.string.webmail_url_EWS_extension);
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

    private static SimpleDateFormat sdfShortFormat = new SimpleDateFormat(INBOX_TEXT_DATE_TIME, Locale.getDefault());
    private static SimpleDateFormat sdfShortThisYearFormat = new SimpleDateFormat(INBOX_TEXT_DATE_THIS_YEAR, Locale.getDefault());
    private static SimpleDateFormat sdfShortNotThisYearFormat = new SimpleDateFormat(INBOX_TEXT_DATE_NOT_THIS_YEAR, Locale.getDefault());
    /**
     * @return Customized Date field string to be displayed in mail list view  for mails
     */
    public static CharSequence getShortDate(Date dDate) {

        String strDate="";
        Calendar now = Calendar.getInstance();
        if (null != dDate){

            long dateDiff= Utilities.getNumberOfDaysFromToday(dDate) ;

            if (dateDiff <= 0){
                strDate = sdfShortFormat.format(dDate.getTime());
                return strDate;
            }
            //checking whether the given year is the current year.
            // subtracting 1900 because java.date.util.Date.getYear returns the current year minus 1900 (for whatever reason they did)
            if ( (now.get(Calendar.YEAR)-1900) == dDate.getYear()) {
                strDate = sdfShortThisYearFormat.format(dDate.getTime());
            }else{
                strDate = sdfShortNotThisYearFormat.format(dDate.getTime());
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
    public static void startMNWorker(final Context context) {

        ListenableFuture<List<WorkInfo>> wif =  WorkManager.getInstance(context).getWorkInfosByTag(WORKER_TAG_PULL_MN);
        try {
            List<WorkInfo> workInfos = wif.get();
            if (workInfos.size() >0){
                for (WorkInfo wi : workInfos) {
                    Log.d(LOG_TAG_PullMnWorker, workInfos.size() + " Worker(s) present for tag: " + WORKER_TAG_PULL_MN + ": " + wi);
                }
            }else{
                Log.d(LOG_TAG_PullMnWorker, "No pull mail worker present");

            }
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (!getInstance().isWrongPwd()) {
            //Log.d(LOG_TAG_PullMnWorker, "startMNWorker -> Number of workers: " + )
            if (generalSettings.isNotificationEnabled(context)) {
                //not creating a log here since this is called everytime when opening inbox
                //context.startService(new Intent(context,MailNotificationService.class));

                PeriodicWorkRequest pullMnWork =
                        new PeriodicWorkRequest.Builder(PullMnWorker.class, PeriodicWorkRequest.MIN_PERIODIC_INTERVAL_MILLIS, TimeUnit.MILLISECONDS)
                                .addTag(WORKER_TAG_PULL_MN)
                                .build();
                WorkManager
                        .getInstance(context)
                        .enqueueUniquePeriodicWork(WORKER_TAG_PULL_MN, ExistingPeriodicWorkPolicy.KEEP, pullMnWork);
            } else {
                Log.e(LOG_TAG_PullMnWorker, "Notification not enabled in preference. Hence cannot start Mail Notification Worker.");
            }
        }else{
            Log.w(LOG_TAG_PullMnWorker, "Not starting Mail Notification worker because of the wrong password");
        }
    }

    /** This method will stop the MNS service, the alarm for polling and subscription and clear any notification messages
     * @param context
     */
    public static void stopMNWorker(final Context context) {
        Log.w(LOG_TAG_PullMnWorker, "Cancelling "+ WORKER_TAG_PULL_MN + " - Stopping Mail Notification worker");
        WorkManager.getInstance(context).cancelUniqueWork(WORKER_TAG_PULL_MN);
    }

    /** When pull duration for PullMNS is changed, call this to update the time in thread
     * @throws Exception
     */
    @Deprecated
    public static void onChangeMNSResetPullDuration(Long millis) throws Exception {
        //PullSubscriptionThread.resetAlarm(millis);
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

    /** Opens the Playstore link
     *
     * @param context
     * @param packageName package name of the app
     * @throws Exception
     */

    public static void openPlayStoreLink(Context context, String packageName) throws Exception{
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse("market://details?id=" + packageName));
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
            Log.e(LOG_TAG, "MailApplication-> composeEmailForContact() => ContactSerializable is null");
        }
    }

    /** will be true when the the password is wrong. This will be set back to false when the user saves a
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
                        Log.d(LOG_TAG, "ViewMailActivity -> processBodyHTMLWithImages() -> Processing attachment " + attachment.getName());
                    }
                    cid="cid:"+ attachment.getContentId();
                    if(BuildConfig.DEBUG){
                        Log.d(LOG_TAG, "ViewMailActivity ->cid "+cid);
                    }
                    imagePath=CacheDirectories.getMailCacheImageDirectory(context) + "/" + itemId + "/"+attachment.getName();

                    imageHtmlUrl=Utilities.getHTMLImageUrl(attachment.getContentType(), imagePath);
                    if(BuildConfig.DEBUG){
                        Log.d(LOG_TAG, "Replacing " + cid + " in body with " + imageHtmlUrl);
                    }
                    bodyWithImage=bodyWithImage.replaceAll(cid, imageHtmlUrl);
                }
            } catch (Exception e) {
                Utilities.generalCatchBlock(e, thisClass);
            }
        }

        return bodyWithImage;
    }

    public static void startGetMoreFoldersThread(MyActivity activity, Handler handler){
        // if already ran, then restart thread
         if( getMoreFoldersThread==null
                || (getMoreFoldersThread!=null  && getMoreFoldersThread.getCurrentStatus() != GetMoreFoldersThread.Status.RUNNING)){
            getMoreFoldersThread = new GetMoreFoldersThread(activity, handler);
            getMoreFoldersThread.start();
        }
    }

    public static GetMoreFoldersThread getMoreFoldersThread(){
        return getMoreFoldersThread;
    }

    public boolean isNotificationChannelInitialized() {
        return isNotificationChannelInitialized;
    }

    public void setNotificationChannelInitialized(boolean notificationChannelInitialized) {
        isNotificationChannelInitialized = notificationChannelInitialized;
    }

    /*
     toggle for enabling or disabling the view mail transition animation with shared elements. Disable if too intrusive.
     */
    public boolean isViewMailTransitionEnabled() {
        return isViewMailTransitionEnabled;
    }
}
