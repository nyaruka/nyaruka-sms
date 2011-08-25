package com.nyaruka.db;

import junit.framework.TestCase;

public class DBTest extends TestCase {

	public void testDBCreation() throws Exception {
		DB db = new DB();
		db.open();
		db.init();
		
		// no collection yet
		assertFalse(db.collectionExists("test"));
		
		// create it
		RecordType type = db.ensureCollection("test");
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
		
		RecordType type = db.ensureCollection("test");
		type.ensureStrIndex("strIndex");
		assertEquals(0, type.getStrIndex("strIndex"));
		assertEquals(-1, type.getStrIndex("notThere"));
		
		type.ensureIntIndex("intIndex");
		assertEquals(0, type.getIntIndex("intIndex"));
		assertEquals(-1, type.getIntIndex("notThere"));
		
		db.saveRecordType(type);
		
		// reload
		db.load();
		
		type = db.ensureCollection("test");
	
		assertEquals(0, type.getStrIndex("strIndex"));
		assertEquals(0, type.getIntIndex("intIndex"));
		
		// add another str index
		type.ensureStrIndex("str2Index");
		assertEquals(1, type.getStrIndex("str2Index"));
		
		// and remove our original
		type.deleteIndex("strIndex");
		assertEquals(-1, type.getStrIndex("strIndex"));
		
		// save and reload
		db.saveRecordType(type);
		
		db.load();
		
		assertEquals(1, type.getStrIndex("str2Index"));
		assertEquals(-1, type.getStrIndex("strIndex"));
		
		// new index should fill gap now
		type.ensureStrIndex("str3Index");
		assertEquals(0, type.getStrIndex("str3Index"));
	}
}
