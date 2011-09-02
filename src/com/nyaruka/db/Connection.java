
package com.nyaruka.db;

public interface Connection {

	public void open(boolean b);

	public Statement prepare(String sql);
	
	public long getLastInsertId();
	
	public Connection exec(String sql);
	
	public void dispose();

}
