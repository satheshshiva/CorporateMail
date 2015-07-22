
package com.wipromail.sathesh.sqlite.db.cache.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.wipromail.sathesh.sqlite.db.cache.CacheDbConstants;
import com.wipromail.sathesh.sqlite.db.cache.CacheDbHelper;
import com.wipromail.sathesh.sqlite.db.cache.tables.TableDrawerMenu;
import com.wipromail.sathesh.sqlite.db.cache.vo.DrawerMenuVO;

import java.util.List;

/** DAO for the Table DRAWER_MENU
 * @author sathesh
 *
 */

public class DrawerMenuDAO extends BaseCacheDAO {

    //All the DAOs should have fully qualified class names of table class and vo class for auto wiring
    private Class tableClass= TableDrawerMenu.class;
    private Class voClass= DrawerMenuVO.class;

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
    public long createOrUpdate(List<DrawerMenuVO> vos) throws Exception {
        long insertId=0;
        try{
            open(cacheDbHelper);
            for(DrawerMenuVO vo: vos){
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
    public long createOrUpdate(DrawerMenuVO vo) throws Exception {
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
    public List<DrawerMenuVO> getAllRecords() throws Exception {

        List<DrawerMenuVO> returnList =null;
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
    public List<DrawerMenuVO> getFaves() throws Exception {

        List<DrawerMenuVO> returnList =null;
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

    /*** PRIVATE METHODS ***/

    /** private function which calls the insert query for a single VO
     *
     */
    private long saveVOInDB(DrawerMenuVO vo) {
        ContentValues values = autoMapVoToContentValues(vo,tableClass);
        return database.insertWithOnConflict(CacheDbConstants.table.DRAWER_MENU, null,
                values, SQLiteDatabase.CONFLICT_REPLACE);
    }
}