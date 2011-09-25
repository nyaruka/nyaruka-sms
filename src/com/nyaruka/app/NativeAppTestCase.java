package com.nyaruka.app;

import java.util.Properties;

import junit.framework.TestCase;

import com.nyaruka.app.auth.AuthApp;
import com.nyaruka.app.auth.User;
import com.nyaruka.http.HttpRequest;
import com.nyaruka.http.HttpResponse;
import com.nyaruka.http.RequestParameters;
import com.nyaruka.vm.Session;
import com.nyaruka.vm.SessionManager;
import com.nyaruka.vm.TestBoaServer;
import com.nyaruka.vm.VM;

public abstract class NativeAppTestCase extends TestCase {
	
	protected TestBoaServer m_server;
	private User m_user;
	
	public void setUp(){		
		m_server = new TestBoaServer();	
		m_cookies.clear();
		
		// create a test user
		AuthApp auth = new AuthApp(getVM());
		m_user = auth.createUser("test", "user");

	}
	
	public HttpRequest getLoggedInRequest(String path) {
		HttpRequest request = new HttpRequest(path);
		request.setUser(m_user);
		return request;
	}
	
	public HttpRequest getLoggedInPost(String path, RequestParameters params) {
		HttpRequest request = new HttpRequest(path, "POST", new Properties(), params);
		request.setUser(m_user);
		return request;
		
	}
	
	public VM getVM() {
		return m_server.getVM();
	}

	public HttpResponse getResponse(NativeApp app, HttpRequest request){
		// set our cookies on the request
		request.setCookies(m_cookies);
		
		Session session = getVM().getSessions().ensureSession(request);
		HttpResponse response = null;
		
		// preprocess
		response = app.preProcess(request);
		if (response != null){
			return response;
		}
		
		for (Route route : app.getRoutes()){
			String[] groups = route.matches(request.url());
			if (groups != null){
				response = route.getView().handleRequest(request, groups);
			}
		}
		
		// postprocess
		HttpResponse override = app.postProcess(request, response);
		if (override != null){
			response = override;
		}
		
		getVM().getSessions().save(session);
		if (session.isNew()){
			response.setCookie(SessionManager.SESSION_KEY, session.getKey());
		}
		
		if (response != null){
			for(Object key : response.getCookies().keySet()){
				m_cookies.put(key.toString(), response.getCookies().get(key).toString());
			}
		}

		return response;
	}
	
	public void assert200(HttpResponse response){
		assertNotNull(response);
		assertEquals("200 OK", response.getStatus());
	}
	
	public void assertRedirect(HttpResponse response, String url){
		assertTrue(response instanceof RedirectResponse);
		RedirectResponse red = (RedirectResponse) response;
		assertTrue(red.getDestination().contains(url));
	}
	
	protected Properties m_cookies = new Properties();
}
