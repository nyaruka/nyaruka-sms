package com.nyaruka.db;

public interface Statement {

	public long columnLong(int recordId);
	public String columnString(int recordData);
	public boolean step();
	public int columnInt(int i);
	public void bind(int i, String intIndex);
	public void bind(int i, int id);
	public void bind(int i, long id);
	public void bindNull(int i);
	public int columnCount();
	public String getColumnName(int i);
	public Object columnValue(int i);

}
