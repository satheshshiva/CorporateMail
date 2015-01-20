
package com.wipromail.sathesh.sqlite.db.cache.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.wipromail.sathesh.sqlite.db.cache.CacheDbConstants;
import com.wipromail.sathesh.sqlite.db.cache.CacheDbHelper;
import com.wipromail.sathesh.sqlite.db.cache.vo.CachedMailHeaderVO;
import com.wipromail.sathesh.sqlite.db.cache.tables.TableCachedMailHeader;

import java.util.ArrayList;
import java.util.List;

/** DAO for the Table CACHED_MAIL_HEADERS
 * @author sathesh
 *
 */

public class CachedMailHeaderDAO extends BaseCacheDAO {

    //All the DAOs should have fully qualified class names of table class and vo class for auto wiring
    private Class tableClass= com.wipromail.sathesh.sqlite.db.cache.tables.TableCachedMailHeader.class;
    private Class voClass= com.wipromail.sathesh.sqlite.db.cache.vo.CachedMailHeaderVO.class;

    /** Constructor for the DAO. initializes the Database helper
     * @param context
     */
    public CachedMailHeaderDAO(Context context) {
        this.context = context;
        this.cacheDbHelper = CacheDbHelper.getInstance(context);
    }

    /*** CREATE QUERIES ***/

    /** New records
     * @return
     */
    public long createOrUpdate(List<CachedMailHeaderVO> vos) throws Exception {
        long insertId=0;
        try{
            open(cacheDbHelper);
            for(CachedMailHeaderVO vo: vos){
                insertId= saveVOInDB(vo);
            }
        }
        finally{
            try{close(cacheDbHelper);}catch(Exception e){}
        }
        return insertId;
    }

    /** New record
     * @return
     */
    public long createOrUpdate(CachedMailHeaderVO vo) throws Exception {
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
                open(cacheDbHelper);
                //call the select query with the where clause mail type
                Cursor cursor = database.rawQuery(TableCachedMailHeader.getAllRecordsByMailTypeQuery(),
                        new String[]{mailTypeStr});
                returnList =(List)autoMapCursorToVo(cursor,voClass);
            }finally{
                close(cacheDbHelper);
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
            open(cacheDbHelper);
            //call the select query with where clause as folder id
            Cursor cursor = database.rawQuery(TableCachedMailHeader.getAllRecordsByFolderIdQuery(),
                    new String[]{folderId});
            returnList =(List)autoMapCursorToVo(cursor,voClass);
        }finally{
            close(cacheDbHelper);
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
                open(cacheDbHelper);
                Cursor cursor = database.rawQuery(TableCachedMailHeader.getAllRecordsCountByMailTypeQuery(),
                        new String[]{mailTypeStr});
                if(cursor!=null){
                    cursor.moveToFirst();
                    count=cursor.getInt(0);
                }
            }finally{
                close(cacheDbHelper);
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
            open(cacheDbHelper);
            Cursor cursor = database.rawQuery(TableCachedMailHeader.getAllRecordsCountByFolderIdQuery(),
                    new String[]{folderId});
            if(cursor!=null){
                cursor.moveToFirst();
                count=cursor.getInt(0);
            }
        }finally{
            close(cacheDbHelper);
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
                open(cacheDbHelper);
                Cursor cursor = database.rawQuery(TableCachedMailHeader.getUnreadByMailTypeQuery(),
                        new String[]{mailTypeStr});
                if(cursor!=null){
                    cursor.moveToFirst();
                    count=cursor.getInt(0);
                }
            }finally{
                close(cacheDbHelper);
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
            open(cacheDbHelper);
            Cursor cursor = database.rawQuery(TableCachedMailHeader.getUnreadCountByFolderIdQuery(),
                    new String[]{folderId});
            if(cursor!=null){
                cursor.moveToFirst();
                count=cursor.getInt(0);
            }
        }finally{
            close(cacheDbHelper);
        }
        return count;
    }

    /*** UPDATE QUERIES ***/

    /** Marks mail as read
     * Where Clause - Item Id
     * @throws Exception
     */
    public void markMailAsReadUnread(String itemId, boolean isRead) throws Exception {

        try{
            open(cacheDbHelper);
            if(isRead) {
                database.execSQL(TableCachedMailHeader.getMarkMailAsReadQuery(),
                        new String[]{itemId});
            }
            else {
                database.execSQL(TableCachedMailHeader.getMarkMailAsUnReadQuery(),
                        new String[]{itemId});
            }
        }finally{
            close(cacheDbHelper);
        }
    }

    /** Marks mail as read/ unread
     * Where Clause - Item Id
     * param isRead true if the mail is read
     * @throws Exception
     */
    public void markMailsAsReadUnread(ArrayList<String> itemIds, boolean isRead) throws Exception {

        String query ;
        query = isRead ? TableCachedMailHeader.getMarkMailAsReadQuery() : TableCachedMailHeader.getMarkMailAsUnReadQuery();
        try{
            open(cacheDbHelper);
            for(String itemId:itemIds) {
                database.execSQL(query,
                        new String[]{itemId});
            }
        }finally{
            close(cacheDbHelper);
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
            open(cacheDbHelper);
            database.execSQL(TableCachedMailHeader.getDeleteAllQueryByMailTypeQuery(),
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
            database.execSQL(TableCachedMailHeader.getDeleteAllQueryByFolderIdQuery(),
                    new String[]{mailFolderId});
        }finally{
            close(cacheDbHelper);
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
            open(cacheDbHelper);
            database.execSQL(TableCachedMailHeader.getDeleteNQueryByMailTypeQuery(n),
                    new String[]{mailTypeStr,mailTypeStr});
        }finally{
            close(cacheDbHelper);
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
            open(cacheDbHelper);
            database.execSQL(TableCachedMailHeader.getDeleteNQueryByFolderIdQuery(n),
                    new String[]{mailFolderId,mailFolderId});
        }finally{
            close(cacheDbHelper);
        }
    }

    /** Delete Item
     * Where Clause - Folder Id
     * @param itemIds - Item ids to delete
     * @throws Exception
     */
    public void deleteItems(ArrayList<String> itemIds) throws Exception {

        try{
            open(cacheDbHelper);
            for(String itemId: itemIds) {
                database.execSQL(TableCachedMailHeader.getDeleteItemQuery(),
                        new String[]{itemId});
            }
        }finally{
            close(cacheDbHelper);
        }
    }

    /** Delete Item
     * Where Clause - Folder Id
     * @param itemIds - Item ids to delete
     * @throws Exception
     */
    public void deleteItem(ArrayList<String> itemIds) throws Exception {

        try{
            open(cacheDbHelper);
            for(String itemId: itemIds) {
                database.execSQL(TableCachedMailHeader.getDeleteItemQuery(),
                        new String[]{itemId});
            }
        }finally{
            close(cacheDbHelper);
        }
    }

    /** Delete Item Id
     * Where Clause - Folder Id
     * @param itemId - Item id to delete
     * @throws Exception
     */
    public void deleteItemId(String itemId) throws Exception {

        try{
            open(cacheDbHelper);
            database.execSQL(TableCachedMailHeader.getDeleteItemQuery(),
                    new String[]{itemId});
        }finally{
            close(cacheDbHelper);
        }
    }

    /*** PRIVATE METHODS ***/

    /** private function which calls the insert query for a single VO
     *
     */
    private long saveVOInDB(CachedMailHeaderVO vo) {
        ContentValues values = autoMapVoToContentValues(vo,tableClass);
        return database.insertWithOnConflict(CacheDbConstants.table.CACHED_MAIL_HEADERS, null,
                values,SQLiteDatabase.CONFLICT_REPLACE);
    }

}