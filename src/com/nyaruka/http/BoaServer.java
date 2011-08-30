package com.nyaruka.http;

import java.io.CharArrayWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import net.asfun.jangod.template.TemplateEngine;

import com.nyaruka.util.FileUtil;
import com.nyaruka.vm.BoaApp;
import com.nyaruka.vm.HttpRequest;
import com.nyaruka.vm.HttpResponse;
import com.nyaruka.vm.VM;

/**
 * Root HTTP class, where the magic happens.
 * 
 * @author nicp
 */
public abstract class BoaServer extends NanoHTTPD {
	
	public BoaServer(int port) throws IOException{
		super(port, null);		
	}
	
	public void start() {
		m_vm.reset();
		loadApps();
		m_vm.reload();		
		m_templates.getConfiguration().setWorkspace("assets/sys");
		m_appTemplates.getConfiguration().setWorkspace("assets/apps");
	}
	
	public synchronized Response serve( String url, String method, Properties header, Properties params, Properties files ){
		
		m_vm.getLog().append(method + " " + url + "\n");
		
		try{
			start();
			
			if (url.indexOf('.') > -1){
				return serveFile(url, header);			
			}
			else if (url.equals("/edit")) {
				if (!params.containsKey("filename")) {
					throw new IllegalArgumentException("The editor respectfully requests a file to edit.");
				} else {
					File file = new File("assets/apps/" + (String)params.get("filename"));
					if (method.equals("POST")) {												
						String contents = (String)params.getProperty("editor");
						FileUtil.writeFile(file, contents);
						return renderTemplate("success.html", new HashMap<String,Object>());
					} else {
						return renderEditor(file, params.getProperty("filename"));
					}
				}
			}
			else if (url.equals("/log")) {		
				HashMap<String,Object> data = new HashMap<String,Object>();
				data.put("log", m_vm.getLog().toString());
				return renderTemplate("log.html", data);			
			}
			
			if (url.startsWith("/")) {
				url = url.substring(1);
			}
			
			HttpRequest request = new HttpRequest(url, method, params);
			
			HttpResponse response = m_vm.handleHttpRequest(request);

			if (response != null) {
				
				String templateFile = response.getTemplate();
				if (templateFile == null){
					templateFile = url + ".html";
				}
				
				if (!templateFile.startsWith("/")) {
					templateFile = "/" + response.getApp().getNamespace() + "/" + templateFile;
				}
				
				Map<String,Object> data = response.getData().toMap();				
				String html = m_appTemplates.process(templateFile, data);
				return new Response(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_HTML, html);
			} else {
				return new Response(NanoHTTPD.HTTP_NOTFOUND, NanoHTTPD.MIME_HTML, "File not found: " + url);
			}
		} catch (Throwable t){
			return renderError(t);
		} finally {
			m_vm.stop();
		}
	}
	
	public static class Logger {
		public Logger(StringBuffer log){
			m_log = log;
		}
		
		public void log(String msg){
			m_log.append(msg + "\n");
		}
		
		StringBuffer m_log;
	}
	
	/**
	 * Serves file from homeDir and its' subdirectories (only).
	 * Uses only URI, ignores all headers and HTTP parameters.
	 */
	public Response serveFile(String uri, Properties header){
		Response res = null;

		if (res == null) {
			// Remove URL arguments
			uri = uri.trim().replace(File.separatorChar, '/');
			if (uri.indexOf('?') >= 0) {
				uri = uri.substring(0, uri.indexOf('?'));
			}

			// Prohibit getting out of current directory
			if (uri.startsWith("..") || uri.endsWith("..") || uri.indexOf("../") >= 0) {
				res = new Response(HTTP_FORBIDDEN, MIME_PLAINTEXT, "FORBIDDEN: Won't serve ../ for security reasons.");
			}
		}

		if (uri.charAt(0) == '/'){
			uri = uri.substring(1);
		}
		
		InputStream is = null;
		try{
			is = getInputStream(uri);
		} catch (Throwable t){
			t.printStackTrace();
			return new Response(HTTP_NOTFOUND, MIME_PLAINTEXT, "Error 404, file not found.");
		}
		
		try {
			if (res == null) {
				// Get MIME type from file name extension, if possible
				String mime = null;
				int dot = uri.lastIndexOf('.');
				if (dot >= 0){
					mime = (String) theMimeTypes.get(uri.substring(dot + 1).toLowerCase());
				}
				if (mime == null){
					mime = MIME_DEFAULT_BINARY;
				}

				// Calculate etag
				String etag = Integer.toHexString(uri.hashCode() + is.available());

				// Support (simple) skipping:
				long startFrom = 0;
				long endAt = -1;
				String range = header.getProperty("range");
				if (range != null) {
					if (range.startsWith("bytes=")) {
						range = range.substring("bytes=".length());
						int minus = range.indexOf('-');
						try {
							if (minus > 0) {
								startFrom = Long.parseLong(range.substring(0, minus));
								endAt = Long.parseLong(range.substring(minus + 1));
							}
						} catch (NumberFormatException nfe) {
						}
					}
				}

				// Change return code and add Content-Range header when skipping
				// is requested
				long fileLen = is.available();
				if (range != null && startFrom >= 0) {
					if (startFrom >= fileLen) {
						res = new Response(HTTP_RANGE_NOT_SATISFIABLE, MIME_PLAINTEXT, "");
						res.addHeader("Content-Range", "bytes 0-0/" + fileLen);
						//res.addHeader("ETag", etag);
					} else {
						if (endAt < 0){
							endAt = fileLen - 1;
						}
						long newLen = endAt - startFrom + 1;
						if (newLen < 0){
							newLen = 0;
						}

						final long dataLen = newLen;
						is.skip(startFrom);

						res = new Response(HTTP_PARTIALCONTENT, mime, is);
						res.addHeader("Content-Length", "" + dataLen);
						res.addHeader("Content-Range", "bytes " + startFrom	+ "-" + endAt + "/" + fileLen);
						//res.addHeader("ETag", etag);
					}
				} else {
					res = new Response(HTTP_OK, mime, is);
					res.addHeader("Content-Length", "" + is.available());
					//res.addHeader("ETag", etag);
				}
			}
		} catch (IOException ioe) {
			res = new Response(HTTP_FORBIDDEN, MIME_PLAINTEXT, "FORBIDDEN: Reading file failed.");
		}

		res.addHeader("Accept-Ranges", "bytes"); // Announce that the file server accepts partial content requests
		return res;
	}
	
	private Response renderEditor(File file, String filename) {
		String fileContents = FileUtil.slurpFile(file);
		Map<String,Object> data = new HashMap<String,Object>();
		data.put("contents", fileContents);		
		data.put("filename", filename);
		return renderTemplate("editor.html", data);
	}
	
	private Response renderTemplate(String template, Map<String,Object> variables) {
		try {
			String html = m_templates.process(template, variables);
			return new Response(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_HTML, html);
		} catch (Throwable t) {
			return renderError(t);
		}
	}
	
	private Response renderError(Throwable error) {		
		try {
			CharArrayWriter stack = new CharArrayWriter();
			error.printStackTrace(new PrintWriter(stack));
			HashMap<String, Object> data = new HashMap<String,Object>();
			data.put("error", stack.toString());
			String html = m_templates.process("error.html", data);
			return new Response(NanoHTTPD.HTTP_INTERNALERROR, NanoHTTPD.MIME_HTML, html);
		} catch (Throwable t) {
			t.printStackTrace();			
			CharArrayWriter stack = new CharArrayWriter();
			t.printStackTrace(new PrintWriter(stack));
			return new Response(NanoHTTPD.HTTP_INTERNALERROR, NanoHTTPD.MIME_HTML, stack.toString());
		}		
	}
	
	private void loadApps() {
		File apps = getAppsDir();		
		for (File appDir : apps.listFiles()) {
			if (appDir.isDirectory()) {
				File mainFile = new File(appDir, "main.js");
				if (mainFile.exists()) {
					String main = FileUtil.slurpFile(mainFile);
					BoaApp app = new BoaApp(appDir.getName(), main);
					m_vm.addApp(app);					
				} else {
					m_vm.getLog().append("No main.js found for " + appDir.getName() + "\n");
				}
			}
		}		
	}
	
	private TemplateEngine m_templates = new TemplateEngine();
	private TemplateEngine m_appTemplates = new TemplateEngine();
	
	/** this vm is where all the magic happens */
	private VM m_vm = new VM();
	
	public abstract InputStream getInputStream(String path) throws IOException;
	
	public abstract File getAppsDir();
	public abstract File getStaticDir();
	public abstract File getSysDir();
}