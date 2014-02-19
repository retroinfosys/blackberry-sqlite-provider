package com.app.sqlite.helper;

import java.util.Enumeration;
import java.util.Hashtable;

import com.app.sqlite.base.DatabaseField;
import com.app.sqlite.base.BaseModel;

import net.rim.device.api.database.Cursor;
import net.rim.device.api.database.Database;
import net.rim.device.api.database.DatabaseException;
import net.rim.device.api.database.DatabaseFactory;
import net.rim.device.api.database.DatabaseIOException;
import net.rim.device.api.database.Statement;
import net.rim.device.api.io.URI;

public final class DatabaseHelper {
	/**
	 * Create a database at the provided databasePath
	 * @param	databasePath	The path to create the database
	 * @return	A database object that relates to the created database
	 */
	public static Database openOrCreate(String databasePath) {
		Database database = null;
		
		try {
			URI databaseUri = URI.create(databasePath);
			database = DatabaseFactory.openOrCreate(databaseUri);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		
		return database;
	}
	
	/**
	 * Open the database at the provided databasePath
	 * @param	databasePath	The path to create the database
	 * @return	A database object that relates to the created database
	 */
	public static Database open(String databasePath) {
		Database database = null;
		
		try {
			URI databaseUri = URI.create(databasePath);
			database = DatabaseFactory.open(databaseUri);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		
		return database;
	}
	
	/**
	 * Starts a database transaction
	 */
	public void beginTransaction(Database database) {
		try {
			database.beginTransaction();
		} catch (DatabaseException e) { }
	}
	
	/**
	 * Rollback a database transaction
	 */
	public void rollbackTransaction(Database database) {
		try {
			database.rollbackTransaction();
		} catch (DatabaseException e) { }
	}
	
	/**
	 * Commits a database transaction
	 */
	public void commitTransaction(Database database) {
		try {
			database.commitTransaction();
		} catch (DatabaseException e) { }
	}
	
	/**
	 * Deletes the database at the provided path
	 * @param	databasePath	The path of the database that should be removed
	 */
	public static void deleteDatabase(String databasePath) {
		try {
			URI databaseUri = URI.create(databasePath);
			DatabaseFactory.delete(databaseUri);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}
	
	/**
	 * Does the SQLite database exist
	 * @param	databasePath	The path to the SQLite database
	 * @return	Does the SQLite database exist?
	 */
	public static boolean databaseExists(String databasePath) {
		boolean databaseExists = false;
		
		try {
			URI databaseUri = URI.create(databasePath);
			if (DatabaseFactory.exists(databaseUri)) {
				databaseExists = true;
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		
		return databaseExists;
	}
	
	/**
	 * Close the provided database object
	 * @param	database	Database to close
	 */
	public static void closeDatabase(Database database) {
		if (database instanceof Database) {
			try {
				database.close();
			} catch (DatabaseIOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Execute a write query
	 * @param	databasePath	The path to the SQLite database
	 * @param	query	The query to execute
	 * @return	Has the query executed successfully?
	 */
	public static boolean executeWriteQuery(String query, Database database) {
		boolean queryExecuted = false;
		
		try {			
			Statement statement = database.createStatement(query);
			statement.prepare();
			statement.execute();
			statement.close();
			
			queryExecuted = true;
		} catch (Exception e) { 
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		
		return queryExecuted;
	}
	
	/**
	 * Get the lastInsertedRowID from the provided database object
	 * @param	database	The database to get the lastInsertedRowID
	 * @return	The lastInsertedRowID
	 */
	public static long getLastWriteRowId(Database database) {
		long lastInsertedRowId = -1;
		
		try {
			return database.lastInsertedRowID();
		} catch (DatabaseException e) { }
		
		return lastInsertedRowId;
	}
	
	/**
	 * Execute a read query
	 * @param	databasePath	The path to the SQLite database
	 * @param	query	The query to execute
	 * @return	A cursor of query results
	 */
	public static Cursor executeReadQuery(String databasePath, String query, Database database) {
		Cursor results = null;
		
		try {
			Statement statement = database.createStatement(query);
			
			statement.prepare();
			results = statement.getCursor();
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		
		return results;
	}
	
	/**
	 * Builds an insert query 
	 * @param	tableName	The name of the table being queried
	 * @param	fieldCount	Amount of fields to populate the insert query for	
	 * @return	An insert query
	 */
	public static String buildInsertQuery(String tableName, Hashtable fieldHashTable) {
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append("INSERT INTO ");
		stringBuffer.append(tableName);
		stringBuffer.append(" (");
		
		// first pass to append columns
        Enumeration keys = fieldHashTable.keys();
        while(keys.hasMoreElements()) {
           String key = (String)keys.nextElement();
           stringBuffer.append(key + ", ");
        }
        
        // remove the trailing comma and space 
        stringBuffer.delete(stringBuffer.length()-2, stringBuffer.length());
        
        stringBuffer.append(") VALUES (");
        
		// second pass to append values
        keys = fieldHashTable.keys();
        while(keys.hasMoreElements()) {
           String key = (String)keys.nextElement();
            
            DatabaseField field = (DatabaseField)fieldHashTable.get(key);
            
            if (field.getType() != BaseModel.TYPE_PRIMARY_KEY) {
            	stringBuffer.append("?, ");
            } else {
            	stringBuffer.append("null, ");
            }
        }
		
		// remove the trailing comma and space 
		stringBuffer.delete(stringBuffer.length()-2, stringBuffer.length());
		
		stringBuffer.append(")");

		return stringBuffer.toString();
	}
	
	/**
	 * Builds a SELECT * query
	 * @param	tableName	The table to SELECT * from
	 * @return	A string that contains SELECT * [tableName]
	 */
	public static String buildSelectAllQuery(String tableName) {
		return "SELECT * FROM " + tableName;
	}
	
	/**
	 * Builds a "SELECT * FROM Table where tableColumn = value" query
	 * @param	tableName	The table to query
	 * @param	whereColumn	The column that the condition should be used on
	 * @param	whereValue	The value that is being used with the condition
	 * @return
	 */
	public static String buildSelectWhereQuery(String tableName, String whereColumn, String whereValue) {
		return "SELECT * FROM " + tableName + " WHERE " + whereColumn + " = '" + whereValue + "'";
	}
}
