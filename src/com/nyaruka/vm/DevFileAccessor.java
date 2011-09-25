package com.nyaruka.vm;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import com.nyaruka.util.FileUtil;

public class DevFileAccessor implements FileAccessor {
	
	public DevFileAccessor(String path) {
		if (path != null && !path.endsWith("/")) {
			path += "/";
		}
		m_path = path;
		
		System.out.println("Initializing DevFileAccessor for path: " + new File(m_path).getAbsolutePath());
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
	public String[] getFiles(String path) {
		File dir = new File(getPath(path));
		System.out.println(dir.getAbsolutePath());
		return dir.list();
	}
	
	
	@Override
	public OutputStream getOutputStream(String path) {
		try {
			return new FileOutputStream(new File(getPath(path)));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean exists(String path) {
		System.out.println("Looking for: " + new File(getPath(path)).getAbsolutePath());
		return new File(getPath(path)).exists();
	}

	@Override
	public void createDirectory(String path) {
		new File(getPath(path)).mkdirs();
	}

	@Override
	public void writeFile(String path, String contents) {
		OutputStream os = getOutputStream(path);
		FileUtil.writeStream(os, contents);
	}

	@Override
	public void copyDirectory(String source, String dest) {
		File in = new File(getPath(source));
		
		File out = new File(getPath(dest));
		out.mkdirs();
		
		if (!in.exists()) {
			throw new RuntimeException("Couldn't find directory to copy: " + in.getAbsolutePath());
		}
		
		for (File f : in.listFiles()) {
			FileUtil.copyFile(f, new File(out, f.getName()));			
		}
	}

	@Override
	public void deleteDirectory(String path) {
		FileUtil.delete(new File(getPath(path)));
	}

	@Override
	public String getPath(String path) {
		return m_path + "/" + path;
	}


	private String m_path;


}
