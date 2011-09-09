package com.nyaruka.vm;

import java.util.Properties;

import com.nyaruka.db.DB;
import com.nyaruka.db.dev.DevDB;
import com.nyaruka.http.HttpRequest;
import com.nyaruka.http.HttpResponse;

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
		
		// test the response encoding
		HttpResponse response = new HttpResponse();
		response.setCookie("session", "12345678901234578901234567890");
		assertEquals("session=12345678901234578901234567890; expires=Sat, 03 May 2025 17:44:22 GMT", response.getCookieString("session"));
	}
	
	public void testCreation(){
		DB db = new DevDB();
		db.open();
		db.init();
		
		SessionManager manager = new SessionManager(db);
		
		HttpRequest request = new HttpRequest("", "GET", new Properties(), new Properties());
		Session session = manager.ensureSession(request);
		
		assertTrue(session.isNew());
		assertTrue(session.getId() > 0);
		assertNotNull(session.getKey());
		
		// stuff some data in it
		session.set("user", "nicolas");
		manager.save(session);
		
		long sessionId = session.getId();
		String sessionKey = session.getKey();
		
		// new request should now get us the same session and data
		Properties headers = new Properties();
		headers.put("cookie", SessionManager.SESSION_KEY + "=" + sessionKey);
		request = new HttpRequest("", "GET", headers, new Properties());
		
		session = manager.ensureSession(request);
		
		assertFalse(session.isNew());
		assertEquals(sessionKey, session.getKey());
		assertEquals(sessionId, session.getId());
		assertEquals("nicolas", session.getData().getString("user"));
		
		// remove the field, try again
		session.clear("user");
		manager.save(session);
		
		request = new HttpRequest("", "GET", headers, new Properties());
		session = manager.ensureSession(request);
		
		assertFalse(session.isNew());
		assertEquals(sessionKey, session.getKey());
		assertEquals(sessionId, session.getId());
		assertFalse(session.getData().has("user"));
		
		// remove the session entirely
		manager.clearSession(session.getKey());
		
		request = new HttpRequest("", "GET", headers, new Properties());
		session = manager.ensureSession(request);
		
		assertTrue(session.isNew());
		assertTrue(session.getId() > 0);
		assertTrue(session.getKey() != sessionKey);
	}
}
