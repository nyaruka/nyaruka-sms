package com.nyaruka.app;

import com.nyaruka.http.HttpRequest;
import com.nyaruka.http.HttpResponse;
import com.nyaruka.vm.BoaServer;

/**
 * Responsible for providing pages to login, create accounts and authenticate.
 * 
 * @author nicp
 */
public class AuthApp extends NativeApp {

	public AuthApp(BoaServer boa){
		super("auth");
	}
	
	class LoginView extends View {
		@Override
		public HttpResponse handle(HttpRequest request) {
			return new HttpResponse("<html>hello world</html>");
		}
	}

	@Override
	public void buildRoutes() {
		addRoute(buildActionRegex("login"), new LoginView());
	}
}
