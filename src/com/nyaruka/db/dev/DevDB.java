package com.nyaruka.db.dev;

import java.io.File;

import com.almworks.sqlite4java.SQLiteConnection;
import com.nyaruka.db.Connection;
import com.nyaruka.db.DB;

public class DevDB extends DB {

	public DevDB() {
		
	}
	
	public DevDB(File dbFile) {
		m_file = dbFile;
	}

	@Override
	public Connection getConnection() {
		if (m_file != null){
			return new DevConnection(new SQLiteConnection(m_file));
		}
		return new DevConnection(new SQLiteConnection());
	}
	
	private File m_file;

}
