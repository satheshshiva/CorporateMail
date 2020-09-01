package com.sathesh.corporatemail.constants;

public interface Constants {

	String LOG_TAG = "CMail";
	String LOG_TAG_PullMnWorker = "CMail_PullMnWorker";


	/* -------------------------------------------------------------------------------------
	----------------------------------  SHARED PREFERENCES ---------------------------------
	--------------------------------------------------------------------------------------*/

	//Shared Preferences Storage file names
	String CRED_PREFS_NAME = "CRED_PREFS_NAME";				//shared pref which stores the credentials
	String SYSTEM_VARIABLES = "SYSTEM_VARIABLES";			//shared pref which stores the application level variables like updated date, sync state, etc.,
	String USER_ACCT_DETAILS = "USER_ACCT_DETAILS";			//shared pref which stores the user acct details

	/** Credentials Shared Preference 	**/
	//credentials Shared Preferences Storage key value name for username and password
	String CRED_PREFS_USERNAME = "username";
	String CRED_PREFS_PASSWORD = "password";

	//credentials Shared Preferences Storage when signed out
	String USERNAME_NULL = "";
	String PASSWORD_NULL = "";

	/** System Variables Shared Preference **/
	//storage key value which stores the EWS sync state
	String EWS_SYNC_STATE = "EwsSyncState";
	String INBOX_SYNC_PULL_WATERMARK = "InboxSyncPullWatermark";
	String LAST_SUCCESSFUL_INBOX_UPDATE = "LastSuccessfulInboxUpdate";
	String LAST_SUCCESSFUL_AUTO_UPDATE_CHECK = "LastSuccessfulAutoUpdateCheck";
	String IS_INBOX_INITIAL_SYNC_DONE = "IsInitialSyncDone";
	String PULL_SUBSCRIPTION_ID = "PullSubscriptionId";
	String PULL_WATERMARK = "PullWatermark";


	/** User Account Details Shared Preference **/
	String SIGNED_IN_USER_DISP_NAME = "SignedInUserDispName";
    String SIGNED_IN_USER_COMP_NAME = "SignedInUserCompanyName";
	String SIGNED_IN_USER_EMAIL = "SignedInUserEmail";
	String COUNTER_OPENED_MAILS = "CounterOpenedMails";
	String DO_NOT_RATE_APP = "DoNotRateApp";

	/* -------------------------------------------------------------------------------------
	----------------------------------  CACHE ----------------------------------------------
	--------------------------------------------------------------------------------------*/
	String CACHE_DIRECTORY_INLINE_IMGS = "inlineImgs";
	String CACHE_DIRECTORY_ATTACHMENTS = "attachments";

	/* -------------------------------------------------------------------------------------
	----------------------------------  INBOX ----------------------------------------------
	--------------------------------------------------------------------------------------*/

	//Mail List View
	int MIN_NO_OF_MAILS = 25;
	int MORE_NO_OF_MAILS = 25;	//when the user scrolls, load this no of mails
	
	//this no of mails will be retained in cache on activity exit
	int CACHE_MAX_MAIL_HEADERS_TO_KEEP = 50;
	int CACHE_MAX_MAIL_BODY_TO_KEEP = 50;
	
	String INBOX_FILTER_TEXT_FROM = "(WT01"; 	//for the inbox, the From field text will be displayed only up to this text

	//inbox date formats
	String INBOX_TEXT_DATE_TIME = "h:mm a";
	String INBOX_TEXT_DATE_THIS_YEAR = "d-MMM";
	String INBOX_TEXT_DATE_NOT_THIS_YEAR = "d-MMM-yyyy";

	//inbox date headerformats
	String INBOX_TEXT_DATE_DAY_HEADER = "EEEE";
	String INBOX_TEXT_MONTH_HEADER = "MMMM";
	String INBOX_TEXT_DATE_HEADER = "d-MMM-yyyy";

	//Update MailFunctions
	//the autosync will fetch this no. changes at a time
	int UPDATE_INBOX_PAGESIZE=512;
	long INBOX_UPDATER_MIN_INTERVAL = 20*1000;			//the minimum amt of interal in millis before any 2 sync

	//sync using search filter
	int INBOX_LATESTMAIL_OFFSET = 2;			//while syncing for the latest mails, this the no of mails it will fetch at a time


	/* -------------------------------------------------------------------------------------
	----------------------------------  View Mail ----------------------------------------------
	--------------------------------------------------------------------------------------*/

	int MAX_TO_RECEIVERS_DISPLAY =2; //if the no. of To receivers is more than while opening email it will be hidedn with a button
	int MAX_CC_RECEIVERS_DISPLAY =2;

	String VIEW_MAIL_DATE_FORMAT="E d-MMM-yy h:mm a";
	String VIEW_MAIL_ERROR_HTML="<span style=\"{color:red}\">Cannot load email. Please try again later.</span>";


	/* -------------------------------------------------------------------------------------
	----------------------------------  Compose ----------------------------------------------
	--------------------------------------------------------------------------------------*/
	String EMAIL_DELIMITER_DISP=";";
    String EMAIL_NAMEEMAIL_STORAGE_DELIM="#%!";
    String EMAIL_STORAGE_DELIM =";";
	boolean SEND_EMAIL_SAVE_COPY_IN_SENT=true;

	/* -------------------------------------------------------------------------------------
	----------------------------------  CACHE ----------------------------------------------
	--------------------------------------------------------------------------------------*/
	int HELP_TEXT_LAYOUT_HEIGHT=50;

	/* -------------------------------------------------------------------------------------
	----------------------------------  PULL MAIL SERVICE -----------------------------------
	--------------------------------------------------------------------------------------*/

	public static int MAIL_NOTIFICATION_SERVICE_CONN_TIMEOUT = 30;

	//public static long PULL_DURATION = 30*1000;	//30 secs
	public static long PULL_SUBSCRIPTION_RENEWAL = 23*60*60*1000;	//23 hours
	public static int PULL_SUBSCRIPTION_TIMEOUT = 1440;	//in mins

	/* -------------------------------------------------------------------------------------
	----------------------------------  ACTIONBAR CONSTANTS -------------------------------------------
	--------------------------------------------------------------------------------------*/
	String ACTIONBAR_CHECK_FOR_UPDATES = "Check For Updates";
	String ACTIONBAR_SETTINGS = "Settings";
	String ACTIONBAR_REFRESH = "Refresh";
	String ACTIONBAR_COMPOSE = "Compose";
	String ACTIONBAR_ABOUT = "About";
    String ACTIONBAR_LOGIN = "Login";
    String ACTIONBAR_OVERFLOW = "More";

	/* -------------------------------------------------------------------------------------
	----------------------------------  UI TEXTS -------------------------------------------
	--------------------------------------------------------------------------------------*/

	long SPLASH_NOT_SIGNED_IN_TIME = 2000;
	//these shld be moved to R.String
	String SETTINGS_NOT_SIGNED_IN_TEXT = "(not signed in)";
	String LOGGING_IN_PROG1_TEXT = "Connecting to server..";
	String LOGGING_IN_PROG2_TEXT = "Connecting to server..";
	String LOGGING_IN_PROG3_TEXT = "Getting Folder List";

	String VIEW_MAIL_WEBVIEW_BODY_NO_CONTENT="";

	//Text to be displayed in buttons
	String ERROR_ALERT_DISMISS_TEXT = "Dismiss";
	String INFO_ALERT_DISMISS_TEXT = "Dismiss";	

	//Error Messages
	String AUTHENICATION_FAILED_TEXT = "Authentication Failed!";
	String MALFORMED_WEBMAIL_URL_TEXT = "Malformed Webmail URL";

	//Title bar display
	int TITLEBAR_NO_OF_CHARS_DISP_NAME = 40;	//no of chars to be displayed in the title bar for the display name

	String LOADING_HTML_URL="file:///android_asset/loading.html";

	/* -------------------------------------------------------------------------------------
	----------------------------------  Rate Application ----------------------------
	--------------------------------------------------------------------------------------*/
	boolean showRateApp = false;

	/* -------------------------------------------------------------------------------------
	----------------------------------  External ----------------------------
	--------------------------------------------------------------------------------------*/
	//String FB_LIKE_URL_APP = "fb://profile/370304669770278"; 
	String FB_LIKE_URL_APP = "https://www.facebook.com/corporatemail";
	String FB_LIKE_URL_BROWSER = "https://www.facebook.com/corporatemail";

	/* -------------------------------------------------------------------------------------
	----------------------------------  Sqlite db ----------------------------
	--------------------------------------------------------------------------------------*/
	String DB_DATE_FORMAT="yyyy-MM-dd HH:mm:ss";

	/* -------------------------------------------------------------------------------------
	----------------------------------  Security ----------------------------
	--------------------------------------------------------------------------------------*/
	String ENCRYPTION_PASSPHRASE = "sJfDg3k4GgrEdbA5";	
	boolean IS_USERNAME_ENCRYPTION_ENABLED = false;
	boolean IS_PASSWORD_ENCRYPTION_ENABLED = true;

	/* -------------------------------------------------------------------------------------
	----------------------------------  Notifications ----------------------------
	--------------------------------------------------------------------------------------*/

	interface NotificationConstants {
		String channelIdNewEmail = "mn_channel_01";
		String channelIdImportantAlerts= "mn_channel_02";
		String groupNameMultiEmail = "NewEmailsGroup";
		String notificationIconColorString = "#2cb3f5";	//the color string shown in the small notification icon
		String notificationAlertIconColorString = "#FF0000";	//the color string shown in the small notification icon - during important alerts like expired pwd
	}

	/*--- Rate App  ---*/
	int RATE_APP_DIALOG_OPEN_MAIL_FREQUENCY=20;	//open the rate app dialog for every 20 mails opened


	//Mail type used to identify the type of mail used in MailListViewActivity etc.,
	interface MailType{
		int INBOX=1;
		int DRAFTS=2;
		int SENT_ITEMS=3;
		int DELETED_ITEMS=4;
		int OUTBOX=5;
		int JUNK_EMAIL=6;
		int CONVERSATION_HISTORY=7;
		int FOLDER_WITH_ID=8;
		int INBOX_SUBFOLDER_WITH_ID=9;

		/* -------------------------------------------------------------------------------------
----------------------------------  Workers ----------------------------
--------------------------------------------------------------------------------------*/
		String WORKER_TAG_PULL_MN = "PullMnWorker";}

		/* -------------------------------------------------------------------------------------
----------------------------------  TransitionShared Elements ----------------------------
--------------------------------------------------------------------------------------*/

	interface TransitionSharedElementNames{
		String subject = "transitionSubjectShared";
		String from = "transitionFromShared";
		String date = "transitionDateShared";
		String webview = "transitionWebviewShared";
		String contact = "transitionContactShared";
	}

}
