package com.nyaruka.vm;

import org.json.JSONObject;

/**
 * Our representation of an HTTP Response. 
 * 
 * @author nicp
 */
public class HttpResponse {

	public HttpResponse(){
	}
	
	public void set(String key, String value){
		try{
			m_data.put(key, value);
		} catch (Exception e){
			throw new RuntimeException(e);
		}
	}
	
	public void set(String key, int value){
		try{
			m_data.put(key, value);
		} catch (Exception e){
			throw new RuntimeException(e);
		}
	}
	
	public void set(String key, boolean value){
		try{
			m_data.put(key, value);
		} catch (Exception e){
			throw new RuntimeException(e);
		}
	}
	
	public void set(String key, double value){
		try{
			m_data.put(key, value);
		} catch (Exception e){
			throw new RuntimeException(e);
		}
	}
	
	public JSONObject getData(){
		return m_data;
	}
	
	public void setTemplate(String template){
		m_template = template;
	}
	
	public String getTemplate(){
		return m_template;
	}
	
	private JSONObject m_data = new JSONObject();
	private String m_template = null;
}
