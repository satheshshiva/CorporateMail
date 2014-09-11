package com.wipromail.sathesh.sqlite.db.dao;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.wipromail.sathesh.sqlite.db.DbConstants;
import com.wipromail.sathesh.sqlite.db.DbHelper;
import com.wipromail.sathesh.sqlite.db.pojo.vo.PojoVO;
import com.wipromail.sathesh.sqlite.db.pojo.vo.TempVariablesVO;

/** DAO for the Table TEMP_VARIABLES
 * @author sathesh
 *
 */

public class TempVariablesDAO extends BaseDAO{

	//All the DAOs should have fully qualified class names of table class and vo class for auto wiring
	private Class tableClass=com.wipromail.sathesh.sqlite.db.tables.TableTempVariables.class;
	private Class voClass=com.wipromail.sathesh.sqlite.db.pojo.vo.TempVariablesVO.class;


	/** Constructor for the DAO. initializes the Database helper
	 * @param context
	 */
	public TempVariablesDAO(Context context) {
		this.context = context;
		dbHelper = DbHelper.getInstance(context);
	}


	/** New record 
	 * @param location
	 * @return
	 */
	public long createOrUpdate(TempVariablesVO vo) throws Exception {
		long insertId=0;
		ContentValues values = autoMapVoToContentValues(vo,tableClass);
		try{
			open();
			insertId = database.insertWithOnConflict(DbConstants.table.TEMP_VARIABLES, null,
					values,SQLiteDatabase.CONFLICT_REPLACE);
		}
		finally{
			try{close();}catch(Exception e){}
		}
		return insertId;
	}
	
	/** Get all records
	 * @param location
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<TempVariablesVO> getAllRecords() throws Exception {
		
		List<TempVariablesVO> returnList =null;
		try{
		open();
		Cursor cursor = database.query(DbConstants.table.TEMP_VARIABLES,
				null, null, null, null, null, null);
		returnList =(List)autoMapCursorToVo(cursor,voClass);
		}finally{
		close();
		}
		return returnList;
	}
}
