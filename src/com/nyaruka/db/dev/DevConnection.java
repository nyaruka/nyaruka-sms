package com.nyaruka.db.dev;

import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteException;
import com.nyaruka.db.Connection;
import com.nyaruka.db.DBException;
import com.nyaruka.db.Statement;

public class DevConnection implements Connection {

	public DevConnection(SQLiteConnection connection) {
		m_connection = connection;
	}
	

	@Override
	public void open(boolean allowCreate) {
		try {
			m_connection.open(allowCreate);
		} catch (SQLiteException e) {
			throw new DBException(e);
		}
	}

	@Override
	public Statement prepare(String sql) {
		try {
			return new DevStatement(m_connection.prepare(sql));
		} catch (SQLiteException e) {
			throw new DBException(e);
		}
	}

	@Override
	public long getLastInsertId() {
		try {
			return m_connection.getLastInsertId();
		} catch (SQLiteException e) {
			throw new DBException(e);
		}
	}

	@Override
	public Connection exec(String sql) {
		try {
			return new DevConnection(m_connection.exec(sql));
		} catch (SQLiteException e) {
			throw new DBException(e);
		}
	}

	@Override
	public void dispose() {
		m_connection.dispose();
	}
	
	private SQLiteConnection m_connection;


}
