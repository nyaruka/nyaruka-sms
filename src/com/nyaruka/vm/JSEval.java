package com.nyaruka.vm;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ScriptableObject;

public class JSEval {
		public JSEval(String js, String name) {
			m_js = js;
			m_name = name;			
		}
		
		public void exec(Context context, ScriptableObject scope) {
			try{
				context.evaluateString(scope, m_js, m_name, 1, null);			
			} catch (Throwable t){
				t.printStackTrace();
			}
		}

		public String getName() { return m_name; }
		public String getJS() { return m_js; }
		
		private String m_name;
		private String m_js;
}
