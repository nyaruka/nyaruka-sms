package com.nyaruka.android;

import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.content.res.AssetManager;

import com.nyaruka.http.HttpServer;

/**
 * Android version of our HTTP server. 
 * 
 * Android serves its static files using Android's AssetManager.
 * 
 * @author nicp
 *
 */
public class AndroidHttpServer extends HttpServer {

	public AndroidHttpServer(int port, Context context) throws IOException {
		super(port);
		m_assets = context.getAssets();		
	}
	
	/**
	 * Grab our files from the asset manager instead of the file system.
	 */
	public InputStream getInputStream(String path) throws IOException {
		return m_assets.open(path);
	}
	
	/** Our applications asset manager */
	private AssetManager m_assets;
}
