package com.app.sqlite.base;

/**
 * An object that represents a database field, the SQLProvider will use
 * this field to populate a SQL query with the correct value or type
 * @author samkirton
 */
public final class DatabaseField {
	private int mType;
	private Object mValue;
	
	public int getType() {
		return mType;
	}
	
	public void setType(int newVal) {
		mType = newVal;
	}
	
	public Object getValue() {
		return mValue;
	}
	
	public void setValue(Object newVal) {
		mValue = newVal;
	}
	
	/**
	 * A list of types is defined in the BaseModel class
	 * @param	type	The type of the database field
	 */
	public DatabaseField(int type) {
		mType = type;
	}
}
