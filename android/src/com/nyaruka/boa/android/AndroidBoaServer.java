package com.nyaruka.boa.android;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import net.asfun.jangod.template.TemplateEngine;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import com.nyaruka.db.dev.DevDB;
import com.nyaruka.vm.BoaApp;
import com.nyaruka.vm.BoaServer;

/**
 * Android version of our HTTP server. 
 */
public class AndroidBoaServer extends BoaServer {

	public static final String TAG = AndroidBoaServer.class.getSimpleName();
	
	public AndroidBoaServer(int port, Context context) throws IOException {
		super(port, new DevDB(new File("boa.db")));
		m_assets = context.getAssets();
		Log.d(TAG, "Loading AndroidBoaServer..");
		
	}

	@Override
	public List<BoaApp> getApps() {
		// TODO Auto-generated method stub
		return new ArrayList<BoaApp>();
	}
	
	@Override
	public void configureTemplateEngines(TemplateEngine systemTemplates, TemplateEngine appTemplates) {
		// TODO: add our own asset locator for loading templates
	}

	@Override
	public InputStream getInputStream(String path) {
		try {
			return m_assets.open(path);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	/** Our applications asset manager */
	private AssetManager m_assets;

}
