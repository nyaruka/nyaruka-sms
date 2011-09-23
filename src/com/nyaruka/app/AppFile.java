package com.nyaruka.app;

import java.io.InputStream;

import com.nyaruka.util.FileUtil;
import com.nyaruka.vm.BoaApp;
import com.nyaruka.vm.BoaServer;

public class AppFile implements Comparable {

	private boolean m_active;
	private int m_order;
	
	private String m_path;
	private BoaServer m_server;
	private BoaApp m_app;

	public AppFile(BoaServer server, BoaApp app, String path) {
		m_path = path;
		m_server = server;
		m_app = app;
	}
	
	public boolean isTemplate() {
		return m_path.endsWith(".html");
	}
	
	public boolean isCode() {
		return m_path.endsWith(".js");
	}
	
	public String getId() {
		return m_path.replace(".", "_");
	}
	
	public String toString() {
		return m_path;
	}
	
	public String getPath() {
		return m_path;
	}
	
	public void setActive(boolean active) {
		m_active = active;
	}
	
	public void setOrder(int order) {
		m_order = order;
	}
	
	public String getContents() {
		// TODO: Abstract out file access from BoaServer
		String fullPath = "apps/" + m_app.getNamespace() + "/" + m_path;
		InputStream is = m_server.getInputStream(fullPath);
		return FileUtil.slurpStream(is);
	}
	
	public boolean getActive() {
		return m_active;
	}
	
	@Override
	public int compareTo(Object o) {
		if (!(o instanceof AppFile)) {
			return -1;
		}
		
		AppFile other = (AppFile)o;
		
		if (other.m_order > m_order) {
			return -1;
		} else if (other.m_order < m_order) {
			return 1;
		}
		
		if (other.isCode()) {
			return 1;
		} else {
			return m_path.compareTo(other.m_path);
		}		
	}

	
}
