
package com.sathesh.corporatemail.sqlite.db.cache.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.sathesh.corporatemail.sqlite.db.cache.CacheDbConstants;
import com.sathesh.corporatemail.sqlite.db.cache.CacheDbHelper;
import com.sathesh.corporatemail.sqlite.db.cache.tables.TableDrawerMenu;
import com.sathesh.corporatemail.sqlite.db.cache.vo.FolderVO;

import java.util.List;

/** DAO for the Table DRAWER_MENU
 * @author sathesh
 *
 */

public class DrawerMenuDAO extends BaseCacheDAO {

    //All the DAOs should have fully qualified class names of table class and vo class for auto wiring
    private Class tableClass= TableDrawerMenu.class;
    private Class voClass= FolderVO.class;

    /** Constructor for the DAO. initializes the Database helper
     * @param context
     */
    public DrawerMenuDAO(Context context) {
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
            cursor = database.rawQuery(TableDrawerMenu.getAllRecordsQuery(), null);
            returnList =(List)autoMapCursorToVo(cursor,voClass);
        }finally{
            try{cursor.close();}catch(Exception e ){}
            close(cacheDbHelper);
        }
        return returnList;
    }

    /** Get Favourite Folders
     * @param
     * @return List<VO>
     */
    public List<FolderVO> getFaves() throws Exception {

        List<FolderVO> returnList =null;
        Cursor cursor=null;
        try{
            open(cacheDbHelper);
            //call the select query with where clause as folder id
            cursor = database.rawQuery(TableDrawerMenu.getFavesQuery(), null);
            returnList =(List)autoMapCursorToVo(cursor,voClass);
        }finally{
            try{cursor.close();}catch(Exception e ){}
            close(cacheDbHelper);
        }
        return returnList;
    }

    /** DELETE QUERIES
     *
     * @param vo
     * @throws Exception
     */
    public void deleteVO(FolderVO vo) throws Exception {
        try{
            open(cacheDbHelper);
            deleteVOInDb(vo);
        }
        finally{
            close(cacheDbHelper);
        }
    }

    public void deleteFavourite(FolderVO vo) throws Exception {
        try{
            open(cacheDbHelper);
            deleteFavouriteinDb(vo);
        }
        finally{
            close(cacheDbHelper);
        }
    }

    /*** PRIVATE METHODS ***/

    /** private function which calls the insert query for a single VO
     *
     */
    private long saveVOInDB(FolderVO vo) {
        ContentValues values = autoMapVoToContentValues(vo,tableClass);
        return database.insertWithOnConflict(CacheDbConstants.table.DRAWER_MENU, null,
                values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    private long deleteVOInDb(FolderVO vo) {
        ContentValues values = autoMapVoToContentValues(vo,tableClass);
        return database.delete(CacheDbConstants.table.DRAWER_MENU, TableDrawerMenu.getWhereDeleteVO(), new String[]{vo.getFolder_id()});
    }
    private long deleteFavouriteinDb(FolderVO vo) {
        ContentValues values = autoMapVoToContentValues(vo,tableClass);
        return database.delete(CacheDbConstants.table.DRAWER_MENU, TableDrawerMenu.getWhereDeleteFaveVO(), new String[]{vo.getFolder_id()});
    }
}