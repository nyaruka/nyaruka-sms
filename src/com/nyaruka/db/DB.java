package com.nyaruka.db;

import java.util.HashMap;

import org.json.JSONObject;

import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteStatement;

public class DB {
	
	public DB(){
	}

	public void open(){
		try{
			m_db = new SQLiteConnection();
			m_db.open(true);
		} catch (Throwable t){
			throw new RuntimeException(t);
		}
	}
	
	private void exec(String sql){
		SQLiteConnection conn = null;
		try{
			conn = m_db.exec(sql);
		} catch (Throwable t){
			throw new RuntimeException(t);
		} finally {
			if (conn != null){
				//conn.dispose();
			}
		}		
	}
	
	public void clear(){
		exec("DELETE FROM `collections`;");
		m_collections.clear();
	}
	
	public void init(){
		// record types, maybe should be called collections? 
		exec("CREATE TABLE `collections` (" +
			 "`id` integer PRIMARY KEY," +
			 "`name` varchar(128)," + 
			 "`int0` varchar(128)," +
			 "`int1` varchar(128)," +
			 "`int2` varchar(128)," +
			 "`int3` varchar(128)," +
			 "`int4` varchar(128)," +
			 "`str0` varchar(128)," +
			 "`str1` varchar(128)," +			 
			 "`str2` varchar(128)," +			 			 
			 "`str3` varchar(128)," + 
			 "`str4` varchar(128)" +			 			 			 			 
		")");
		
		// our actual records
		exec("CREATE TABLE `records` (" +
			  "`id` integer PRIMARY KEY," +
			  "`collection` integer," +
			  "`data` text," +			  
			  "`int0` integer," +
			  "`int1` integer," +
			  "`int2` integer," +
			  "`int3` integer," +
			  "`int4` integer," +
			  "`str0` string," +
			  "`str1` string," +
			  "`str2` string," +
			  "`str3` string," +
			  "`str4` string" +
		")");
	}
	
	/**
	 * Clears our in memory data and reloads it from the database.
	 */
	public void load(){
		m_collections.clear();

		try{
			SQLiteStatement st = m_db.prepare("SELECT * FROM collections");
			while (st.step()){
				//for (int i=0; i<st.columnCount(); i++){
				//	System.out.println(st.getColumnName(i) + " = " + st.columnValue(i));
				//}
				
				int id = st.columnInt(0);
				String name = st.columnString(1);
				Collection type = new Collection(this, id, name);
				m_collections.put(name, type);
				
				for(int i=0; i<10; i++){
					if (i < 5){
						type.setIntIndex(i, st.columnString(i+2));
					} else {
						type.setStrIndex(i-5, st.columnString(i+2));						
					}
				}
			}
		} catch (Throwable t){
			throw new RuntimeException(t);
		}
	}
	
	public boolean collectionExists(String name) {
		return m_collections.containsKey(name);
	}

	public void saveCollection(Collection type){
		try{
			SQLiteStatement st = m_db.prepare("UPDATE collections SET int0=?, int1=?, int2=?, int3=?, int4=?," +
											  "                        str0=?, str1=?, str2=?, str3=?, str4=? " + 
											  " WHERE id=?");
			// bind our int indexes
			for(int i=0; i<5; i++){
				st.bind(i+1, type.getIntIndex(i));
			}
			
			// then our str indexes
			for(int i=0; i<5; i++){
				st.bind(i+6, type.getStrIndex(i));
			}
			
			// bind our id
			st.bind(11, type.getId());
			st.step();
		} catch (Throwable t){
			throw new RuntimeException(t);
		}
	}
	
	public Collection ensureCollection(String name) {
		if (!collectionExists(name)){
			try{
				SQLiteStatement st = m_db.prepare("INSERT INTO collections VALUES(NULL, ?, " +
												  "NULL, NULL, NULL, NULL, NULL, " +
												  "NULL, NULL, NULL, NULL, NULL)");
				st.bind(1, name);
				st.step();
			} catch (Throwable t){
				throw new RuntimeException(t);
			}
			load();
		}
		return m_collections.get(name);
	}
	
	public Record insertRecord(Collection collection, JSONObject data){
		try{
			SQLiteStatement st = m_db.prepare("INSERT INTO records VALUES(NULL, ?, ?," +
			    		                       "NULL, NULL, NULL, NULL, NULL, " +
											   "NULL, NULL, NULL, NULL, NULL " + 
											   ")");
			st.bind(1, collection.getId());
			st.bind(2, data.toString());
			st.step();
			return new Record(m_db.getLastInsertId());
		} catch (Throwable t){
			throw new RuntimeException(t);
		}
	}
	
	public Record getRecord(Collection collection, long id){
		try{
			SQLiteStatement st = m_db.prepare("SELECT id, collection, data " +
											  "from records where id=?");
			st.bind(1, id);
			
			Record rec = null;
			if (st.step()){
				for (int i=0; i<st.columnCount(); i++){
					System.out.println(st.getColumnName(i) + " = " + st.columnValue(i));
				}
				
				rec = new Record(id);
				String text = st.columnString(2);
				if (text != null){
					JSONObject json = new JSONObject(text);
					rec.setData(json);
				}
				return rec;
			}
			return rec;
		} catch (Throwable t){
			throw new RuntimeException(t);
		}		
	}
	
	private SQLiteConnection m_db;
	private HashMap<String, Collection> m_collections = new HashMap<String, Collection>();
}
