package com.nyaruka.vm;

import org.mozilla.javascript.ScriptableObject;

import com.nyaruka.db.Collection;
import com.nyaruka.db.DB;

public class DBWrapper extends ScriptableObject {

	public DBWrapper(){}
	
	@Override
	public String getClassName() {
		return "DBWrapper";
	}

	public void ensureCollection(String name){
		VM.getVM().getDB().ensureCollection(name);
	}
	
	public void addCollection(String name, Collection collection){
		ScriptableObject.putProperty(this, name, collection);
	}
	
	private DB m_db;
}
