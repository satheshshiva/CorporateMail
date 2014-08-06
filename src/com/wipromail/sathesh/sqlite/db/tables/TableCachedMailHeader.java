package com.wipromail.sathesh.sqlite.db.tables;

import java.util.List;

import com.wipromail.sathesh.sqlite.db.DbConstants;
import com.wipromail.sathesh.sqlite.db.DbTable;

/**  Table to store the cached mail headers
 * @author sathesh
 *
 */
public class TableCachedMailHeader implements DbConstants, DbTable{

	private static String tableName = DbConstants.table.CACHED_MAIL_HEADERS;
	
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_MAIL_TYPE = "MAIL_TYPE";
	public static final String COLUMN_FOLDER_ID = "FOLDER_ID";
	public static final String COLUMN_FOLDER_NAME = "FOLDER_NAME";
	public static final String COLUMN_ITEM_ID = "ITEM_ID";
	public static final String COLUMN_FROM = "MAIL_FROM";
	public static final String COLUMN_TO = "MAIL_TO";
	public static final String COLUMN_CC = "MAIL_CC";
	public static final String COLUMN_SUBJECT = "MAIL_SUBJECT";
	public static final String COLUMN_DATETIMERECEIVED = "MAIL_DATETIMERECEIVED";
	public static final String COLUMN_ISREAD = "MAIL_ISREAD";
	public static final String COLUMN_HAS_ATTACHMENTS = "MAIL_HAS_ATTACHMENTS";
	
	@Override
	public String getCreateQuery(){
		return "CREATE TABLE "
				+ tableName + "(" 
				+ COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " 
				+ COLUMN_MAIL_TYPE + " TEXT,"
				+ COLUMN_FOLDER_ID + " TEXT,"
				+ COLUMN_FOLDER_NAME + " TEXT,"
				+ COLUMN_ITEM_ID + " TEXT UNIQUE NOT NULL ON CONFLICT REPLACE,"
				+ COLUMN_FROM + " TEXT,"
				+ COLUMN_TO + " TEXT,"
				+ COLUMN_CC + " TEXT,"
				+ COLUMN_SUBJECT + " TEXT,"
				+ COLUMN_DATETIMERECEIVED + " TEXT,"
				+ COLUMN_ISREAD + " TEXT, "
				+ COLUMN_HAS_ATTACHMENTS + " TEXT"
				+");";
	}
	
	@Override
	public String getOnUpgradeDropQuery(){
		return "DROP TABLE IF EXISTS " + tableName;
	}
	
	/** SELECT QUERY
	 * WHERE CLAUSE - MAIL TYPE
	 * @return
	 */
	public static String getAllRecordsByMailTypeQuery(){
		return "SELECT * FROM " + tableName + " WHERE " + COLUMN_MAIL_TYPE + "=? ORDER BY datetime(" + COLUMN_DATETIMERECEIVED + ") DESC";
	}
	
	/** SELECT QUERY 
	 * WHERE CLAUSE - FOLDER ID
	 * @return
	 */
	public static String getAllRecordsByFolderIdQuery(){
		return "SELECT * FROM " + tableName + " WHERE " + COLUMN_FOLDER_ID + "=? ORDER BY datetime(" + COLUMN_DATETIMERECEIVED + ") DESC";
	}
	
	/** SELECT COUNT QUERY
	 * WHERE CLAUSE - MAIL TYPE
	 * @return
	 */
	public static String getAllRecordsCountByMailTypeQuery(){
		return "SELECT COUNT(*) FROM " + tableName + " WHERE " + COLUMN_MAIL_TYPE + "=?";
	}
	
	/** SELECT COUNT QUERY
	 * WHERE CLAUSE - FOLDER ID
	 * @return
	 */
	public static String getAllRecordsCountByFolderIdQuery(){
		return "SELECT COUNT(*) FROM " + tableName + " WHERE " + COLUMN_FOLDER_ID + "=?";
	}
	
	/** DELETE 
	 * WHERE CLAUSE - MAIL TYPE
	 * @return
	 */
	public static String getDeleteAllQueryByMailTypeQuery(){
		return "DELETE from " + tableName  + " WHERE " + COLUMN_MAIL_TYPE + "=?";
	}

	/** DELETE 
	 * WHERE CLAUSE - FOLDER ID
	 * @return
	 */
	public static String getDeleteAllQueryByFolderIdQuery(){
		return "DELETE from " + tableName + " WHERE " + COLUMN_FOLDER_ID + "=?";
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
