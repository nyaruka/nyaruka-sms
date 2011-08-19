package com.nyaruka.dev;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.nyaruka.http.HttpServer;

public class DevHttpServer extends HttpServer {

	public DevHttpServer(int port, File root) throws IOException {
		super(port);
		m_root = root;
	}

	public InputStream getInputStream(String path) throws IOException {
		return new FileInputStream(new File(m_root, path));
	}
	
	public static void main(String argv[]){
		String directory = argv[0];
		
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
	
	private File m_root;
}
