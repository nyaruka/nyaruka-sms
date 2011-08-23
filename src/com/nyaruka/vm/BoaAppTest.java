package com.nyaruka.vm;

import java.util.Properties;

import org.json.JSONObject;

import junit.framework.TestCase;

public class BoaAppTest extends TestCase {

	public void testSyntaxError(){
		StringBuffer log = new StringBuffer();
		BoaApp app = new BoaApp("test");
		assertFalse(app.load("asdf()", log));
		assertTrue(log.length() > 0);
	}
	
	public void testEmpty(){
		StringBuffer log = new StringBuffer();
		BoaApp app = new BoaApp("test");
		String empty = "";

		assertTrue(app.load(empty, log));
		
		assertNull(app.getRouter().lookupHttpHandler("foo"));
		
		HttpRequest request = new HttpRequest("hello", "GET", new Properties());
		assertNull(app.handleHttpRequest(request, log));
	}
	
	public void testLogging(){
		StringBuffer log = new StringBuffer();
		BoaApp app = new BoaApp("test");
		String main = "console.log(\"hello world\");";
		assertTrue(app.load(main, log));
		
		assertEquals("hello world", log.toString());
		
		assertNull(app.getRouter().lookupHttpHandler("foo"));
		
		HttpRequest request = new HttpRequest("hello", "GET", new Properties());
		assertNull(app.handleHttpRequest(request, log));
	}
	
	public void testBasic(){
		StringBuffer log = new StringBuffer();
		String basic =
			"function hello(req, resp){ " +
			"  resp.set('hello', 'world'); " +
			"  resp.setTemplate('hello.html'); " +			
			"  console.log(\"hello called\");" +
			"};  " + 
			"router.addHttpHandler('hello', hello);";
		
		BoaApp app = new BoaApp("test");		
		assertTrue(app.load(basic, log));
		
		assertEquals("", log.toString());
		assertNull(app.getRouter().lookupHttpHandler("foo"));
		assertNotNull(app.getRouter().lookupHttpHandler("hello"));
		
		// try calling the handler
		HttpRequest request = new HttpRequest("hello", "GET", new Properties());
		HttpResponse response = app.handleHttpRequest(request, log);
		
		assertNotNull(response);
		assertEquals("{\"hello\":\"world\"}", response.getData().toString());
		assertEquals("hello.html", response.getTemplate());
		assertEquals("hello called", log.toString());		
	}

	public void testTypes() throws Exception {
		StringBuffer log = new StringBuffer();
		String main =
			"function hello(req, resp){" +
			"	resp.set('int', 10);" +
			"	resp.set('long', 6000000000);" +
			"	resp.set('bool', true);" +
			"	resp.set('string', 'string');" +
			"	resp.set('double', 4.5);" +
			"}" + 
			"router.addHttpHandler('hello', hello);";
		
		BoaApp app = new BoaApp("test");		
		assertTrue(app.load(main, log));
		
		HttpRequest request = new HttpRequest("hello", "GET", new Properties());
		HttpResponse response = app.handleHttpRequest(request, log);
		
		JSONObject d = response.getData();
		assertEquals(true, d.getBoolean("bool")); 
		assertEquals(10, d.getInt("int")); 
		assertEquals(6000000000l, d.getLong("long")); 
		assertEquals("string", d.getString("string")); 
		assertEquals(4.5, d.getDouble("double")); 
	}
	
	public void testFunctions(){
		StringBuffer log = new StringBuffer();
		String main =
			"function add(a, b){" +
			"	return a + b;" +
			"};" +
			"function hello(req, resp){ " +
			"	resp.set('test', add(3,5));" +
			"};  " + 
			"router.addHttpHandler('hello', hello);";
		
		BoaApp app = new BoaApp("test");		
		assertTrue(app.load(main, log));
		
		HttpRequest request = new HttpRequest("hello", "GET", new Properties());
		HttpResponse response = app.handleHttpRequest(request, log);
		assertNotNull(response);
		assertEquals("{\"test\":8}", response.getData().toString());
	}
	
}
