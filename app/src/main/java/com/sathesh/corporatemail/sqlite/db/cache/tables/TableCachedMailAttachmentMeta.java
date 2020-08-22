package com.sathesh.corporatemail.sqlite.db.cache.tables;

import android.content.Context;

import com.sathesh.corporatemail.sqlite.db.cache.CacheDbConstants;
import com.sathesh.corporatemail.sqlite.db.cache.DbTable;

import java.util.List;

/**  Table to store the file attachments meta data. The actual file is not stored here
 * @author sathesh
 *
 */
public class TableCachedMailAttachmentMeta implements CacheDbConstants, DbTable{

	private static String tableName = table.CACHED_ATTACHMENT_META;
	
	public static final String COLUMN_ID = "ID";
	public static final String COLUMN_ITEM_ID = "ITEM_ID";
	public static final String COLUMN_ATTACHMENT_ID = "ATTACHMENT_ID";
    public static final String COLUMN_FILE_NAME = "FILE_NAME";
	public static final String COLUMN_SIZE_BYTES = "SIZE_BYTES";
	public static final String COLUMN_HUMAN_READABLE_SIZE = "HUMAN_READABLE_SIZE";
	public static final String COLUMN_FILE_PATH = "FILE_PATH";
	public static final String COLUMN_CONTENT_TYPE = "CONTENT_TYPE";
	public static final String COLUMN_CREATED_DATE= "CREATED_DATE";
	public static final String COLUMN_LAST_ACCESSED_DATE= "LAST_ACCESSED_DATE";

	@Override
	public String getCreateQuery(Context context){
		return "CREATE TABLE "
				+ tableName + "(" 
				+ COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ COLUMN_ITEM_ID + " TEXT,"
				+ COLUMN_ATTACHMENT_ID + " TEXT,"
                + COLUMN_FILE_NAME + " TEXT,"
				+ COLUMN_SIZE_BYTES + " INTEGER,"
				+ COLUMN_HUMAN_READABLE_SIZE + " TEXT,"
				+ COLUMN_FILE_PATH + " TEXT,"
				+ COLUMN_CONTENT_TYPE + " TEXT,"
				+ COLUMN_CREATED_DATE + " INT,"
				+ COLUMN_LAST_ACCESSED_DATE + " INT"
				+");";
	}

    /** SELECT QUERY
     * WHERE CLAUSE -  Item Id
     * @return
     */
    public static String getAllRecordsByItemId(){
        return "SELECT * FROM " + tableName + " WHERE " + COLUMN_ITEM_ID + "=?";
    }

    /** SELECT QUERY
     * WHERE CLAUSE -  Item Id+Attachment Id
     * @return
     */
    public static String getAllRecordsByItemIdAttachmentId(){
        return "SELECT * FROM " + tableName + " WHERE " + COLUMN_ITEM_ID + "=? AND " + COLUMN_ATTACHMENT_ID + "=?";
    }

    /** UPDATE
     * WHERE CLAUSE -  Item Id+Attachment Id
     * @return
     */
    public static String updateLastAccessedTime(){
        return "UPDATE " + tableName + " SET " + COLUMN_LAST_ACCESSED_DATE + "=?," + COLUMN_FILE_PATH + "=? WHERE " + COLUMN_ITEM_ID + "=? AND " + COLUMN_ATTACHMENT_ID + "=?";
    }

	/** DELETE
	 * WHERE CLAUSE -  Item Id+Attachment Id
	 * @return
	 */
	public static String deleteItemIdAttachment(){
		return "DELETE FROM " + tableName + " WHERE " + COLUMN_ITEM_ID + "=? AND " + COLUMN_ATTACHMENT_ID + "=?";
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
