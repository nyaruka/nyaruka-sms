package com.nyaruka.app;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.nyaruka.http.HttpRequest;
import com.nyaruka.http.HttpResponse;
import com.nyaruka.vm.BoaApp;
import com.nyaruka.vm.BoaServer;
import com.nyaruka.vm.VM;

public class AppApp extends AdminApp {

	Pattern APP_MATCHER = Pattern.compile("^/admin/app/([a-zA-Z]+)/$");
		
	public AppApp(String name, VM vm) {
		super(name, vm);
	}
	
	public AppApp(BoaServer server, VM vm) {
		super("app", vm);
		m_server = server;
	}

	class IndexView extends View {		
		public HttpResponse handle(HttpRequest r, String[] groups) {
			HashMap<String,Object> context = getAdminContext();
			
			if (r.method().equalsIgnoreCase("POST")){
				String appName = r.params().getProperty("name");
				// TODO: sanitize or complain about bad app names
				m_server.createApp(appName);
				return new RedirectResponse("/admin/app/" + appName + "/");
			}
			
			return new TemplateResponse("app/index.html", context);
		}
	}
	
	class AppView extends View {		
		public HttpResponse handle(HttpRequest r, String[] groups) {	

			HashMap<String,Object> context = getAdminContext();
				
			BoaApp app = m_vm.getApp(groups[0]);		

			if (r.method().equalsIgnoreCase("POST")){
				
				// removing an app
				if (r.params().containsKey("remove")) {
					m_server.removeApp(groups[0]);
					return new RedirectResponse("/admin/app/");
				}
				
				// adding a new file
				if (r.params().containsKey("file_name")) {
					
					boolean isCode = false;						
					String filename = r.params().getProperty("file_name");
					
					if (r.params().get("is_code").equals("1")) {
						isCode = true;
						if (!filename.endsWith(".js")) {
							filename += ".js";
						}
					} else {
						if (!filename.endsWith(".html")) {
							filename += ".html";
						}							
					}
					
					m_server.createFile(app, filename, isCode);
				}					
			}

			context.put("app", app);		
			
			String[] files = m_server.getFiles(app);
			List<AppFile> appFiles = new ArrayList<AppFile>();
			for (String path : files) {
				appFiles.add(new AppFile(m_server, app, path));
			}			
			Collections.sort(appFiles);
			context.put("files", appFiles);				
			return new TemplateResponse("app/view.html", context);								

		}
	}
	
	@Override
	public void buildRoutes() {
		addRoute("^/admin/app/([a-zA-Z]+)/$", new AppView());		
		addRoute("^/admin/app/$", new IndexView());		
		addRoute("^/admin/$", new IndexView());		
	}
	
	private BoaServer m_server;

}
