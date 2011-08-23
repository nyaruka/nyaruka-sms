package com.nyaruka.http;

import java.io.File;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Properties;
import java.util.Scanner;

import org.json.JSONObject;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

/**
 * Root HTTP class, where the magic happens.
 * 
 * @author nicp
 */
public abstract class HttpServer extends NanoHTTPD {
	
	public HttpServer(int port) throws IOException{
		super(port, null);
	}

	public Response serve( String uri, String method, Properties header, Properties parms, Properties files ){
		if (uri.equals("/")){
			return serveEditor(uri, method, header, parms, files);
		}
		else if (uri.indexOf('.') > -1){
			return serveFile(uri, header);			
		}
		else {
			return servePage(uri, method, header, parms, files);
		}
	}

	/**
	 * Responsible for rendering a page in our framework style.
	 * 
	 * Process is essentially:
	 *    - take path, append '.js' .. if that file is found, then that is the 'view' which is executed to create
	 *      a JSON blob of data.  If not found, an empty blob of JSON data is used instead.
	 *    - take path, append '.ejs' .. this is rendered as EJS using the data supplied by the JSON.  If this file 
	 *      isn't found, then return a 404
	 */
	public Response servePage(String uri, String method, Properties header, Properties parms, Properties files){
		StringBuffer log = new StringBuffer();
		
		// first make sure our template exists, otherwise it's a 404
		try{
			InputStream is = getInputStream(uri + ".ejs");
			is.close();
		} catch (Throwable t){
			return new Response(HTTP_NOTFOUND, MIME_PLAINTEXT, "Error 404, file not found.");						
		}
		
		// evaluate our view (if not found, this will return an empty dict)
		JSONObject response = evaluateView(log, uri, method, header, parms, files);

		String html = "<html>";
		html += "<head>";
		html += "<link rel='stylesheet' type='text/css' href='/css/style.css' />";
		html += "<script type='text/javascript' src='/js/ejs.js'></script></head>";
		html += "<body><script type='text/javascript'>";
		html += "var data = " + response.toString() + ";";
		html += "var html = new EJS({url:'" + uri + ".ejs'}).render(data);";
		html += "document.writeln(html);";
		html += "</script>";
		html += "<div id='log'>";
		html += log.toString();
		html += "</html>";
		html += "</body>";
		
		return new NanoHTTPD.Response( HTTP_OK, MIME_HTML, html);			
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
	
	public JSONObject evaluateView(final StringBuffer log, String uri, String method, Properties headers, Properties params, Properties files){
		// first see if we can find the view
		String script = null;
		try{
			InputStream is = getInputStream(uri + ".js");
			script = new Scanner(is).useDelimiter("\\A").next();
		} catch (Throwable t){
			// not found, that's ok
			log.append("No view found for '" + uri + "'\n");
		}
		
		// our view renders into a map
		JSONObject response = new JSONObject(); 
		
		if (script != null){
			// Create our context and turn off compilation
			Context cx = Context.enter();
			cx.setOptimizationLevel(-1);

			// build our request object
			HashMap request = new HashMap();
			request.put("method", method);
			request.put("uri", uri);
			request.put("headers", headers);
			request.put("params", params);
			request.put("files", files);

			// Initialize the scope
			ScriptableObject scope = cx.initStandardObjects();
			ScriptableObject.putProperty(scope, "response", response);
			ScriptableObject.putProperty(scope, "request", request);
			ScriptableObject.putProperty(scope, "console", new Logger(log));			

			Object result;
			try{
				result = cx.evaluateString(scope, script, "", 1, null);
			} catch (Throwable t){
				CharArrayWriter stack = new CharArrayWriter();
				t.printStackTrace(new PrintWriter(stack));
				log.append(stack.toCharArray());
			}
			Context.exit();
		}

		return response;
	}
		
	public Response serveEditor(String uri, String method, Properties header, Properties parms, Properties files){
		System.out.println( method + " '" + uri + "' " );
		String msg = "<html><body><h1>Code</h1>\n";
		
		String code = parms.getProperty("code"); 
		
		msg += "<form action='?' method='get'>\n" +
				"<textarea cols=80 rows=20 name='code'>";
		
		if (code != null){
			msg += code;
		}

		msg +=  "</textarea><p><input type='submit' value='run'></form>\n";
		
		if (code != null){
			msg += "<pre>" + executeJS(code) + "</pre>";
		}

		msg += "</body></html>\n";
		return new NanoHTTPD.Response( HTTP_OK, MIME_HTML, msg );
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
	
	public Object executeJS(String script){
		/*String address = "250788383383";
		String message = "hello world";
		
		HttpService.getThis().setScript(script);
		return HttpService.getThis().executeJS(script, address, message);*/
		return "";
	}

	public abstract InputStream getInputStream(String path) throws IOException;
}