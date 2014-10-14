package com.wipromail.sathesh.sqlite.db.cache.tables;

import java.util.List;

import com.wipromail.sathesh.sqlite.db.cache.CacheDbConstants;
import com.wipromail.sathesh.sqlite.db.cache.DbTable;

/**  Table to store the cached mail headers
 * @author sathesh
 *
 */
public class TableMailHeaders implements CacheDbConstants, DbTable{

	private String tableName = table.CACHED_MAIL_HEADERS;
	
	public static final String COLUMN_ID = "ID";
	public static final String COLUMN_FOLDER_ID = "FOLDER_ID";
	public static final String COLUMN_FOLDER_NAME = "FOLDER_NAME";
	public static final String COLUMN_ITEM_ID = "ITEM_ID";
	public static final String COLUMN_FROM = "MAIL_FROM";
	public static final String COLUMN_TO = "MAIL_TO";
	public static final String COLUMN_SUBJECT = "MAIL_SUBJECT";
	public static final String COLUMN_DATETIMERECEIVED = "MAIL_DATETIMERECEIVED";
	public static final String COLUMN_ISREAD = "MAIL_ISREAD";
	
	@Override
	public String getCreateQuery(){
		return "CREATE TABLE "
				+ tableName + "(" 
				+ COLUMN_ID + " INTEGER primary key autoincrement, " 
				+ COLUMN_FOLDER_ID + " TEXT,"
				+ COLUMN_FOLDER_NAME + " TEXT,"
				+ COLUMN_ITEM_ID + " TEXT,"
				+ COLUMN_FROM + " TEXT,"
				+ COLUMN_TO + " TEXT,"
				+ COLUMN_SUBJECT + " TEXT,"
				+ COLUMN_DATETIMERECEIVED + " TEXT,"
				+ COLUMN_ISREAD + " TEXT"
				+");";
	}
	
	@Override
	public String getOnUpgradeDropQuery(){
		return "DROP TABLE IF EXISTS " + tableName;
	}
	
	/** GET QUERY
	 * @return
	 */
	public String getMailHeaders(){
		return "SELECT * from " + tableName + " WHERE " + COLUMN_FOLDER_ID + "=?" ;
	}
	
	/** DELETE 
	 * @return
	 */
	public String getDeleteMailsQuery(){
		return "DELETE from " + tableName + " WHERE " + COLUMN_FOLDER_ID + "=?" ;
	}

	/** DELETE EVERYTHING
	 * @return
	 */
	public String getDeleteAllQuery(){
		return "DELETE from " + tableName  ;
	}
	
	/* These queries will be executed when the table is newly created in the database. 
	 * The default records which needs to be inserted can be mentioned here (like default settings record for settings table or trial data for development)
	 * @see com.sathesh.carparking.db.DbTable#getNewTableQueries()
	 */
	@Override
	public List<String> getNewTableQueries() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
