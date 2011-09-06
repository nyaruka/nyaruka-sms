package com.nyaruka.boa.android.db;

import java.io.File;

import android.database.sqlite.SQLiteDatabase;

import com.nyaruka.db.Connection;
import com.nyaruka.db.DB;

public class AndroidDB extends DB {

	public AndroidDB(File dbFile) {
		m_dbFile = dbFile;		
	}
	
	public Connection getConnection() {
		return new AndroidConnection(SQLiteDatabase.openOrCreateDatabase(m_dbFile, null));
	}
	
	private File m_dbFile;

}
