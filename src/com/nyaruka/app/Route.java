package com.nyaruka.app;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Route {
	private Pattern m_pattern;
	private String m_regex;
	private View m_view;
	
	public Route(String regex, View view){
		m_regex = regex;
		m_view = view;
		m_pattern = Pattern.compile(regex);
	}

	/**
	 * Tries to match the passed in URL to this route.  If it does match
	 * then returns a String array of the matching Regex groups.  Otherwise
	 * returns null.
	 * 
	 * @param url
	 * @return
	 */
	public String[] matches(String url){
		Matcher matcher = m_pattern.matcher(url);
		if (matcher.find()){
			// build our groups
			String[] groups = new String[matcher.groupCount()];
			for(int i=0; i<groups.length; i++){
				groups[i] = matcher.group(i=1);
			}
			
			return groups;
		} else {
			return null;
		}
	}
	
	public String getRegex(){ return m_regex; }
	public View getView(){ return m_view; }
}
