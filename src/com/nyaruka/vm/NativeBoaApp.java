package com.nyaruka.vm;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.nyaruka.http.HttpRequest;

/**
 * Abstract base class for all Boa apps that are written natively in Java.
 *   
 * @author nicp
 */
public abstract class NativeBoaApp {
	private VM m_vm;
	
	public NativeBoaApp(VM vm){
		m_vm = vm;
	}

	Pattern APP_PATTERN = Pattern.compile("^/(\\w+)/(.*)$");
	Pattern ACTION_PATTERN = Pattern.compile("^/(\\w+)/(\\w+)/(.*)$");		
	
	/** 
	 * Given an HTTP request returns the action for the request.  This is essentially just the
	 * first slug after the name in the path.. ergo, an app called 'auth' with a url like:
	 *     /auth/login/
	 *     
	 * Would return 'login' as the action.  Extra elements to the path are ignored.
	 * 
	 * @param name
	 * @param request
	 * @return
	 */
	public String getAction(String name, HttpRequest request){
		Matcher matcher = APP_PATTERN.matcher(request.url());
		if (!matcher.find()){
			return null;
		}
		
		// make sure it matches our app
		String app = matcher.group(1);
		if (!app.equalsIgnoreCase(name)){
			return null;
		}
		
		// now match against our action
		matcher = ACTION_PATTERN.matcher(request.url());
		if (!matcher.find()){
			return "";
		}
		
		return matcher.group(1).toLowerCase();
	}
}
