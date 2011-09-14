package com.nyaruka.app;

import java.util.HashMap;

import com.nyaruka.vm.VM;

public abstract class AdminApp extends NativeApp {

	VM m_vm;
	
	public AdminApp(String name, VM vm){
		super(name);
		m_vm = vm;
	}
	
	/**
	 * The base context for the admin view
	 */
	HashMap<String,Object> getAdminContext() {
		HashMap<String, Object> context = new HashMap<String, Object>();
		context.put("collections", m_vm.getDB().getCollections());
		context.put("apps", m_vm.getApps());
		return context;
	}
}
