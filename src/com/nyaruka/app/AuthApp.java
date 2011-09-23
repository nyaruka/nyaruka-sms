package com.nyaruka.app;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.nyaruka.db.Collection;
import com.nyaruka.db.Cursor;
import com.nyaruka.db.Record;
import com.nyaruka.http.HttpRequest;
import com.nyaruka.http.HttpResponse;
import com.nyaruka.json.JSON;
import com.nyaruka.util.Base64;
import com.nyaruka.vm.VM;

/**
 * Responsible for providing pages to login, create accounts and authenticate.
 * 
 * @author nicp
 */
public class AuthApp extends NativeApp {

	public static class User {
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
		
		private long m_id;
		private JSON m_data;
		private String m_username;
		private String m_password;
		private String m_salt;
		private String m_name;
		private String m_email;
	}
	
	public static class AuthException extends RuntimeException {
		public AuthException(String returnURL){
			this(returnURL, "You must be logged in to access this page.");
		}
		
		public AuthException(String returnURL, String error){
			m_returnURL = returnURL;
			m_error = error;
		}
		
		public String getReturnURL(){ return m_returnURL; }
		private String m_returnURL;
		
		public String getError(){ return m_error; }
		private String m_error;
	}
	
	public AuthApp(VM vm){
		super("auth");
		m_vm = vm;
	}
	
	private Collection getCollection(){
		Collection coll = m_vm.getDB().ensureCollection("users");
		coll.ensureStrIndex("username");
		return coll;
	}
	
	public User lookupUser(String username){
		Collection coll = getCollection();
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
		Collection coll = getCollection();
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
	
	class IndexView extends AuthView {
		@Override
		public HttpResponse handle(HttpRequest request, String[] groups) {
			ResponseContext context = new ResponseContext();
			User user = request.user();
			
			if (user != null){
				context.put("user", user);
				List<User> users = new ArrayList<User>();
				Cursor cursor = getCollection().find("{}");
				while(cursor.hasNext()){
					users.add(new User(cursor.next()));
				}
				context.put("users", users);
			}
			
			return new TemplateResponse("auth/index.html", context);
		}
	}
	
	class CreateView extends AuthView {
		@Override
		public HttpResponse handle(HttpRequest request, String[] groups) {
			ResponseContext context = new ResponseContext();
			
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
			
			return new TemplateResponse("auth/create.html", context);
		}
	}
	
	class EditView extends AuthView {
		@Override
		public HttpResponse handle(HttpRequest request, String[] groups) {
			ResponseContext context = new ResponseContext();
			User user = lookupUser(groups[1]);
			if (user == null){
				context.put("error", "No user found with the username '" + groups[1] + "'");
			} else {
				if (request.method().equals(request.POST)){
					user.setName(request.params().getProperty("name"));
					user.setEmail(request.params().getProperty("email"));
					getCollection().save(user.toJSON());
					
					return new RedirectResponse("/auth/");
				}
			}
			context.put("user", user);
			return new TemplateResponse("auth/edit.html", context);
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
		addRoute(buildActionRegex("create"), new CreateView());				
		addRoute("^/auth/edit/(.*?)/$", new EditView());				
		addRoute("^/auth/$", new IndexView());		
	}
	
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

	private VM m_vm;
}
