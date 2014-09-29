package com.wipromail.sathesh.sqlite.db.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.wipromail.sathesh.sqlite.db.DbConstants;
import com.wipromail.sathesh.sqlite.db.DbHelper;
import com.wipromail.sathesh.sqlite.db.pojo.vo.CachedMailBodyVO;
import com.wipromail.sathesh.sqlite.db.tables.TableCachedMailBody;

import java.util.List;

/** DAO for the Table MAIL_BODY
 * @author sathesh
 *
 */

public class CachedMailBodyDAO extends BaseDAO{

    //All the DAOs should have fully qualified class names of table class and vo class for auto wiring
    private Class tableClass=com.wipromail.sathesh.sqlite.db.tables.TableCachedMailBody.class;
    private Class voClass=com.wipromail.sathesh.sqlite.db.pojo.vo.CachedMailBodyVO.class;


    /** Constructor for the DAO. initializes the Database helper
     * @param context
     */
    public CachedMailBodyDAO(Context context) {
        this.context = context;
        dbHelper = DbHelper.getInstance(context);
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
            open();
            insertId= saveVOInDB(vo);
        }
        finally{
            try{close();}catch(Exception e){}
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
            open();
            //call the select query with the where clause mail type
            Cursor cursor = database.rawQuery(TableCachedMailBody.getAllRecordsByItemIdQuery(),
                    new String[]{itemId});
            returnList =(List)autoMapCursorToVo(cursor,voClass);
        }finally{
            close();
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
            open();
            database.execSQL(TableCachedMailBody.getDeleteAllQueryByMailTypeQuery(),
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
            database.execSQL(TableCachedMailBody.getDeleteAllQueryByFolderIdQuery(),
                    new String[]{mailFolderId});
        }finally{
            close();
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
            open();
            database.execSQL(TableCachedMailBody.getDeleteNQueryByMailTypeQuery(n),
                    new String[]{mailTypeStr,mailTypeStr});
        }finally{
            close();
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
            open();
            database.execSQL(TableCachedMailBody.getDeleteNQueryByFolderIdQuery(n),
                    new String[]{mailFolderId,mailFolderId});
        }finally{
            close();
        }
    }

    /*** PRIVATE METHODS ***/

    /** private function which calls the insert query for a single VO
     *
     */
    private long saveVOInDB(CachedMailBodyVO vo) {
        ContentValues values = autoMapVoToContentValues(vo,tableClass);
        return database.insertWithOnConflict(DbConstants.table.CACHED_MAIL_BODY, null,
                values, SQLiteDatabase.CONFLICT_REPLACE);
    }
}
