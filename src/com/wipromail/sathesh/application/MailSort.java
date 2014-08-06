package com.wipromail.sathesh.application;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.wipromail.sathesh.constants.Constants;

public class MailSort implements Constants{

	private static Date d1,d2;
	
	public static void sortByDateTimeReceived(List<Map<String, Object>> mailListFromcache){
			Collections.sort(mailListFromcache, new Comparator<Map<String, Object>>() {
			    @Override
			    public int compare(Map<String, Object> m1,Map<String, Object> m2) {

			    	d1 = (Date)m1.get(MAPKEY_INBOX_CACHE_DATETIMERECEIVED);
			    	d2 = (Date)m2.get(MAPKEY_INBOX_CACHE_DATETIMERECEIVED);
			    	
			    	if (d1 != null && d2 != null){
			        return d2.compareTo(d1);
			    	
			    	}
			    	else
			    	{
			    		return 0;
			    	}
			    }
			});
	}
}
