package com.wipromail.sathesh.application.interfaces;

import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextSwitcher;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

public interface MailListDataPasser {

	public int getMailType();

	public String getStrFolderId();

	public String getMailFolderName();
}
