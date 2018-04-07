package com.sathesh.corporatemail.sqlite.db.cache.views;

import android.content.Context;

import com.sathesh.corporatemail.constants.Constants;
import com.sathesh.corporatemail.sqlite.db.cache.CacheDbConstants;
import com.sathesh.corporatemail.sqlite.db.cache.DbTable;

import java.util.List;

/**  Table to store the cached mail body
 * @author sathesh
 *
 */
public class ViewDrawerMenu implements CacheDbConstants, DbTable, Constants{

    private static String viewName = table.DRAWER_MENU;

    public static final String COLUMN_ID = "ID";
    public static final String COLUMN_MENU_NAME = "MENU_NAME";
    public static final String COLUMN_FOLDER_ID = "FOLDER_ID";
    public static final String COLUMN_FOLDER_TYPE = "FOLDER_TYPE";
    public static final String IS_HEADER = "IS_HEADER";
    public static final String IS_FAVOURITE = "IS_FAVOURITE";

    @Override
    public String getCreateQuery(Context context){
        return "CREATE VIEW "
                + viewName + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_MENU_NAME + " TEXT,"
                + COLUMN_FOLDER_ID + " TEXT,"
                + COLUMN_FOLDER_TYPE + " TEXT,"
                + IS_HEADER + " TEXT,"
                + IS_FAVOURITE + " TEXT"
                +");";
    }


    /*** APPLICATION LEVEL QUERIES ***/
	
	/* These queries will be executed when the table is newly created in the database. 
	 * The default records which needs to be inserted can be mentioned here (like default settings record for settings table or trial data for development)
	 * @see com.sathesh.carparking.db.DbTable#getNewTableQueries()
	 */
    @Override
    public List<String> getNewTableQueries(Context context) {
        return null;
    }

    @Override
    public String getOnUpgradeDropQuery(Context context){
        return "DROP VIEW IF EXISTS " + viewName;
    }

}
