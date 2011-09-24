package com.nyaruka.app;

import com.nyaruka.http.HttpRequest;
import com.nyaruka.http.HttpResponse;
import com.nyaruka.app.AuthApp.AuthException;
import com.nyaruka.app.AuthApp.User;

public abstract class AuthView extends View {
	
	public String getPermission(){
		return null;
	}
	
	public void checkPermission(HttpRequest request){
		User user = request.user();
		if (user == null){
			throw new AuthException(request.url());
		}
		
		String permission = getPermission();
		if (permission != null){
			if (!user.hasPermission(permission)){
				throw new AuthException(request.url());
			}
		}
	}
	
	public HttpResponse pre(HttpRequest request, String[] groups){
		checkPermission(request);
		return null;
	}
}
