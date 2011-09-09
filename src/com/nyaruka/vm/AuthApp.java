package com.nyaruka.vm;

import com.nyaruka.http.HttpRequest;
import com.nyaruka.http.HttpResponse;
import com.nyaruka.http.NanoHTTPD;

/**
 * Responsible for providing pages to login, create accounts and authenticate.
 * 
 * @author nicp
 */
public class AuthApp {

	private VM m_vm;
	
	public AuthApp(VM vm){
		m_vm = vm;
	}

	/**
	 * Handles the passed in HttpRequest
	 * 
	 * @param request
	 * @return
	 */
	public HttpResponse handle(HttpRequest request){
						
		
		return new HttpResponse(NanoHTTPD.HTTP_NOTFOUND, "not found, sorry"); 
	}
}
