package com.sathesh.corporatemail.sqlite.db.cache;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.sathesh.corporatemail.constants.Constants;

import java.util.ArrayList;
import java.util.List;

public class CacheDbHelper extends SQLiteOpenHelper implements Constants, CacheDbConstants {

	private static CacheDbHelper cacheDbHelper;
    private static Context context;
	public CacheDbHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	
	/** use a singleton object to avoid memory leaks to call the close() to the correct object
	 * @param context
	 * @return
	 */
	public static CacheDbHelper getInstance(Context context){
		
		if(cacheDbHelper ==null){
			cacheDbHelper = new CacheDbHelper(context.getApplicationContext());
		}
        CacheDbHelper.context = context;

		return cacheDbHelper;
	}

	@Override
	public void onCreate(SQLiteDatabase database) {
		ArrayList<DbTable> listTables = TableMetaData.getListOfAllTables();
		for(DbTable dbTable : listTables){
			database.execSQL(dbTable.getCreateQuery(context));
			//execute the default queries for the table (if any)
			List<String> defaultQueries = dbTable.getNewTableQueries(context);
			if(defaultQueries!=null && defaultQueries.size()>0){
				for(String query : defaultQueries){
					database.execSQL(query);
				}
			}
		}
		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(LOG_TAG,
				"Upgrading database from version " + oldVersion + " to "
						+ newVersion + ", which will destroy all old data");
		ArrayList<DbTable> listTables = TableMetaData.getListOfAllTables();
		for(DbTable dbTable : listTables){
			db.execSQL(dbTable.getOnUpgradeDropQuery(context));
		}
		onCreate(db);
	}

    /** Deletes this database
     * 
     * @param context
     */
    public static void deleteDatabase(Context context){

        context.deleteDatabase(DATABASE_NAME);
        cacheDbHelper=null;
    }
}
