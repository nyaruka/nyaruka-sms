package com.nyaruka.vm;

import java.util.Properties;
import org.json.JSONObject;

import com.nyaruka.db.Collection;
import com.nyaruka.json.JSON;

import junit.framework.TestCase;

public class VMTest extends TestCase {

	public void testSyntaxError(){
		VM vm = new VM();
		BoaApp app = new BoaApp("test", "asdf()");
		vm.addApp(app);
		
		vm.start();
		
		assertEquals(BoaApp.ERROR, app.getState()); 
		assertTrue(vm.getLog().length() > 0);
	}
	
	public void testEmpty(){
		VM vm = new VM();		
		BoaApp app = new BoaApp("test", "");
		vm.addApp(app);
		vm.start();
		
		assertNull(vm.getRouter().lookupHttpHandler("foo"));
		
		HttpRequest request = new HttpRequest("hello", "GET", new Properties());
		assertNull(vm.handleHttpRequest(request));
	}
	
	public void testLogging(){
		VM vm = new VM();
		BoaApp app = new BoaApp("test", "console.log(\"hello world\");");
		vm.addApp(app);
		vm.start();
		
		assertEquals("hello world", vm.getLog().toString());
	}
	
	public void testReload(){
		VM vm = new VM();
		String main =
			"function hello(req, resp){ " +
			"  console.log(\"original\");" +
			"};  " + 
			"router.addHttpHandler('hello', hello);";
		BoaApp app = new BoaApp("test", main);
		vm.addApp(app);
		vm.start();
		
		HttpRequest request = new HttpRequest("hello", "GET", new Properties());
		HttpResponse response = vm.handleHttpRequest(request);
		assertEquals("original", vm.getLog().toString());
		
		vm.getLog().setLength(0);
		main =
			"function hello(req, resp){ " +
			"  console.log(\"reloaded\");" +
			"};  " + 
			"router.addHttpHandler('hello', hello);";
		app.setMain(main);
		vm.reload();
		
		response = vm.handleHttpRequest(request);
		assertEquals("reloaded", vm.getLog().toString());
	}
	
	public void testBasic(){
		VM vm = new VM();
		String basic =
			"function hello(req, resp){ " +
			"  resp.set('hello', 'world'); " +
			"  resp.setTemplate('hello.html'); " +			
			"  console.log(\"hello called\");" +
			"};  " + 
			"router.addHttpHandler('hello', hello);";
		BoaApp app = new BoaApp("test", basic);
		vm.addApp(app);
		vm.start();
		
		assertNull(vm.getRouter().lookupHttpHandler("foo"));
		assertNotNull(vm.getRouter().lookupHttpHandler("hello"));
		
		// try calling the handler
		HttpRequest request = new HttpRequest("hello", "GET", new Properties());
		HttpResponse response = vm.handleHttpRequest(request);
		
		assertNotNull(response);
		assertEquals("{\"hello\":\"world\"}", response.getData().toString());
		assertEquals("hello.html", response.getTemplate());
		assertEquals("hello called", vm.getLog().toString());		
	}

	public void testTypes() throws Exception {
		VM vm = new VM();
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
		vm.start();
		
		HttpRequest request = new HttpRequest("hello", "GET", new Properties());
		HttpResponse response = vm.handleHttpRequest(request);
		
		JSON d = response.getData();
		assertEquals(true, d.getBoolean("bool")); 
		assertEquals(10, d.getInt("int")); 
		assertEquals(6000000000l, d.getLong("long")); 
		assertEquals("string", d.getString("string")); 
		assertEquals(4.5, d.getDouble("double")); 
	}
	
	public void testFunctions(){
		VM vm = new VM();
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
		vm.start();
		
		HttpRequest request = new HttpRequest("hello", "GET", new Properties());
		HttpResponse response = vm.handleHttpRequest(request);
		assertNotNull(response);
		assertEquals("{\"test\":8}", response.getData().toString());
	}
	
	public void testDB(){
		VM vm = new VM();
		String main = 
			"db.ensureCollection('contacts');" +
			"db.contacts.ensureIntIndex('age');" +
			"function addContact(req, resp){" +
			"  db.contacts.save({ name: req.get('name')," +
			"                     age: req.get('age') }); " +
			"}" +
			"router.addHttpHandler('/contacts/add', addContact);";
		BoaApp app = new BoaApp("contacts", main);
		vm.addApp(app);
		vm.start();
		
		Properties params = new Properties();
		params.put("name", "Eric Newcomer");
		//params.put("age", "32");
		HttpRequest request = new HttpRequest("/contacts/add", "GET", params);
		HttpResponse response = vm.handleHttpRequest(request);
		
		assertNotNull(response);
		
		Collection contacts = vm.getDB().ensureCollection("contacts");
		assertEquals(1, contacts.find("{}").count());
		assertEquals("Eric Newcomer", contacts.find("{}").next().getData().getString("name"));
	}
}
