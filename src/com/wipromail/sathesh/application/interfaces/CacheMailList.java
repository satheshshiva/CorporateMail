package com.wipromail.sathesh.application.interfaces;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;

public interface CacheMailList extends CacheData{
	public List<Map<String, Object>> getInboxListFromCache(Context context) throws Exception;
	public void setInboxListToCache(Context context, List<Map<String, Object>> mailListCache) throws Exception;
	
}
