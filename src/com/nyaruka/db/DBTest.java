package com.nyaruka.db;

import org.json.JSONObject;

import junit.framework.TestCase;

public class DBTest extends TestCase {

	public void testDBCreation() throws Exception {
		DB db = new DB();
		db.open();
		db.init();
		
		// no collection yet
		assertFalse(db.collectionExists("test"));
		
		// create it
		Collection type = db.ensureCollection("test");
		assertNotNull(type);
		assertEquals(1, type.getId());
		
		// ensure it is there
		assertTrue(db.collectionExists("test"));
		
		// reload from our tables
		db.load();
		
		// ensure it is still there
		assertTrue(db.collectionExists("test"));
		
		// clear the db
		db.clear();
		
		// assert it's all gone
		assertFalse(db.collectionExists("test"));
		
		// try to reload from the tables
		db.load();
		
		// still gone
		assertFalse(db.collectionExists("test"));
	}
	
	public void testSavingTypes() throws Exception {
		DB db = new DB();
		db.open();
		db.init();
		
		Collection collection = db.ensureCollection("test");
		collection.ensureStrIndex("strIndex");
		assertEquals(0, collection.getStrIndex("strIndex"));
		assertEquals(-1, collection.getStrIndex("notThere"));
		
		collection.ensureIntIndex("intIndex");
		assertEquals(0, collection.getIntIndex("intIndex"));
		assertEquals(-1, collection.getIntIndex("notThere"));
		
		db.saveCollection(collection);
		
		// reload
		db.load();
		
		collection = db.ensureCollection("test");
	
		assertEquals(0, collection.getStrIndex("strIndex"));
		assertEquals(0, collection.getIntIndex("intIndex"));
		
		// add another str index
		collection.ensureStrIndex("str2Index");
		assertEquals(1, collection.getStrIndex("str2Index"));
		
		// and remove our original
		collection.deleteIndex("strIndex");
		assertEquals(-1, collection.getStrIndex("strIndex"));
		
		// save and reload
		db.saveCollection(collection);
		
		db.load();
		
		assertEquals(1, collection.getStrIndex("str2Index"));
		assertEquals(-1, collection.getStrIndex("strIndex"));
		
		// new index should fill gap now
		collection.ensureStrIndex("str3Index");
		assertEquals(0, collection.getStrIndex("str3Index"));
	}
	
	public void testAddingRecords() throws Exception {
		DB db = new DB();
		db.open();
		db.init();
		
		Collection col = db.ensureCollection("test");
		
		JSONObject simple = new JSONObject();
		simple.put("string", "mystring");
		simple.put("int", 10);
		simple.put("long", 10000000000l);
		simple.put("double", 1.234d);
		simple.put("boolean", false);
		
		// add the record to our collection
		Record rec = col.addRecord(simple);
		
		assertNotNull(rec);
		assertEquals(1, rec.getId());
		
		// fetch it from the db
		rec = col.getRecord(rec.getId());
		
		// make sure what we got it is the same as we put in
		JSONObject data = rec.getData();
		assertEquals("mystring", data.getString("string"));
		assertEquals(10, data.getInt("int"));
		assertEquals(10000000000l, data.getLong("long"));
		assertEquals(1.234d, data.getDouble("double"));
		assertEquals(false, data.getBoolean("boolean"));
	}
}
