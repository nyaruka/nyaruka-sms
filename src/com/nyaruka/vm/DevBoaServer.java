package com.nyaruka.vm;

import java.io.File;

import net.asfun.jangod.template.TemplateEngine;

import com.nyaruka.db.dev.DevDB;

public class DevBoaServer extends BoaServer {
	
	public DevBoaServer(int port, File dbFile, String rootDir) {
		super(port, new DevDB(dbFile), new DevFileAccessor(rootDir));
	}
	
	@Override
	public void configureTemplateEngines(TemplateEngine systemTemplates, TemplateEngine appTemplates) {
		systemTemplates.getConfiguration().setWorkspace(getFiles().getPath("sys"));
		appTemplates.getConfiguration().setWorkspace(getFiles().getPath("apps"));		
	}

	public static void main(String[] args) {
		
		if (args.length == 0) {
			System.out.println("Usage: java com.nyaruka.vm.DevBoaServer 8080 [assets]");
			return;
		}
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
			new DevBoaServer(port, dbFile, path);
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

}
