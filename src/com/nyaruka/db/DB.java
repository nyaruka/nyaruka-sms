package com.nyaruka.db;

import java.util.HashMap;

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
		exec("DELETE FROM `record_types`;");
		m_recordTypes.clear();
	}
	
	public void init(){
		exec("CREATE TABLE `record_types` (" +
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
	}
	
	/**
	 * Clears our in memory data and reloads it from the database.
	 */
	public void load(){
		m_recordTypes.clear();

		try{
			SQLiteStatement st = m_db.prepare("SELECT * FROM record_types");
			while (st.step()){
				//for (int i=0; i<st.columnCount(); i++){
				//	System.out.println(st.getColumnName(i) + " = " + st.columnValue(i));
				//}
				
				int id = st.columnInt(0);
				String name = st.columnString(1);
				RecordType type = new RecordType(id, name);
				m_recordTypes.put(name, type);
				
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
		return m_recordTypes.containsKey(name);
	}

	public void saveRecordType(RecordType type){
		try{
			SQLiteStatement st = m_db.prepare("UPDATE record_types SET int0=?, int1=?, int2=?, int3=?, int4=?," +
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
	
	public RecordType ensureCollection(String name) {
		if (!collectionExists(name)){
			try{
				SQLiteStatement st = m_db.prepare("INSERT INTO record_types VALUES(NULL, ?, " +
												  "NULL, NULL, NULL, NULL, NULL, " +
												  "NULL, NULL, NULL, NULL, NULL)");
				st.bind(1, name);
				st.step();
			} catch (Throwable t){
				throw new RuntimeException(t);
			}
			load();
		}
		return m_recordTypes.get(name);
	}
	
	private SQLiteConnection m_db;
	private HashMap<String, RecordType> m_recordTypes = new HashMap<String, RecordType>();
}
