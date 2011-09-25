package com.nyaruka.vm;

import java.util.ArrayList;

import net.asfun.jangod.template.TemplateEngine;

import com.nyaruka.db.DB;
import com.nyaruka.db.dev.DevDB;

public class MockBoaServer extends BoaServer {

	public MockBoaServer() {
		super(new VM(new DevDB()), new DevFileAccessor("/tmp/boa"));
		
		DB db = m_vm.getDB();
		db.open();
		db.init();
		
		m_vm.start(new ArrayList<JSEval>());
	}
	
	public FileAccessor getFiles() {
		return new DevFileAccessor("/tmp/boa");
	}

	@Override
	public void configureTemplateEngines(TemplateEngine systemTemplates,
			TemplateEngine appTemplates) {
		
	}

	public VM getVM() {
		return m_vm;
	}
	
	public DB getDB() {
		return m_vm.getDB();
	}

}
