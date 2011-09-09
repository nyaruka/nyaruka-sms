package com.nyaruka.http;

import java.util.Properties;

/**
 * Our representation of an HTTP request.
 * 
 * @author nicp
 */
public class HttpRequest {

	public static final String COOKIE_HEADER = "cookie";
	
	public HttpRequest(String url, String method, Properties headers, Properties params){
		m_url = url;
		m_method = method;
		m_params = params;
		m_headers = headers;
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
	
	public Properties headers(){
		return m_headers;
	}
	
	public void parseCookies(){
		Properties cookies = new Properties();
		String cookieHeader = m_headers.getProperty(COOKIE_HEADER);

		// if we have a cookie header,  parse it out
		if (cookieHeader != null){
			CookieTokenizer tokenizer = new CookieTokenizer();
			int numTokens = tokenizer.tokenize(cookieHeader);
			
			// cookie tokenizer is a bit crazy, keys are even numbers, values are odd
			for(int i=0; i<numTokens; i+=2){
				cookies.put(tokenizer.tokenAt(i), tokenizer.tokenAt(i+1));
			}
		}
		m_cookies = cookies;
	}
	
	public String getCookie(String key){
		return (String) getCookies().get(key);
	}
	public Properties getCookies(){ 
		if (m_cookies == null){
			parseCookies();
		}
		return m_cookies;
	}
	
	private String m_url;
	private String m_method;
	private Properties m_params;
	private Properties m_headers;
	private Properties m_cookies;
}
