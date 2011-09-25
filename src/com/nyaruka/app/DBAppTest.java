package com.nyaruka.app;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

import com.nyaruka.db.Collection;
import com.nyaruka.db.dev.DevDB;
import com.nyaruka.http.HttpRequest;
import com.nyaruka.http.HttpResponse;
import com.nyaruka.http.RequestParameters;
import com.nyaruka.vm.JSEval;
import com.nyaruka.vm.VM;

import junit.framework.TestCase;

public class DBAppTest extends NativeAppTestCase {
	
	public void testIndex(){
		
		DBApp app = new DBApp(getVM());
			
		// test our index page
		TemplateResponse resp = (TemplateResponse) getResponse(app, getLoggedInRequest("/db/"));
		assert200(resp);
		HashMap<String, Object> context = resp.getContext();
		
		ArrayList<Collection> colls = (ArrayList<Collection>) context.get("collections");

		// should be two collections, users and sessions
		assertEquals(2, colls.size());
		assertEquals("users", colls.get(0).getName());
		assertEquals("sessions", colls.get(1).getName());
		
		// create a new collection via a post
		RequestParameters params = new RequestParameters();
		params.put("name", "contacts");
		
		resp = (TemplateResponse) getResponse(app, getLoggedInPost("/db/", params));
		assert200(resp);
		context = resp.getContext();		
		
		// should now be three collections
		colls = (ArrayList<Collection>) context.get("collections");		
		assertEquals(3, colls.size());
	}
}
