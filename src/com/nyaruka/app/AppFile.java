package com.nyaruka.app;

public class AppFile implements Comparable {

	public AppFile(String path) {
		m_path = path;
	}
	
	public boolean isTemplate() {
		return m_path.endsWith(".html");
	}
	
	public boolean isCode() {
		return m_path.endsWith(".js");
	}
	
	public String toString() {
		return m_path;
	}
	
	private String m_path;

	@Override
	public int compareTo(Object o) {
		if (!(o instanceof AppFile)) {
			return -1;
		}
		
		AppFile other = (AppFile)o;
		if (other.isCode()) {
			return 1;
		} else {
			return m_path.compareTo(other.m_path);
		}		
	}
}
