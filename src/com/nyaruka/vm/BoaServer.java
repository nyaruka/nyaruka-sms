package com.nyaruka.vm;

import java.io.CharArrayWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.asfun.jangod.template.TemplateEngine;

import com.nyaruka.app.AppApp;
import com.nyaruka.app.DBApp;
import com.nyaruka.app.EditorApp;
import com.nyaruka.app.NativeApp;
import com.nyaruka.app.Route;
import com.nyaruka.app.auth.AuthApp;
import com.nyaruka.db.DB;
import com.nyaruka.http.BoaHttpServer;
import com.nyaruka.http.HttpRequest;
import com.nyaruka.http.HttpResponse;
import com.nyaruka.http.NanoHTTPD;
import com.nyaruka.util.FileUtil;

public abstract class BoaServer {

	public BoaServer(VM vm, FileAccessor files) {
		m_vm = vm;
		m_files = files;
	}
	
	public BoaServer(int port, DB db, FileAccessor files) {
		try {
			new BoaHttpServer(port, this);
		} catch (Throwable t) {
			throw new RuntimeException(t);
		}
		
		m_vm = new VM(db);
		m_files = files;
		
		// init our auth app
		addNativeApp(new AuthApp(m_vm));
		addNativeApp(new DBApp(m_vm));
		addNativeApp(new AppApp(m_vm, this));
		addNativeApp(new EditorApp(m_vm, m_files));
	}
	
	public FileAccessor getFiles() {
		return m_files;
	}
	
	/** Direct template engines how to find files on the given platform */
	public abstract void configureTemplateEngines(TemplateEngine systemTemplates, TemplateEngine appTemplates);

	public void start() {
		List<JSEval> evals = new ArrayList<JSEval>();
		evals.add(new JSEval(readFile("static/js/json2.js"), "json2.js"));
		evals.add(new JSEval(readFile("sys/js/jsInit.js"), "jsInit.js"));

		m_vm.reset();
		loadApps();
		m_vm.reload(evals);
		
		configureTemplateEngines(m_templates, m_appTemplates);		
		m_requestInit = new JSEval(readFile("sys/js/requestInit.js"), "requestInit.js");
	}
	
	public void createApp(String namespace) {
		
		FileAccessor files = getFiles();
		
		if (files.exists("apps/" + namespace)) {
			throw new RuntimeException("App with name '" + namespace + "' already exists.");
		}

		// create our app dir and write our rendered main js
		files.copyDirectory("sys/app/new_app", "apps/" + namespace);

		// render our main.js as a template
		HashMap<String,Object> context = new HashMap<String,Object>();
		context.put("name", namespace);
		String mainJS = renderTemplate(files.getPath("sys/app/new_app/main.js"), context);
		
		files.writeFile("apps/" + namespace + "/main.js", mainJS);
		
	}
	
	public void removeApp(String namespace) {
		getFiles().deleteDirectory("apps/"+namespace);
	}
	
	public List<BoaApp> getApps() {
		
		FileAccessor files = getFiles();
		
		List<BoaApp> apps = new ArrayList<BoaApp>();
		
		String[] appDirs = files.getFiles("apps");
		
		for (String appDir : appDirs) {
			
			String main = "apps/" + appDir + "/main.js";
			if (files.exists(main)) {
				String contents = FileUtil.slurpStream(files.getInputStream(main));
				apps.add(new BoaApp(appDir, contents));
			} else {
				log("No main.js found for " + appDir + "\n");
			}			
		}
		
		return apps;
	}
	
	public void stop() {
		m_vm.stop();
	}

	public Session initSession(HttpRequest request){
		return m_vm.getSessions().ensureSession(request);
	}
	
	public void saveSession(Session session){
		m_vm.getSessions().save(session);
	}
	
	public void log(String message) {
		m_vm.getLog().append(message).append("\n");
	}

	public void addNativeApp(NativeApp app){
		m_nativeApps.add(app);
		m_nativeRoutes.addAll(app.getRoutes());
	}

	public HttpResponse handleNativeRequest(HttpRequest request){
		try{
			HttpResponse response = null;
		
			// first run through all our pre-processors
			for (NativeApp app : m_nativeApps){
				HttpResponse shortcut = app.preProcess(request);
				if (shortcut != null){
					response = shortcut;
					break;
				}
			}
		
			// if we don't have a response yet, find the route we belong to and run that
			if (response == null){
				// find a view
				String url = request.url();
				for (Route route : m_nativeRoutes){
					String[] groups = route.matches(url);
					if (groups != null){
						response = route.getView().handleRequest(request, groups);
						break;
					}
				}
			}
		
			// now run our post processor for each of our native apps
			for (NativeApp app : m_nativeApps){
				HttpResponse replacement = app.postProcess(request, response);
				if (replacement != null){
					response = replacement;
				}
			}
		
			// return our final response
			return response;
			
		} catch (Throwable t){
			t.printStackTrace();
			
			for (NativeApp app : m_nativeApps){
				HttpResponse response = app.handleError(t);
				if (response != null){
					return response;
				}
			}
			throw new RuntimeException(t);
		}
	}

	public HttpResponse handleAppRequest(HttpRequest request){
		try {
			BoaResponse response = m_vm.handleHttpRequest(request, m_requestInit);
			String url = request.url();
	
			if (response != null) {
					
				String templateFile = response.getTemplate();
				if (templateFile == null){
					templateFile = url + ".html";
				}
				
				if (!templateFile.startsWith("/")) {
					templateFile = "/" + response.getApp().getNamespace() + "/" + templateFile;
				}
				
				Map<String,Object> data = response.getData().toMap();
				response.setBody(m_appTemplates.process(templateFile, data));
				return response;
			}
	
			return new HttpResponse(NanoHTTPD.HTTP_NOTFOUND, NanoHTTPD.MIME_HTML, "File not found: " + url);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public HttpResponse renderLog(HttpRequest request) {
		HashMap<String,Object> data = new HashMap<String,Object>();
		data.put("log", m_vm.getLog().toString());
		return renderToResponse("log.html", data);
	}
	
	public HttpResponse renderAdmin(HttpRequest request) {
		
		Pattern APP = Pattern.compile("^/admin/app/([a-zA-Z]+)/$");

					
		HashMap<String,Object> context = getAdminContext();

		String url = request.url();
	
		if (url.startsWith("/admin/app")){

			Matcher matcher = APP.matcher(url);
			if (matcher.find()) {
				String appName = matcher.group(1);		
				
				if (request.method().equalsIgnoreCase("POST")){
					removeApp(appName);
					return redirect("/admin/app/");
				}
				
				context.put("app", m_vm.getApp(appName));
				return renderToResponse("app/view.html", context);								
			} else {

				if (request.method().equalsIgnoreCase("POST")){
					String appName = request.params().getProperty("name");
					
					// TODO: sanitize or complain about bad app names
					createApp(appName);
					return redirect("/admin/app/" + appName);
				}
				
				return renderToResponse("app/index.html", context);
			}
			
		}
		
		
		return renderToResponse("index.html", context);
	}
	
	public HttpResponse renderEditor(File file, String filename) {
		String fileContents = FileUtil.slurpFile(file);
		Map<String,Object> data = new HashMap<String,Object>();
		data.put("contents", fileContents);		
		data.put("filename", filename);
		return renderToResponse("editor.html", data);
	}

	public void loadApps() {
		List<BoaApp> apps = getApps();
		log("Found " + apps.size() + " apps to load");
		for (BoaApp app : apps) {
			m_vm.addApp(app);
			log("Added " + app.getNamespace() + " app");
		}
	}
	
	/**
	 * Render an exception nicely in our error template
	 */
	public HttpResponse renderError(Throwable error) {		
		try {
			CharArrayWriter stack = new CharArrayWriter();
			error.printStackTrace(new PrintWriter(stack));
			HashMap<String, Object> context = getAdminContext();
			context.put("error", error.getLocalizedMessage());
			context.put("stack", stack.toString());
			return renderToResponse("error.html", context);
		} catch (Throwable t) {
			t.printStackTrace();
			throw new RuntimeException(t);
		}		
	}
	
	/**
	 * Read the contents of the file at the given path
	 */
	public String readFile(String path) {
		InputStream is = getFiles().getInputStream(path);
		return FileUtil.slurpStream(is);
	}

	/**
	 * The base context for the admin view
	 */
	private HashMap<String,Object> getAdminContext() {
		HashMap<String, Object> context = new HashMap<String, Object>();
		context.put("collections", m_vm.getDB().getCollections());
		context.put("apps", m_vm.getApps());
		return context;
	}
	
	/**
	 * Render a template file with its context
	 */
	public String renderTemplate(String templateFile, Map<String,Object> context) {
		try {
			return m_templates.process(templateFile, context);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Render a template file with it's context to get an http response.
	 * This will return a status 200 with text/html
	 */
	public HttpResponse renderToResponse(String template, Map<String,Object> context) {
		String html = renderTemplate(template, context);
		return new HttpResponse(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_HTML, html);
	}

	/**
	 * Return a redirection response at the give location
	 */
	public HttpResponse redirect(String location) {
		HttpResponse resp = new HttpResponse(NanoHTTPD.HTTP_REDIRECT, NanoHTTPD.MIME_PLAINTEXT, location);
		resp.addHeader("Location", location);
		return resp;
	}

	private JSEval m_requestInit;
	private TemplateEngine m_templates = new TemplateEngine();
	private TemplateEngine m_appTemplates = new TemplateEngine();
	
	/** this vm is where all the magic happens */
	protected VM m_vm;
	
	/** access in and out of our file system */
	protected FileAccessor m_files;
	
	/** Our native apps */
	private ArrayList<NativeApp> m_nativeApps = new ArrayList<NativeApp>();
	
	/** And their routes */
	private ArrayList<Route> m_nativeRoutes = new ArrayList<Route>();

}
