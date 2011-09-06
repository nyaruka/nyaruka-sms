package com.nyaruka.boa.android;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import net.asfun.jangod.base.Application;
import net.asfun.jangod.base.ConfigInitializer;
import net.asfun.jangod.base.Configuration;
import net.asfun.jangod.template.TemplateEngine;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import com.nyaruka.boa.android.db.AndroidDB;
import com.nyaruka.util.FileUtil;
import com.nyaruka.vm.BoaApp;
import com.nyaruka.vm.BoaServer;

/**
 * Android version of our HTTP server. 
 */
public class AndroidBoaServer extends BoaServer {

	public static final String TAG = AndroidBoaServer.class.getSimpleName();
	
	public AndroidBoaServer(int port, Context context) throws IOException {
		super(port, new AndroidDB(new File("/data/data/com.nyaruka.boa.android", "boa.db")));	
		Configuration.getDefault().setBootstrap("file.locator", new AssetLocator(context.getAssets()));
		m_assets = context.getAssets();
		Log.d(TAG, "Loading AndroidBoaServer..");
		
	}

	@Override
	public List<BoaApp> getApps() {

		List<BoaApp> apps = new ArrayList<BoaApp>();

		try {
			for (String appName : m_assets.list("apps")) {
				try {
					String main = FileUtil.slurpStream(m_assets.open("apps/" + appName + "/main.js"));
					apps.add(new BoaApp(appName, main));
				} catch (Exception e) {
					log("Couldn't load main for app " + appName);
				}
				
			}
		} catch (Throwable t) {
			log("Failed loading apps");
			t.printStackTrace();
		}
		
		return apps;
	}
	
	@Override
	public void configureTemplateEngines(TemplateEngine systemTemplates, TemplateEngine appTemplates) {
			
		systemTemplates.getConfiguration().setWorkspace("sys");
		appTemplates.getConfiguration().setWorkspace("apps");	
		
		// Application app = new Application();
		// app.getConfiguration().properties.put("file.locator", "com.nyaruka.boa.android.AssetFileLocator");
		
		// systemTemplates.getConfiguration().properties.put("file.locator", "com.nyaruka.boa.android.AssetFileLocator");
		// appTemplates.getConfiguration().properties.put("file.locator", "com.nyaruka.boa.android.AssetFileLocator");
		
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
