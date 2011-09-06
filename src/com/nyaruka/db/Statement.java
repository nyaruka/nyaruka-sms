package com.nyaruka.db;

public interface Statement {

	public long executeInsert();
	public void executeUpdate();
	public void executeQuery();
	public void execute();
	
	public long columnLong(int index);
	public int columnInt(int index);
	public String columnString(int index);
	
	public boolean step();
	
	public void bind(int index, String value);
	public void bind(int index, int value);
	public void bind(int index, long value);
	public void bindNull(int index);
	
	public int columnCount();
	public String getColumnName(int i);
}
