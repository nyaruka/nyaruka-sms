package com.nyaruka.vm;

import java.util.ArrayList;
import java.util.List;

import org.mozilla.javascript.Function;

public class Router {
	
	static class HttpRoute {
		public HttpRoute(String regex, Function handler){
			m_regex = regex;
			m_handler = handler;
		}
		
		public boolean matches(String url){
			return url.matches(m_regex);
		}
		
		public Function getHandler(){ return m_handler; }
		
		private Function m_handler;
		private String m_regex;
	}
	
	public void addHttpHandler(String regex, Function function){
		m_httpRoutes.add(new HttpRoute(regex, function));
	}
	
	public Function lookupHttpHandler(String url){
		for (HttpRoute route : m_httpRoutes){
			if (route.matches(url)){
				return route.getHandler();
			}
		}
		return null;
	}
	
	public void reset(){
		m_httpRoutes.clear();
	}

	List<HttpRoute> m_httpRoutes = new ArrayList<HttpRoute>();
}
