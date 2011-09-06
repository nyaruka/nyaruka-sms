package com.nyaruka.boa.android.db;

import android.database.sqlite.SQLiteDatabase;

import com.nyaruka.db.Connection;
import com.nyaruka.db.Statement;

public class AndroidConnection extends Connection {

	public AndroidConnection(SQLiteDatabase database) {
		m_db = database;
	}

	@Override
	public void open(boolean b) {
	}

	@Override
	public Statement query(String sql) {
		return null;
	}
		
	@Override
	public Statement prepare(String sql) {
		return new AndroidStatement(m_db, sql);
	}

	@Override
	public void exec(String sql) {
		m_db.execSQL(sql);
	}

	@Override
	public void dispose() {
		m_db.close();		
	}
		
	private SQLiteDatabase m_db;


}
