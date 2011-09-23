package com.nyaruka.app;

import com.nyaruka.http.HttpResponse;
import com.nyaruka.http.NanoHTTPD;

public class RedirectResponse extends HttpResponse {
	
	public RedirectResponse(String destination){
		super(NanoHTTPD.HTTP_REDIRECT, NanoHTTPD.MIME_PLAINTEXT, destination);
		addHeader("Location", destination);
		m_destination = destination;
	}
	
	public String getDestination(){ return m_destination; }
	
	private String m_destination;
}
