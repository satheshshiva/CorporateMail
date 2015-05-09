
package com.wipromail.sathesh.sqlite.db.cache.dao;

import android.content.Context;
import android.database.Cursor;

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
            cursor = database.rawQuery(TableDrawerMenu.getAllRecords(), null);
            returnList =(List)autoMapCursorToVo(cursor,voClass);
        }finally{
            try{cursor.close();}catch(Exception e ){}
            close(cacheDbHelper);
        }
        return returnList;
    }

}