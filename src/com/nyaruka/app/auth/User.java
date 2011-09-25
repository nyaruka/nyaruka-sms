package com.nyaruka.app.auth;

import java.security.MessageDigest;
import java.util.Random;

import com.nyaruka.db.Record;
import com.nyaruka.json.JSON;
import com.nyaruka.util.Base64;

public class User {
	public User(Record r){
		JSON record = r.getData();
		m_username = record.getString("username");
		m_password = record.getString("password");
		m_salt = record.getString("salt");
		if (record.has("name")){
			m_name = record.getString("name");
		}
		if (record.has("email")){
			m_email = record.getString("email");
		}
		
		if (record.has("data")){
			m_data = r.getData().getJSON("data");
		} else {
			m_data = new JSON();
		}
		m_id = r.getId();
	}
	
	public User(String username, String password){
		m_username = username;
		m_salt = generateSalt();
		m_password = hashPassword(password, m_salt);
		m_data = new JSON();
	}
	
	public boolean checkPassword(String password){
		String hashed = hashPassword(password, m_salt);
		return hashed.equals(m_password);
	}
	
	public boolean hasPermission(String permission){
		return false;
	}
	
	public String getUsername(){ return m_username; }
	public JSON getData(){ return m_data; }

	public JSON toJSON(){
		JSON json = new JSON();
		json.put("username", m_username);
		json.put("password", m_password);
		json.put("email", m_email);
		json.put("name", m_name);
		json.put("salt", m_salt);
		json.put("data", m_data);
		
		if (m_id > 0){
			json.put("id", m_id);
		}
		
		return json;
	}
	
	public String getEmail(){ return m_email; }
	public String getName(){ return m_name; }
	public void setEmail(String email){ m_email = email; }
	public void setName(String name){ m_name = name; }
	
	static String hashPassword(String password, String salt){
		try{
			MessageDigest digest = MessageDigest.getInstance("SHA-1");
			digest.update(password.getBytes());
			digest.update(salt.getBytes());
			byte[] hash = digest.digest();
			return Base64.encodeBytes(hash);
		} catch (Throwable t){
			t.printStackTrace();
			return null;
		}
	}
	
	static String generateSalt(){
		long seed = System.currentTimeMillis();
		Random r = new Random(seed);
		byte[] salt = new byte[32];
		r.nextBytes(salt);
		return Base64.encodeBytes(salt);
	}
	
	private long m_id;
	private JSON m_data;
	private String m_username;
	private String m_password;
	private String m_salt;
	private String m_name;
	private String m_email;
}
