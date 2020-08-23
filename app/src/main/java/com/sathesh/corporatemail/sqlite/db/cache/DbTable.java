package com.sathesh.corporatemail.sqlite.db.cache;

import android.content.Context;

import java.util.List;

public interface DbTable {

	 String getCreateQuery(Context context);
	 String getOnUpgradeDropQuery(Context context);
	
	/** These queries will be executed when the table is newly created in the database
	 * @return
	 */
	List<String> getNewTableQueries(Context context);
}
