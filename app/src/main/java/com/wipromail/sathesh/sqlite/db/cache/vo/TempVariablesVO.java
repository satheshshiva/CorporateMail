package com.wipromail.sathesh.sqlite.db.cache.vo;


/** This holds the VO for the records returned from the database.
 * each column variable in the db (starting with COLUMN_) should have a corresponding matching variable here
 * COLUMN_FIELDNAME=FIELDNAME=Actual database column name = vo.variable_name
 * @author sathesh
 *
 */
public class TempVariablesVO implements PojoVO {

	//COLUMN_FIELDNAME=FIELDNAME=Actual database column name = vo.variable_name
	private String name="";
	private boolean value;


	public boolean isValue() {
		return value;
	}

	public void setValue(boolean value) {
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("TempVariablesVO [name=");
		builder.append(name);
		builder.append(", value=");
		builder.append(value);
		builder.append("]");
		return builder.toString();
	}



}
