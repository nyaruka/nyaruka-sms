package com.nyaruka.vm;

import java.util.UUID;

import com.nyaruka.json.JSON;

/**
 * Represents a sesion in the system.
 *
 * @author nicp
 */
public class Session {

	/** db id */
	private long m_id;
	
	/** cookie key */
	private String m_key;
	
	/** whether our key needs to be persisted in the response */
	private boolean m_isNew = false;
	
	/** whether this session data needs to be saved */
	private boolean m_needsSaving = false;
	
	/** the username of the logged in user for this session, if there is one */
	private String m_user;
	
	/** the data of our session */
	private JSON m_data;
	
	public Session(long id, String key, String user, JSON data){
		m_id = id;
		m_key = key;
		m_data = data;
		m_user = user;
	}
	
	public Session(String key){
		m_key = key;
		m_data = new JSON();
		m_isNew = true;
	}
	
	public static Session generate(){
		return new Session(UUID.randomUUID().toString());
	}
	
	/** Used to determine whether this session's id needs to be set as a cookie */ 
	public boolean isNew(){ return m_isNew; }
	public JSON getData(){ return m_data; }

	/** The logged in user */
	public void setUser(String user){
		m_user = user;
		requestSave();
	}
	
	public String getUser(){ return m_user; }
	
	/** convenience wrapper to our JSON data store */
	public void set(String key, String value){
		m_data.put(key, value);
		requestSave();		
	}
	
	/** convenience wrapper to our JSON data store */
	public void set(String key, long value){
		m_data.put(key, value);
		requestSave();
	}
	
	public String get(String key){
		return m_data.get(key).toString();
	}
	
	public void clear(String key){
		m_data.remove(key);
		requestSave();
	}
	
	public void requestSave(){ m_needsSaving = true; }
	public boolean needsSaving(){ return m_needsSaving; }
	
	public void setId(long id){ m_id = id; }
	public long getId(){ return m_id; }
	
	public String getKey(){ return m_key; }
	
	public String toString(){
		return asJSON().toString();
	}
	
	/** JSON blob ready to be stored in the db */
	public JSON asJSON(){
		JSON json = new JSON();
		json.put("key", m_key);
		json.put("data", m_data);
		
		if (m_id > 0){
			json.put("id", m_id);
		}
		
		if (m_user != null){
			json.put("user", m_user);
		}
		
		return json; 
	}
}
