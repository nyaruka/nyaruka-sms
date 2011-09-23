package com.nyaruka.http;

import java.util.Properties;

import com.nyaruka.app.AuthApp.User;
import com.nyaruka.vm.Session;

/**
 * Our representation of an HTTP request.
 * 
 * @author nicp
 */
public class HttpRequest {

	public static final String COOKIE_HEADER = "cookie";
	public static final String POST = "POST";
	public static final String GET = "GET";
	
	public HttpRequest(String url){
		this(url, GET, new Properties(), new RequestParameters());
	}
	
	public HttpRequest(String url, String method){
		this(url, method, new Properties(), new RequestParameters());
	}	
	
	public HttpRequest(String url, String method, Properties headers, RequestParameters params){
		m_url = url;
		m_method = method;
		m_params = params;
		m_headers = headers;
	}
	
	public String url(){
		return m_url;
	}
	
	public Session session(){
		return m_session;
	}
	
	public void setSession(Session session){
		m_session = session;
	}
	
	public void setUser(User user){
		m_user = user;
	}
	
	public User user(){
		return m_user;
	}
	
	public String method(){
		return m_method;
	}
	
	public RequestParameters params(){
		return m_params;
	}
	
	public Properties headers(){
		return m_headers;
	}
	
	public void setParam(String key, String value){
		m_params.put(key, value);
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
	
	public void setCookies(Properties cookies){
		m_cookies = cookies;
	}
	
	public String toString(){
		return m_method + " " + m_url;
	}
	
	private String m_url;
	private String m_method;
	private RequestParameters m_params;
	private Properties m_headers;
	private Properties m_cookies;
	private Session m_session;
	private User m_user;
}
