package com.nyaruka.http;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RequestParameters {

	private Map<String,List<String>> m_params;
	
	public RequestParameters() {
		m_params = new HashMap<String,List<String>>();
	}
	
	/**
	 * Adds a new parameter key/value pair returning whether that key already existed
	 */
	public void put(String key, String value) {

		List<String> values = m_params.get(key);		
		if (values == null) {
			values = new ArrayList<String>();
			m_params.put(key,  values);			
		}
		
		values.add(value);
	}
	
	public List<String> get(String key) {
		return m_params.get(key);
	}
	
	public Set<String> keys() {
		return m_params.keySet();
	}

	public boolean containsKey(String key) {
		return m_params.containsKey(key);
	}
	
	/**
	 * Shortcut for getting the first value for a key
	 */
	public String getProperty(String key) {
		List<String> values = m_params.get(key);
		if (values != null && values.size() > 0) {
			return values.get(0);
		}
		return null;
	}
	
}
