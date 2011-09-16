package com.nyaruka.app;

import com.nyaruka.http.HttpRequest;
import com.nyaruka.http.HttpResponse;

/**
 * Given an HTTPRequest object will return an HTTPResponse
 * 
 * @author nicp
 */
public abstract class View {
	public abstract HttpResponse handle(HttpRequest request, String[] groups);
}
