package com.nyaruka.app;

import java.util.HashMap;
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
		public HttpResponse handle(HttpRequest r) {
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
		public HttpResponse handle(HttpRequest r) {	

			HashMap<String,Object> context = getAdminContext();
			Pattern APP = Pattern.compile("^/admin/app/([a-zA-Z]+)/$");

			Matcher matcher = APP.matcher(r.url());
			if (matcher.find()) {
				String appName = matcher.group(1);						
				if (r.method().equalsIgnoreCase("POST")){
					m_server.removeApp(appName);
					return new RedirectResponse("/admin/app/");
				}

				BoaApp app = m_vm.getApp(appName);		
				context.put("app", app);			
				context.put("files", m_server.getFiles(app));
				return new TemplateResponse("app/view.html", context);								
			}
			
			return new RedirectResponse("/admin/app/");
		}
	}
	
	@Override
	public void buildRoutes() {
		addRoute("^/admin/app/([a-zA-Z]+)/$", new AppView());		
		addRoute("^/admin/app/$", new IndexView());		
		addRoute("^/admin/", new IndexView());		
	}
	
	private BoaServer m_server;

}
