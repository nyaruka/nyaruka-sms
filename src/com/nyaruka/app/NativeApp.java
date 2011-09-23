package com.nyaruka.app;

import java.util.ArrayList;
import java.util.List;

import com.nyaruka.http.HttpRequest;
import com.nyaruka.http.HttpResponse;

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
	
	/**
	 * To be overloaded by our subclass.  This is where we wire all our routes.
	 */
	public abstract void buildRoutes();
	
	/** 
	 * Builds a regular expression for this app and action.
	 */
	public String buildActionRegex(String action){
		return "^/" + m_name + "/" + action + "/(.*)$";
	}

	/**
	 * Middleware-like hook for Native Apps.  Every request will have preProcess run on
	 * it for each NativeApp.  A Native app can either manipulate the request that is 
	 * passed in, or shortcut the request entirely by returning an HttpResponse.
	 * 
	 * @param request
	 * @return
	 */
	public HttpResponse preProcess(HttpRequest request){
		return null;
	}
	
	/**
	 * Middleware-like hook for Native Apps.  Every response will have postProcess
	 * run on it for each NativeApp.  A NativeApp can manipulate the request and/or
	 * response, or return a new response entirely.
	 * 
	 * @param request
	 * @param response
	 */
	public HttpResponse postProcess(HttpRequest request, HttpResponse response){
		return null;
	}
	
	/** 
	 * Middleware-like hook for Native Apps which allows them to manipulate a response
	 * if an exception is thrown. 
	 * 
	 * @param error The error that occurred
	 */
	public HttpResponse handleError(Throwable error){
		return null;
	}
}
