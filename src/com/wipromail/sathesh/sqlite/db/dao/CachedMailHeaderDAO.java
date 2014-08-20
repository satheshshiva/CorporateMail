
package com.wipromail.sathesh.sqlite.db.dao;

import java.util.Arrays;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.wipromail.sathesh.sqlite.db.DbConstants;
import com.wipromail.sathesh.sqlite.db.DbHelper;
import com.wipromail.sathesh.sqlite.db.pojo.vo.CachedMailHeaderVO;
import com.wipromail.sathesh.sqlite.db.tables.TableCachedMailHeader;

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
		dbHelper = DbHelper.getInstance(context);
	}

	/** New records
	 * @param vo
	 * @return
	 */
	public long createOrUpdate(List<CachedMailHeaderVO> vos) throws Exception {
		long insertId=0;
		try{
			open();
			for(CachedMailHeaderVO vo: vos){
				insertId= saveVOInDB(vo);
			}
		}
		finally{
			try{close();}catch(Exception e){}
		}
		return insertId;
	}

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
				open();
				//call the select query with the where clause mail type
				Cursor cursor = database.rawQuery(TableCachedMailHeader.getAllRecordsByMailTypeQuery(),
						new String[]{mailTypeStr});
				returnList =(List)autoMapCursorToVo(cursor,voClass);
			}finally{
				close();
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
			open();
			//call the select query with where clause as folder id
			Cursor cursor = database.rawQuery(TableCachedMailHeader.getAllRecordsByFolderIdQuery(),
					new String[]{folderId});
			returnList =(List)autoMapCursorToVo(cursor,voClass);
		}finally{
			close();
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
				open();
				Cursor cursor = database.rawQuery(TableCachedMailHeader.getAllRecordsCountByMailTypeQuery(), 
						new String[]{mailTypeStr});
				if(cursor!=null){
					cursor.moveToFirst();
					count=cursor.getInt(0);
				}
			}finally{
				close();
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
			open();
			Cursor cursor = database.rawQuery(TableCachedMailHeader.getAllRecordsCountByFolderIdQuery(), 
					new String[]{folderId});
			if(cursor!=null){
				cursor.moveToFirst();
				count=cursor.getInt(0);
			}
		}finally{
			close();
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
				open();
				Cursor cursor = database.rawQuery(TableCachedMailHeader.getUnreadByMailTypeQuery(), 
						new String[]{mailTypeStr});
				if(cursor!=null){
					cursor.moveToFirst();
					count=cursor.getInt(0);
				}
			}finally{
				close();
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
			open();
			Cursor cursor = database.rawQuery(TableCachedMailHeader.getUnreadCountByFolderIdQuery(), 
					new String[]{folderId});
			if(cursor!=null){
				cursor.moveToFirst();
				count=cursor.getInt(0);
			}
		}finally{
			close();
		}
		return count;
	}

	
	/** Delete Cache
	 * Where Clause - MailType
	 * @param 
	 * @return 
	 */
	public void deleteAllByMailType(int mailType) throws Exception {
		
		try{
			String mailTypeStr = String.valueOf(mailType);
			open();
			database.execSQL(TableCachedMailHeader.getDeleteAllQueryByMailTypeQuery(),
					new String[]{mailTypeStr});
		}finally{
			close();
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
			open();
			database.execSQL(TableCachedMailHeader.getDeleteAllQueryByFolderIdQuery(),
					new String[]{mailFolderId});
		}finally{
			close();
		}
	}

	/** private function which calls the insert query for a single VO
	 * 
	 */
	private long saveVOInDB(CachedMailHeaderVO vo) {
		// TODO Auto-generated method stub
		ContentValues values = autoMapVoToContentValues(vo,tableClass);
		return database.insertWithOnConflict(DbConstants.table.CACHED_MAIL_HEADERS, null,
				values,SQLiteDatabase.CONFLICT_REPLACE);
	}
	
	public CachedMailHeaderVO autoMapCursorToVOAtPoisition(Cursor cursor, int position) throws Exception{
		CachedMailHeaderVO vo= (CachedMailHeaderVO)processCursorToVOAtPosition(cursor,position,voClass);
		return vo;
	}
	
}