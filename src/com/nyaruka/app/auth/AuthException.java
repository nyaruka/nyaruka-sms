package com.nyaruka.app.auth;

public class AuthException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public AuthException(String returnURL){
		this(returnURL, "You must be logged in to access this page.");
	}
	
	public AuthException(String returnURL, String error){
		m_returnURL = returnURL;
		m_error = error;
	}
	
	public String getReturnURL(){ return m_returnURL; }
	private String m_returnURL;
	
	public String getError(){ return m_error; }
	private String m_error;
}