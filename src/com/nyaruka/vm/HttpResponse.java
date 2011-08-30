package com.nyaruka.vm;

import org.json.JSONArray;

import com.nyaruka.json.JSON;

/**
 * Our representation of an HTTP Response. 
 * 
 * @author nicp
 */
public class HttpResponse {

	public HttpResponse(BoaApp app) {
		m_app = app;
	}

	public void setJSON(String key, String json){
		m_data.put(key, new JSON(json));
	}
	
	public void setJSONArray(String key, String json){
		try{
			m_data.put(key, new JSONArray(json));
		} catch (Throwable t){
			throw new RuntimeException(t);
		}
	}
	
	public void set(String key, boolean value){
		m_data.put(key, value);
	}
	
	public void set(String key, String value){
		m_data.put(key, value);
	}
	
	public void set(String key, int value){
		m_data.put(key, value);
	}
	
	public void set(String key, long value){
		m_data.put(key, value);
	}
	
	public void set(String key, double value){
		m_data.put(key, value);
	}
	
	public JSON getData(){
		return m_data;
	}
	
	public void setTemplate(String template){
		m_template = template;
	}
	
	public String getTemplate(){
		return m_template;
	}
	
	public BoaApp getApp() {
		return m_app;
	}
	
	private BoaApp m_app;
	private JSON m_data = new JSON();
	private String m_template = null;
}
