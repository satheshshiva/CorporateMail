package com.wipromail.sathesh.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.SubMenu;
import com.google.analytics.tracking.android.EasyTracker;
import com.wipromail.sathesh.R;
import com.wipromail.sathesh.application.interfaces.ViewMailFragmentDataPasser;
import com.wipromail.sathesh.asynccaller.DeleteMailAsyncCaller;
import com.wipromail.sathesh.constants.Constants;
import com.wipromail.sathesh.ui.OptionsUIContent;

public class ViewMailActivity extends SherlockFragmentActivity implements Constants{
	
	private ViewMailFragmentDataPasser viewMailFragment;
	/** ON CREATE **
	 *  Fragment : ViewMailFragment
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_view_mail);
		
		// declaring the fragment 
		viewMailFragment = (ViewMailFragmentDataPasser) getSupportFragmentManager()
				.findFragmentById(R.id.viewMailFragment);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		MenuItem subMenuItem;

		//Overflow submenu
		SubMenu subMenuReply = menu.addSubMenu("Reply Options");

		subMenuReply
		.add(ACTIONBAR_REPLY)
		.setIcon(OptionsUIContent.getReplyIcon())
		.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);


		subMenuReply
		.add(ACTIONBAR_REPLYALL)
		.setIcon(OptionsUIContent.getReplyAllIcon())
		.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

		subMenuReply
		.add(ACTIONBAR_FORWARD)
		.setIcon(OptionsUIContent.getForwardIcon())
		.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

		//Reply  main menu
		subMenuItem = subMenuReply.getItem();
		subMenuItem.setIcon(OptionsUIContent.getReplyIcon());
		subMenuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);


		menu.add(ACTIONBAR_DELETE)
		.setIcon(OptionsUIContent.getDeleteIcon())
		.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS|MenuItem.SHOW_AS_ACTION_WITH_TEXT);

		// Attachment main menu
		/*	menu.add(ACTIONBAR_ATTACHMENT)
		.setIcon(OptionsUIContent.getAttachementIcon())
		.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS|MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		 */

		//Overflow submenu
		SubMenu subMenu = menu.addSubMenu("Others");

		/*
		subMenu
		.add(ACTIONBAR_SETTINGS)
		.setIcon(OptionsUIContent.getSettingsIcon())
		.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);


		subMenu
		.add(ACTIONBAR_ABOUT)
		.setIcon(OptionsUIContent.getAboutIcon())
		.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		 */

		//Overflow  main menu
		subMenuItem = subMenu.getItem();
		subMenuItem.setIcon(OptionsUIContent.getMoreoverFlowIcon());
		subMenuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);


		return true;
	}
	
	@Override
	public void onDestroy()
	{
		super.onDestroy();
		System.out.println("LIFE>ON DESTROY");
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		if(item!=null && item.getItemId()==android.R.id.home){
			finish();
		}

		else if(item!=null && item.getTitle().equals(ACTIONBAR_REPLY)){
			try {
				viewMailFragment.replyMail(false);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				Log.e(TAG, "ViewMailActivity -> Reply Mail exception");
				e.printStackTrace();
			}
		}
		else if(item!=null && item.getTitle().equals(ACTIONBAR_REPLYALL)){
			try {
				viewMailFragment.replyMail(true);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				Log.e(TAG, "ViewMailActivity -> Reply Mail exception");
				e.printStackTrace();
			}
		}
		else if(item!=null && item.getTitle().equals(ACTIONBAR_FORWARD)){
			try {
				viewMailFragment.forwardMail();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				Log.e(TAG, "ViewMailActivity -> Forward Mail exception");
				e.printStackTrace();
			}
		}

		else if(item!=null && item.getTitle().equals(ACTIONBAR_DELETE)){
			if (viewMailFragment.getMessage() !=null){
				if(viewMailFragment.getMailType() != MailType.DELETED_ITEMS){
					DeleteMailAsyncCaller deleteCaller = new DeleteMailAsyncCaller(this, viewMailFragment.getMessage(), false);
					deleteCaller.startDeleteMailAsyncTask();
				}else{
					viewMailFragment.showAlertdialogPermanentDelete();
				}
				
			}
		}

		else if(item!=null && item.getTitle().equals(ACTIONBAR_ATTACHMENT)){
		//	Notifications.showToast(this.getApplicationContext(),getString(R.string.attachment_feature_not_avaialable) , Toast.LENGTH_SHORT);
		}

		else if(item!=null && item.getTitle().equals(ACTIONBAR_SETTINGS)){
			Intent intent = new Intent(this, PreferencesActivity.class);
			startActivity(intent);
		}
		else if(item!=null && item.getTitle().equals(ACTIONBAR_ABOUT)){
			Intent intent = new Intent(this, AboutActivity.class);
			startActivity(intent);
		}

		return super.onOptionsItemSelected(item);
	}
	
	// Google Analytics
	@Override
	public void onStart() {
		super.onStart();
		EasyTracker.getInstance().activityStart(this); 
	}

	@Override
	public void onStop() {
		super.onStop();
		EasyTracker.getInstance().activityStop(this); 
	}

}


