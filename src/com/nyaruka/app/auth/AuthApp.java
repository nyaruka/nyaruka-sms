package com.nyaruka.app.auth;

import java.util.ArrayList;
import java.util.List;

import com.nyaruka.app.AdminApp;
import com.nyaruka.app.RedirectResponse;
import com.nyaruka.app.ResponseContext;
import com.nyaruka.app.TemplateResponse;
import com.nyaruka.app.View;
import com.nyaruka.db.Collection;
import com.nyaruka.db.Cursor;
import com.nyaruka.db.Record;
import com.nyaruka.http.HttpRequest;
import com.nyaruka.http.HttpResponse;
import com.nyaruka.vm.VM;

/**
 * Responsible for providing pages to login, create accounts and authenticate.
 * 
 * @author nicp
 */
public class AuthApp extends AdminApp {
	
	public AuthApp(VM vm){
		super("auth", vm);
		m_vm = vm;
	}
	
	private Collection getUserCollection(){
		Collection coll = m_vm.getDB().ensureCollection("users");
		coll.ensureStrIndex("username");
		return coll;
	}
	
	private Collection getPermCollection(){
		Collection coll = m_vm.getDB().ensureCollection("permissions");
		coll.ensureStrIndex("slug");
		return coll;
	}
	
	public User lookupUser(String username){
		Collection coll = getUserCollection();
		Cursor c = coll.find("username", username);
		if (c.hasNext()){
			return new User(c.next());
		} else {
			// always a default admin user, create if lazily
			if (username.equals("admin")){
				return createUser("admin", "nyaruka");
			}

			return null;
		}
	}
	
	/**
	 * Creates a new user with the passed in username and password, returning
	 * the instance created.
	 * @param username
	 * @param password
	 * @return
	 */
	public User createUser(String username, String password){
		Collection coll = getUserCollection();
		Cursor c = coll.find("username", username);
		
		// doesn't exist, create it
		if (!c.hasNext()){
			User u = new User(username, password);
			Record r = coll.save(u.toJSON());
			return new User(r);
		} else {
			return new User(c.next());
		}
	}
	
	public Permission lookupPermission(String slug){
		Cursor c = getPermCollection().find("slug", slug);
		if (c.hasNext()){
			return new Permission(c.next());
		} else {
			return null;
		}
	}
	
	public Permission createPermission(String slug){
		Permission perm = new Permission(slug);
		getPermCollection().save(perm.toJSON());
		return lookupPermission(slug);
	}
	
	public List<Permission> getPermissions(){
		List<Permission> perms = new ArrayList<Permission>();
		Cursor cursor = getPermCollection().find("{}");
		while(cursor.hasNext()){
			perms.add(new Permission(cursor.next()));
		}
		return perms;
	}
	
	class PermissionView extends AuthView {
		@Override
		public HttpResponse handle(HttpRequest request, String[] groups){
			ResponseContext context = getAdminContext();
			List<Permission> perms = getPermissions();
			context.put("permissions", perms);
			return new TemplateResponse("auth/perm_list.html", context);
		}
	}
	
	public class EditPermissionView extends AuthView {
		@Override
		public HttpResponse handle(HttpRequest request, String[] groups){
			ResponseContext context = getAdminContext();
			Permission perm = lookupPermission(groups[1]);
			if (perm == null){
				context.put("error", "No permission found with the slug '" + groups[1] + "'");
			} else {
				if (request.method().equals(request.POST)){
					perm.setDescription(request.params().getProperty("description"));
					getPermCollection().save(perm.toJSON());
					
					return new RedirectResponse("/auth/perm/");
				}
			}
			context.put("permission", perm);
			return new TemplateResponse("auth/perm_edit.html", context);
		}
	}
	
	public class CreatePermissionView extends AuthView {
		@Override
		public HttpResponse handle(HttpRequest request, String[] groups){
			ResponseContext context = getAdminContext();
			
			if (request.method().equals(request.POST)){
				String slug = request.params().getProperty("slug");
				context.put("slug", slug);
				
				if (slug == null){
					context.put("error", "You must include a slug for your permission");
				} else{
					Permission perm = lookupPermission(slug);
					if (perm != null){
						context.put("error", "A permission with that slug already exists");
					} else {
						perm = createPermission(slug);
						return new RedirectResponse("/auth/perm/edit/" + slug + "/");
					}
				}
			}
			
			return new TemplateResponse("auth/perm_create.html", context);			
		}
	}
	
	class IndexView extends AuthView {
		@Override
		public HttpResponse handle(HttpRequest request, String[] groups) {
			ResponseContext context = getAdminContext();
			User user = request.user();
			
			if (user != null){
				context.put("user", user);
				List<User> users = new ArrayList<User>();
				Cursor cursor = getUserCollection().find("{}");
				while(cursor.hasNext()){
					users.add(new User(cursor.next()));
				}
				context.put("users", users);
			}
			
			return new TemplateResponse("auth/index.html", context);
		}
	}
	
	class CreateUserView extends AuthView {
		@Override
		public HttpResponse handle(HttpRequest request, String[] groups) {
			ResponseContext context = getAdminContext();
			
			if (request.method().equals(request.POST)){
				String username = request.params().getProperty("username");
				String password = request.params().getProperty("password");

				context.put("username", username);
				
				if (username == null || password == null){
					context.put("error", "You must include a username and password");
				}
				else if (password.length() < 8){
					context.put("error", "The password must be 8 characters or longer");
				} else{
					User user = lookupUser(username);
					if (user != null){
						context.put("error", "A user with that username already exists");
					} else {
						user = createUser(username, password);
						return new RedirectResponse("/auth/edit/" + username + "/");
					}
				}
			}
			
			return new TemplateResponse("auth/user_reate.html", context);
		}
	}
	
	class EditUserView extends AuthView {
		@Override
		public HttpResponse handle(HttpRequest request, String[] groups) {
			ResponseContext context = getAdminContext();
			User user = lookupUser(groups[1]);
			List<Permission> permissions = getPermissions();
			context.put("permissions", permissions);
			
			// populate them
			user.populatePermissions(permissions);
			
			if (user == null){
				context.put("error", "No user found with the username '" + groups[1] + "'");
			} else {
				if (request.method().equals(request.POST)){
					user.setEmail(request.params().getProperty("email"));
					
					ArrayList<String> newPermissions = new ArrayList<String>();
					for (Permission perm : permissions){
						String prop = request.params().getProperty("perm_" + perm.getSlug());
						if (prop != null){
							newPermissions.add(perm.getSlug());
						}
					}
					user.setPermissions(newPermissions.toArray(new String[newPermissions.size()]));
					getUserCollection().save(user.toJSON());
					
					return new RedirectResponse("/auth/");
				}
			}
			context.put("user", user);
			return new TemplateResponse("auth/user_edit.html", context);
		}
	}

	class LoginView extends View {
		@Override
		public HttpResponse handle(HttpRequest request, String[] groups) {
			ResponseContext context = new ResponseContext();
			context.put("error", request.params().getProperty("error"));
			
			if (request.method().equals(request.POST)){
				String username = request.params().getProperty("username");
				String password = request.params().getProperty("password");
		
				// is this username and password valid?
				User u = lookupUser(username);
				if (u != null && u.checkPassword(password)){
					// set our user in our session
					request.session().setUser(username);
					request.setUser(u);
								
					// do we have a return URL?
					String returnURL = request.params().getProperty("return");
					if (returnURL == null) returnURL = "/";
					
					// if so, redirect to our index
					return new RedirectResponse(returnURL);
				} else {
					context.put("username", username);
					context.put("error", "Incorrect username or password");
				}
			}
			
			return new TemplateResponse("auth/login.html", context);
		}
	}
	
	class LogoutView extends View {
		@Override
		public HttpResponse handle(HttpRequest request, String[] groups) {
			// clear out the user in our session, that's it
			request.session().setUser(null);
			request.setUser(null);
			return new RedirectResponse("/");
		}
	}
	
	@Override
	/**
	 * Looks at our session and populates the User variable in our request if 
	 * our session contains a logged in user.
	 */
	public HttpResponse preProcess(HttpRequest request){
		System.out.println(request.session());
		
		// see if there is a user in this session
		String sessionUser = request.session().getUser();
		if (sessionUser != null){
			request.setUser(lookupUser(sessionUser));
		}
		
		return null;
	}
	
	/**
	 * Let's us catch authentication exceptions and have the user be redirected 
	 * to the login page.
	 * 
	 * @param t
	 */
	public HttpResponse handleError(Throwable t){
		if (t instanceof AuthException){
			AuthException ae = (AuthException) t;
			return new RedirectResponse("/auth/login/?return=" + ae.getReturnURL() + "&error=" + ae.getError());
		}
		return null;
	}

	@Override
	public void buildRoutes() {
		addRoute(buildActionRegex("login"), new LoginView());
		addRoute(buildActionRegex("logout"), new LogoutView());		
		addRoute(buildActionRegex("create"), new CreateUserView());
		addRoute("^/auth/edit/(.*?)/$", new EditUserView());				
		addRoute("^/auth/$", new IndexView());		
		
		addRoute("^/auth/perm/$", new PermissionView());
		addRoute("^/auth/perm/create/$", new CreatePermissionView());
		addRoute("^/auth/perm/edit/(.*?)/$", new EditPermissionView());
	}

	private VM m_vm;
}
