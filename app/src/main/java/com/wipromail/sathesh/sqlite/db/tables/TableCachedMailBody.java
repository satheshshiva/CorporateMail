package com.wipromail.sathesh.sqlite.db.tables;

import java.util.List;

import com.wipromail.sathesh.sqlite.db.DbConstants;
import com.wipromail.sathesh.sqlite.db.DbTable;

/**  Table to store the cached mail body
 * @author sathesh
 *
 */
public class TableCachedMailBody implements DbConstants, DbTable{

	private String tableName = DbConstants.table.CACHED_MAIL_BODY;
	
	public static final String COLUMN_ID = "ID";
	public static final String COLUMN_FOLDER_ID = "FOLDER_ID";
	public static final String COLUMN_FOLDER_NAME = "FOLDER_NAME";
	public static final String COLUMN_ITEM_ID = "ITEM_ID";
	public static final String COLUMN_BODY = "MAIL_BODY";
	
	@Override
	public String getCreateQuery(){
		return "CREATE TABLE "
				+ tableName + "(" 
				+ COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " 
				+ COLUMN_FOLDER_ID + " TEXT,"
				+ COLUMN_FOLDER_NAME + " TEXT,"
				+ COLUMN_ITEM_ID + " TEXT UNIQUE NOT NULL ON CONFLICT REPLACE,"
				+ COLUMN_BODY + " TEXT"
				+");";
	}
	
	@Override
	public String getOnUpgradeDropQuery(){
		return "DROP TABLE IF EXISTS " + tableName;
	}
	
	/** SELECT QUERY
	 * @return
	 */
	public String getRecordsQuery(){
		return "SELECT * from " + tableName + " WHERE " + COLUMN_FOLDER_ID + "=?" ;
	}
	
	/** DELETE  QUERY
	 * @return
	 */
	public String getDeleteQuery(){
		return "DELETE from " + tableName + " WHERE " + COLUMN_FOLDER_ID + "=?" ;
	}

	/** DELETE ALL QUERY
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
