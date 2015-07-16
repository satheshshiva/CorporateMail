package com.wipromail.sathesh.sqlite.db.cache.tables;

import android.content.Context;

import com.wipromail.sathesh.sqlite.db.cache.CacheDbConstants;
import com.wipromail.sathesh.sqlite.db.cache.DbTable;

import java.util.ArrayList;
import java.util.List;

/** Table to store the folders from the recursive folders thread
 * @author sathesh
 *
 */
public class TableMoreFolders implements CacheDbConstants, DbTable{

	private static String tableName = table.MORE_FOLDERS;
	
	//the column is mapped to VO by, COLUMN_FIELDNAME in here as FIELDNAME in VO. 
	//COLUMN_FIELDNAME=FIELDNAME=Actual database column name = vo.variable_name
	public static final String COLUMN_ID = "ID";
	public static final String COLUMN_FOLDER_NAME = "NAME";
	public static final String COLUMN__PARENT_NAME = "PARENT_NAME";
	public static final String COLUMN__FOLDER_ID = "FOLDER_ID";
	public static final String COLUMN__IS_HEADER = "IS_HEADER";
	public static final String COLUMN_ORDERING = "ORDERING";

	@Override
	public String getCreateQuery(Context context){
		return "CREATE TABLE "
				+ tableName + "(" 
				+ COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " 
				+ COLUMN_FOLDER_NAME + " TEXT NOT NULL,"
				+ COLUMN__PARENT_NAME + " TEXT, "
				+ COLUMN__FOLDER_ID + " TEXT, "
				+ COLUMN__IS_HEADER + " TEXT, "
				+ COLUMN_ORDERING + " INTEGER"
				+");";
	}
	
	@Override
	public String getOnUpgradeDropQuery(Context context){
		return "DROP TABLE IF EXISTS " + tableName;
	}
	
	/** SELECT QUERY
	 * @return
	 */
	public static String getAllRecordsQuery(){
		return "SELECT * from " + tableName  ;
	}
	
	/** DELETE QUERY
	 * @return
	 */
	public String getDeleteQuery(){
		return "DELETE from " + tableName ;
	}

	
	/* These queries will be executed when the table is newly created in the database. 
	 * The default records which needs to be inserted can be mentioned here (like default settings record for settings table or trial data for development)
	 * @see com.sathesh.carparking.db.DbTable#getNewTableQueries()
	 */
	@Override
	public List<String> getNewTableQueries(Context context) {
		ArrayList<String> queries=new ArrayList<String>();
		//queries.add("INSERT INTO " + tableName + " VALUES('','asd', ' asd')");
		return queries;
	}
	
}
