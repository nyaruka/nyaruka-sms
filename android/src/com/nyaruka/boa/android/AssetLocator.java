package com.nyaruka.boa.android;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import com.nyaruka.util.FileUtil;

import android.content.res.AssetManager;

import net.asfun.jangod.base.ResourceLocater;

public class AssetLocator implements ResourceLocater {

	public AssetLocator(AssetManager assets) {
		m_assets = assets;
	}
	
	@Override
	public String getDirectory(String path) throws IOException {
		return new File(path).getParent();
	}

	@Override
	public String getFullName(String name, String dir) throws IOException {
		if (name.startsWith("/")) {
			return dir + name;
		}
		return dir + "/" + name;
	}

	@Override
	public String getFullName(String relativeName, String relativeDir, String defaultDir) throws IOException {
		if (relativeName.startsWith("/")) {
			return defaultDir + relativeName;
		}
		return defaultDir + "/" + relativeName;
	}

	@Override
	public Reader getReader(String arg0, String arg1) throws IOException {
		return new InputStreamReader(m_assets.open(arg0));
	}

	@Override
	public String getString(String file, String enc) throws IOException {
		return FileUtil.slurpStream(m_assets.open(file));
	}

	private AssetManager m_assets;
}
