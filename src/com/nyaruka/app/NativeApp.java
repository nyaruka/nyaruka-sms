package com.nyaruka.app;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.nyaruka.http.HttpRequest;
import com.nyaruka.http.HttpResponse;
import com.nyaruka.vm.BoaServer;

/**
 * Abstract base class for all Boa apps that are written natively in Java.
 *   
 * @author nicp
 */
public abstract class NativeApp {
	protected String m_name;
	protected ArrayList<Route> m_routes = new ArrayList<Route>();
	
	public NativeApp(String name){
		m_name = name;
		buildRoutes();
	}
	
	public List<Route> getRoutes(){ return m_routes; }
	public void addRoute(String regex, View view){ m_routes.add(new Route(regex, view)); }
	
	public Route findRoute(String path){
		for (Route route : m_routes){
			if (route.matches(path)){
				return route;
			}
		}
		return null;
	}
	
	/**
	 * To be overloaded by our subclass.  This is where we wire all our routes.
	 */
	public abstract void buildRoutes();
	
	/**
	 * Entry point for requests. 
	 */
	public HttpResponse handle(HttpRequest request){
		Route route = findRoute(request.url());
		if (route != null){
			return route.getView().handle(request);
		} else {
			return null;
		}
	}
	
	/** 
	 * Builds a regular expression for this app and action.
	 */
	public String buildActionRegex(String action){
		return "^/" + m_name + "/" + action + "/(.*)$";
	}
}
