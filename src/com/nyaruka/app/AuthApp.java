package com.nyaruka.app;

import java.security.MessageDigest;
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
			m_data = r.getData();
		}
		
		public User(String username, String password){
			m_data = new JSON();
			m_data.put("username", username);
			String salt = generateSalt();
			m_data.put("salt", salt);
			m_data.put("password", hashPassword(password, salt));
		}
		
		public boolean checkPassword(String password){
			String hashed = hashPassword(password, m_data.getString("salt"));
			return hashed.equals(m_data.getString("password"));
		}
		
		public String getUsername(){ return m_data.getString("username"); }
		public JSON getData(){ return m_data; }
		
		private JSON m_data;
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
			Record r = coll.save(u.getData());
			return new User(r);
		} else {
			return new User(c.next());
		}
	}
	
	class IndexView extends View {
		@Override
		public HttpResponse handle(HttpRequest request, String[] groups) {
			ResponseContext context = new ResponseContext();
			context.put("user", request.user());
			return new TemplateResponse("auth/index.html", context);
		}
	}

	class LoginView extends View {
		@Override
		public HttpResponse handle(HttpRequest request, String[] groups) {
			ResponseContext context = new ResponseContext();
			
			if (request.method().equals(request.POST)){
				String username = request.params().getProperty("username");
				String password = request.params().getProperty("password");
		
				// is this username and password valid?
				User u = lookupUser(username);
				if (u != null && u.checkPassword(password)){
					// set our user in our session
					request.session().setUser(username);
					request.setUser(u);
					
					// if so, redirect to our index
					return new RedirectResponse("/");
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
		System.out.println("session: "+ request.session());
		
		String sessionUser = request.session().getUser();
		if (sessionUser != null){
			request.setUser(lookupUser(sessionUser));
		}
		
		return null;
	}

	@Override
	public void buildRoutes() {
		addRoute(buildActionRegex("login"), new LoginView());
		addRoute(buildActionRegex("logout"), new LogoutView());		
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
