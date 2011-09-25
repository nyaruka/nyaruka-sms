package com.nyaruka.app;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONObject;

import com.nyaruka.app.auth.AuthView;
import com.nyaruka.db.Collection;
import com.nyaruka.db.Cursor;
import com.nyaruka.db.Record;
import com.nyaruka.http.HttpRequest;
import com.nyaruka.http.HttpResponse;
import com.nyaruka.json.JSON;
import com.nyaruka.vm.VM;

public class DBApp extends AdminApp {

	public DBApp(VM vm){
		super("db", vm);
	}
	
	class IndexView extends AuthView {
		public HttpResponse handle(HttpRequest r, String[] groups) {
			if (r.method().equalsIgnoreCase("POST")){
				m_vm.getDB().ensureCollection(r.params().getProperty("name"));
			}
			
			return new TemplateResponse("db/index.html", getAdminContext());
		}
	}
	
	class CollectionView extends AuthView {
		public HttpResponse handle(HttpRequest r, String[] groups) {
			String collName = groups[1];
			Collection coll = m_vm.getDB().getCollection(collName);
			
			// they are adding a new record
			if (r.method().equalsIgnoreCase("POST")){
				JSON json = new JSON(r.params().getProperty("json"));
				coll.save(json);
			}
			
			// our set of keys
			HashSet<String> keys = new HashSet<String>();
			
			// build our list of matches
			ArrayList records = new ArrayList();
			Cursor cursor = coll.find("{}");
			while (cursor.hasNext()){
				Record record = cursor.next();
				JSON data = record.getData();
				
				// add all our unique keys
				Iterator item_keys = data.keys();
				while(item_keys.hasNext()){
					String key = item_keys.next().toString();
					
					// get the value
					Object value = data.get(key);
					
					// skip this key if the value is complex
					if ((value instanceof JSON) || (value instanceof JSONObject) || (value instanceof JSONArray)){
						// pass
					} else {
						keys.add(key);
					}
				}
				
				records.add(record.toJSON().toMap());
			}
			
			ResponseContext context = getAdminContext();
			context.put("keys", keys);
			context.put("collection", coll);
			context.put("records", records);
			return new TemplateResponse("db/list.html", context);
		}
	}
	
	class DeleteCollectionView extends AuthView {
		public HttpResponse handle(HttpRequest r, String[] groups){
			if (r.method().equals(r.POST)){
				String collName = groups[1];
				Collection coll = m_vm.getDB().getCollection(collName);
				m_vm.getDB().deleteCollection(coll);
			}
			return new RedirectResponse("/db/");
		}
	}
	
	class RecordView extends AuthView {
		public HttpResponse handle(HttpRequest r, String[] groups) {
			String collName = groups[1];
			Collection coll = m_vm.getDB().getCollection(collName);
			
			long id = Long.parseLong(groups[2]);
			Record rec = coll.getRecord(id);

			// they are posting new data
			if (r.method().equalsIgnoreCase("POST")){
				JSON json = new JSON(r.params().getProperty("json"));
				json.put("id", rec.getId());
				rec = coll.save(json);
			} 

			JSON data = rec.getData();
				
			// add all our unique keys
			ArrayList<String> fields = new ArrayList<String>();
			Iterator item_keys = data.keys();
			while(item_keys.hasNext()){
				fields.add(item_keys.next().toString());
			}
			
			ResponseContext context = getAdminContext();
			context.put("collection", coll);
			context.put("record", rec);
			context.put("values", rec.toJSON().toMap());
			context.put("fields", fields);
			context.put("json", rec.getData().toString());
			return new TemplateResponse("db/read.html", context);
		}
	}
	
	class DeleteRecordView extends AuthView {
		public HttpResponse handle(HttpRequest r, String[] groups) {
			String collName = groups[1];
			Collection coll = m_vm.getDB().getCollection(collName);
			
			if (r.method().equals(r.POST)){
				long id = Long.parseLong(groups[2]);
				coll.delete(id);
			}
			return new RedirectResponse("/db/" + coll.getName() + "/");
		}
	}

	static final String COLLECTION = "^/db/([a-zA-Z]+)/$";
	static final String DELETE_COLLECTION = "^/db/([a-zA-Z]+)/delete/$";						
	static final String DELETE_RECORD = "^/db/([a-zA-Z]+)/(\\d+)/delete/$";
	static final String RECORD = "^/db/([a-zA-Z]+)/(\\d+)/$";
	static final String INDEX = "^/db/$";
			
	@Override
	public void buildRoutes() {
		addRoute(INDEX, new IndexView());
		addRoute(COLLECTION, new CollectionView());
		addRoute(RECORD, new RecordView());
		addRoute(DELETE_RECORD, new DeleteRecordView());		
		addRoute(DELETE_COLLECTION, new DeleteCollectionView());				
	}
}
