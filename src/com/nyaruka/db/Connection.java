
package com.nyaruka.db;

public abstract class Connection {

	public abstract void open(boolean b);

	public abstract Statement query(String sql);
	
	public abstract Statement prepare(String sql);
	
	public abstract void exec(String sql);
	
	public abstract void dispose();
	
	public long insert(Statement statement) {
		return statement.executeInsert();
	}

	public void update(Statement statement) {
		statement.executeUpdate();		
	}

	public void exec(Statement statement) {
		statement.execute();
	}

	


}
