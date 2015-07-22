package com.wipromail.sathesh.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.wipromail.sathesh.R;
import com.wipromail.sathesh.application.MailApplication;
import com.wipromail.sathesh.application.MyActivity;
import com.wipromail.sathesh.constants.Constants;
import com.wipromail.sathesh.service.data.WellKnownFolderName;
import com.wipromail.sathesh.ui.components.SignOutAlertDialog;
import com.wipromail.sathesh.ui.util.OptionsUIContent;

@Deprecated
public class HomePageActivity extends MyActivity implements Constants{

	private WebView webview;
	private MyActivity activity;
	private Context context;
	private Intent intent;
	private ActionBar myActionBar;
	@Override
	protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_home_page);

        //Initialize toolbar
        MailApplication.toolbarInitialize(this);

		getSupportActionBar().setIcon(R.drawable.ic_menu_home);
		activity=this;
		context=this;
		
		webview = (WebView)findViewById(R.id.home_page_webview);
		WebSettings webSettings = webview.getSettings();
		webSettings.setAllowFileAccess(true);

		webSettings.setJavaScriptEnabled(true);	//this is important
		webSettings.setSupportZoom(false);
		webSettings.setBuiltInZoomControls(false);

		//	webview.setWebChromeClient(new CommonWebChromeClient());
		webview.addJavascriptInterface(new JavaScriptInterface(), "homepage");

		webview.setWebChromeClient(new WebChromeClient() {
			public void onProgressChanged(WebView view, int progress)   
			{

				// Return the app name after finish loading
				if(progress == 100)
					setSupportProgressBarIndeterminateVisibility(false);
			}
		});

		webview.loadUrl("file:///android_asset/www/pages/homepage.html");

		myActionBar = getSupportActionBar();
		setSupportProgressBarIndeterminateVisibility(true);

		try {
			//setting the display name
			//textViewLoginId.setText(MailApplication.getTitleBarDisplayName(activity));
			String strDispName;
			CharSequence cDispName = getTitleBarDisplayName(activity);

			strDispName=(cDispName!=null) ? cDispName.toString(): "";

			if(strDispName!= null && !(strDispName.equals(""))){
				myActionBar.setSubtitle(getTitleBarDisplayName(activity));
			}

		} catch (Exception e) {
			Log.e(TAG, "Error while setting up the Disply name in title bar");
			e.printStackTrace();
		}
	}

    private class JavaScriptInterface{
		JavaScriptInterface() {
		}

		private Intent intent = new Intent(activity, MailListViewActivity.class), intent1;
    //ignore comment
        @JavascriptInterface
		public void openInbox() {
			intent.putExtra(MailListViewActivity.MAIL_TYPE_EXTRA, MailType.INBOX);
			intent.putExtra(MailListViewActivity.FOLDER_NAME_EXTRA, WellKnownFolderName.Inbox.toString());
			startActivity(intent);
		}

        @JavascriptInterface
		public void openDrafts() {
			intent.putExtra(MailListViewActivity.MAIL_TYPE_EXTRA, MailType.DRAFTS);
			intent.putExtra(MailListViewActivity.FOLDER_NAME_EXTRA, WellKnownFolderName.Drafts.toString());
			startActivity(intent);
		}

        @JavascriptInterface
		public void openSentItems() {
			intent.putExtra(MailListViewActivity.MAIL_TYPE_EXTRA, MailType.SENT_ITEMS);
			intent.putExtra(MailListViewActivity.FOLDER_NAME_EXTRA, WellKnownFolderName.SentItems.toString());
			startActivity(intent);
		}

        @JavascriptInterface
		public void openDeletedItems() {
			intent.putExtra(MailListViewActivity.MAIL_TYPE_EXTRA,  MailType.DELETED_ITEMS);
			intent.putExtra(MailListViewActivity.FOLDER_NAME_EXTRA, WellKnownFolderName.DeletedItems.toString());
			startActivity(intent);
		}

        @JavascriptInterface
		public void openAllFolders() {
			intent1=new Intent(activity, OtherFoldersPageActivity.class);
			startActivity(intent1);
		}

        @JavascriptInterface
		public void openSearchContacts(){
			intent1=new Intent(activity, SearchContactsActivity.class);
			startActivity(intent1);
		}

        @JavascriptInterface
		public void openSignOutAlertBox(){
			SignOutAlertDialog.showAlertdialog(activity, context);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

        MenuItem menuItem;

		//Always Visible menu
        menuItem=menu.add(ACTIONBAR_COMPOSE)
		.setIcon(OptionsUIContent.getComposeIcon());

        MenuItemCompat.setShowAsAction(menuItem,
                MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);

		//Submenu
        SubMenu subMenu;

		subMenu = menu.addSubMenu(ACTIONBAR_OVERFLOW).setIcon(OptionsUIContent.getMoreoverFlowIcon());
        //Overflow Submenu
        MenuItem subMenuItem = subMenu.getItem();
        MenuItemCompat.setShowAsAction(subMenuItem, MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);

        //Compose Icon
        menuItem=subMenu
		.add(ACTIONBAR_COMPOSE)
		.setIcon(OptionsUIContent.getComposeIcon());

        MenuItemCompat.setShowAsAction(menuItem,
                MenuItem.SHOW_AS_ACTION_IF_ROOM);

        //Settings Icon
        menuItem=subMenu
		.add(ACTIONBAR_SETTINGS)
		.setIcon(OptionsUIContent.getSettingsIcon());

        MenuItemCompat.setShowAsAction(menuItem,
                MenuItem.SHOW_AS_ACTION_IF_ROOM);

        //Check for Updates Icon
        menuItem=subMenu
		.add(ACTIONBAR_CHECK_FOR_UPDATES)
		.setIcon(OptionsUIContent.getUpdatesIcon());

        MenuItemCompat.setShowAsAction(menuItem,
                MenuItem.SHOW_AS_ACTION_IF_ROOM);

        //About Icon
        menuItem=subMenu
		.add(ACTIONBAR_ABOUT)
		.setIcon(OptionsUIContent.getAboutIcon());

        MenuItemCompat.setShowAsAction(menuItem,
                MenuItem.SHOW_AS_ACTION_IF_ROOM);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		if(item!=null && item.getTitle().equals(ACTIONBAR_COMPOSE)){
			intent = new Intent(this, ComposeActivity.class);
			startActivity(intent);
		}
		else if(item!=null && item.getTitle().equals(ACTIONBAR_CHECK_FOR_UPDATES)){
			//intent = new Intent(this, AboutActivity.class);
			//intent.putExtra(AboutActivity.CHECK_UPDATES_ONLOAD_EXTRA, true);
			startActivity(intent);
		}
		else if(item!=null && item.getTitle().equals(ACTIONBAR_SETTINGS)){
			intent = new Intent(this, MyPreferencesActivity.class);
			startActivity(intent);
		}
		else if(item!=null && item.getTitle().equals(ACTIONBAR_ABOUT)){
		//	intent = new Intent(this, AboutActivity.class);
			startActivity(intent);
		}

		return super.onOptionsItemSelected(item);
	}

	/** the customization of the displayname in title bar
	 * @return String
	 * @throws Exception
	 */
	public static CharSequence getTitleBarDisplayName(Context context) throws Exception {

		String username = MailApplication.getUserDisplayName(context);
		if(username != null && username.length() >= TITLEBAR_NO_OF_CHARS_DISP_NAME){
			username =  username.substring(0, TITLEBAR_NO_OF_CHARS_DISP_NAME);
			username = username + "..";
		}
		return username;
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
