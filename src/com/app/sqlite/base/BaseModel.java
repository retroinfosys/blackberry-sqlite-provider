package com.app.sqlite.base;

import java.util.Hashtable;

/**
 * The base class for all database Model classes, each model class
 * is responsible for defining a DatabaseField[] that defines the name 
 * and types that can be stored in the database
 * @author samkirton
 */
public abstract class BaseModel {
	protected Hashtable fields;
	
	public static final String FIELD_PID = "pid";
	
	public static final int TYPE_STRING = 0;
	public static final int TYPE_LONG = 1;
	public static final int TYPE_PRIMARY_KEY = 2;
	public static final int TYPE_INTEGER = 3;
	public static final int TYPE_FLOAT = 4;
	
	public BaseModel() {
		fields = new Hashtable();
		fields.put(FIELD_PID,  new DatabaseField(TYPE_PRIMARY_KEY));
	}
	
	public Long getPid() {
		return (Long)((DatabaseField)fields.get(FIELD_PID)).getValue();
	}
	
	public void setValue(String key, Object value) {
		((DatabaseField)fields.get(key)).setValue(value);
	}
	
	/**
	 * @return	The table name that this model relates to
	 */
	public abstract String getTableName();
	
	/**
	 * each model class is responsible for defining a DatabaseField[] that defines the name 
	 * and types that can be stored in the database
	 */
	public abstract Hashtable getFields();
}
