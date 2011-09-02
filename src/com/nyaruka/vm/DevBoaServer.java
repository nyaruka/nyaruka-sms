package com.nyaruka.vm;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.nyaruka.db.dev.DevDB;
import com.nyaruka.util.FileUtil;

import net.asfun.jangod.template.TemplateEngine;

public class DevBoaServer extends BoaServer {

	public DevBoaServer(int port, File dbFile, String rootDir) {
		super(port, new DevDB(dbFile));
		
		m_rootDir = rootDir;
		if (m_rootDir != null && !m_rootDir.endsWith("/")) {
			m_rootDir += "/";
		}
	}

	@Override
	public InputStream getInputStream(String path) {
		try {
			return new FileInputStream(getPath(path));
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public void configureTemplateEngines(TemplateEngine systemTemplates, TemplateEngine appTemplates) {
		systemTemplates.getConfiguration().setWorkspace(getPath("sys"));
		appTemplates.getConfiguration().setWorkspace(getPath("apps"));		
	}
	
	@Override
	public List<BoaApp> getApps() {
		List<BoaApp> apps = new ArrayList<BoaApp>();		
		File appsDir = new File(getPath("apps"));
		for (File appDir : appsDir.listFiles()) {
			if (appDir.isDirectory()) {
				File mainFile = new File(appDir, "main.js");
				if (mainFile.exists()) {
					String main = FileUtil.slurpFile(mainFile);
					apps.add(new BoaApp(appDir.getName(), main));
				} else {
					log("No main.js found for " + appDir.getName() + "\n");
				}
			}
		}
		
		return apps;
	}
	
	private String getPath(String path) {
		if (m_rootDir != null) {
			path = m_rootDir + path;
		}
		return path;
	}
	
	public static void main(String[] args) {
		int port = Integer.parseInt(args[0]);
		
		String path = null;
		if (args.length > 1) {
			path = args[1];
		}

		File dbFile = null;
		if (args.length > 2) {
			dbFile = new File(args[2]);
		}
		
		try {
			DevBoaServer boa = new DevBoaServer(port, dbFile, path);
			new Thread(){
				public void run(){
					while(true){
						try{
							Thread.sleep(1000000);
						} catch (Throwable t){}
					}
				}
				
			}.start();
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
	
	
	private String m_rootDir;

}
