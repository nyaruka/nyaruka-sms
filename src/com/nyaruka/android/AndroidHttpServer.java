package com.nyaruka.android;

import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.content.res.AssetManager;

import com.nyaruka.http.HttpServer;

public class AndroidHttpServer extends HttpServer {

	public AndroidHttpServer(int port, Context context) throws IOException {
		super(port);
		m_assets = context.getAssets();		
	}
	
	@Override
	public InputStream getInputStream(String path) throws IOException {
		return m_assets.open(path);
	}
	
	private AssetManager m_assets;
}
