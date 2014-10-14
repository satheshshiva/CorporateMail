/**
 * 
 */
package com.wipromail.sathesh.sqlite.db.cache.dao;

import android.annotation.TargetApi;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.util.Log;

import com.wipromail.sathesh.BuildConfig;
import com.wipromail.sathesh.constants.Constants;
import com.wipromail.sathesh.sqlite.db.cache.CacheDbHelper;
import com.wipromail.sathesh.sqlite.db.cache.vo.PojoVO;
import com.wipromail.sathesh.util.Utilities;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author sathesh
 *
 */
public class BaseCacheDAO implements Constants{

	protected static Context context;
	protected static SQLiteDatabase database;
    protected static CacheDbHelper cacheDbHelper;

	/** opens the db helper for writing. the file(db file) is initialized in the helper.
	 * @throws SQLException
	 */
	protected void open(CacheDbHelper cacheDbHelper)  {
		database = cacheDbHelper.getWritableDatabase();
	}

	/** closes the db
	 * 
	 */
	protected void close(CacheDbHelper cacheDbHelper) {
		cacheDbHelper.close();
	}

	/** VO to DB TABLE(ContentValue)
	 * USES GETTER IN VO
	 * Maps the Pojo Objects(VO) to the content values. It should be called before insert query
	 * First gets the column name from the Table class in which the columns are defined as static variables with name "COLUMN_ActualColumnName" 
	 * It gets the ActualColumnName and matches with the "setActualColumnName" in the VO Object class
	 * @param vo
	 */
	protected ContentValues autoMapVoToContentValues( PojoVO vo, Class tableClass){
		//Field[] fields = DummyClass.class.getDeclaredFields();
		Field[] fields; 
		ContentValues contentValues = new ContentValues();
		try{
			//Class tableClass = Class.forName(tableClassName);
			//the DbTableClass getter inside the VO should return the correct table class
			if(vo!=null && tableClass!=null){
				fields = tableClass.getDeclaredFields();
				//iterate all the fields inside the Db Table to get the colmn name define.
				//Example , 
				//public static final COLUMN_NAME="NAME"
				for (Field f : fields) {
					//get only the static field and validate whether it starts with COLUMN_. 
					if (Modifier.isStatic(f.getModifiers() )   
							&& f.getType().getName().equals("java.lang.String")
							&& isRightColumnName(f.getName())) {
						String columnName=(String)f.get(f.getName());
						Class voClass = vo.getClass();

						Method[] methods = voClass.getMethods();

						//iterate all the methods mentioned in the VO object
						// we should find the method, getName() as in our example
						for(Method method: methods){
							//check whether the method name is vo.getName()
							if(("get" + columnName).equalsIgnoreCase(method.getName())){
								//get the value of the method vo.getName()
								if(method.getReturnType().getName().equals("java.lang.String")){
									String strValue = (String)method.invoke(vo,null);
									contentValues.put(columnName, strValue);
								}
								else if(method.getReturnType().getName().equals("java.util.Date")){
									Date dateValue = (Date)method.invoke(vo,null);
									SimpleDateFormat sdf = new SimpleDateFormat(DB_DATE_FORMAT);
									contentValues.put(columnName, sdf.format(dateValue));
								}
								else if(method.getReturnType().getName().equals("int")){
									int intValue = (Integer)method.invoke(vo,null);
									contentValues.put(columnName, intValue);
								}
								else if(method.getReturnType().getName().equals("long")){
									long longValue = (Long)method.invoke(vo,null);
									contentValues.put(columnName, longValue);
								}
								else if(method.getReturnType().getName().equals("boolean")){
									boolean boolValue = (Boolean)method.invoke(vo,null);
									contentValues.put(columnName, boolValue);
								}

								//else if other types if needed
							}
							else if(("is" + columnName).equalsIgnoreCase(method.getName())){
								if(method.getReturnType().getName().equals("boolean")){
									boolean boolValue = (Boolean)method.invoke(vo,null);
									contentValues.put(columnName, boolValue);
								}
							}
						}
					} 
				}
			}
		}catch(Exception e){
			Utilities.generalCatchBlock(e, this.getClass());
		}
		return contentValues;
	}


	/** This calls processCursorToVOAtPosition() but porcesses list of VOs
	 * @param cursor
	 * @return list - list of VOs
	 */
	protected List<PojoVO> autoMapCursorToVo(Cursor cursor,Class voClass){
		List<PojoVO> list = new ArrayList<PojoVO>();
		PojoVO vo;

		try{
			if (cursor!=null){

				cursor.moveToFirst();
				for(int i=0; i<cursor.getCount(); i++){
					vo = processCursorToVOAtPosition(cursor, i, voClass );
					//add the VO to the list of VOs. 
					// note that we are adding to the list only after all the fields are set and not for each field
					list.add(vo);
				}
			}

		}
		catch(Exception e){
			Utilities.generalCatchBlock(e, this.getClass());
		}
		finally{
			cursor.close();
		}
		return list;	
	}
 
	/**DB TABLE to VO 
	 * USES SETTER IN VO
	 * Maps the Cursor column values to the corresponding VO object members automatically
	 * First gets the column index one by one and matches the column name with the VO object member "setActualColumnName"
	 * gets the column value and invokes the "setActualColumnName" method
	 *  
	 * This method process the cursor at certain position and returns a vo corresponding to that position
	 * @param cursor - the cursor
	 * @param i - position to which convert to vo
	 * @param voClass - the class which corresponds to the return vo
	 * @return
	 * @throws Exception
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	protected PojoVO processCursorToVOAtPosition(Cursor cursor, int i, Class voClass) throws Exception {
		PojoVO vo;
		int columnIndex;
		cursor.moveToPosition(i);
		//create new object for the VO
		vo = (PojoVO)voClass.newInstance();
		Method[] methods = vo.getClass().getMethods();

		//iterate all the methods mentioned in the VO object
		// we should find the method, setActualColumnName() 
		for(Method method: methods){
			//iterate all the columns from the cursor
			for(String columnName:cursor.getColumnNames()){
				//match column name from the cursor with the method name
				if(columnName!=null && ("set" + columnName).equalsIgnoreCase(method.getName())){
					columnIndex=cursor.getColumnIndex(columnName);
					//data type matching is only for API >= 11
					if (android.os.Build.VERSION.SDK_INT>=android.os.Build.VERSION_CODES.HONEYCOMB) {
						if(cursor.getType(columnIndex) == Cursor.FIELD_TYPE_STRING){
							//The datatype from the database is of type String
							//call the VO objects set method with the value from the db table as argument

							if(method.getParameterTypes()!=null && method.getParameterTypes().length ==1){
								String setParameterType="";
								//get the corresponding set methods data type(target datatype) from the set methods argument. it chooses only the method with
								//one parameter
								setParameterType=method.getParameterTypes()[0].getName();

								//target set method's data type is String
								if(setParameterType.equals("java.lang.String"))
								{
									method.invoke(vo,cursor.getString(columnIndex));
								}
                                //target set method's data type is int
                                else if(setParameterType.equals("int"))
                                {
                                    method.invoke(vo,cursor.getInt(columnIndex));
                                }
								//target set method's data type is boolean
								else if (setParameterType.equals("boolean"))
								{
									// for boolean data type the value true or false is stored as 1 or 0
									String strValue=cursor.getString(columnIndex);
									if(strValue.equals("1") || strValue.equals("true")){
										method.invoke(vo,true);
									}
									else if(strValue.equals("0") || strValue.equals("false")){
										method.invoke(vo,false);
									}
									else{
										if(BuildConfig.DEBUG)
											Log.w(TAG, "BaseCacheDAO -> the VO set parameter is of type boolean but the value is not of type boolean");
									}
								}
								else if(setParameterType.equals("java.util.Date"))
								{
									try {
										String strDateValue = cursor.getString(columnIndex);
										SimpleDateFormat dt = new SimpleDateFormat(DB_DATE_FORMAT); 
										Date date = dt.parse(strDateValue);
										method.invoke(vo,date);
									} catch (Exception e) {
										if(BuildConfig.DEBUG){
											Log.w(TAG, "BaseCacheDAO -> the VO set parameter is of type java.util.Date but the value is not of type Date");
											e.printStackTrace();
										}
									}
								}
                                //HAVE TO BE CODED IF NEEDED FOR LONG AND OTHER DATA TYPES

							}
							else{
								if(BuildConfig.DEBUG){
									Log.w(TAG, "BaseCacheDAO -> the VO set parameter has more than 1 parameter types");
								}
							}

						}

						else if(cursor.getType(columnIndex) == Cursor.FIELD_TYPE_INTEGER){
							//The datatype from the database is of type Integer
							//call the VO objects set method with the value from the db table as argument
							method.invoke(vo,cursor.getInt(columnIndex));
						}
						else if(cursor.getType(columnIndex) == Cursor.FIELD_TYPE_FLOAT){
							//The datatype from the database is of type Float
							//call the VO objects set method with the value from the db table as argument
							method.invoke(vo,cursor.getFloat(columnIndex));
						}
						else if(cursor.getType(columnIndex) == Cursor.FIELD_TYPE_BLOB){
							//The datatype from the database is of type blob
							// to be implemented if needed
						}
						else if(cursor.getType(columnIndex) == Cursor.FIELD_TYPE_NULL){
							//The datatype from the database is of type Null
							//to be implemented if needed
						}
					}
					else{
						//pre honey comb devices. no type checking for column. Automatically considers as string
						//call the VO objects set method with the value from the db table as argument
						method.invoke(vo,cursor.getString(columnIndex));
					}
				}
			}
		}
		return vo;

	}


	/** check the fields name whether it is the right table column name starting with "COLUMN_"
	 * @param name
	 * @return
	 */
	private static boolean isRightColumnName(String name) {
		if(name!=null)
			return name.startsWith("COLUMN_");
		else
			return false;
	}


}
