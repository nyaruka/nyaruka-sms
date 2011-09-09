package com.nyaruka.vm;

import com.nyaruka.db.DB;
import com.nyaruka.http.HttpRequest;

/**
 * Responsible for looking up or creating sessions for incoming requests.
 * 
 * @author nicp
 */
public class SessionManager {

	private DB m_db;
	public static final String SESSION_KEY = "BOA_SESSION_ID";
	
	public SessionManager(DB db){
		m_db = db;
	}
	
	public Session ensureSession(HttpRequest request){
		if (request.headers().contains(SESSION_KEY)){
		}
		
		return null;
	}
	
	public void clearSession(String sessionId){
		
	}
}
