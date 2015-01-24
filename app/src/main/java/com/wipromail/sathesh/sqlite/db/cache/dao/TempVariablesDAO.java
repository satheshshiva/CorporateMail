package com.wipromail.sathesh.sqlite.db.cache.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.wipromail.sathesh.sqlite.db.cache.CacheDbConstants;
import com.wipromail.sathesh.sqlite.db.cache.CacheDbHelper;
import com.wipromail.sathesh.sqlite.db.cache.vo.TempVariablesVO;

import java.util.List;

/** DAO for the Table TEMP_VARIABLES
 * @author sathesh
 *
 */

public class TempVariablesDAO extends BaseCacheDAO {

    //All the DAOs should have fully qualified class names of table class and vo class for auto wiring
    private Class tableClass= com.wipromail.sathesh.sqlite.db.cache.tables.TableTempVariables.class;
    private Class voClass= com.wipromail.sathesh.sqlite.db.cache.vo.TempVariablesVO.class;


    /** Constructor for the DAO. initializes the Database helper
     * @param context
     */
    public TempVariablesDAO(Context context) {
        this.context = context;
        cacheDbHelper = CacheDbHelper.getInstance(context);
    }


    /** New record
     * @return
     */
    public long createOrUpdate(TempVariablesVO vo) throws Exception {
        long insertId=0;
        ContentValues values = autoMapVoToContentValues(vo,tableClass);
        try{
            open(cacheDbHelper);
            insertId = database.insertWithOnConflict(CacheDbConstants.table.TEMP_VARIABLES, null,
                    values,SQLiteDatabase.CONFLICT_REPLACE);
        }
        finally{
            try{close(cacheDbHelper);}catch(Exception e){}
        }
        return insertId;
    }

    /** Get all records
     * @return
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public List<TempVariablesVO> getAllRecords() throws Exception {

        List<TempVariablesVO> returnList =null;
        Cursor cursor=null;
        try{
            open(cacheDbHelper);
            cursor = database.query(CacheDbConstants.table.TEMP_VARIABLES,
                    null, null, null, null, null, null);
            returnList =(List)autoMapCursorToVo(cursor,voClass);
        }finally{
            try{cursor.close();}catch (Exception e){}
            close(cacheDbHelper);
        }
        return returnList;
    }
}
