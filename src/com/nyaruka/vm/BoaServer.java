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
import com.nyaruka.app.AuthApp;
import com.nyaruka.app.DBApp;
import com.nyaruka.app.NativeApp;
import com.nyaruka.app.Route;
import com.nyaruka.app.TemplateResponse;
import com.nyaruka.db.DB;
import com.nyaruka.http.BoaHttpServer;
import com.nyaruka.http.HttpRequest;
import com.nyaruka.http.HttpResponse;
import com.nyaruka.http.NanoHTTPD;
import com.nyaruka.util.FileUtil;

public abstract class BoaServer {

	public BoaServer(int port, DB db) {
		try {
			new BoaHttpServer(port, this);
		} catch (Throwable t) {
			throw new RuntimeException(t);
		}
		
		m_vm = new VM(db);
		
		// init our auth app
		addNativeApp(new AuthApp(this));
		addNativeApp(new DBApp(m_vm));
		addNativeApp(new AppApp(this, m_vm));
	}
	
	/** Define out to get the contents of a given path */
	public abstract InputStream getInputStream(String path);

	/** Direct template engines how to find files on the given platform */
	public abstract void configureTemplateEngines(TemplateEngine systemTemplates, TemplateEngine appTemplates);

	/** Read the apps from whatever storage mechanism is appropriate for the server */
	public abstract List<BoaApp> getApps();
	
	/** Create an app with the given namespace */
	public abstract void createApp(String name);
	
	/** Remove the app with the give namespace */
	public abstract void removeApp(String name);
	
	public abstract String[] getFiles(BoaApp app);
	
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
		HttpResponse response = null;
		
		// find a view
		for (Route route : m_nativeRoutes){
			if (route.matches(request.url())){
				response = route.getView().handle(request);
				break;
			}
		}
		
		if (response instanceof TemplateResponse){
			TemplateResponse tp = (TemplateResponse) response;
			tp.setBody(renderTemplate(tp.getTemplate(), tp.getContext()));
		}
		
		
		// otherwise, nothing found
		return response;
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
		InputStream is = getInputStream(path);
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
	private VM m_vm;
	
	/** Our native apps */
	private ArrayList<NativeApp> m_nativeApps = new ArrayList<NativeApp>();
	
	/** And their routes */
	private ArrayList<Route> m_nativeRoutes = new ArrayList<Route>();
}
