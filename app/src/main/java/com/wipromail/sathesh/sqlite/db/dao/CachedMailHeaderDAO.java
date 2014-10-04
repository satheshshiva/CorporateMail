
package com.wipromail.sathesh.sqlite.db.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.wipromail.sathesh.sqlite.db.DbConstants;
import com.wipromail.sathesh.sqlite.db.DbHelper;
import com.wipromail.sathesh.sqlite.db.pojo.vo.CachedMailHeaderVO;
import com.wipromail.sathesh.sqlite.db.tables.TableCachedMailHeader;

import java.util.List;

/** DAO for the Table CACHED_MAIL_HEADERS
 * @author sathesh
 *
 */

public class CachedMailHeaderDAO extends BaseDAO{

	//All the DAOs should have fully qualified class names of table class and vo class for auto wiring
	private Class tableClass=com.wipromail.sathesh.sqlite.db.tables.TableCachedMailHeader.class;
	private Class voClass=com.wipromail.sathesh.sqlite.db.pojo.vo.CachedMailHeaderVO.class;

	/** Constructor for the DAO. initializes the Database helper
	 * @param context
	 */
	public CachedMailHeaderDAO(Context context) {
		this.context = context;
		this.dbHelper = DbHelper.getInstance(context);
	}

    /*** CREATE QUERIES ***/

	/** New records
	 * @return
	 */
	public long createOrUpdate(List<CachedMailHeaderVO> vos) throws Exception {
		long insertId=0;
		try{
			open(dbHelper);
			for(CachedMailHeaderVO vo: vos){
				insertId= saveVOInDB(vo);
			}
		}
		finally{
			try{close(dbHelper);}catch(Exception e){}
		}
		return insertId;
	}

    /*** SELECT QUERIES ***/

	/** Get all records
	 * Where Clause - Mail Type
	 * @param 
	 * @return List<VO>
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<CachedMailHeaderVO> getAllRecordsByMailType(int mailType) throws Exception {

		List<CachedMailHeaderVO> returnList =null;
		if(mailType>0){
			String mailTypeStr = String.valueOf(mailType);
			try{
				open(dbHelper);
				//call the select query with the where clause mail type
				Cursor cursor = database.rawQuery(TableCachedMailHeader.getAllRecordsByMailTypeQuery(),
						new String[]{mailTypeStr});
				returnList =(List)autoMapCursorToVo(cursor,voClass);
			}finally{
				close(dbHelper);
			}
		}
		return returnList;
	}

	/** Get all records
	 * Where Clause - Folder Id
	 * @param 
	 * @return List<VO>
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<CachedMailHeaderVO> getAllRecordsByFolderId(String folderId) throws Exception {

		List<CachedMailHeaderVO> returnList =null;
		try{
			open(dbHelper);
			//call the select query with where clause as folder id
			Cursor cursor = database.rawQuery(TableCachedMailHeader.getAllRecordsByFolderIdQuery(),
					new String[]{folderId});
			returnList =(List)autoMapCursorToVo(cursor,voClass);
		}finally{
			close(dbHelper);
		}
		return returnList;
	}

	/** Get Records Count.
	 * Where Clause - MailType
	 * @param 
	 * @return int count. -1 if an exception
	 */
	public int getRecordsCountByMailType(int mailType) throws Exception {

		int count=-1;
		if(mailType>0){
			String mailTypeStr = String.valueOf(mailType);
			try{
				open(dbHelper);
				Cursor cursor = database.rawQuery(TableCachedMailHeader.getAllRecordsCountByMailTypeQuery(), 
						new String[]{mailTypeStr});
				if(cursor!=null){
					cursor.moveToFirst();
					count=cursor.getInt(0);
				}
			}finally{
				close(dbHelper);
			}
		}
		return count;
	}

	/** Get Records Count.
	 * Where Clause - Folder Id
	 * @param 
	 * @return int count. -1 if an exception
	 */
	public int getRecordsCountByFolderId(String folderId) throws Exception {

		int count=-1;
		try{
			open(dbHelper);
			Cursor cursor = database.rawQuery(TableCachedMailHeader.getAllRecordsCountByFolderIdQuery(), 
					new String[]{folderId});
			if(cursor!=null){
				cursor.moveToFirst();
				count=cursor.getInt(0);
			}
		}finally{
			close(dbHelper);
		}
		return count;
	}

	/** Get Unread mail count by Mail Type
	 * Where Clause - MailType
	 * @param 
	 * @return int count. -1 if an exception
	 */
	public int getUnreadByMailType(int mailType) throws Exception {

		int count=-1;
		if(mailType>0){
			String mailTypeStr = String.valueOf(mailType);
			try{
				open(dbHelper);
				Cursor cursor = database.rawQuery(TableCachedMailHeader.getUnreadByMailTypeQuery(), 
						new String[]{mailTypeStr});
				if(cursor!=null){
					cursor.moveToFirst();
					count=cursor.getInt(0);
				}
			}finally{
				close(dbHelper);
			}
		}
		return count;
	}

	/** Get unread mail count by FolderId
	 * Where Clause - Folder Id
	 * @param 
	 * @return int count. -1 if an exception
	 */
	public int getUnreadCountByFolderId(String folderId) throws Exception {

		int count=-1;
		try{
			open(dbHelper);
			Cursor cursor = database.rawQuery(TableCachedMailHeader.getUnreadCountByFolderIdQuery(), 
					new String[]{folderId});
			if(cursor!=null){
				cursor.moveToFirst();
				count=cursor.getInt(0);
			}
		}finally{
			close(dbHelper);
		}
		return count;
	}

    /*** UPDATE QUERIES ***/

    /** Marks mail as read
     * Where Clause - Item Id
     * @throws Exception
     */
    public void markMailAsRead(String itemId) throws Exception {

        try{
            open(dbHelper);
            database.execSQL(TableCachedMailHeader.getMarkMailAsReadQuery(),
                    new String[]{itemId});
        }finally{
            close(dbHelper);
        }
    }


	/*** DELETE QUERIES ***/

	/** Delete Cache
	 * Where Clause - MailType
	 * @param 
	 * @return 
	 */
	public void deleteAllByMailType(int mailType) throws Exception {
		
		try{
			String mailTypeStr = String.valueOf(mailType);
			open(dbHelper);
			database.execSQL(TableCachedMailHeader.getDeleteAllQueryByMailTypeQuery(),
					new String[]{mailTypeStr});
		}finally{
			close(dbHelper);
		}
	}

	/** Delete Cache
	 * Where Clause - Folder Id
	 * 
	 * @param 
	 * @return 
	 */
	public void deleteAllByFolderId(String mailFolderId) throws Exception {

		try{
			open(dbHelper);
			database.execSQL(TableCachedMailHeader.getDeleteAllQueryByFolderIdQuery(),
					new String[]{mailFolderId});
		}finally{
			close(dbHelper);
		}
	}

	/** Delete Cache by N number
	 * Where Clause - MailType
	 * @param mailType
	 * @param n -	no of mails to delete
	 * @throws Exception
	 */
	public void deleteNByMailType(int mailType, int n) throws Exception {
		
		try{
			String mailTypeStr = String.valueOf(mailType);
			open(dbHelper);
			database.execSQL(TableCachedMailHeader.getDeleteNQueryByMailTypeQuery(n),
					new String[]{mailTypeStr,mailTypeStr});
		}finally{
			close(dbHelper);
		}
	}

	/** Delete Cache by N number
	 * Where Clause - Folder Id
	 * @param mailFolderId
	 * @param n - no of mails to delete
	 * @throws Exception
	 */
	public void deleteNByFolderId(String mailFolderId, int n) throws Exception {

		try{
			open(dbHelper);
			database.execSQL(TableCachedMailHeader.getDeleteNQueryByFolderIdQuery(n),
					new String[]{mailFolderId,mailFolderId});
		}finally{
			close(dbHelper);
		}
	}

    /*** PRIVATE METHODS ***/

	/** private function which calls the insert query for a single VO
	 * 
	 */
	private long saveVOInDB(CachedMailHeaderVO vo) {
		// TODO Auto-generated method stub
		ContentValues values = autoMapVoToContentValues(vo,tableClass);
		return database.insertWithOnConflict(DbConstants.table.CACHED_MAIL_HEADERS, null,
				values,SQLiteDatabase.CONFLICT_REPLACE);
	}
	
}