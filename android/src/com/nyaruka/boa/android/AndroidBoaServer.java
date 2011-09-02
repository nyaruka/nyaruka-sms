package com.nyaruka.boa.android;

import java.io.IOException;
import java.io.InputStream;

import net.asfun.jangod.template.TemplateEngine;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import com.nyaruka.vm.BoaServer;

/**
 * Android version of our HTTP server. 
 */
public class AndroidBoaServer extends BoaServer {

	public static final String TAG = AndroidBoaServer.class.getSimpleName();
	
	public AndroidBoaServer(int port, Context context) throws IOException {
		super(port, "boa.db");
		m_assets = context.getAssets();
		Log.d(TAG, "Loading AndroidBoaServer..");
		
	}

	@Override
	public void configureTemplateEngines(TemplateEngine systemTemplates, TemplateEngine appTemplates) {
		// TODO: add our own asset locator for loading templates
	}

	@Override
	public InputStream getInputStream(String uri) {
		// TODO: read from our asset manager
		return null;
	}
	
	/** Our applications asset manager */
	private AssetManager m_assets;



	

}
