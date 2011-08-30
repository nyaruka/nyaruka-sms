package com.nyaruka.vm;

import java.util.ArrayList;
import java.util.List;

import org.mozilla.javascript.Function;

public class Router {
	
	static class HttpRoute {
		public HttpRoute(BoaApp app, String regex, Function handler){
			m_regex = regex;
			m_handler = handler;
			m_app = app;
		}
		
		public boolean matches(String url){
			return url.matches(m_regex);
		}
		
		public BoaApp getApp() { return m_app; }
		
		public Function getHandler(){ return m_handler; }
		
		private Function m_handler;
		private String m_regex;
		private BoaApp m_app;
	}
	
	public void addHttpHandler(String regex, Function function){		
		m_httpRoutes.add(new HttpRoute(m_currentApp, regex, function));
	}
	
	public HttpRoute lookupHttpHandler(String url){
		for (HttpRoute route : m_httpRoutes){
			if (route.matches(url)){
				return route;
			}
		}
		return null;
	}

	public void setCurrentApp(BoaApp app) {
		m_currentApp = app;
	}

	
	public void reset(){
		m_httpRoutes.clear();
	}

	private BoaApp m_currentApp;
	private List<HttpRoute> m_httpRoutes = new ArrayList<HttpRoute>();

}
