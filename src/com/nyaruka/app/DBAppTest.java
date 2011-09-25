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
		VM vm = new VM(new DevDB());
		vm.start(new ArrayList<JSEval>());
		DBApp app = new DBApp(vm);
		
		// test our index page
		TemplateResponse resp = (TemplateResponse) getResponse(app, new HttpRequest("/db/"));
		assert200(resp);
		HashMap<String, Object> context = resp.getContext();
		
		ArrayList<Collection> colls = (ArrayList<Collection>) context.get("collections");

		// should be one collection, sessions 
		assertEquals(1, colls.size());
		assertEquals("sessions", colls.get(0).getName());
		
		// create a new collection via a post
		RequestParameters params = new RequestParameters();
		params.put("name", "contacts");
		resp = (TemplateResponse) getResponse(app, new HttpRequest("/db/", "POST", new Properties(), params));
		assert200(resp);
		context = resp.getContext();		
		
		// should now be two collections
		colls = (ArrayList<Collection>) context.get("collections");		
		assertEquals(2, colls.size());
	}
}
