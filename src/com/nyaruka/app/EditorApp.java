package com.nyaruka.app;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.nyaruka.http.HttpRequest;
import com.nyaruka.http.HttpResponse;
import com.nyaruka.http.RequestParameters;
import com.nyaruka.util.FileUtil;
import com.nyaruka.vm.BoaApp;
import com.nyaruka.vm.BoaServer;
import com.nyaruka.vm.FileAccessor;
import com.nyaruka.vm.VM;

public class EditorApp extends AdminApp {

	public EditorApp(VM vm, FileAccessor files) {
		super("editor", vm);
		m_files = files;
	}

	public class EditorView extends View {

		@Override
		public HttpResponse handle(HttpRequest request, String[] groups) {
			
			ResponseContext context = getAdminContext();

			BoaApp app = m_vm.getApp(groups[1]);
			RequestParameters params = request.params();
			
			// save our active file to disk
			
			if (request.method().equals("POST") && params.containsKey("save_file")) {
				String saveFile = "apps/" + app.getNamespace() + "/" + params.getProperty("save_file");
				String contents = request.params().getProperty("contents");				
				FileUtil.writeStream(m_files.getOutputStream(saveFile), contents);
			}
			
			// get the list of all the files for our app for our dropdown
			List<String> openFiles = request.params().get("file");

			String[] allFiles = m_files.getFiles("apps/" + app.getNamespace());
			List<AppFile> appFiles = new ArrayList<AppFile>();
			for (String filePath : allFiles) {
				AppFile appFile = new AppFile(m_files, app, filePath);
				
				int order = openFiles.indexOf(filePath);
				if (openFiles.contains(filePath)) {
					appFile.setOrder(order);
					appFile.setActive(true);
				}
				
				appFiles.add(appFile);
			}			
			Collections.sort(appFiles);
			
			context.put("app", app);
			context.put("files", appFiles);				
			
			return new TemplateResponse("editor/editor.html", context);
		}		
	}
	
	@Override
	public void buildRoutes() {
		addRoute("^/admin/editor/([a-zA-Z]+)/", new EditorView());		
		
	}
	
	private FileAccessor m_files;

}
