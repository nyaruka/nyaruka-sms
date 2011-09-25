package com.nyaruka.vm;

import java.io.InputStream;
import java.io.OutputStream;

public interface FileAccessor {
	
	/** Define how to get the contents of a given path */
	public InputStream getInputStream(String path);
	
	/** Write a file out to disk */
	public OutputStream getOutputStream(String path);
	
	/** Get all the files for a given app */
	public String[] getFiles(String path);

	/** Does a file exist for the given path */
	public boolean exists(String path);

	/** Prepend any path information */
	public String getPath(String path);

	/** Create all directories along the given path */
	public void createDirectory(String path);

	/** Write the contnets to a file */
	public void writeFile(String filePath, String content);

	public void copyDirectory(String source, String dest);

	public void deleteDirectory(String string);

}
