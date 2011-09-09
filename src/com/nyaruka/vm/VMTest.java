package com.nyaruka.vm;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import junit.framework.TestCase;

import com.nyaruka.db.Collection;
import com.nyaruka.db.dev.DevDB;
import com.nyaruka.http.HttpRequest;
import com.nyaruka.json.JSON;
import com.nyaruka.util.FileUtil;

public class VMTest extends TestCase {

	public void testSyntaxError(){
		VM vm = new VM(new DevDB());
		BoaApp app = new BoaApp("test", "asdf()");
		vm.addApp(app);
		
		vm.start(getEvals());
		
		assertEquals(BoaApp.ERROR, app.getState()); 
		assertTrue(vm.getLog().length() > 0);
	}
	
	public void testEmpty(){
		VM vm = new VM(new DevDB());		
		BoaApp app = new BoaApp("test", "");
		vm.addApp(app);
		vm.start(getEvals());
		
		assertNull(vm.getRouter().lookupHttpHandler("foo"));
		
		HttpRequest request = new HttpRequest("hello", "GET", new Properties(), new Properties());
		assertNull(vm.handleHttpRequest(request, getRequestInit()));
	}
	
	public void testLogging(){
		VM vm = new VM(new DevDB());
		BoaApp app = new BoaApp("test", "console.log(\"hello world\");");
		vm.addApp(app);
		vm.start(getEvals());
		
		assertEquals("hello world\n", vm.getLog().toString());
	}
	
	public void testReload(){
		VM vm = new VM(new DevDB());
		String main =
			"function hello(req, resp){ " +
			"  console.log(\"original\");" +
			"};  " + 
			"router.addHttpHandler('hello', hello);";
		BoaApp app = new BoaApp("test", main);
		vm.addApp(app);
		
		List<JSEval> evals = getEvals();
		vm.start(evals);
		
		HttpRequest request = new HttpRequest("hello", "GET", new Properties(), new Properties());
		BoaResponse response = vm.handleHttpRequest(request, getRequestInit());
		assertEquals("original\n", vm.getLog().toString());
		
		vm.getLog().setLength(0);
		main =
			"function hello(req, resp){ " +
			"  console.log(\"reloaded\");" +
			"};  " + 
			"router.addHttpHandler('hello', hello);";
		app.setMain(main);
		vm.reload(evals);
		
		response = vm.handleHttpRequest(request, getRequestInit());
		assertEquals("reloaded\n", vm.getLog().toString());
	}
	
	public void testBasic(){
		VM vm = new VM(new DevDB());
		String basic =
			"function hello(req, resp){ " +
			"  resp.set('hello', 'world'); " +
			"  resp.setTemplate('hello.html'); " +			
			"  console.log(\"hello called\");" +
			"};  " + 
			"router.addHttpHandler('hello', hello);";
		BoaApp app = new BoaApp("test", basic);
		vm.addApp(app);
		vm.start(getEvals());
		
		assertNull(vm.getRouter().lookupHttpHandler("foo"));
		assertNotNull(vm.getRouter().lookupHttpHandler("hello"));
		
		// try calling the handler
		HttpRequest request = new HttpRequest("hello", "GET", new Properties(), new Properties());
		BoaResponse response = vm.handleHttpRequest(request, getRequestInit());
		
		assertNotNull(response);
		assertEquals("{\"hello\":\"world\"}", response.getData().toString());
		assertEquals("hello.html", response.getTemplate());
		assertEquals("hello called\n", vm.getLog().toString());		
	}

	public void testTypes() throws Exception {
		VM vm = new VM(new DevDB());
		String main =
			"function hello(req, resp){" +
			"	resp.set('int', 10);" +
			"	resp.set('long', 6000000000);" +
			"	resp.set('bool', true);" +
			"	resp.set('string', 'string');" +
			"	resp.set('double', 4.5);" +
			"}" + 
			"router.addHttpHandler('hello', hello);";
		BoaApp app = new BoaApp("test", main);
		vm.addApp(app);
		vm.start(getEvals());
		
		HttpRequest request = new HttpRequest("hello", "GET", new Properties(), new Properties());
		BoaResponse response = vm.handleHttpRequest(request, getRequestInit());
		
		JSON d = response.getData();
		assertEquals(true, d.getBoolean("bool")); 
		assertEquals(10, d.getInt("int")); 
		assertEquals(6000000000l, d.getLong("long")); 
		assertEquals("string", d.getString("string")); 
		assertEquals(4.5, d.getDouble("double")); 
	}
	
	public void testFunctions(){
		VM vm = new VM(new DevDB());
		String main =
			"function add(a, b){" +
			"	return a + b;" +
			"};" +
			"function hello(req, resp){ " +
			"	resp.set('test', add(3,5));" +
			"};  " + 
			"router.addHttpHandler('hello', hello);";
		BoaApp app = new BoaApp("test", main);		
		vm.addApp(app);
		vm.start(getEvals());
		
		HttpRequest request = new HttpRequest("hello", "GET", new Properties(), new Properties());
		BoaResponse response = vm.handleHttpRequest(request, getRequestInit());
		assertNotNull(response);
		assertEquals("{\"test\":8}", response.getData().toString());
	}
	
	public void testDB(){
		VM vm = new VM(new DevDB());
		String main = 
			"db.ensureCollection('contacts');" +
			"db.contacts.ensureIntIndex('age');" +
			"function addContact(req, resp){" +
			"  db.contacts.save({ name: req.params.name," +
			"                     age: req.params.age }); " +
			"}" +
			"router.addHttpHandler('/contacts/add', addContact);";
		BoaApp app = new BoaApp("contacts", main);
		vm.addApp(app);
		vm.start(getEvals());
		
		Properties params = new Properties();
		params.put("name", "Eric Newcomer");
		//params.put("age", "32");
		HttpRequest request = new HttpRequest("/contacts/add", "GET", new Properties(), params);
		BoaResponse response = vm.handleHttpRequest(request, getRequestInit());
		
		assertNotNull(response);
		
		Collection contacts = vm.getDB().ensureCollection("contacts");
		assertEquals(1, contacts.find("{}").count());
		assertEquals("Eric Newcomer", contacts.find("{}").next().getData().getString("name"));
	}
	
	private JSEval getRequestInit() {
		return new JSEval(FileUtil.slurpFile(new File("android/assets/sys/js/requestInit.js")), "requestInit.js");
	}
	
	private List<JSEval> getEvals() {
		List<JSEval> evals = new ArrayList<JSEval>();
		evals.add(new JSEval(FileUtil.slurpFile(new File("android/assets/static/js/json2.js")), "json2.js"));
		evals.add(new JSEval(FileUtil.slurpFile(new File("android/assets/sys/js/jsInit.js")), "jsInit.js"));
		return evals;
	}

}
