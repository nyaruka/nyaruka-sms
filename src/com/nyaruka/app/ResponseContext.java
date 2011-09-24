package com.nyaruka.app;

import java.util.HashMap;

/**
 * Wrapper around the context used to render our templates.
 * 
 * @author nicp
 */
public class ResponseContext {
	public ResponseContext(){}

	public HashMap<String, Object> toMap(){ return m_map; }
	
	public void put(String key, Object value){
		m_map.put(key, value);
	}
	
	private HashMap<String, Object> m_map = new HashMap<String, Object>();	
}
