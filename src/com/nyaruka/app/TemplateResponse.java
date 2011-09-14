package com.nyaruka.app;

import java.util.HashMap;

import com.nyaruka.http.HttpResponse;

/**
 * Represents an HTTP response who's content is rendered via a template.
 * 
 * The actual rendering is done by calling render() at which point the real body of the response will be set.
 * 
 * @author nicp
 */
public class TemplateResponse extends HttpResponse {

	private String m_template;
	private HashMap<String, Object> m_context;
	
	public TemplateResponse(String template, HashMap<String, Object> context){
		m_template = template;
		m_context = context;
	}
	
	public String getTemplate(){ return m_template; }
	public HashMap<String, Object> getContext(){ return m_context; }
}
