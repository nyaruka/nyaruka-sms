package com.nyaruka.http;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import android.content.res.AssetManager;

public abstract class HttpServer extends NanoHTTPD {
	
	public HttpServer(int port) throws IOException{
		super(port, null);
	}

	public Response serve( String uri, String method, Properties header, Properties parms, Properties files ){
		if (!uri.equals("/")){
			return serveFile(uri, header);
		}
		
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

	public Object executeJS(String script){
		String address = "250788383383";
		String message = "hello world";
		
		HttpService.getThis().setScript(script);
		return HttpService.getThis().executeJS(script, address, message);
	}

	public abstract InputStream getInputStream(String path) throws IOException;
		
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
			return res = new Response(HTTP_NOTFOUND, MIME_PLAINTEXT, "Error 404, file not found.");			
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
				String etag = Integer.toHexString(uri.hashCode());

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
						res.addHeader("ETag", etag);
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
			res = new Response(HTTP_FORBIDDEN, MIME_PLAINTEXT,
					"FORBIDDEN: Reading file failed.");
		}

		res.addHeader("Accept-Ranges", "bytes"); // Announce that the file
													// server accepts partial
													// content requestes
		return res;
	}

	private AssetManager m_assets;
}