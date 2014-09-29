package com.wipromail.sathesh.sqlite.db.tables;

import com.wipromail.sathesh.sqlite.db.DbConstants;
import com.wipromail.sathesh.sqlite.db.DbTable;

import java.util.List;

/**  Table to store the cached mail body
 * @author sathesh
 *
 */
public class TableCachedMailBody implements DbConstants, DbTable{

	private static String tableName = DbConstants.table.CACHED_MAIL_BODY;
	
	public static final String COLUMN_ID = "ID";
    public static final String COLUMN_MAIL_TYPE = "MAIL_TYPE";
	public static final String COLUMN_FOLDER_ID = "FOLDER_ID";
	public static final String COLUMN_FOLDER_NAME = "FOLDER_NAME";
	public static final String COLUMN_ITEM_ID = "ITEM_ID";
	public static final String COLUMN_BODY = "MAIL_BODY";
	
	@Override
	public String getCreateQuery(){
		return "CREATE TABLE "
				+ tableName + "(" 
				+ COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_MAIL_TYPE + " TEXT,"
				+ COLUMN_FOLDER_ID + " TEXT,"
				+ COLUMN_FOLDER_NAME + " TEXT,"
				+ COLUMN_ITEM_ID + " TEXT UNIQUE NOT NULL ON CONFLICT REPLACE,"
				+ COLUMN_BODY + " TEXT"
				+");";
	}

    /*** SELECT QUERIES ***/

    /** SELECT QUERY
     * WHERE CLAUSE - Item Id
     * @return
     */
    public static String getAllRecordsByItemIdQuery(){
        return "SELECT * FROM " + tableName + " WHERE " + COLUMN_ITEM_ID + "=? ";
    }

    /*** UPDATE QUERIES ***/
    // none

    /*** DELETE QUERIES ***/

    /** DELETE by MailType
     * WHERE CLAUSE - MAIL TYPE
     * @return
     */
    public static String getDeleteAllQueryByMailTypeQuery(){
        return "DELETE from " + tableName  + " WHERE " + COLUMN_MAIL_TYPE + "=?";
    }

    /** DELETE by Folder Id
     * WHERE CLAUSE - FOLDER ID
     * @return
     */
    public static String getDeleteAllQueryByFolderIdQuery(){
        return "DELETE from " + tableName + " WHERE " + COLUMN_FOLDER_ID + "=?";
    }

    /** DELETE by MailType
     * WHERE CLAUSE - MailType
     * @param n - no of records to keep on top
     * @return
     */
    public static String getDeleteNQueryByMailTypeQuery(int n){
        return "DELETE from " + tableName  + " WHERE " + COLUMN_MAIL_TYPE + "=? AND " + COLUMN_ID + " NOT IN ("
                + " SELECT " + COLUMN_ID + " FROM " + tableName + " WHERE " + COLUMN_MAIL_TYPE + "=? LIMIT " + n + ")";
    }

    /** DELETE by Folder Id
     * WHERE CLAUSE - FOLDER ID
     * @param n - no of records to keep on top
     * @return
     */
    public static String getDeleteNQueryByFolderIdQuery(int n){
        return "DELETE from " + tableName + " WHERE " + COLUMN_FOLDER_ID + "=? AND " + COLUMN_ID + " NOT IN ("
                + " SELECT " + COLUMN_ID + " FROM " + tableName + " WHERE " + COLUMN_FOLDER_ID + "=? LIMIT " + n + ")";
    }


    /*** APPLICATION LEVEL QUERIES ***/
	
	/* These queries will be executed when the table is newly created in the database. 
	 * The default records which needs to be inserted can be mentioned here (like default settings record for settings table or trial data for development)
	 * @see com.sathesh.carparking.db.DbTable#getNewTableQueries()
	 */
	@Override
	public List<String> getNewTableQueries() {
		// TODO Auto-generated method stub
		return null;
	}

    @Override
    public String getOnUpgradeDropQuery(){
        return "DROP TABLE IF EXISTS " + tableName;
    }
	
}
