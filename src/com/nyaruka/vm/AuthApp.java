package com.nyaruka.vm;

import java.util.HashMap;

import com.nyaruka.http.HttpRequest;
import com.nyaruka.http.HttpResponse;

/**
 * Responsible for providing pages to login, create accounts and authenticate.
 * 
 * @author nicp
 */
public class AuthApp extends NativeBoaApp {

	public AuthApp(BoaServer boa){
		super(boa, "auth");
	}

	public HttpResponse handle(HttpRequest request){
		String action = getAction(request);
		String body = null;
		
		if (action.equals("")){
		}
		else if (action.equals("login")){
			body = renderTemplate("/auth/login.html", new HashMap<String, Object>());
		}
		if (body != null){
			return new HttpResponse(body);
		}
		return null;
	}
}
