package com.nyaruka.vm;

import com.nyaruka.http.HttpRequest;

import junit.framework.TestCase;

public class NativeBoaAppTest extends TestCase {

	public void testGetAction(){
		NativeBoaApp app = new NativeBoaApp(null){};
		
		HttpRequest request = new HttpRequest("/auth/login/");
		assertEquals("login", app.getAction("auth", request));
		
		request = new HttpRequest("/auth/login/nicpottier/");
		assertEquals("login", app.getAction("auth", request));
		
		request = new HttpRequest("/db/foo/");
		assertNull(app.getAction("auth", request));
		
		request = new HttpRequest("/auth/");
		assertEquals("", app.getAction("auth", request));
	}
}