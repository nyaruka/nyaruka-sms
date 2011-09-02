package com.nyaruka.db;

public class DBException extends RuntimeException {

	public DBException(Exception e) {
		super(e);
	}
}
