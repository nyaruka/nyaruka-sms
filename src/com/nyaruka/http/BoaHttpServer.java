package com.nyaruka.http;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.nyaruka.util.FileUtil;
import com.nyaruka.vm.BoaServer;

/**
 * Root HTTP class, where the magic happens.
 * 
 * @author nicp
 */
public class BoaHttpServer extends NanoHTTPD {
	
	public BoaHttpServer(int port, BoaServer boa) throws IOException{
		super(port, null);		
		m_boa = boa;
	}
	
	public synchronized Response serve( String url, String method, Properties header, Properties params, Properties files ){
		
		m_boa.log(method + " " + url);
		
		try{
			m_boa.start();
			
			if (url.indexOf('.') > -1){
				return serveFile(url, header);			
			}
			else if (url.startsWith("/db")){
				return m_boa.renderDB(url, method, params);
			}
			else if (url.equals("/edit")) {
				if (!params.containsKey("filename")) {
					throw new IllegalArgumentException("The editor respectfully requests a file to edit.");
				} else {
					File file = new File("assets/apps/" + (String)params.get("filename"));
					if (method.equals("POST")) {												
						String contents = (String)params.getProperty("editor");
						FileUtil.writeFile(file, contents);
						return new Response(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_PLAINTEXT, "OK");
					} else {
						String editor = m_boa.renderEditor(file, params.getProperty("filename"));
						return new Response(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_HTML, editor);
					}
				}
			}
			else if (url.equals("/log")) {
				return new Response(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_HTML, m_boa.renderLog());
				
			}
			
			if (url.startsWith("/")) {
				url = url.substring(1);
			}
			
			String html = m_boa.handleRequest(url, method, params);			
			if (html != null){
				return new Response(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_HTML, html);				
			} else {
				return new Response(NanoHTTPD.HTTP_NOTFOUND, NanoHTTPD.MIME_HTML, "File not found: " + url);
			}
		} catch (Throwable t){
			return new Response(NanoHTTPD.HTTP_INTERNALERROR, NanoHTTPD.MIME_HTML, m_boa.renderError(t));			
		} finally {
			m_boa.stop();
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
			is = m_boa.getInputStream(uri);
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
	
	private BoaServer m_boa;
}