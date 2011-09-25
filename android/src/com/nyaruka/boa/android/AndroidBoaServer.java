package com.nyaruka.boa.android;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import net.asfun.jangod.base.Configuration;
import net.asfun.jangod.template.TemplateEngine;
import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import com.nyaruka.boa.android.db.AndroidDB;
import com.nyaruka.util.FileUtil;
import com.nyaruka.vm.BoaApp;
import com.nyaruka.vm.BoaServer;
import com.nyaruka.vm.FileAccessor;

/**
 * Android version of our HTTP server. 
 */
public class AndroidBoaServer extends BoaServer {

	
	public static final String TAG = AndroidBoaServer.class.getSimpleName();

	private AndroidFileAccessor m_files;
	
	public AndroidBoaServer(int port, Context context) throws IOException {
		super(port, new AndroidDB(new File("/data/data/com.nyaruka.boa.android", "boa.db")));	
		Configuration.getDefault().setBootstrap("file.locator", new AssetLocator(context.getAssets()));
		Log.d(TAG, "Loading AndroidBoaServer..");
		
		m_files = new AndroidFileAccessor(context.getAssets());
		
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

	public FileAccessor getFiles() {
		return m_files;
	}

}
