package com.wipromail.sathesh.sqlite.db.cache;

import java.util.List;

public interface DbTable {

	public  String getCreateQuery();
	public  String getOnUpgradeDropQuery();
	
	/** These queries will be executed when the table is newly created in the database
	 * @return
	 */
	public List<String> getNewTableQueries();
}
