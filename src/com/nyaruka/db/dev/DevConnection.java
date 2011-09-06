package com.nyaruka.db.dev;

import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteException;
import com.nyaruka.db.Connection;
import com.nyaruka.db.DBException;
import com.nyaruka.db.Statement;

public class DevConnection extends Connection {

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
	public Statement query(String sql) {
		try {
			return new DevStatement(m_connection, m_connection.prepare(sql));
		} catch (SQLiteException e) {
			throw new DBException(e);
		}
	}

	@Override
	public Statement prepare(String sql) {
		try {
			return new DevStatement(m_connection, m_connection.prepare(sql));
		} catch (SQLiteException e) {
			throw new DBException(e);
		}
	}

	@Override
	public void exec(String sql) {
		try {
			m_connection.exec(sql);
		} catch (SQLiteException e) {
			throw new DBException(e);
		}
	}
	
	@Override
	public void exec(Statement st) {
		st.step();
	}
	
	@Override
	public long insert(Statement st) {
		try {
			st.step();
			return m_connection.getLastInsertId();
		} catch(SQLiteException e) {
			throw new DBException(e);
		}
	}

	@Override
	public void update(Statement st) {
		st.step();
	}
	
	@Override
	public void dispose() {
		m_connection.dispose();
	}
	
	private SQLiteConnection m_connection;






}
