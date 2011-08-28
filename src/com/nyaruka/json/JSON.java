package com.nyaruka.json;

import java.util.Iterator;

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
}
