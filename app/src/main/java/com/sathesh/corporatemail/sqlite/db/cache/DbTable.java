package com.sathesh.corporatemail.sqlite.db.cache;

import android.content.Context;

import java.util.List;

public interface DbTable {

	public  String getCreateQuery(Context context);
	public  String getOnUpgradeDropQuery(Context context);
	
	/** These queries will be executed when the table is newly created in the database
	 * @return
	 */
	public List<String> getNewTableQueries(Context context);
}
