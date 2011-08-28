package com.nyaruka.vm;

import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.ScriptableObject;

import com.nyaruka.db.DB;

/**
 * Responsible for managing the Rhino Context and VM, as well as loading and reloading
 * the BoaApps.
 * 
 * @author nicp
 */
public class VM {

	public VM(){
		m_db = new DB();
		m_db.open();
		m_db.init();
		
		s_this = this;
	}
	
	public void addApp(BoaApp app){
		m_apps.add(app);
	}
	
	/**
	 * Starts our server, creating a new context and scope and loading all of
	 * our apps in order.
	 */
	public void start() {
		// Create our context and turn off compilation
		m_context = Context.enter();
		m_context.setOptimizationLevel(-1);

		m_scope = m_context.initStandardObjects();
		
		initContext(m_context, m_scope);
		
		try {
			ScriptableObject.defineClass(m_scope, DBWrapper.class);
		} catch (Throwable t){
			throw new RuntimeException(t);
		}
		
		// create and initialize our router
		m_router = new Router();
		
		// stick our log in the scope
		LoggingWrapper logWrapper = new LoggingWrapper(m_log);
				
		for (BoaApp app: m_apps){
			ScriptableObject.putProperty(m_scope, "router", m_router);
			ScriptableObject.putProperty(m_scope, "_db", m_db);
			ScriptableObject.putProperty(m_scope, "console", logWrapper);			
			
			try{
				app.load(m_context, m_scope);
				app.setState(BoaApp.ERROR);
			} catch (Throwable t){
				CharArrayWriter stack = new CharArrayWriter();
				t.printStackTrace(new PrintWriter(stack));
				m_log.append(stack.toCharArray());
				app.setState(BoaApp.ERROR);
			}
		}
	}
	
	public void initContext(Context context, ScriptableObject scope){
		execFile(new File("assets/js/json2.js"), context, scope);
		execFile(new File("assets/js/jsInit.js"), context, scope);
	}
	
	public void execFile(File file, Context context, ScriptableObject scope){
		try{
			InputStream is = new FileInputStream(file);
			String js = new Scanner(is).useDelimiter("\\A").next();
			context.evaluateString(scope, js, file.getName(), 1, null);			
		} catch (Throwable t){
			t.printStackTrace();
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
	public HttpResponse handleHttpRequest(HttpRequest request){
		Function handler = m_router.lookupHttpHandler(request.url());
		
		// no handler found?  then this app doesn't deal with this, return null
		if (handler == null){
			return null;
		}

		HttpResponse response = new HttpResponse();
		LoggingWrapper logWrapper = new LoggingWrapper(m_log);
		
		ScriptableObject.putProperty(m_scope, "router", m_router);
		ScriptableObject.putProperty(m_scope, "_db", m_db);
		ScriptableObject.putProperty(m_scope, "console", logWrapper);
		ScriptableObject.putProperty(m_scope, "_request", request);
		
		execFile(new File("assets/js/requestInit.js"), m_context, m_scope);
		
		Object args[] = { m_scope.get("__request", m_scope), response };
		try{
			handler.call(m_context, m_scope, m_scope, args);
		} catch (Throwable t){
			CharArrayWriter stack = new CharArrayWriter();
			t.printStackTrace(new PrintWriter(stack));
			m_log.append(stack.toCharArray());
			throw new RuntimeException(t);
		}
		
		return response;
	}
	
	public void reload(){
		Context.exit();
		m_router.reset();
		m_log.setLength(0);
		start();
	}

	public Router getRouter(){ return m_router;	}
	public StringBuffer getLog(){ return m_log; }
	public DB getDB(){ return m_db; }
	public static VM getVM(){ return s_this; }

	/** Our system-wide log */
	private StringBuffer m_log = new StringBuffer();
	
	/** Our list of installed apps, order matters */
	private ArrayList<BoaApp> m_apps = new ArrayList<BoaApp>();
	
	/* Current scope and state of our VM */
	private Context m_context;
	private ScriptableObject m_scope;
	
	/** Our database */
	private DB m_db;
		
	/** our global router in charge of keeping the map of URL patterns to handlers */
	private Router m_router;
	
	/** Singleton instance */
	private static VM s_this;
}
