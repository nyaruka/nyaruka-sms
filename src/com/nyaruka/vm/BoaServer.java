package com.nyaruka.vm;

import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.asfun.jangod.template.TemplateEngine;

import org.json.JSONArray;
import org.json.JSONObject;

import com.nyaruka.db.Collection;
import com.nyaruka.db.Cursor;
import com.nyaruka.db.Record;
import com.nyaruka.http.BoaHttpServer;
import com.nyaruka.json.JSON;
import com.nyaruka.util.FileUtil;
import com.nyaruka.vm.VM.JSEval;

public class BoaServer {

	public BoaServer(String directory) throws IOException {		
		new BoaHttpServer(8080, this);
	}

	public String readFile(String path) {
		return FileUtil.slurpFile(new File(path));
	}
	
	public void start() {

		List<JSEval> evals = new ArrayList<JSEval>();
		evals.add(new JSEval(FileUtil.slurpFile(new File("assets/static/js/json2.js")), "json2.js"));
		evals.add(new JSEval(FileUtil.slurpFile(new File("assets/sys/js/jsInit.js")), "jsInit.js"));

		m_vm.reset();
		loadApps();
		m_vm.reload(evals);		
		m_templates.getConfiguration().setWorkspace("assets/sys");
		m_appTemplates.getConfiguration().setWorkspace("assets/apps");
		
		m_requestInitJS = readFile("assets/sys/js/requestInit.js");
	}
	
	public void stop() {
		m_vm.stop();
	}
	
	private void loadApps() {
		File apps = new File("assets/apps");
		for (File appDir : apps.listFiles()) {
			if (appDir.isDirectory()) {
				File mainFile = new File(appDir, "main.js");
				if (mainFile.exists()) {
					String main = FileUtil.slurpFile(mainFile);
					BoaApp app = new BoaApp(appDir.getName(), main);
					m_vm.addApp(app);
					m_vm.getLog().append("Added app: " + appDir.getName() + "\n");
				} else {
					m_vm.getLog().append("No main.js found for " + appDir.getName() + "\n");
				}
			}
		}		
	}

	public void log(String message) {
		m_vm.getLog().append(message).append("\n");
	}

	public InputStream getInputStream(String uri) throws FileNotFoundException {
		return new FileInputStream("assets/" + uri);
	}

	public String handleRequest(String url, String method, Properties params) {		
		try {
			HttpRequest request = new HttpRequest(url, method, params);
			HttpResponse response = m_vm.handleHttpRequest(request, m_requestInitJS);
	
			if (response != null) {
					
				String templateFile = response.getTemplate();
				if (templateFile == null){
					templateFile = url + ".html";
				}
				
				if (!templateFile.startsWith("/")) {
					templateFile = "/" + response.getApp().getNamespace() + "/" + templateFile;
				}
				
				Map<String,Object> data = response.getData().toMap();				
				return m_appTemplates.process(templateFile, data);
			}
			return null;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}		
	}

	public String renderLog() {
		HashMap<String,Object> data = new HashMap<String,Object>();
		data.put("log", m_vm.getLog().toString());
		return renderTemplate("log.html", data);
	}

	public String renderTemplate(String filename, Map<String,Object> data) {
		try {
			return m_templates.process(filename, data);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public String renderError(Throwable error) {		
		try {
			CharArrayWriter stack = new CharArrayWriter();
			error.printStackTrace(new PrintWriter(stack));
			HashMap<String, Object> data = new HashMap<String,Object>();
			data.put("error", stack.toString());
			return m_templates.process("error.html", data);
		} catch (Throwable t) {
			throw new RuntimeException(t);
		}		
	}
	
	public String renderEditor(File file, String filename) {
		String fileContents = FileUtil.slurpFile(file);
		Map<String,Object> data = new HashMap<String,Object>();
		data.put("contents", fileContents);		
		data.put("filename", filename);
		return renderTemplate("editor.html", data);
	}

	public String renderDB(String url, String method){
		Pattern COLL = Pattern.compile("^/db/(.*)/$");
		Matcher matcher = null;
		
		if (url.equals("/db")){
			HashMap<String, Object> context = new HashMap<String, Object>();
			context.put("collections", m_vm.getDB().getCollectionNames());
			return renderTemplate("db/index.html", context);
		} 
		
		matcher = COLL.matcher(url);
		if (matcher.find()){
			String collName = matcher.group(1);
			Collection coll = m_vm.getDB().ensureCollection(collName);
			
			// our set of keys
			HashSet<String> keys = new HashSet<String>();
			
			// build our list of matches
			ArrayList records = new ArrayList();
			Cursor cursor = coll.find("{}");
			while (cursor.hasNext()){
				Record record = cursor.next();
				JSON data = record.getData();
				
				// add all our unique keys
				Iterator item_keys = data.keys();
				while(item_keys.hasNext()){
					String key = item_keys.next().toString();
					
					// get the value
					Object value = data.get(key);
					
					// skip this key if the value is complex
					if ((value instanceof JSON) || (value instanceof JSONObject) || (value instanceof JSONArray)){
						// pass
					} else {
						keys.add(key);
					}
				}
				
				records.add(record.toJSON().toMap());
			}
			
			HashMap<String, Object> context = new HashMap<String, Object>();
			context.put("keys", keys);
			context.put("collection", coll);
			context.put("records", records);
			return renderTemplate("db/list.html", context);
		}
		
		throw new RuntimeException("Unknown URL: " + url);
	}

	public static void main(String[] args) {
		String directory = args[0];
		
		try {
			BoaServer boa = new BoaServer(directory);
			new Thread(){
				public void run(){
					while(true){
						try{
							Thread.sleep(1000000);
						} catch (Throwable t){}
					}
				}
				
			}.start();
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
	
	private String m_requestInitJS;
	
	private TemplateEngine m_templates = new TemplateEngine();
	private TemplateEngine m_appTemplates = new TemplateEngine();
	
	/** this vm is where all the magic happens */
	private VM m_vm = new VM();

}
