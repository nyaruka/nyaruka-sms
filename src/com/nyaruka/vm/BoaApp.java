package com.nyaruka.vm;

import java.io.CharArrayWriter;
import java.io.PrintWriter;
import java.lang.reflect.Method;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.ScriptableObject;

/**
 * Represents a single app in Boa.
 * 
 * @author nicp
 */
public class BoaApp {

	public BoaApp(String namespace){
		m_namespace = namespace;
		m_router = new Router();
	}

	/**
	 * Loads this application from the passed in string (which will almost
	 * always just be read from main.js)
	 * 
	 * This resets the Rhino VM object. 
	 * 
	 * @param main
	 */
	public boolean load(String main, StringBuffer log){
		// Create our context and turn off compilation
		m_context = Context.enter();
		m_context.setOptimizationLevel(-1);
		
		m_scope = m_context.initStandardObjects();
		
		// clear our current router
		m_router.reset();
		
		LoggingWrapper logWrapper = new LoggingWrapper(log);
		
		// and put it in the scope
		ScriptableObject.putProperty(m_scope, "router", m_router);
		ScriptableObject.putProperty(m_scope, "console", logWrapper);
		
		Object result;
		try{
			result = m_context.evaluateString(m_scope, main, "", 1, null);
		} catch (Throwable t){
			CharArrayWriter stack = new CharArrayWriter();
			t.printStackTrace(new PrintWriter(stack));
			log.append(stack.toCharArray());
			return false;
		} 
		return true;
	}
	
	/**
	 * Returns whether this application has a route that matches the passed in url.
	 * 
	 * @param url
	 */
	public Router getRouter(){
		return m_router;
	}
	
	/**
	 * BoaApp should try to handle the passed in request, setting any values
	 * used in the response object.
	 * 
	 * Returns whether the request was actually handled or not.
	 * 
	 * @param request The request to handle
	 * @param log A buffered string to log any messages to
	 * @return HttpResponse if this app handles this request, null otherwise
	 * @throws RuntimeException if an error occurs running the handler
	 */
	public HttpResponse handleHttpRequest(HttpRequest request, StringBuffer log){
		Function handler = m_router.lookupHttpHandler(request.url());
		
		// no handler found?  then this app doesn't deal with this, return null
		if (handler == null){
			return null;
		}

		HttpResponse response = new HttpResponse();
		
		LoggingWrapper logWrapper = new LoggingWrapper(log);
		ScriptableObject.putProperty(m_scope, "console", logWrapper);		
		
		Object args[] = { request, response };
		try{
			handler.call(m_context, m_scope, m_scope, args);
		} catch (Throwable t){
			CharArrayWriter stack = new CharArrayWriter();
			t.printStackTrace(new PrintWriter(stack));
			log.append(stack.toCharArray());
			throw new RuntimeException(t);
		}
		
		return response;
	}

	/** the namespace for this app, aka, the name */
	private String m_namespace;

	/** our Rhino context */
	private Context m_context;

	/** our Rhino scope */
	private ScriptableObject m_scope;
	
	/** the router for this app */
	private Router m_router;
}
