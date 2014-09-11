package com.wipromail.sathesh.sync;

import android.content.Context;

import com.wipromail.sathesh.application.SharedPreferencesAdapter;
import com.wipromail.sathesh.constants.Constants;
import com.wipromail.sathesh.ews.NetworkCall;
import com.wipromail.sathesh.service.data.ChangeCollection;
import com.wipromail.sathesh.service.data.ExchangeService;
import com.wipromail.sathesh.service.data.ItemChange;

public class SyncUsingStateVariable implements Constants{

	private int pageSize = UPDATE_INBOX_PAGESIZE;
	private String syncState="";
	public int getPageSize() {
		return pageSize;
	}


	private ChangeCollection<ItemChange> changeCollection;
	
	public synchronized ChangeCollection<ItemChange> doSync(Context context, ExchangeService service) throws Exception
	
	{
		syncState = SharedPreferencesAdapter.getSyncState(context);
		//EWS call
		changeCollection = NetworkCall.syncFolderItems(service, pageSize, syncState);
		syncState = changeCollection.getSyncState();

		SharedPreferencesAdapter.storeSyncState(context, syncState);
		
		return changeCollection;
	}
	
	

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public String getSyncState() {
		return syncState;
	}

	public void setSyncState(String syncState) {
		this.syncState = syncState;
	}

	public ChangeCollection<ItemChange> getChangeCollection() {
		return changeCollection;
	}

	public void setChangeCollection(ChangeCollection<ItemChange> changeCollection) {
		this.changeCollection = changeCollection;
	}
	
}
