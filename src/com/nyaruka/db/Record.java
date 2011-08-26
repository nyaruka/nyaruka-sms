package com.nyaruka.db;

import org.json.JSONObject;

public class Record {
	
	public Record(long id){
		m_id = id;
	}
	
	public void setData(String json){
		try{
			m_data = new JSONObject(json);
		} catch (Throwable t){
			throw new RuntimeException(t);
		}
	}
	
	public void setData(JSONObject data){
		m_data = data;
	}
	
	public void setId(int id){
		m_id = id;
	}
	
	public JSONObject getData(){ return m_data; }
	public long getId(){ return m_id; }

	private long m_id;
	private JSONObject m_data;
}
