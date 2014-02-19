package com.app.sqlite.provider;

import java.io.InputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import com.app.sqlite.base.DatabaseField;
import com.app.sqlite.base.BaseModel;
import com.app.sqlite.helper.DatabaseHelper;
import com.app.sqlite.helper.ResourceHelper;

import net.rim.device.api.database.Cursor;
import net.rim.device.api.database.Database;
import net.rim.device.api.database.Row;
import net.rim.device.api.database.Statement;

/**
 * A provider class used to interact with the local SQLite database
 * @author samkirton
 */
public final class SQLProvider {
	private String mDatabaseFileRoot;
	
	public SQLProvider(String databaseFileRoot) {
		mDatabaseFileRoot = databaseFileRoot;
	}
	
	/**
	 * Remove the current database and create a new one
	 */
	public void createDatabase() {
		Database database = null;
		try {
			if (DatabaseHelper.databaseExists(mDatabaseFileRoot)) {
				DatabaseHelper.deleteDatabase(mDatabaseFileRoot);
			}
			
			database =  DatabaseHelper.openOrCreate(mDatabaseFileRoot);	
		} finally {
			DatabaseHelper.closeDatabase(database);
		}
	}
	
	/**
	 * @return	The database file exists
	 */
	public boolean databaseExists() {
		return DatabaseHelper.databaseExists(mDatabaseFileRoot);
	}
	
	/**
	 * Delete the SQLite database
	 */
	public void destroyData() {
		DatabaseHelper.deleteDatabase(mDatabaseFileRoot);
	}
	
	/**
	 * Execute a create schema query
	 * @param	resourceLocation	The location of the SQL query schema
	 * @return	Did the query execute successfully?
	 */
	public boolean executeCreateSchemaQuery(String resourceLocation) {
		boolean queryExecuted = false;
		InputStream inputStream = getClass().getResourceAsStream(resourceLocation);
		String schemaQuery = ResourceHelper.getFileContents(inputStream);
		
		Database database = null;
		try {
			database = DatabaseHelper.open(mDatabaseFileRoot);
			queryExecuted = DatabaseHelper.executeWriteQuery(schemaQuery, database);
		} finally {
			DatabaseHelper.closeDatabase(database);
		}
		
		ResourceHelper.closeInputStream(inputStream);
		
		return queryExecuted;
	}
	
	/**
	 * Execute a SQL query
	 * @param	query	The query to execute
	 * @return	Did the query run successfully?
	 */
	public boolean executeQuery(String query) {
		boolean queryExecuted = false;
		
		Database database = null;
		try {
			database = DatabaseHelper.open(mDatabaseFileRoot);
	        Statement statement = database.createStatement(query);  
	        statement.prepare();      
	        statement.execute(); 
	        statement.close(); 
	        queryExecuted = true;
		} catch (Exception e) {
			System.out.println("Query failed: " + e.getMessage());
		} finally {
			DatabaseHelper.closeDatabase(database);
		}
		
		return queryExecuted;
	}
	
	/**
	 * 
	 * @param queryList
	 * @return
	 */
	public boolean executeMultipleQueries(String[] queryList) {
		boolean queryExecuted = false;
		
		Database database = null;
		try {
			database = DatabaseHelper.open(mDatabaseFileRoot);
			// run the queries in the same transaction
			database.beginTransaction();
			for (int i = 0; i < queryList.length; i++) {
				String query = queryList[i];
				if (query instanceof String) {
			        Statement statement = database.createStatement(query);  
			        statement.prepare();      
			        statement.execute(); 
			        statement.close(); 
				}
			}
			
			// end the transaction
			database.commitTransaction();
			queryExecuted = true;
		} catch (Exception e) {
			System.out.println("Query failed: " + e.getMessage());
		} finally {
			DatabaseHelper.closeDatabase(database);
		}
		
		return queryExecuted;
	}
	
	/**
	 * Insert a model into the provided SQL table, the model data must match
	 * the schema of the tableName
	 * @param	tableName	The table to insert the model into
	 * @param	model	The model to insert
	 */
	public long insertValue(String tableName, BaseModel model) {
		long lastWriteRowId = -1;
		
		// ensure that the tableName and model match
		if (!tableName.equals(model.getTableName())) {
			throw new IllegalArgumentException("The tableName must match model.TABLE_NAME");
		}
		
		Database database = null;
		try {
			database = DatabaseHelper.open(mDatabaseFileRoot);
			// build a SQL INSERT query string
			String insertQuery = DatabaseHelper.buildInsertQuery(tableName,  model.getFields());
	        Statement statement = database.createStatement(insertQuery);
	        statement.prepare();
	        
	        // loop through all values and bind them to the statement, the first field 
	        // is ignored since we assume it is the primary key
	        Hashtable fieldHashTable = model.getFields();
	        int i = 1;
	        Enumeration keys = fieldHashTable.keys();
	        while(keys.hasMoreElements()) {
	        	String key = (String)keys.nextElement();
	            
	            DatabaseField field = (DatabaseField)fieldHashTable.get(key);
	            
	            if (field.getType() == BaseModel.TYPE_STRING) {
	            	if (field.getValue() instanceof String) {
	            		statement.bind(i, (String)field.getValue());
	            	} else {
	            		statement.bind(i, (String)null);
	            	}
	            	
	            	i++;
	            } else if (field.getType() == BaseModel.TYPE_LONG) {
	            	if (field.getValue() instanceof Long) {
	            		statement.bind(i, ((Long)field.getValue()).longValue());
	            	} else {
	            		statement.bind(i, -1);
	            	}
	            	
	            	i++;
	            } else if (field.getType() == BaseModel.TYPE_INTEGER) {
	            	if (field.getValue() instanceof Integer) {
	            		statement.bind(i, ((Integer)field.getValue()).intValue());
	            	} else {
	            		statement.bind(i, -1);
	            	}
	            	
	            	i++;
	            }
	        }
	        
	        statement.execute(); 
	        statement.close(); 
	        
	        // get the row that was just inserted
	        lastWriteRowId = DatabaseHelper.getLastWriteRowId(database);
		} catch (Exception e) {
			System.out.println("Insert failed: " + e.getMessage());
		} finally {
			DatabaseHelper.closeDatabase(database);
		}
        
        return lastWriteRowId;
	}
	
	/**
	 * Delete a value from the provided table
	 * @param	tableName	The table name to delete the row from
	 * @param	columnName	Column name for the condition
	 * @param	condition	Condition for the delete
	 * @return
	 */
	public boolean deleteValue(String tableName, String columnName, String condition) {
		boolean queryExecuted = false;
	
		String deleteQuery = "DELETE FROM " + tableName + " WHERE " + columnName + " = " + condition;
		
		Database database = null;
		try {
			database = DatabaseHelper.open(mDatabaseFileRoot);
			Statement statement = database.createStatement(deleteQuery);
			statement.prepare();
			statement.execute();
			statement.close();
			queryExecuted = true;
		} catch (Exception e) {
			System.out.println(e.getMessage());
		} finally {
			DatabaseHelper.closeDatabase(database);
		}
		
		return queryExecuted;
	}
	
	/**
	 * Insert a  collection of models into the provided SQL table, the model data must match
	 * the schema of the tableName
	 * @param	tableName	The table to insert the model into
	 * @param	model	The model to insert
	 */
	public boolean insertMultipleValues(String tableName, BaseModel[] model) {
		for (int i = 0; i < model.length; i++) {
			long rowID = this.insertValue(tableName, model[i]);
			if (rowID == -1) {
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * Select all values from the SQL table as rows
	 * @param	tableName	The table to select all values from
	 * @param	columnOrderBy	The column to order the results by
	 * @param	direction	The direction of the query
	 * @return	A Vector of rows
	 */
	public Vector selectAll(String tableName, String columnOrderBy, String direction) {
		Vector results = new Vector();
		
		Database database = null;
		try {
			database = DatabaseHelper.open(mDatabaseFileRoot);
			String selectAllQuery = null;
			if (columnOrderBy instanceof String && direction instanceof String) {
				selectAllQuery = DatabaseHelper.buildSelectAllQuery(tableName) +
					" ORDER BY " + columnOrderBy + " " + direction;
			} else {
				selectAllQuery = DatabaseHelper.buildSelectAllQuery(tableName);
			}
			
			Statement statement = database.createStatement(selectAllQuery); 
			statement.prepare();
			Cursor cursor = statement.getCursor();   
			
	        while(cursor.next()) {
	            Row row = cursor.getRow();  
	        	results.addElement(row);
	        }        
	        
	        cursor.close();
	        statement.close();
		} catch (Exception e) {
			System.out.println("Select failed: " + e.getMessage());
		} finally {
			DatabaseHelper.closeDatabase(database);
		}
		
		return results;
	}
	
	/**
	 * An override of selectAllOrderBy that only contains a table
	 * @param	table	The table to select all data from
	 * @return	A vector of database rows returned by the query
	 */
	public Vector selectAll(String table) {
		return selectAll(table, null, null);
	}
	
	/**
	 * Select a values from the SQL table as rows that match the provided where column and value
	 * @param	tableName	The table to select the values from
	 * @param	whereColumn	The where condition column
	 * @param 	whereValue	The where condition value
	 * @return	A Vector of rows
	 */
	public Vector selectWhere(String tableName, String whereColumn, String whereValue) {
		Vector results = new Vector();
		
		Database database = null;
		try {
			database = DatabaseHelper.open(mDatabaseFileRoot);
			String selectWhereQuery = DatabaseHelper.buildSelectWhereQuery(tableName,whereColumn,whereValue);
			Statement statement = database.createStatement(selectWhereQuery); 
			statement.prepare();
			Cursor cursor = statement.getCursor();   
			
	        while(cursor.next()) {
	            Row row = cursor.getRow();  
	        	results.addElement(row);
	        }        
	        
	        cursor.close();
	        statement.close();
		} catch (Exception e) {
			System.out.println("Select failed: " + e.getMessage());
		} finally {
			DatabaseHelper.closeDatabase(database);
		}
		
		return results;
	}
	
	/**
	 * Run a custom SELECT SQL query and return the rows as a Vector
	 * @param	selectQuery	The select query to execute
	 * @return	A vector of rows associated with the provided query
	 */
	public Vector selectQuery(String selectQuery) {	
		Vector results = new Vector();
		
		Database database = null;
		try {
			database = DatabaseHelper.open(mDatabaseFileRoot);
			Statement statement = database.createStatement(selectQuery); 
			statement.prepare();
			Cursor cursor = statement.getCursor();   
			
	        while(cursor.next()) {
	            Row row = cursor.getRow();  
	        	results.addElement(row);
	        }        
	        
	        cursor.close();
	        statement.close();
		} catch (Exception e) {
			System.out.println("Select failed: " + e.getMessage());
		} finally {
			DatabaseHelper.closeDatabase(database);
		}
		
		return results;
	}
}
