package com.wipromail.sathesh.sqlite.db.cache.tables;

import android.content.Context;
import android.os.Build;

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
    public static final String COLUMN_NAME = "NAME";
    public static final String COLUMN_FOLDER_ID = "FOLDER_ID";
    public static final String COLUMN_TYPE = "TYPE";
    public static final String COLUMN_FONT_ICON = "FONT_ICON";

	@Override
	public String getCreateQuery(Context context){
		return "CREATE TABLE "
				+ tableName + "(" 
				+ COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_NAME + " TEXT,"
                + COLUMN_FOLDER_ID + " TEXT,"
				+ COLUMN_TYPE + " INTEGER,"
                + COLUMN_FONT_ICON + " TEXT"
				+");";
	}


    /*** APPLICATION LEVEL QUERIES ***/
	
	/* These queries will be executed when the table is newly created in the database. 
	 * The default records which needs to be inserted can be mentioned here (like default settings record for settings table or trial data for development)
	 * @see com.sathesh.carparking.db.DbTable#getNewTableQueries()
	 */
	@Override
	public List<String> getNewTableQueries(Context context) {
        String sql1="INSERT INTO " + tableName + " (" + COLUMN_NAME + " ," + COLUMN_TYPE + " ," + COLUMN_FONT_ICON + ") VALUES ('";
        List insertQueries =new ArrayList<String>();
        //adding Inbox Menu
        insertQueries.add(sql1 + context.getString(R.string.drawer_menu_inbox)+ "' ," + DrawerMenuRowType.INBOX+ " , '" + context.getString(R.string.fontIcon_drawer_inbox)+ "');");
        //adding Drafts Menu
        insertQueries.add(sql1 + context.getString(R.string.drawer_menu_drafts)+ "' ," + DrawerMenuRowType.DRAFTS+  " , '" + context.getString(R.string.fontIcon_drawer_drafts)+"');");
        //adding SentItems Menu
        insertQueries.add(sql1 + context.getString(R.string.drawer_menu_sent_items)+ "' ," + DrawerMenuRowType.SENT_ITEMS+  " , '" + context.getString(R.string.fontIcon_drawer_sentItems)+"');");
        //adding Deleted Items Menu
        insertQueries.add(sql1 + context.getString(R.string.drawer_menu_deleted_items)+ "' ," + DrawerMenuRowType.DELETED_ITEMS+  " , '" + context.getString(R.string.fontIcon_drawer_deletedItems)+"');");
        //adding More Folders Menu
        insertQueries.add(sql1 + context.getString(R.string.drawer_menu_more_folders)+ "' ," + DrawerMenuRowType.MORE_FOLDERS+  " , '" + context.getString(R.string.fontIcon_drawer_more_folders)+ "');");

        //adding Favourites Header Menu
        insertQueries.add(sql1 + context.getString(R.string.drawer_menu_favourites)+ "' ," + DrawerMenuRowType.FAVOURITES_HEADER+  " , '');");

        //adding CONTACTS_HEADER Menu
        insertQueries.add(sql1 + context.getString(R.string.drawer_menu_contacts)+ "' ," + DrawerMenuRowType.CONTACTS_HEADER+  " , '');");
        //adding SEARCH_CONTACT Menu
        insertQueries.add(sql1 + context.getString(R.string.drawer_menu_search_contact)+ "' ," + DrawerMenuRowType.SEARCH_CONTACT+  " , '" + context.getString(R.string.fontIcon_drawer_contacts_serach)+"');");

        //adding Settings Menu
        insertQueries.add(sql1 + context.getString(R.string.drawer_menu_settings)+ "' ," + DrawerMenuRowType.SETTINGS+  " , '" + context.getString(R.string.fontIcon_drawer_settings)+ "');");
        //adding About Menu
        insertQueries.add(sql1 + context.getString(R.string.drawer_menu_about)+ "' ," + DrawerMenuRowType.ABOUT+  " , '" + context.getString(R.string.fontIcon_drawer_about)+"');");

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //adding empty row
            insertQueries.add(sql1 + "' ," + DrawerMenuRowType.EMPTY_ROW + " , '');");
        }

        //adding Sample Favourite folders
        insertQueries.add(sql1 + context.getString(R.string.drawer_favourite_help_text) + "' ," + DrawerMenuRowType.FAVOURITE_HELPTEXT +  " , '');");

		return insertQueries;
	}

    @Override
    public String getOnUpgradeDropQuery(Context context){
        return "DROP TABLE IF EXISTS " + tableName;
    }

    /*** SELECT QUERIES ***/

    public static String getAllRecordsQuery(){
        return "SELECT * FROM " + tableName + " ORDER BY " + COLUMN_TYPE + " ASC ";
    }

    /*** WHERE CLAUSE ***/

    public static String getWhereDeleteVO(){
        return  COLUMN_FOLDER_ID + "=?";
    }

    public static String getFavesQuery(){
        return "SELECT * FROM " + tableName
                + " WHERE " + COLUMN_TYPE + "= " + DrawerMenuRowType.FAVOURITE_FOLDERS
                + " ORDER BY " + COLUMN_TYPE + " ASC ";
    }
}
