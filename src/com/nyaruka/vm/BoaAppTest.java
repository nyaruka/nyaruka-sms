package com.nyaruka.vm;

import java.util.Properties;

import junit.framework.TestCase;

public class BoaAppTest extends TestCase {
	
	public void testEmpty(){
		StringBuffer log = new StringBuffer();
		BoaApp app = new BoaApp("test");
		String empty = "";
		assertTrue(app.load(empty, log));
		
		assertNull(app.getRouter().lookupHttpHandler("foo"));
		
		HttpRequest request = new HttpRequest("hello", "GET", new Properties());
		assertNull(app.handleHttpRequest(request, log));
	}
	
	public void testBasic(){
		StringBuffer log = new StringBuffer();
		String basic =
			"function hello(req, resp){ " +
			"  resp.set('hello', 'world'); " +
			"};  " + 
			"router.addHttpHandler('hello', hello);";
		
		BoaApp app = new BoaApp("test");		
		assertTrue(app.load(basic, log));
		
		System.out.println(log);
		
		assertNull(app.getRouter().lookupHttpHandler("foo"));
		assertNotNull(app.getRouter().lookupHttpHandler("hello"));
		
		// try calling the handler
		HttpRequest request = new HttpRequest("hello", "GET", new Properties());
		HttpResponse response = app.handleHttpRequest(request, log);
		
		assertNotNull(response);
		assertEquals("{\"hello\":\"world\"}", response.getData().toString());
	}
	
}
