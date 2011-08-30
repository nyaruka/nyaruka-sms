package com.nyaruka.vm;

public class LoggingWrapper {

	public LoggingWrapper(StringBuffer buffer){
		m_buffer = buffer;
	}
	
	public void log(String msg){
		m_buffer.append(msg + "\n");
	}
	
	StringBuffer m_buffer;
}
