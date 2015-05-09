package com.wipromail.sathesh.sqlite.db.cache.tables;

import android.content.Context;

import com.wipromail.sathesh.R;
import com.wipromail.sathesh.constants.Constants;
import com.wipromail.sathesh.constants.DrawerMenuRowType;
import com.wipromail.sathesh.sqlite.db.cache.CacheDbConstants;
import com.wipromail.sathesh.sqlite.db.cache.DbTable;

import java.util.ArrayList;
import java.util.List;

/**  Table to store the cached mail body
 * @author sathesh
 *
 */
public class TableDrawerMenu implements CacheDbConstants, DbTable, Constants{

	private static String tableName = table.DRAWER_MENU;
	
	public static final String COLUMN_ID = "ID";
    public static final String COLUMN_MENU_NAME = "MENU_NAME";
    public static final String COLUMN_FOLDER_ID = "FOLDER_ID";
    public static final String COLUMN_TYPE = "TYPE";


	@Override
	public String getCreateQuery(Context context){
		return "CREATE TABLE "
				+ tableName + "(" 
				+ COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_MENU_NAME + " TEXT,"
                + COLUMN_FOLDER_ID + " TEXT,"
				+ COLUMN_TYPE + " INTEGER"
				+");";
	}


    /*** APPLICATION LEVEL QUERIES ***/
	
	/* These queries will be executed when the table is newly created in the database. 
	 * The default records which needs to be inserted can be mentioned here (like default settings record for settings table or trial data for development)
	 * @see com.sathesh.carparking.db.DbTable#getNewTableQueries()
	 */
	@Override
	public List<String> getNewTableQueries(Context context) {
        String sql1="INSERT INTO " + tableName + " (" + COLUMN_MENU_NAME + " ," + COLUMN_TYPE + ") VALUES ('";
        List insertQueries =new ArrayList<String>();
        insertQueries.add(sql1 + context.getString(R.string.drawer_menu_inbox)+ "' ," + DrawerMenuRowType.INBOX+ ");");
        insertQueries.add(sql1 + context.getString(R.string.drawer_menu_drafts)+ "' ," + DrawerMenuRowType.DRAFTS+ ");");
        insertQueries.add(sql1 + context.getString(R.string.drawer_menu_sent_items)+ "' ," + DrawerMenuRowType.SENT_ITEMS+ ");");
        insertQueries.add(sql1 + context.getString(R.string.drawer_menu_deleted_items)+ "' ," + DrawerMenuRowType.DELETED_ITEMS+ ");");

        insertQueries.add(sql1 + context.getString(R.string.drawer_menu_more_folders)+ "' ," + DrawerMenuRowType.MORE_FOLDERS+ ");");

        insertQueries.add(sql1 + context.getString(R.string.drawer_menu_favourites)+ "' ," + DrawerMenuRowType.FAVOURITES_HEADER+ ");");

        insertQueries.add(sql1 + context.getString(R.string.drawer_menu_settings)+ "' ," + DrawerMenuRowType.SETTINGS+ ");");
        insertQueries.add(sql1 + context.getString(R.string.drawer_menu_about)+ "' ," + DrawerMenuRowType.ABOUT+ ");");

        insertQueries.add(sql1 + "fave1" + "' ," + DrawerMenuRowType.FAVOURITE_FOLDERS + ");");
        insertQueries.add(sql1 + "fave2" + "' ," + DrawerMenuRowType.FAVOURITE_FOLDERS + ");");

		return insertQueries;
	}

    @Override
    public String getOnUpgradeDropQuery(Context context){
        return "DROP TABLE IF EXISTS " + tableName;
    }
	
}
