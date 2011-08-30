package com.nyaruka.vm;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ScriptableObject;

/**
 * Represents a single app in Boa.
 * 
 * @author nicp
 */
public class BoaApp {

	public static final char NEW = 'N';
	public static final char ERROR = 'E';
	public static final char RUNNING = 'L';
	public static final char STALE = 'S';
	
	public BoaApp(String namespace, String main){
		m_namespace = namespace;
		m_main = main;
	}

	/**
	 * Loads this application from the passed in string (which will almost
	 * always just be read from main.js)
	 * 
	 * This resets the Rhino VM object. 
	 * 
	 * @param main
	 */
	public void load(Context context, ScriptableObject scope){
		context.evaluateString(scope, m_main, m_namespace + "/main.js", 1, null);
	}

	public void setMain(String main){
		m_main = main;
		m_state = STALE;
	}

	public String getNamespace() {
		return m_namespace;
	}

	public char getState(){ return m_state; }
	public void setState(char state){ m_state = state; }
	
	/** the state of this app */
	private char m_state = 'N';
	
	/** the namespace for this app, aka, the name */
	private String m_namespace;
	
	/** Our main, essentially the code for this app */
	private String m_main;

}
