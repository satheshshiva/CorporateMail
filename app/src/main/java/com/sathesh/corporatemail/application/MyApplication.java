package com.sathesh.corporatemail.application;

import androidx.multidex.MultiDexApplication;

import com.sathesh.corporatemail.constants.Constants;
import com.sathesh.corporatemail.sqlite.db.cache.vo.CachedMailHeaderVO;

public class MyApplication extends MultiDexApplication implements Constants{

	private CachedMailHeaderVO lastViewedMailByViewPager;		//used between ViewMailActivity and MailListViewActivity to communicate the currently loaded mail
																		// changed using view pager. Enable the transition of these activities. Simply the position cannot be passed because the listview not just have mail. It has headers as well.

	  @Override
	  public void onCreate() {
	      super.onCreate();

	  }

	public CachedMailHeaderVO getLastViewedMailByViewPager() {
		return lastViewedMailByViewPager;
	}

	public void setLastViewedMailByViewPager(CachedMailHeaderVO lastViewedMailByViewPager) {
		this.lastViewedMailByViewPager = lastViewedMailByViewPager;
	}
}
