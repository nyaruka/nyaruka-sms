package com.nyaruka.vm;

import java.util.Properties;

import com.nyaruka.http.HttpRequest;

import junit.framework.TestCase;

public class SessionTest extends TestCase {

	public void testCookies(){
		HttpRequest request = new HttpRequest("", "GET", new Properties(), new Properties());
		assertNull(request.getCookie("foo"));
		
		Properties headers = new Properties();
		headers.setProperty("cookie", "sessionid=abc, csrftoken=efg;");
		request = new HttpRequest("", "GET", headers, new Properties());
		
		assertNull(request.getCookie("foo"));
		assertEquals("abc", request.getCookie("sessionid"));
		assertEquals("efg", request.getCookie("csrftoken"));
	}
	
	public void testCreation(){
		
	}
}
