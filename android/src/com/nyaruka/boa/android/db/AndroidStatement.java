package com.nyaruka.boa.android.db;

import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import com.nyaruka.db.Statement;

public class AndroidStatement implements Statement {

	private static final String TAG = AndroidStatement.class.getSimpleName();
	
	public AndroidStatement(SQLiteDatabase connection, String sql) {

		m_connection = connection;
		
		m_sql = sql;
		
		int count = 0;
		for (int i=0; i<sql.length(); i++) {
			if (sql.charAt(i) == '?') {
				count++;
			}
		}
		
		Log.d(TAG, "Created statement with " + count + " parameters");
		m_params = new Object[count];		
		m_statement = m_connection.compileStatement(m_sql);
		
	}
	
	@Override
	public void bind(int index, String value) {
		Log.d(TAG, "Binding " + index + " to " + value);
		if (value == null) {
			bindNull(index);
		} else {
			m_params[index-1] = value;
			m_statement.bindString(index, value);
		}
	}

	@Override
	public void bind(int index, int value) {
		m_params[index-1] = value;
		m_statement.bindDouble(index, value);
	}

	@Override
	public void bind(int index, long value) {
		m_params[index-1] = value;
		m_statement.bindLong(index, value);
	}

	@Override
	public void bindNull(int index) {
		m_params[index-1] = null;
		m_statement.bindNull(index);
	}
	
	@Override
	public boolean step() {
		if (m_cursor == null) {
			executeQuery();
		}
		return m_cursor.moveToNext();
	}
	
	@Override
	public long columnLong(int index) {
		return m_cursor.getLong(index);
	}

	@Override
	public String columnString(int index) {
		return m_cursor.getString(index);
	}

	@Override
	public int columnInt(int index) {
		return m_cursor.getInt(index);
	}

	@Override
	public int columnCount() {
		return m_cursor.getColumnCount();
	}

	@Override
	public String getColumnName(int index) {
		return m_cursor.getColumnName(index);
	}

	@Override
	public void executeQuery() {
		
		String replaced = m_sql;
		
		for (int i=0; i<m_params.length; i++) {
			Object p = m_params[i];
			if (p == null) {
				replaced = replaced.replaceFirst("\\?", "null");
			} else if (p instanceof Integer) {
				replaced = replaced.replaceFirst("\\?", ((Integer)p).toString());
			} else {
				replaced = replaced.replaceFirst("\\?", "'" + p.toString() + "'");
			}
		}
		
		Log.d(TAG, "Executing query: " + replaced);
		m_cursor = m_connection.rawQuery(replaced, null);
	}
	
	@Override
	public long executeInsert() {
		return m_statement.executeInsert();
	}

	@Override
	public void executeUpdate() {
		m_statement.execute();		
	}

	@Override
	public void execute() {
		m_statement.execute();		
	}

	private SQLiteDatabase m_connection;
	
	private Object[] m_params;
	private String m_sql;
	private Cursor m_cursor;
	private SQLiteStatement m_statement;
}
