package com.wipromail.sathesh.application.interfaces;

import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextSwitcher;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

public interface MailListDataPasser {

	public TextSwitcher getTitlebar_inbox_status_textswitcher();
	
	public TextView getTextViewLoginId();
	
	public PullToRefreshListView getPullRefreshListView();
	

	public ImageButton getMaillist_refresh_button();

	public ImageButton getMaillist_compose_button();

	public ActionBar getMyActionBar();
	public ProgressBar getMaillist_refresh_progressbar();

	public ImageView getSuccessIcon();

	public ImageView getFailureIcon();
	
	public ImageView getReadIcon();

	public ImageView getUnreadIcon();

	public ProgressBar getMaillist_update_progressbar();

	public int getMailType();

	public String getStrFolderId();

	public String getMailFolderName();
}
