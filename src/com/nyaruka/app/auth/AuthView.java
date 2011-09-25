package com.nyaruka.app.auth;

import com.nyaruka.http.HttpRequest;
import com.nyaruka.http.HttpResponse;
import com.nyaruka.app.View;

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
				throw new AuthException(request.url(), "You do not have permission to view this page. (" + permission + ")");
			}
		}
	}
	
	public HttpResponse pre(HttpRequest request, String[] groups){
		checkPermission(request);
		return null;
	}
}
