package com.nyaruka.app;

import java.util.ArrayList;
import java.util.Properties;

import com.nyaruka.db.DB;
import com.nyaruka.db.dev.DevDB;
import com.nyaruka.http.HttpRequest;
import com.nyaruka.http.HttpResponse;
import com.nyaruka.vm.JSEval;
import com.nyaruka.vm.Session;
import com.nyaruka.vm.SessionManager;
import com.nyaruka.vm.VM;

import junit.framework.TestCase;

public abstract class NativeAppTestCase extends TestCase {
	
	public void setUp(){
		m_db = new DevDB();
		m_vm = new VM(m_db);
		m_vm.start(new ArrayList<JSEval>());
		m_cookies.clear();
	}
	
	public HttpResponse getResponse(NativeApp app, HttpRequest request){
		// set our cookies on the request
		request.setCookies(m_cookies);
		
		Session session = m_vm.getSessions().ensureSession(request);
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
		
		m_vm.getSessions().save(session);
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
	
	protected DB m_db;
	protected VM m_vm;
	protected Properties m_cookies = new Properties();
}
