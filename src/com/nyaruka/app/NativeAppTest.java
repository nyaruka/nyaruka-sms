package com.nyaruka.app;

import com.nyaruka.http.HttpRequest;
import com.nyaruka.http.HttpResponse;

import junit.framework.TestCase;

public class NativeAppTest extends TestCase {

	static class TestView extends View {
		public boolean called = false;
		
		public HttpResponse handle(HttpRequest request, String[] groups) {
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
	
	public void testGetRoutes(){
		NativeApp app = new TestApp();
		View loginView = new TestView();
		app.addRoute(app.buildActionRegex("login"), loginView);
		assertEquals(1, app.getRoutes().size());
		
		View indexView = new TestView();
		app.addRoute("/auth/", indexView);
		
		assertEquals(2, app.getRoutes().size());
	}
}
