package com.wipromail.sathesh.sqlite.db.cache;

import com.wipromail.sathesh.sqlite.db.cache.tables.TableCachedMailBody;
import com.wipromail.sathesh.sqlite.db.cache.tables.TableCachedMailHeader;
import com.wipromail.sathesh.sqlite.db.cache.tables.TableDrawerMenu;
import com.wipromail.sathesh.sqlite.db.cache.tables.TableTempVariables;

import java.util.ArrayList;

/** When a new table is added, add it here to get initialized in DbHelper. 
 * Start from here for adding a new table.
 * @author sathesh
 *
 */
public class TableMetaData {

	public static ArrayList<DbTable> getListOfAllTables(){
		DbTable dbTable;
		ArrayList<DbTable> list = new ArrayList<DbTable>();
		dbTable= new TableCachedMailHeader();
		list.add(dbTable);
		dbTable= new TableCachedMailBody();
		list.add(dbTable);
        dbTable= new TableDrawerMenu();
        list.add(dbTable);
		dbTable= new TableTempVariables();
		list.add(dbTable);
		return list;
	}
	
}
