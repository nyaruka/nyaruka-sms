package com.nyaruka.json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

public class JSON {

	static class JSONException extends RuntimeException {
		public JSONException(Throwable t){
			super(t);
		}
	}
	
	public JSON(JSONObject object){
		m_o = object;
	}
	
	public JSON(){
		m_o = new JSONObject();
	}
	
	public JSON(String json){
		try{
			m_o = new JSONObject(json);
		} catch (Throwable t){
			throw new JSONException(t);
		}
	}
	
	public String getString(String key){
		try{
			return m_o.getString(key);
		} catch (Throwable t){
			throw new JSONException(t);
		}
	}
	
	public int getInt(String key){
		try{
			return m_o.getInt(key);
		} catch (Throwable t){
			throw new JSONException(t);
		}
	}
		
	public long getLong(String key){
		try{
			return m_o.getLong(key);
		} catch (Throwable t){
			throw new JSONException(t);
		}
	}
	
	public double getDouble(String key){
		try{
			return m_o.getDouble(key);
		} catch (Throwable t){
			throw new JSONException(t);
		}
	}
	
	public boolean getBoolean(String key){
		try{
			return m_o.getBoolean(key);
		} catch (Throwable t){
			throw new JSONException(t);
		}
	}	
	
	public Object get(String key){
		try{
			return m_o.get(key);
		} catch (Throwable t){
			throw new JSONException(t);
		}
	}	
	
	public JSON getJSON(String key){
		try{
			return new JSON(m_o.getJSONObject(key));
		} catch (Throwable t){
			throw new JSONException(t);
		}
	}
	
	public void put(String key, JSONArray val){
		try{
			m_o.put(key, val);
		} catch (Throwable t){
			throw new JSONException(t);
		}
	}
	
	public void put(String key, long val){
		try{
			m_o.put(key, val);
		} catch (Throwable t){
			throw new JSONException(t);
		}
	}
	
		public void put(String key, boolean val){
		try{
			m_o.put(key, val);
		} catch (Throwable t){
			throw new JSONException(t);
		}
	}
		
	public void put(String key, double val){
		try{
			m_o.put(key, val);
		} catch (Throwable t){
			throw new JSONException(t);
		}
	}
	
	public void put(String key, String val){
		try{
			m_o.put(key, val);
		} catch (Throwable t){
			throw new JSONException(t);
		}
	}
	
	public void put(String key, JSON val){
		try{
			m_o.put(key, val.getObject());
		} catch (Throwable t){
			throw new JSONException(t);
		}
	}
	
	public JSONObject getObject(){
		return m_o;
	}
	
	public boolean has(String name){
		return m_o.has(name);
	}
	
	public Iterator keys(){
		return m_o.keys();
	}
	
	public String toString(){
		return m_o.toString();
	}
	
	private JSONObject m_o;

	public Map<String, Object> toMap() {
		
		HashMap<String,Object> map = new HashMap<String,Object>();		
		Iterator keys = keys();
		while (keys.hasNext()) {
			String key = (String) keys.next();
			
			Object value = get(key);			
			if (value instanceof JSON) {
				map.put(key, ((JSON)value).toMap());
			} else if (value instanceof JSONArray){
				JSONArray array = (JSONArray)value;
				map.put(key, arrayToList(array));
			} else {
				map.put(key, get(key));
			}
		}
		
		return map;
	}
	
	private static ArrayList arrayToList(JSONArray jsonArray) {
		try{
			ArrayList list = new ArrayList();
			for (int i=0; i<jsonArray.length(); i++) {
				Object o = jsonArray.get(i);			
				if (o instanceof JSONArray) {
					list.add(arrayToList((JSONArray)o));
				} else {
					list.add(new JSON((JSONObject) o).toMap());
				}
			}
			return list;
		} catch (Exception e){
			throw new RuntimeException(e);
		}
	}
}
