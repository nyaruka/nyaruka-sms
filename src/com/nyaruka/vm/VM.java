package com.nyaruka.vm;

import java.io.CharArrayWriter;
import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ScriptableObject;

import com.nyaruka.db.DB;
import com.nyaruka.http.HttpRequest;
import com.nyaruka.vm.Router.HttpRoute;

/**
 * Responsible for managing the Rhino Context and VM, as well as loading and reloading
 * the BoaApps.
 * 
 * @author nicp
 */
public class VM {

	public VM(DB db) {
		s_this = this;
		m_db = db;
	}
	
	public void addApp(BoaApp app){
		m_apps.add(app);
	}
	
	/**
	 * Starts our server, creating a new context and scope and loading all of
	 * our apps in order.
	 */
	public void start(List<JSEval> evals) {
		m_db.open();
		m_db.init();
		m_sessions = new SessionManager(m_db);
		
		// Create our context and turn off compilation
		m_context = Context.enter();
		
		m_context.setOptimizationLevel(-1);

		m_scope = m_context.initStandardObjects();
		
		// evaluate our JS init
		for (JSEval eval : evals) {
			eval.exec(m_context, m_scope);
		}
		
		try {
			ScriptableObject.defineClass(m_scope, DBWrapper.class);
		} catch (Throwable t){
			throw new RuntimeException(t);
		}
		
		// stick our log in the scope
		LoggingWrapper logWrapper = new LoggingWrapper(m_log);
				
		for (BoaApp app: m_apps){
			ScriptableObject.putProperty(m_scope, "router", m_router);
			ScriptableObject.putProperty(m_scope, "_db", m_db);
			ScriptableObject.putProperty(m_scope, "console", logWrapper);			
			
			try{
				m_router.setCurrentApp(app);
				app.load(m_context, m_scope);
				app.setState(BoaApp.RUNNING);
			} catch (Throwable t){
				CharArrayWriter stack = new CharArrayWriter();
				t.printStackTrace(new PrintWriter(stack));
				m_log.append(stack.toCharArray());
				app.setState(BoaApp.ERROR);
			}
		}
	}
	
	/**
	 * Try to handle the passed in request, setting any values
	 * used in the response object.
	 * 
	 * Returns whether the request was actually handled or not.
	 * 
	 * @param request The request to handle
	 * @param log A buffered string to log any messages to
	 * @return HttpResponse if this app handles this request, null otherwise
	 * @throws RuntimeException if an error occurs running the handler
	 */
	public BoaResponse handleHttpRequest(HttpRequest request, JSEval requestInit){
		HttpRoute route = m_router.lookupHttpHandler(request.url());
		
		// no route found?  then this app doesn't deal with this, return null
		if (route == null){
			return null;
		}

		BoaResponse response = new BoaResponse(route.getApp());
		LoggingWrapper logWrapper = new LoggingWrapper(m_log);
		
		ScriptableObject.putProperty(m_scope, "router", m_router);
		ScriptableObject.putProperty(m_scope, "_db", m_db);
		ScriptableObject.putProperty(m_scope, "console", logWrapper);
		ScriptableObject.putProperty(m_scope, "_request", request);
		ScriptableObject.putProperty(m_scope, "_response", response);		
		
		requestInit.exec(m_context, m_scope);
		
		Object args[] = { m_scope.get("__request", m_scope), m_scope.get("__response", m_scope) };
		try{
			route.getHandler().call(m_context, m_scope, m_scope, args);
		} catch (Throwable t){
			CharArrayWriter stack = new CharArrayWriter();
			t.printStackTrace(new PrintWriter(stack));
			m_log.append(stack.toCharArray());
			throw new RuntimeException(t);
		}
		
		return response;
	}
	
	public void reset(){
		m_apps.clear();
	}

	public void stop(){
		Context.exit();
		
		m_db.close();
	}
	
	public void reload(List<JSEval> evals){
		m_router.reset();
		start(evals);
	}

	public List<BoaApp> getApps() {
		return m_apps;
	}
	
	public BoaApp getApp(String name) {
		for (BoaApp app : m_apps) {
			if (app.getNamespace().equals(name)) {
				return app;
			}
		}
		return null;
	}
	
	public Router getRouter(){ return m_router;	}
	public StringBuffer getLog(){ return m_log; }
	public DB getDB(){ return m_db; }
	public static VM getVM(){ return s_this; }
	public SessionManager getSessions(){ return m_sessions; }
	
	/** Our system-wide log */
	private StringBuffer m_log = new StringBuffer();
	
	/** Our list of installed apps, order matters */
	private ArrayList<BoaApp> m_apps = new ArrayList<BoaApp>();
	
	/* Current scope and state of our VM */
	private Context m_context;
	private ScriptableObject m_scope;
	
	/** where our DB will write */
	private File m_dbFile;
	
	/** Our database */
	private DB m_db;
		
	/** our global router in charge of keeping the map of URL patterns to handlers */
	private Router m_router = new Router();
	
	/** Singleton instance */
	private static VM s_this;
	
	/** our session manager */
	private SessionManager m_sessions;
}
