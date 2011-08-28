package com.nyaruka.db;

import com.almworks.sqlite4java.SQLiteStatement;

public class Cursor {

	public Cursor(SQLiteStatement st, int count){
		m_count = count;
		m_st = st;
	}
	
	public boolean hasNext(){
		try{
			if (m_shouldStep){
				m_hasNext = m_st.step();
				m_shouldStep = false;
			}
		} catch (Throwable t){
			throw new RuntimeException(t);
		}
		return m_hasNext;
	}
	
	public Record next(){
		try{
			if (m_shouldStep){
				m_st.step();
			}
			m_shouldStep = true;
			
			return new Record(m_st);			
		} catch (Throwable t){
			throw new RuntimeException(t);
		}
	}
	
	public int count(){
		return m_count;
	}
	
	private boolean m_shouldStep = true;
	private boolean m_hasNext = false;
	private SQLiteStatement m_st;
	private int m_count;
}
