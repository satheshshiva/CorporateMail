package com.sathesh.corporatemail.sqlite.db.cache.tables;

import android.content.Context;

import com.sathesh.corporatemail.sqlite.db.cache.CacheDbConstants;
import com.sathesh.corporatemail.sqlite.db.cache.DbTable;

import java.util.List;

/**  Table to store the cached mail headers
 * @author sathesh
 *
 */
public class TableCachedMailHeader implements CacheDbConstants, DbTable{

	private static String tableName = CacheDbConstants.table.CACHED_MAIL_HEADERS;
	
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_MAIL_TYPE = "MAIL_TYPE";
	public static final String COLUMN_FOLDER_ID = "FOLDER_ID";
	public static final String COLUMN_FOLDER_NAME = "FOLDER_NAME";
	public static final String COLUMN_ITEM_ID = "ITEM_ID";
	public static final String COLUMN_FROM = "MAIL_FROM";
	public static final String COLUMN_TO = "MAIL_TO";
	public static final String COLUMN_CC = "MAIL_CC";
	public static final String COLUMN_BCC = "MAIL_BCC";
	public static final String COLUMN_SUBJECT = "MAIL_SUBJECT";
	public static final String COLUMN_DATETIMERECEIVED = "MAIL_DATETIMERECEIVED";
	public static final String COLUMN_ISREAD = "MAIL_ISREAD";
	public static final String COLUMN_HAS_ATTACHMENTS = "MAIL_HAS_ATTACHMENTS";
	
	@Override
	public String getCreateQuery(Context context){
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
				+ COLUMN_BCC + " TEXT,"
				+ COLUMN_SUBJECT + " TEXT,"
				+ COLUMN_DATETIMERECEIVED + " TEXT,"
				+ COLUMN_ISREAD + " TEXT, "
				+ COLUMN_HAS_ATTACHMENTS + " TEXT"
				+");";
	}

    /*** SELECT QUERIES ***/

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
	
	/** SELECT UNREAD COUNT QUERY
	 * WHERE CLAUSE - MAIL TYPE
	 * @return
	 */
	public static String getUnreadByMailTypeQuery(){
		return "SELECT COUNT(*) FROM " + tableName + " WHERE " + COLUMN_MAIL_TYPE + "=? AND " + COLUMN_ISREAD + "=0";
	}
	
	/** SELECT UNREAD COUNT QUERY
	 * WHERE CLAUSE - FOLDER ID
	 * @return
	 */
	public static String getUnreadCountByFolderIdQuery(){
		return "SELECT COUNT(*) FROM " + tableName + " WHERE " + COLUMN_FOLDER_ID + "=? AND " + COLUMN_ISREAD + "=0";
	}

    /*** UPDATE QUERIES ***/

	/** UPDATE 
	 * Mark MAIL AS READ
	 * @return
	 */
	public static String getMarkMailAsReadQuery(){
		return "UPDATE " + tableName + " SET " + COLUMN_ISREAD + "='1' WHERE " + COLUMN_ITEM_ID + "=? "   ;
	}

    /** UPDATE
     * Mark MAIL AS UN-READ
     * @return
     */
    public static String getMarkMailAsUnReadQuery(){
        return "UPDATE " + tableName + " SET " + COLUMN_ISREAD + "='0' WHERE " + COLUMN_ITEM_ID + "=? "   ;
    }

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

    /** DELETE item
     * WHERE CLAUSE - FOLDER ID
     * @return
     */
    public static String getDeleteItemQuery(){
        return "DELETE from " + tableName + " WHERE " + COLUMN_ITEM_ID + "=?";
    }


    /*** APPLICATION LEVEL QUERIES ***/

	/* These queries will be executed when the table is newly created in the database. 
	 * The default records which needs to be inserted can be mentioned here (like default settings record for settings table or trial data for development)
	 * @see com.sathesh.carparking.db.DbTable#getNewTableQueries()
	 */
	@Override
	public List<String> getNewTableQueries(Context context) {
		return null;
	}

    @Override
    public String getOnUpgradeDropQuery(Context context){
        return "DROP TABLE IF EXISTS " + tableName;
    }


}
