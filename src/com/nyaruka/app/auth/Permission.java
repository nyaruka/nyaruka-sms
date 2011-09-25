package com.nyaruka.app.auth;

import com.nyaruka.db.Record;
import com.nyaruka.json.JSON;

public class Permission {
	
	public Permission(Record r){
		JSON data = r.getData();		
		m_slug = data.getString("slug");
		m_id = r.getId();
		
		if (data.has("name")) m_name = data.getString("name");
		if (data.has("description")) m_description = data.getString("description");
	}
	
	public Permission(String slug, String name, String description){
		m_slug = slug;
		m_name = name;
		m_description = description;
	}
	
	public Permission(String slug){
		m_slug = slug;
	}
	
	public JSON toJSON(){
		JSON json = new JSON();
		json.put("slug", m_slug);
		if (m_name != null)	json.put("name", m_name);
		if (m_description != null) json.put("description", m_description);
		if (m_id > 0) json.put("id", m_id);
		return json;
	}

	public String getName(){ return m_name; }
	public void setName(String name){ m_name = name; }
	
	public String getDescription(){ return m_description; }
	public void setDescription(String description){ m_description = description; }
	
	public String getSlug(){ return m_slug; }
	public long getId(){ return m_id; }
	
	private long m_id;
	private String m_name;
	private String m_slug;
	private String m_description;
}
