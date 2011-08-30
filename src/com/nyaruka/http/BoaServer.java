package com.nyaruka.http;

import java.io.File;

import java.io.CharArrayWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.util.HashMap;
import java.util.Properties;
import java.util.Scanner;
import java.util.regex.Pattern;

import org.json.JSONObject;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ScriptableObject;

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
	}
	
	public synchronized Response serve( String uri, String method, Properties header, Properties params, Properties files ){
		
		m_vm.getLog().append(method + " " + uri + "\n");
		
		if (uri.startsWith("/templates")) {			
			uri = "/apps/" + uri.substring("/templates".length());
			return serveFile(uri, header);			
		}
		else if (uri.indexOf('.') > -1){
			return serveFile(uri, header);			
		} 
		else if (uri.equals("/log")) {
			
			String log = FileUtil.slurpFile(new File(getSysDir(), "log.html"));
			log = log.replace("##LOG", m_vm.getLog().toString());			
			return new Response(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_HTML, log);
		}
		
		if (uri.startsWith("/")) {
			uri = uri.substring(1);
		}
		
		HttpRequest request = new HttpRequest(uri, method, params);
		
		try{
			start();

			HttpResponse response = m_vm.handleHttpRequest(request);

			if (response != null) {
				String render = FileUtil.slurpFile(new File(getSysDir(), "render.html"));

				String templateFile = response.getTemplate();
				if (!templateFile.startsWith("/")) {
					templateFile = "/" + response.getApp().getNamespace() + "/" + templateFile;
				}
				
				templateFile = "/templates" + templateFile;
				
				render = render.replace("##TEMPLATE", templateFile);
				render = render.replace("##DATA", response.getData().toString());

				// wrap it in nanohttp response
				return new Response(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_HTML, render);
			} else {
				return new Response(NanoHTTPD.HTTP_NOTFOUND, NanoHTTPD.MIME_HTML, "File not found: " + uri);
			}
		} catch (Throwable t){
			t.printStackTrace();
			
			String log = FileUtil.slurpFile(new File(getSysDir(), "error.html"));
			log = log.replace("##LOG", m_vm.getLog().toString());	
			CharArrayWriter stack = new CharArrayWriter();
			t.printStackTrace(new PrintWriter(stack));
			log = log.replace("##ERROR", stack.toString());
			return new Response(NanoHTTPD.HTTP_INTERNALERROR, NanoHTTPD.MIME_HTML, log);
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
	
	/** this vm is where all the magic happens */
	private VM m_vm = new VM();
	
	public abstract InputStream getInputStream(String path) throws IOException;
	
	public abstract File getAppsDir();
	public abstract File getStaticDir();
	public abstract File getSysDir();
}