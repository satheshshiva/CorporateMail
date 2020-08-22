package com.sathesh.corporatemail.sqlite.db.cache.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.sathesh.corporatemail.sqlite.db.cache.CacheDbConstants;
import com.sathesh.corporatemail.sqlite.db.cache.CacheDbHelper;
import com.sathesh.corporatemail.sqlite.db.cache.tables.TableCachedMailAttachmentMeta;
import com.sathesh.corporatemail.sqlite.db.cache.vo.CachedAttachmentMetaVO;

import java.util.Date;
import java.util.List;

/** DAO for the Table CACHED_ATTACHMENT_META
 * @author sathesh
 *
 */

public class CachedAttachmentsMetaDAO extends BaseCacheDAO {

    //All the DAOs should have fully qualified class names of table class and vo class for auto wiring
    private Class tableClass= TableCachedMailAttachmentMeta.class;
    private Class voClass= CachedAttachmentMetaVO.class;


    /** Constructor for the DAO. initializes the Database helper
     * @param context
     */
    public CachedAttachmentsMetaDAO(Context context) {
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
    public long createOrUpdate(CachedAttachmentMetaVO vo) throws Exception {
        long insertId=0;
        try{
            open(cacheDbHelper);
            insertId= saveVOInDB(vo);
        }
        finally{
            close(cacheDbHelper);
        }
        return insertId;
    }

    /** New records
     *
     * @param
     * @return
     * @throws Exception
     */
    public long createOrUpdate(List<CachedAttachmentMetaVO> vos) throws Exception {
        long insertId=0;
        try{
            open(cacheDbHelper);

            for(CachedAttachmentMetaVO vo:vos) {
                insertId = saveVOInDB(vo);
            }
        }
        finally{
            close(cacheDbHelper);
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
    public List<CachedAttachmentMetaVO> getAllRecordsByItemId(String itemId) throws Exception {
        List<CachedAttachmentMetaVO> returnList =null;
        Cursor cursor=null;
        try{
            open(cacheDbHelper);
            //call the select query with the where clause mail type
            cursor = database.rawQuery(TableCachedMailAttachmentMeta.getAllRecordsByItemId(),
                    new String[]{itemId});
            returnList =(List)autoMapCursorToVo(cursor,voClass);
        }finally{
            try{cursor.close();}catch(Exception e ){}
            close(cacheDbHelper);
        }
        return returnList;
    }


    /** Get records by Item id and attachment id
     * Where Clause - Item Id and attachment id
     * @param
     * @return List<VO>
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public List<CachedAttachmentMetaVO> getAllRecordsByItemIdAttachmentId(String itemId, String attachmentId) throws Exception {
        List<CachedAttachmentMetaVO> returnList =null;
        Cursor cursor=null;
        try{
            open(cacheDbHelper);
            //call the select query with the where clause mail type
            cursor = database.rawQuery(TableCachedMailAttachmentMeta.getAllRecordsByItemIdAttachmentId(),
                    new String[]{itemId, attachmentId});
            returnList =(List)autoMapCursorToVo(cursor,voClass);
        }finally{
            try{cursor.close();}catch(Exception e ){}
            close(cacheDbHelper);
        }
        return returnList;
    }

    /*** UPDATE QUERIES ***/

    /** Marks mail as read
     * Where Clause - Item Id
     * @throws Exception
     */
    public void updateLastAccessedTime(String itemId, String attachmentId, String filePath) throws Exception {

        try{
            open(cacheDbHelper);
            database.execSQL(TableCachedMailAttachmentMeta.updateLastAccessedTime(),
                        new String[]{String.valueOf(System.currentTimeMillis()), filePath, itemId, attachmentId});
        }finally{
            close(cacheDbHelper);
        }
    }

    /*** DELETE QUERIES ***/
    public void delete(List<CachedAttachmentMetaVO> vos) throws Exception {

        try{
            open(cacheDbHelper);
            for(CachedAttachmentMetaVO vo: vos) {
                database.execSQL(TableCachedMailAttachmentMeta.deleteItemIdAttachment(),
                        new String[]{vo.getItem_id(), vo.getAttachment_id()});
            }
        }finally{
            close(cacheDbHelper);
        }
    }

    /*** PRIVATE METHODS ***/

    /** private function which calls the insert query for a single VO
     *
     */
    private long saveVOInDB(CachedAttachmentMetaVO vo) {
        ContentValues values = autoMapVoToContentValues(vo,tableClass);
        values.put(TableCachedMailAttachmentMeta.COLUMN_CREATED_DATE, System.currentTimeMillis());
        return database.insertWithOnConflict(CacheDbConstants.table.CACHED_ATTACHMENT_META, null,
                values, SQLiteDatabase.CONFLICT_REPLACE);
    }
}
