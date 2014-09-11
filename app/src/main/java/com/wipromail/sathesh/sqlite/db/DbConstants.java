package com.wipromail.sathesh.sqlite.db;

public interface DbConstants {

	public static final String DATABASE_NAME = "Main.db";
	public static final int DATABASE_VERSION = 2;
	
	public interface table{
	public static final String CACHED_MAIL_HEADERS = "CACHED_MAIL_HEADERS";
	public static final String CACHED_MAIL_BODY = "CACHED_MAIL_BODY";
	public static final String TEMP_VARIABLES = "TEMP_VARIABLES";
	}
	
}
