
package com.sathesh.corporatemail.sqlite.db.cache.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.sathesh.corporatemail.sqlite.db.cache.CacheDbConstants;
import com.sathesh.corporatemail.sqlite.db.cache.CacheDbHelper;
import com.sathesh.corporatemail.sqlite.db.cache.tables.TableMoreFolders;
import com.sathesh.corporatemail.sqlite.db.cache.vo.FolderVO;

import java.util.List;

/** DAO for the Table MORE_FOLDERS
 * @author sathesh
 *
 */

public class MoreFoldersDAO extends BaseCacheDAO {

    //All the DAOs should have fully qualified class names of table class and vo class for auto wiring
    private Class tableClass= TableMoreFolders.class;
    private Class voClass= FolderVO.class;

    /** Constructor for the DAO. initializes the Database helper
     * @param context
     */
    public MoreFoldersDAO(Context context) {
        this.context = context;
        this.cacheDbHelper = CacheDbHelper.getInstance(context);
    }

    /*** CREATE QUERIES ***/

    /** New records
     * @return
     */
    public long createOrUpdate(List<FolderVO> vos) throws Exception {
        long insertId=0;
        try{
            open(cacheDbHelper);
            for(FolderVO vo: vos){
                insertId= saveVOInDB(vo);
            }
        }
        finally{
            close(cacheDbHelper);
        }
        return insertId;
    }

    /** New record
     * @return
     */
    public long createOrUpdate(FolderVO vo) throws Exception {
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

    /*** UPDATE QUERIES ***/
    public long update(FolderVO vo) throws Exception {
        long insertId=0;
        try{
            open(cacheDbHelper);
            insertId= updateVOInDB(vo);
        }
        finally{
            close(cacheDbHelper);
        }
        return insertId;
    }

    /*** SELECT QUERIES ***/

    /** Get all records
     * @param
     * @return List<VO>
     */
    public List<FolderVO> getAllRecords() throws Exception {

        List<FolderVO> returnList =null;
        Cursor cursor=null;
        try{
            open(cacheDbHelper);
            //call the select query with where clause as folder id
            cursor = database.rawQuery(TableMoreFolders.getAllRecordsQuery(), null);
            returnList =(List)autoMapCursorToVo(cursor,voClass);
        }finally{
            try{cursor.close();}catch(Exception e ){}
            close(cacheDbHelper);
        }
        return returnList;
    }

    /** Delete All records
     * Where Clause - Folder Id
     * @throws Exception
     */
    public void deleteAllRecords() throws Exception {

        try{
            open(cacheDbHelper);
            database.execSQL(TableMoreFolders.getDeleteAllQuery());
        }finally{
            close(cacheDbHelper);
        }
    }

    /*** PRIVATE METHODS ***/

    /** private function which calls the insert query for a single VO
     *
     */
    private long saveVOInDB(FolderVO vo) {
        ContentValues values = autoMapVoToContentValues(vo,tableClass);
        return database.insertWithOnConflict(CacheDbConstants.table.MORE_FOLDERS, null,
                values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    /** private function which calls the update query for a single VO
     *
     */
    private long updateVOInDB(FolderVO vo) {
        ContentValues values = autoMapVoToContentValues(vo,tableClass);
        return database.update(CacheDbConstants.table.MORE_FOLDERS,
                values, TableMoreFolders.getWhereForUpdateQuery(), new String[]{vo.getFolder_id()});
    }
}