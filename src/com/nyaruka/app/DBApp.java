package com.nyaruka.app;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONObject;

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
	
	class IndexView extends View {
		public HttpResponse handle(HttpRequest r) {
			if (r.method().equalsIgnoreCase("POST")){
				m_vm.getDB().ensureCollection(r.params().getProperty("name"));
			}
			
			return new TemplateResponse("db/index.html", getAdminContext());
		}
	}
	
	class CollectionView extends View {
		Pattern COLLECTION = Pattern.compile("^/db/([a-zA-Z]+)/$");
		
		public HttpResponse handle(HttpRequest r) {
			Matcher matcher = COLLECTION.matcher(r.url());
			matcher.find();
			String collName = matcher.group(1);
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
			
			HashMap<String, Object> context = getAdminContext();
			context.put("keys", keys);
			context.put("collection", coll);
			context.put("records", records);
			return new TemplateResponse("db/list.html", context);
		}
	}
	
	class DeleteCollectionView extends View {
		Pattern DELETE_COLLECTION = Pattern.compile("^/db/([a-zA-Z]+)/delete/$");					
		
		public HttpResponse handle(HttpRequest request){
			Matcher matcher = DELETE_COLLECTION.matcher(request.url());
			matcher.find();
			String collName = matcher.group(1);
			Collection coll = m_vm.getDB().getCollection(collName);
			m_vm.getDB().deleteCollection(coll);
			return new RedirectResponse("/db/");
		}
	}
	
	class RecordView extends View {
		Pattern RECORD = Pattern.compile("^/db/([a-zA-Z]+)/(\\d+)/$");
		
		public HttpResponse handle(HttpRequest r) {
			Matcher matcher = RECORD.matcher(r.url());
			matcher.find();
			String collName = matcher.group(1);
			Collection coll = m_vm.getDB().getCollection(collName);
			
			long id = Long.parseLong(matcher.group(2));
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
			
			HashMap<String, Object> context = getAdminContext();
			context.put("collection", coll);
			context.put("record", rec);
			context.put("values", rec.toJSON().toMap());
			context.put("fields", fields);
			context.put("json", rec.getData().toString());
			return new TemplateResponse("db/read.html", context);
		}
	}
	
	class DeleteRecordView extends View {
		Pattern DELETE_RECORD = Pattern.compile("^/db/([a-zA-Z]+)/(\\d+)/delete/$");		
		
		public HttpResponse handle(HttpRequest r) {
			Matcher matcher = DELETE_RECORD.matcher(r.url());
			matcher.find();
			String collName = matcher.group(1);

			Collection coll = m_vm.getDB().getCollection(collName);
			
			if (r.method() == "POST"){
				long id = Long.parseLong(matcher.group(2));
				Record rec = coll.getRecord(id);			
				coll.delete(id);
			}
			return new RedirectResponse("/db/" + coll.getName() + "/");
		}
	}
		
	@Override
	public void buildRoutes() {
		addRoute("^/db/$", new IndexView());
		addRoute("^/db/([a-zA-Z]+)/$", new CollectionView());
		addRoute("^/db/([a-zA-Z]+)/(\\d+)/$", new RecordView());
		addRoute("^/db/([a-zA-Z]+)/(\\d+)/delete/$", new DeleteRecordView());		
		addRoute("^/db/([a-zA-Z]+)/delete/$", new DeleteCollectionView());				
	}
}
