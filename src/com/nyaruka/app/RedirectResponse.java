package com.nyaruka.app;

import com.nyaruka.http.HttpResponse;
import com.nyaruka.http.NanoHTTPD;

public class RedirectResponse extends HttpResponse {
	
	public RedirectResponse(String url){
		super(NanoHTTPD.HTTP_REDIRECT, NanoHTTPD.MIME_PLAINTEXT, url);
		addHeader("Location", url);
	}
}
