package com.nyaruka.dev;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.nyaruka.http.HttpServer;

/**
 * Dev subclass of HttpServer, just services the files locally and provides
 * a main() for local development.
 * 
 * Usage:
 *     java com.nyaruka.dev.DevHttpServer <path to assets dir> [port (default 8000)]
 * 
 * @author nicp
 */
public class DevHttpServer extends HttpServer {

	public DevHttpServer(int port, File assetsDir) throws IOException {
		super(port);
		m_assetsDir = assetsDir;
	}

	/**
	 * For the dev server we just serve our files straight from the file system.
	 */
	public InputStream getInputStream(String path) throws IOException {
		return new FileInputStream(new File(m_assetsDir, path));
	}
	
	public static void main(String argv[]){
		String directory = argv[0];
		
		System.out.println("Starting server for " + directory);
		int port = 8000;
		if (argv.length == 2){
			port = Integer.parseInt(argv[1]);
		}
		
		try{
			new DevHttpServer(port, new File(directory));
			System.out.println("HTTP server started on port: " + port);
			System.out.println("CTRL-C to Stop");
			new Thread(){
				public void run(){
					while(true){
						try{
							Thread.sleep(1000000);
						} catch (Throwable t){}
					}
				}
				
			}.start();
		} catch (Throwable t){
			t.printStackTrace();
		}

	}
	
	/** The filesystem root for our assets directory */
	private File m_assetsDir;
}
