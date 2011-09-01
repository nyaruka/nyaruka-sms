package com.nyaruka.db;

import org.json.JSONObject;

import com.nyaruka.json.JSON;

public class Collection {

	public static final int INDEX_COLS = 5;
	
	public Collection(DB db, int id, String name){
		m_db = db;
		m_id = id;
		m_name = name;
	}
	
	public Record save(JSON data) {
		return m_db.save(this, data);
	}
	
	public Record save(JSONObject data){
		return save(new JSON(data));
	}
	
	public Record save(String data){
		try{
			System.out.println(data);
			JSON json = new JSON(data);
			return save(json);
		} catch (Throwable t){
			throw new RuntimeException(t);
		}
	}
	
	public Cursor find(JSON query) {
		return m_db.findRecords(this, query);
	}
	
	public Cursor find(String query) {
		try{
			JSON json = new JSON(query);
			return find(json);
		} catch (Throwable t){
			throw new RuntimeException(t);
		}
	}
	
	public Record getRecord(long id){
		return m_db.getRecord(this, id);
	}
	
	public int getId(){ return m_id; }

	public String getIntIndex(int index){
		return m_ints[index];
	}
	
	public String getStrIndex(int index){
		return m_strs[index];
	}
	
	public int getIntIndex(String name){
		for(int i=0; i<INDEX_COLS; i++){
			if (name.equals(m_ints[i])){
				return i;
			}
		}
		return -1;
	}
	
	public int getStrIndex(String name){
		for(int i=0; i<INDEX_COLS; i++){
			if (name.equals(m_strs[i])){
				return i;
			}
		}
		return -1;
	}
	
	public boolean deleteIndex(String name){
		int idx = getIntIndex(name);
		if (idx != -1){
			m_ints[idx] = null;
			return true;
		}
		
		idx = getStrIndex(name);
		if (idx != -1){
			m_strs[idx] = null;
			return true;
		}
		
		return false;
	}
	
	public int getSize(){
		return find("{}").count();
	}
	
	/**
	 * Ensures that an index is present within the passed in array
	 * @param map The array of index names
	 * @param name The name of the index to search for
	 * @return Whether we had to add the index ourselves (false if it existed)
	 */
	private boolean ensureIndex(String[] map, String name){
		int empty = -1;
		for(int i=0; i<INDEX_COLS; i++){
			if (map[i] != null){
				if (map[i].equals(name)){
					return false;
				}
			} else if (empty == -1){
				empty = i;
			}
		}
		
		// no empty slot?  no can do
		if (empty == -1){
			throw new RuntimeException("No room remaining for a new index");
		}
		
		// otherwise, add it
		map[empty] = name;
		return true;
	}
	
	public void ensureStrIndex(String name){
		if (ensureIndex(m_strs, name)){
			m_db.saveCollection(this);
			m_db.populateIndex(this, name);
		}
	}
	
	public void ensureIntIndex(String name){
		if (ensureIndex(m_ints, name)){
			m_db.saveCollection(this);
			m_db.populateIndex(this, name);
		}
	}
	
	public void setIntIndex(int index, String name){
		m_ints[index] = name;
	}
	
	public void setStrIndex(int index, String name){
		m_strs[index] = name;
	}
	
	public String getIndexName(String name){
		String field = null;
		int index = getIntIndex(name);
		if (index >= 0){
			field = "int" + index;
		} else{
			index = getStrIndex(name);
			if (index >= 0){
				field = "str" + index;
			}
		}
		return field;
	}
	
	public void delete(long id) {
		m_db.deleteRecord(this, id);
	}
	
	public String getName(){ return m_name; }
		
	private DB m_db;
	private int m_id;
	private String m_name;
	private String[] m_ints = new String[INDEX_COLS];
	private String[] m_strs = new String[INDEX_COLS];
}
