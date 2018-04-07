package com.sathesh.corporatemail.sqlite.db.cache;

public interface CacheDbConstants {

	String DATABASE_NAME = "Cache.db";
	int DATABASE_VERSION = 3;

	interface table{
		String CACHED_MAIL_HEADERS = "CACHED_MAIL_HEADERS";
		String CACHED_MAIL_BODY = "CACHED_MAIL_BODY";
		String DRAWER_MENU = "DRAWER_MENU";
		String MORE_FOLDERS = "MORE_FOLDERS";
		String TEMP_VARIABLES = "TEMP_VARIABLES";
	}

}
