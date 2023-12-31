package com.sathesh.corporatemail.sqlite.db.cache.tables;

import android.content.Context;

import com.sathesh.corporatemail.sqlite.db.cache.CacheDbConstants;
import com.sathesh.corporatemail.sqlite.db.cache.DbTable;

import java.util.ArrayList;
import java.util.List;

/** Table to store the temporary variables used in the app
 * @author sathesh
 *
 */
public class TableTempVariables implements CacheDbConstants, DbTable{

	private String tableName = CacheDbConstants.table.TEMP_VARIABLES;
	
	//the column is mapped to VO by, COLUMN_FIELDNAME in here as FIELDNAME in VO. 
	//COLUMN_FIELDNAME=FIELDNAME=Actual database column name = vo.variable_name
	public static final String COLUMN_ID = "ID";
	public static final String COLUMN_NAME = "NAME";
	public static final String COLUMN_VALUE = "VALUE";

	@Override
	public String getCreateQuery(Context context){
		return "CREATE TABLE "
				+ tableName + "(" 
				+ COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " 
				+ COLUMN_NAME + " TEXT UNIQUE NOT NULL ON CONFLICT REPLACE,"
				+ COLUMN_VALUE + " TEXT"
				+");";
	}
	
	@Override
	public String getOnUpgradeDropQuery(Context context){
		return "DROP TABLE IF EXISTS " + tableName;
	}
	
	/** SELECT QUERY
	 * @return
	 */
	public String getRecordsQuery(){
		return "SELECT * from " + tableName + " WHERE " + COLUMN_NAME + "=?" ;
	}
	
	/** DELETE QUERY
	 * @return
	 */
	public String getDeleteQuery(){
		return "DELETE from " + tableName + " WHERE " + COLUMN_NAME + "=?" ;
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
