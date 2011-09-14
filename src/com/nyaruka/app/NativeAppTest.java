package com.nyaruka.app;

import java.util.List;

import com.nyaruka.http.HttpRequest;
import com.nyaruka.http.HttpResponse;

import junit.framework.TestCase;

public class NativeAppTest extends TestCase {

	static class TestView extends View {
		public boolean called = false;
		
		public HttpResponse handle(HttpRequest request) {
			called = true;
			return new HttpResponse("test");
		}
	}
	
	static class TestApp extends NativeApp {
		public TestApp(){
			super("auth");
		}
		
		public void buildRoutes() {
		}
	}
	
	public void testGetAction(){
		NativeApp app = new TestApp();
		View loginView = new TestView();
		app.addRoute(app.buildActionRegex("login"), loginView);
		
		assertEquals(loginView, app.findRoute("/auth/login/").getView());
		assertEquals(loginView, app.findRoute("/auth/login/nicpottier/").getView());
		assertNull(app.findRoute("/db/foo/"));
		assertNull(app.findRoute("/auth/"));		
		
		View indexView = new TestView();
		app.addRoute("/auth/", indexView);
		
		assertEquals(indexView, app.findRoute("/auth/").getView());
		assertEquals(loginView, app.findRoute("/auth/login/").getView());		
	}
}
