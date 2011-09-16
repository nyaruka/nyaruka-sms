package com.nyaruka.app;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

import com.nyaruka.db.Collection;
import com.nyaruka.db.dev.DevDB;
import com.nyaruka.http.HttpRequest;
import com.nyaruka.http.HttpResponse;
import com.nyaruka.vm.JSEval;
import com.nyaruka.vm.VM;

import junit.framework.TestCase;

public class DBAppTest extends TestCase {

	private HttpResponse getResponse(NativeApp app, HttpRequest request){
		for (Route route : app.getRoutes()){
			String[] groups = route.matches(request.url());
			if (groups != null){
				HttpResponse response = route.getView().handle(request, groups);
				assertEquals("200 OK", response.getStatus());
				return response;
			}
		}
		return null;
	}
	
	public void testIndex(){
		VM vm = new VM(new DevDB());
		vm.start(new ArrayList<JSEval>());
		DBApp app = new DBApp(vm);
		
		// test our index page
		TemplateResponse resp = (TemplateResponse) getResponse(app, new HttpRequest("/db/"));
		HashMap<String, Object> context = resp.getContext();
		
		ArrayList<Collection> colls = (ArrayList<Collection>) context.get("collections");

		// should be one collection, sessions 
		assertEquals(1, colls.size());
		assertEquals("sessions", colls.get(0).getName());
		
		// create a new collection via a post
		Properties params = new Properties();
		params.put("name", "contacts");
		resp = (TemplateResponse) getResponse(app, new HttpRequest("/db/", "POST", new Properties(), params));
		context = resp.getContext();		
		
		// should now be two collections
		colls = (ArrayList<Collection>) context.get("collections");		
		assertEquals(2, colls.size());
	}
}
