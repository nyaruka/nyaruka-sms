package com.nyaruka.vm;

import java.util.Properties;

/**
 * Our representation of an HTTP request.
 * 
 * @author nicp
 */
public class HttpRequest {
	
	public HttpRequest(String url, String method, Properties params){
		m_url = url;
		m_method = method;
		m_params = params;
	}
	
	public String url(){
		return m_url;
	}
	
	public String method(){
		return m_method;
	}
	
	public Properties params(){
		return m_params;
	}
	
	private String m_url;
	private String m_method;
	private Properties m_params;
}
