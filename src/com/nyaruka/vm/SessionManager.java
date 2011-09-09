package com.nyaruka.vm;

import com.nyaruka.db.Collection;
import com.nyaruka.db.Cursor;
import com.nyaruka.db.DB;
import com.nyaruka.db.Record;
import com.nyaruka.http.HttpRequest;
import com.nyaruka.json.JSON;

/**
 * Responsible for looking up or creating sessions for incoming requests.
 * 
 * @author nicp
 */
public class SessionManager {

	private DB m_db;
	public static final String SESSION_KEY = "BOA_SESSION_KEY";
	
	public SessionManager(DB db){
		m_db = db;
		Collection collection = m_db.ensureCollection("sessions");
		collection.ensureStrIndex("key");
	}
	
	public Session ensureSession(HttpRequest request){
		String sessionKey = request.getCookie(SESSION_KEY);
		Session session = null;
		
		Collection collection = m_db.ensureCollection("sessions");
		if (sessionKey != null){
			JSON json = new JSON();
			json.put("key", sessionKey);
			Cursor found = collection.find(json);
			if (found.count() == 1){
				Record record = found.next();
				JSON data = record.getData();
				session = new Session(record.getId(), data.getString("key"), data.getJSON("data")); 
			}
		}

		if (session == null){
			// we either didn't find a session, or we didn't find the key,
			// either way we need to create a new session
			session = Session.generate();
		
			// stuff it in our db
			Record record = collection.save(session.asJSON());
			session.setId(record.getId());
		}
		
		// stuff it in our request
		request.setSession(session);
		return session;
	}
	
	public void save(Session session){
		if (session.needsSaving()){
			Collection collection = m_db.ensureCollection("sessions");
			collection.save(session.asJSON());
		}
	}
	
	public void clearSession(String sessionKey){
		// remove that key from the db
		Collection collection = m_db.ensureCollection("sessions");
		JSON json = new JSON();
		json.put("key", sessionKey);
		Cursor sessions = collection.find(json);
		while (sessions.hasNext()){
			collection.delete(sessions.next().getId());
		}
	}
}
