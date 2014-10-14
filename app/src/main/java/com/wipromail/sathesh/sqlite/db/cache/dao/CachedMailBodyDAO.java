package com.wipromail.sathesh.sqlite.db.cache.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.wipromail.sathesh.sqlite.db.cache.CacheDbConstants;
import com.wipromail.sathesh.sqlite.db.cache.CacheDbHelper;
import com.wipromail.sathesh.sqlite.db.cache.vo.CachedMailBodyVO;
import com.wipromail.sathesh.sqlite.db.cache.tables.TableCachedMailBody;

import java.util.List;

/** DAO for the Table MAIL_BODY
 * @author sathesh
 *
 */

public class CachedMailBodyDAO extends BaseCacheDAO {

    //All the DAOs should have fully qualified class names of table class and vo class for auto wiring
    private Class tableClass= com.wipromail.sathesh.sqlite.db.cache.tables.TableCachedMailBody.class;
    private Class voClass= com.wipromail.sathesh.sqlite.db.cache.vo.CachedMailBodyVO.class;


    /** Constructor for the DAO. initializes the Database helper
     * @param context
     */
    public CachedMailBodyDAO(Context context) {
        this.context = context;
        cacheDbHelper = CacheDbHelper.getInstance(context);
    }

    /*** CREATE QUERIES ***/

    /** New record
     *
     * @param vo
     * @return
     * @throws Exception
     */
    public long createOrUpdate(CachedMailBodyVO vo) throws Exception {
        long insertId=0;
        try{
            open(cacheDbHelper);
            insertId= saveVOInDB(vo);
        }
        finally{
            try{close(cacheDbHelper);}catch(Exception e){}
        }
        return insertId;
    }

    /*** SELECT QUERIES ***/

    /** Get records by Item id
     * Where Clause - Item Id
     * @param
     * @return List<VO>
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public List<CachedMailBodyVO> getAllRecordsByItemId(String itemId) throws Exception {
        List<CachedMailBodyVO> returnList =null;
        try{
            open(cacheDbHelper);
            //call the select query with the where clause mail type
            Cursor cursor = database.rawQuery(TableCachedMailBody.getAllRecordsByItemIdQuery(),
                    new String[]{itemId});
            returnList =(List)autoMapCursorToVo(cursor,voClass);
        }finally{
            close(cacheDbHelper);
        }
        return returnList;
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
            open(cacheDbHelper);
            database.execSQL(TableCachedMailBody.getDeleteAllQueryByMailTypeQuery(),
                    new String[]{mailTypeStr});
        }finally{
            close(cacheDbHelper);
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
            open(cacheDbHelper);
            database.execSQL(TableCachedMailBody.getDeleteAllQueryByFolderIdQuery(),
                    new String[]{mailFolderId});
        }finally{
            close(cacheDbHelper);
        }
    }

    /** Delete Cache by N number
     * Where Clause - MailType
     * @param mailType
     * @param n - no of records to keep on top
     * @throws Exception
     */
    public void deleteNByMailType(int mailType, int n) throws Exception {

        try{
            String mailTypeStr = String.valueOf(mailType);
            open(cacheDbHelper);
            database.execSQL(TableCachedMailBody.getDeleteNQueryByMailTypeQuery(n),
                    new String[]{mailTypeStr,mailTypeStr});
        }finally{
            close(cacheDbHelper);
        }
    }

    /** Delete records leaving "n" no of records on top
     * Where Clause - Folder Id
     * @param mailFolderId
     * @param n - no of mails to delete
     * @throws Exception
     */
    public void deleteNByFolderId(String mailFolderId, int n) throws Exception {

        try{
            open(cacheDbHelper);
            database.execSQL(TableCachedMailBody.getDeleteNQueryByFolderIdQuery(n),
                    new String[]{mailFolderId,mailFolderId});
        }finally{
            close(cacheDbHelper);
        }
    }

    /*** PRIVATE METHODS ***/

    /** private function which calls the insert query for a single VO
     *
     */
    private long saveVOInDB(CachedMailBodyVO vo) {
        ContentValues values = autoMapVoToContentValues(vo,tableClass);
        return database.insertWithOnConflict(CacheDbConstants.table.CACHED_MAIL_BODY, null,
                values, SQLiteDatabase.CONFLICT_REPLACE);
    }
}
