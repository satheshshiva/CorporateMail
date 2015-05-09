package com.wipromail.sathesh.sqlite.db.cache;

public interface CacheDbConstants {

	public static final String DATABASE_NAME = "Cache.db";
	public static final int DATABASE_VERSION = 2;
	
	public interface table{
	public static final String CACHED_MAIL_HEADERS = "CACHED_MAIL_HEADERS";
	public static final String CACHED_MAIL_BODY = "CACHED_MAIL_BODY";
    public static final String DRAWER_MENU = "DRAWER_MENU";
	public static final String TEMP_VARIABLES = "TEMP_VARIABLES";
	}
	
}
