package com.nyaruka.app;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import com.nyaruka.http.HttpRequest;
import com.nyaruka.http.RequestParameters;
import com.nyaruka.util.FileUtil;
import com.nyaruka.vm.BoaApp;

public class AppAppTest extends NativeAppTestCase {
	
	
	public void testIndex(){
		
		// create a test dir for us
		File testDir = new File("/tmp/boa");
		if (testDir.exists()) {
			FileUtil.delete(testDir);
		}
		
		// create an empty directory
		new File("/tmp/boa/sys/app/new_app").mkdirs();
		FileUtil.writeFile(new File("/tmp/boa/sys/app/new_app/main.js"), "blarg");
		
		// fetch the index
		AppApp app = new AppApp(m_server.getVM(), m_server);
		assert200(getResponse(app, new HttpRequest("/admin/app/")));
		
	}
	
	public void testAddApp() {
		
		AppApp app = new AppApp(m_server.getVM(), m_server);

		// create a new app
		RequestParameters params = new RequestParameters();
		params.put("name", "test_app");
		RedirectResponse redirect = (RedirectResponse) getResponse(app, new HttpRequest("/admin/app/", "POST", new Properties(), params));
		assertNotNull(redirect);
		assertEquals("301 Moved Permanently", redirect.getStatus());

		// tell our server to reload our apps
		m_server.loadApps();
		
		// now fetch the index and see that our new app is there
		TemplateResponse resp = (TemplateResponse) getResponse(app, new HttpRequest("/admin/app/"));
		HashMap<String,Object> context = resp.getContext();	
		List<BoaApp> apps = (List<BoaApp>) context.get("apps");
		assertEquals(1, apps.size());
		assertEquals("test_app", apps.get(0).getNamespace());	
		
	}
}
