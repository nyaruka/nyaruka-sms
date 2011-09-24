package com.nyaruka.http;

import java.util.List;

import junit.framework.TestCase;

public class RequestParametersTest extends TestCase {
	
	public void testParameters() {
		
		RequestParameters params = new RequestParameters();
		
		// key doesn't exist
		assertFalse(params.containsKey("pottier"));
		
		// stuff a key in there, and make sure it shows
		params.put("pottier", "is");
		assertTrue(params.containsKey("pottier"));
		
		// test single string parameter
		assertEquals("is", params.getProperty("pottier"));
		assertEquals(1, params.get("pottier").size());
		
		// put a few more things in there
		params.put("pottier", "a");
		params.put("pottier", "very");
		params.put("pottier", "french");
		params.put("pottier", "name");
		
		assertEquals(5, params.get("pottier").size());
		
		// make sure value list is as expected
		List<String> values = params.get("pottier");
		assertEquals("is", values.get(0));
		assertEquals("a", values.get(1));
		assertEquals("very", values.get(2));
		assertEquals("french", values.get(3));
		assertEquals("name", values.get(4));
		
	}

}
