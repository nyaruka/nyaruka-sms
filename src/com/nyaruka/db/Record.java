package com.nyaruka.db;

import com.almworks.sqlite4java.SQLiteStatement;
import com.nyaruka.json.JSON;

public class Record {
	
	public Record(long id, JSON data){
		m_id = id;
		m_data = data;
	}
	
	public Record(SQLiteStatement st){
		try{
			m_id = st.columnLong(DB.RECORD_ID);
			String text = st.columnString(DB.RECORD_DATA);
			if (text != null){
				JSON json = new JSON(text);
				setData(json);
			}
		} catch (Throwable t){
			throw new RuntimeException(t);
		}
	}
	
	public void setData(String json){
		m_data = new JSON(json);
	}
	
	public void setData(JSON data){
		m_data = data;
	}
	
	public void setId(int id){
		m_id = id;
	}
	
	/**
	 * Returns a FULL json representation of a Record
	 * @return
	 */
	public String toString(){
		JSON json = new JSON(m_data.toString());
		json.put("id", m_id);
		return json.toString();
	}

	public JSON getData(){ return m_data; }
	public long getId(){ return m_id; }

	private long m_id;
	private JSON m_data;
}
