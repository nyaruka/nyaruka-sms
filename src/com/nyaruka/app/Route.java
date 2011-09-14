package com.nyaruka.app;

public class Route {
	private String m_regex;
	private View m_view;
	
	public Route(String regex, View view){
		m_regex = regex;
		m_view = view;
	}
	
	public boolean matches(String url){
		return url.matches(m_regex);
	}
	
	public String getRegex(){ return m_regex; }
	public View getView(){ return m_view; }
}
