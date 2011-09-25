package com.nyaruka.app;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import com.nyaruka.http.HttpRequest;
import com.nyaruka.http.HttpResponse;
import com.nyaruka.util.FileUtil;
import com.nyaruka.vm.BoaApp;
import com.nyaruka.vm.BoaServer;
import com.nyaruka.vm.VM;

public class AppApp extends AdminApp {

	Pattern APP_MATCHER = Pattern.compile("^/admin/app/([a-zA-Z]+)/$");
		
	public AppApp(String name, VM vm) {
		super(name, vm);
	}
	
	public AppApp(VM vm, BoaServer server) {
		super("app", vm);
		m_server = server;
	}

	class IndexView extends View {		
		public HttpResponse handle(HttpRequest r, String[] groups) {
			ResponseContext context = getAdminContext();
			
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

			ResponseContext context = getAdminContext();
				
			BoaApp app = m_vm.getApp(groups[1]);		

			if (r.method().equalsIgnoreCase("POST")){
				
				// removing an app
				if (r.params().containsKey("remove")) {
					m_server.removeApp(groups[1]);
					return new RedirectResponse("/admin/app/");
				}
				
				// adding a new file
				String contents = "";
					
				if (r.params().containsKey("file_name")) {
					
					String filename = r.params().getProperty("file_name");
					
					if (r.params().getProperty("is_code").equals("1")) {
						if (!filename.endsWith(".js")) {
							filename += ".js";
						}
						
						InputStream contentStream = m_server.getFiles().getInputStream("sys/app/new_file/code.js");
						contents = FileUtil.slurpStream(contentStream);
					
					} else {
						if (!filename.endsWith(".html")) {
							filename += ".html";
						}
						
						InputStream contentStream = m_server.getFiles().getInputStream("sys/app/new_file/template.html");
						contents = FileUtil.slurpStream(contentStream);
					}
					
					m_server.getFiles().writeFile("apps/" + app.getNamespace() + "/" + filename, contents);
					
				}					
			}

			context.put("app", app);		
			
			String[] files = m_server.getFiles().getFiles("apps/" + app.getNamespace());
			List<AppFile> appFiles = new ArrayList<AppFile>();
			for (String path : files) {
				appFiles.add(new AppFile(m_server.getFiles(), app, path));
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
