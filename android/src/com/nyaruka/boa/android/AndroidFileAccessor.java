package com.nyaruka.boa.android;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.res.AssetManager;

import com.nyaruka.vm.BoaApp;
import com.nyaruka.vm.FileAccessor;

public class AndroidFileAccessor implements FileAccessor {

	public AndroidFileAccessor(AssetManager assets) {
		m_assets = assets;
	}
	
	@Override
	public InputStream getInputStream(String path) {
		try {
			return m_assets.open(path);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public OutputStream getOutputStream(String path) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getFiles(String path) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean exists(String path) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getPath(String path) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void createDirectory(String path) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void writeFile(String filePath, String content) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void copyDirectory(String source, String dest) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void deleteDirectory(String string) {
		// TODO Auto-generated method stub
		
	}
	
	/** Our applications asset manager */
	private AssetManager m_assets;


}
