package com.sathesh.corporatemail.constants;

public interface Constants {

	public static final String TAG = "Corporate Mail";
	public static final String TAG_MNS = "Corporate Mail MNS";


	/* -------------------------------------------------------------------------------------
	----------------------------------  SHARED PREFERENCES ---------------------------------
	--------------------------------------------------------------------------------------*/

	//Shared Preferences Storage file names
	public static final String CRED_PREFS_NAME = "CRED_PREFS_NAME";				//shared pref which stores the credentials
	public static final String SYSTEM_VARIABLES = "SYSTEM_VARIABLES";			//shared pref which stores the application level variables like updated date, sync state, etc.,
	public static final String USER_ACCT_DETAILS = "USER_ACCT_DETAILS";			//shared pref which stores the user acct details

	/** Credentials Shared Preference 	**/
	//credentials Shared Preferences Storage key value name for username and password
	public static final String CRED_PREFS_USERNAME = "username";
	public static final String CRED_PREFS_PASSWORD = "password";

	//credentials Shared Preferences Storage when signed out
	public static final String USERNAME_NULL = "";
	public static final String PASSWORD_NULL = "";

	/** System Variables Shared Preference **/
	//storage key value which stores the EWS sync state
	public static final String EWS_SYNC_STATE = "EwsSyncState";
	public static final String INBOX_SYNC_PULL_WATERMARK = "InboxSyncPullWatermark";
	public static final String LAST_SUCCESSFUL_INBOX_UPDATE = "LastSuccessfulInboxUpdate";
	public static final String LAST_SUCCESSFUL_AUTO_UPDATE_CHECK = "LastSuccessfulAutoUpdateCheck";
	public static final String IS_INBOX_INITIAL_SYNC_DONE = "IsInitialSyncDone";


	/** User Account Details Shared Preference **/
	public static final String SIGNED_IN_USER_DISP_NAME = "SignedInUserDispName";
    public static final String SIGNED_IN_USER_COMP_NAME = "SignedInUserCompanyName";
	public static final String SIGNED_IN_USER_EMAIL = "SignedInUserEmail";
	public static final String COUNTER_OPENED_MAILS = "CounterOpenedMails";
	public static final String DO_NOT_RATE_APP = "DoNotRateApp";

	/* -------------------------------------------------------------------------------------
	----------------------------------  CACHE ----------------------------------------------
	--------------------------------------------------------------------------------------*/
	public static final String CACHE_DIRECTORY_INLINE_IMGS = "inlineImgs";

	/* -------------------------------------------------------------------------------------
	----------------------------------  INBOX ----------------------------------------------
	--------------------------------------------------------------------------------------*/

	//Mail List View
	public static final int MIN_NO_OF_MAILS = 25;
	public static final int MORE_NO_OF_MAILS = 25;	//when the user scrolls, load this no of mails
	
	//this no of mails will be retained in cache on activity exit
	public static final int CACHE_MAX_MAIL_HEADERS_TO_KEEP = 50;
	public static final int CACHE_MAX_MAIL_BODY_TO_KEEP = 50;
	
	public static final String INBOX_FILTER_TEXT_FROM = "(WT01"; 	//for the inbox, the From field text will be displayed only up to this text

	//inbox date formats
	public static final String INBOX_TEXT_DATE_TIME = "h:mm a";
	public static final String INBOX_TEXT_DATE = "d-MMM-yy";

	//inbox date headerformats
	public static final String INBOX_TEXT_DATE_DAY_HEADER = "EEEE";
	public static final String INBOX_TEXT_MONTH_HEADER = "MMMM";
	public static final String INBOX_TEXT_DATE_HEADER = "d-MMM-yy";

	//Update MailFunctions
	//the autosync will fetch this no. changes at a time
	public static final int UPDATE_INBOX_PAGESIZE=512;
	public static final long INBOX_UPDATER_MIN_INTERVAL = 20*1000;			//the minimum amt of interal in millis before any 2 sync

	//sync using search filter
	public static final int INBOX_LATESTMAIL_OFFSET = 2;			//while syncing for the latest mails, this the no of mails it will fetch at a time


	/* -------------------------------------------------------------------------------------
	----------------------------------  View Mail ----------------------------------------------
	--------------------------------------------------------------------------------------*/

	public static final int MAX_TO_RECEIVERS_DISPLAY =2; //if the no. of To receivers is more than while opening email it will be hidedn with a button
	public static final int MAX_CC_RECEIVERS_DISPLAY =2;

	public static final String VIEW_MAIL_DATE_FORMAT="E d-MMM-yy h:mm a";
	public static final String VIEW_MAIL_ERROR_HTML="<span style=\"{color:red}\">Cannot load email. Please try again later.</span>";


	/* -------------------------------------------------------------------------------------
	----------------------------------  Compose ----------------------------------------------
	--------------------------------------------------------------------------------------*/
	public static final String EMAIL_DELIMITER_DISP=";";
    public static final String EMAIL_NAMEEMAIL_STORAGE_DELIM="#%!";
    public static final String EMAIL_STORAGE_DELIM =";";
	public static final boolean SEND_EMAIL_SAVE_COPY_IN_SENT=true;

	/* -------------------------------------------------------------------------------------
	----------------------------------  CACHE ----------------------------------------------
	--------------------------------------------------------------------------------------*/
	public static final int HELP_TEXT_LAYOUT_HEIGHT=50;

	/* -------------------------------------------------------------------------------------
	----------------------------------  PULL MAIL SERVICE -----------------------------------
	--------------------------------------------------------------------------------------*/

	public static int MAIL_NOTIFICATION_SERVICE_CONN_TIMEOUT = 30;

	//public static long PULL_DURATION = 30*1000;	//30 secs
	public static long PULL_SUBSCRIPTION_RENEWAL = 23*60*60*1000;	//23 hours

	/* -------------------------------------------------------------------------------------
	----------------------------------  ACTIONBAR CONSTANTS -------------------------------------------
	--------------------------------------------------------------------------------------*/
	public static String ACTIONBAR_CHECK_FOR_UPDATES = "Check For Updates";
	public static String ACTIONBAR_SETTINGS = "Settings";
	public static String ACTIONBAR_REFRESH = "Refresh";
	public static String ACTIONBAR_COMPOSE = "Compose";
	public static String ACTIONBAR_ABOUT = "About";
    public static String ACTIONBAR_LOGIN = "Login";
    public static String ACTIONBAR_OVERFLOW = "More";

	/* -------------------------------------------------------------------------------------
	----------------------------------  UI TEXTS -------------------------------------------
	--------------------------------------------------------------------------------------*/

	public static final long SPLASH_NOT_SIGNED_IN_TIME = 2000;
	//these shld be moved to R.String
	public static final String SETTINGS_NOT_SIGNED_IN_TEXT = "(not signed in)";
	public static final String LOGGING_IN_PROG1_TEXT = "Connecting to server..";
	public static final String LOGGING_IN_PROG2_TEXT = "Connecting to server..";
	public static final String LOGGING_IN_PROG3_TEXT = "Getting Folder List";

	public static final String VIEW_MAIL_WEBVIEW_BODY_NO_CONTENT="";
	public static final String VIEW_MAIL_WEBVIEW_NO_SUBJECT="(no subject)";

	//Text to be displayed in buttons
	public static final String ERROR_ALERT_DISMISS_TEXT = "Dismiss";
	public static final String INFO_ALERT_DISMISS_TEXT = "Dismiss";	

	//Error Messages
	public static final String AUTHENICATION_FAILED_TEXT = "Authentication Failed!";
	public static final String MALFORMED_WEBMAIL_URL_TEXT = "Malformed Webmail URL";

	//Title bar display
	public static final int TITLEBAR_NO_OF_CHARS_DISP_NAME = 40;	//no of chars to be displayed in the title bar for the display name

	public static final String LOADING_HTML_URL="file:///android_asset/loading.html";

	/* -------------------------------------------------------------------------------------
	----------------------------------  Rate Application ----------------------------
	--------------------------------------------------------------------------------------*/
	public static boolean showRateApp = false;

	/* -------------------------------------------------------------------------------------
	----------------------------------  Application Auto Updater ----------------------------
	--------------------------------------------------------------------------------------*/

	public static final long UPDATE_AUTO_CHECK_NO_OF_DAYS = 2;
	public static final String APPLICATION_APK_DOWNLOAD_URL1_DEV = "http://tiny.cc/wipromaildev";
	public static final String APPLICATION_LATEST_VERSION_PROP_URL_DEV = "http://tiny.cc/wipromailpropdev";
	public static final String APPLICATION_CHANGELOG_URL_DEV = "http://tiny.cc/wipromailchangelogdev";

	public static final String APPLICATION_APK_DOWNLOAD_URL1_REL = "http://tiny.cc/wipromail";
	public static final String APPLICATION_LATEST_VERSION_PROP_URL_REL = "http://tiny.cc/wipromailprop";
	public static final String APPLICATION_CHANGELOG_URL_REL = "http://tiny.cc/wipromailchangelog";

	//public static final String APPLICATION_APK_DOWNLOAD_URL1 = "https://dl.dropbox.com/s/dn6nvxpk4a4fs6h/MainActivity-debug.apk?dl=1";
	public static final String APPLICATION_APK_DOWNLOAD_TEMPLOC = "/WiproMail/download/";

	public static final String APPLICATION_APK_DOWNLOAD_TEMP_FILENAME = "WiproMail.apk";
	public static final String LATEST_VERSION_CODE = "LATEST_VERSION_CODE";	//look for this prop in remote prop file 
	public static final String LATEST_VERSION_NAME = "LATEST_VERSION_NAME";	//look for this prop in remote prop file 

	/* -------------------------------------------------------------------------------------
	----------------------------------  External ----------------------------
	--------------------------------------------------------------------------------------*/
	//public static final String FB_LIKE_URL_APP = "fb://profile/370304669770278"; 
	public static final String FB_LIKE_URL_APP = "https://www.facebook.com/wipromail"; 
	public static final String FB_LIKE_URL_BROWSER = "https://www.facebook.com/wipromail"; 

	/* -------------------------------------------------------------------------------------
	----------------------------------  Sqlite db ----------------------------
	--------------------------------------------------------------------------------------*/
	public static final String DB_DATE_FORMAT="yyyy-MM-dd HH:mm:ss";

	/* -------------------------------------------------------------------------------------
	----------------------------------  Security ----------------------------
	--------------------------------------------------------------------------------------*/
	public static final String ENCRYPTION_PASSPHRASE = "sJfDg3k4GgrEdbA5";	
	public static final boolean IS_USERNAME_ENCRYPTION_ENABLED = false;
	public static final boolean IS_PASSWORD_ENCRYPTION_ENABLED = true;

	/* -------------------------------------------------------------------------------------
	----------------------------------  Notification Channel ----------------------------
	--------------------------------------------------------------------------------------*/

	public static final String NOTIFICATION_CHANNEL_NEW_MAIL = "cm_channel_01";
	public static final String NOTIFICATION_CHANNEL_LOGIN_ERROR = "cm_channel_02";
	public static final String NOTIFICATION_CHANNEL_NEW_MAIL_DESC = "corp_mail_newmail_channel";
	public static final String NOTIFICATION_CHANNEL_LOGIN_ERROR_DESC = "corp_mail_login_error_channel";

	/*--- Rate App  ---*/
	public static final int RATE_APP_DIALOG_OPEN_MAIL_FREQUENCY=20;	//open the rate app dialog for every 20 mails opened


	//Mail type used to identify the type of mail used in MailListViewActivity etc.,
	public interface MailType{
		public final static int INBOX=1;
		public final static int DRAFTS=2;
		public final static int SENT_ITEMS=3;
		public final static int DELETED_ITEMS=4;
		public final static int OUTBOX=5;
		public final static int JUNK_EMAIL=6;
		public final static int CONVERSATION_HISTORY=7;
		public final static int FOLDER_WITH_ID=8;
		public final static int INBOX_SUBFOLDER_WITH_ID=9;
	}

}
