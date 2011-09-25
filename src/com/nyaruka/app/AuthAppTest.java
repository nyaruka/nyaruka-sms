package com.nyaruka.app;

import com.nyaruka.app.AuthApp.User;
import com.nyaruka.http.HttpRequest;
import com.nyaruka.http.HttpResponse;

public class AuthAppTest extends NativeAppTestCase {
	
	public void testLogin(){
		AuthApp app = new AuthApp(m_vm);
		
		// assert we can reach the login page as a get
		HttpResponse resp = getResponse(app, new HttpRequest("/auth/login/"));
		assert200(resp);
	
		// try to login without a valid user
		HttpRequest req = new HttpRequest("/auth/login/", HttpRequest.POST);
		req.setParam("username", "hansolo");
		req.setParam("password", "mfalcon");
		resp = getResponse(app, req);
		
		// should still be a 200 (the page saying you need to try again cuz it's a bad login)
		assert200(resp);
		
		// create a user, check our password algorithm
		User u = app.createUser("hansolo", "mfalcon");
		assertFalse(u.checkPassword("leah"));
		assertTrue(u.checkPassword("mfalcon"));
		
		// try to log in again, should work this time
		resp = getResponse(app, req);
		
		// this time we should be redirected to '/' as we had a successful login
		assertRedirect(resp, "/");
		
		// hit our user page
		req = new HttpRequest("/auth/");
		resp = getResponse(app, req);
		assert200(resp);
		
		// make sure our user was set in the request
		User user = req.user();
		assertNotNull(user);
		assertEquals("hansolo", user.getUsername());
		
		// ok, now let's log out
		resp = getResponse(app, new HttpRequest("/auth/logout/"));
		assertRedirect(resp, "/");
		
		// hitting our user page, we shouldn't have a user
		req = new HttpRequest("/auth/");
		resp = getResponse(app, req);
		assert200(resp);	
		
		user = req.user();
		assertNull(user);
	}
}
