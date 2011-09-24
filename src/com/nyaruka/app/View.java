package com.nyaruka.app;

import com.nyaruka.http.HttpRequest;
import com.nyaruka.http.HttpResponse;

/**
 * Given an HTTPRequest object will return an HTTPResponse
 * 
 * @author nicp
 */
public abstract class View {
	
	public HttpResponse pre(HttpRequest request, String[] groups){
		return null;
	}
	
	public HttpResponse handleRequest(HttpRequest request, String[] groups){
		HttpResponse resp = pre(request, groups);
		if (resp == null){
			resp = handle(request, groups);
		}
		
		HttpResponse overload = post(request, groups, resp);
		if (overload != null){
			resp = overload;
		}
		
		return resp;
	}
	
	public abstract HttpResponse handle(HttpRequest request, String[] groups);
	
	public HttpResponse post(HttpRequest request, String[] groups, HttpResponse response){
		return null;
	}
}
