package com.nyaruka.db;

import org.json.JSONObject;

public class Collection {

	public static final int INDEX_COLS = 5;
	
	public Collection(DB db, int id, String name){
		m_db = db;
		m_id = id;
		m_name = name;
	}
	
	public Record addRecord(JSONObject data) {
		return m_db.insertRecord(this, data);
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
	
	private boolean ensureIndex(String[] map, String name){
		int empty = -1;
		for(int i=0; i<INDEX_COLS; i++){
			if (map[i] != null){
				if (map[i].equals(name)){
					return true;
				}
			} else if (empty == -1){
				empty = i;
			}
		}
		
		// no empty slot?  no can do
		if (empty == -1){
			return false;
		}
		
		// otherwise, add it
		map[empty] = name;
		return true;
	}
	
	public boolean ensureStrIndex(String name){
		return ensureIndex(m_strs, name);
	}
	
	public boolean ensureIntIndex(String name){
		return ensureIndex(m_ints, name);
	}
	
	public void setIntIndex(int index, String name){
		m_ints[index] = name;
	}
	
	public void setStrIndex(int index, String name){
		m_strs[index] = name;
	}
	
	private DB m_db;
	private int m_id;
	private String m_name;
	private String[] m_ints = new String[INDEX_COLS];
	private String[] m_strs = new String[INDEX_COLS];
}
