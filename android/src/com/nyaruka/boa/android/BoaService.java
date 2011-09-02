package com.nyaruka.boa.android;

import java.io.IOException;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class BoaService extends Service implements SMSModem.SmsModemListener {

	private static final String TAG = BoaService.class.getSimpleName();
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
    @Override
    public void onCreate(){
    	s_this = this;
    	
    	try {
    		m_server = new AndroidBoaServer(8080, getApplicationContext());
    	} catch (IOException e){
    		e.printStackTrace();
    	}
		
		try {
			m_modem = new SMSModem(getApplicationContext(), this);
		} catch (Throwable t){
			t.printStackTrace();
		}
    }
    
    @Override
	public void onNewSMS(String address, String message) {
    	// String response = executeJS(m_script, address, message).toString();
    	// m_modem.sendSms(address, response, "hi");
	}

	@Override
	public void onSMSSendError(String token, String errorDetails) {
		
	}

	@Override
	public void onSMSSent(String token) {
		
	}

	/*	
 	public Object executeJS(String script, String address, String message){
    	// Create our context and turn off compilation
        Context cx = Context.enter();
        cx.setOptimizationLevel( -1 );

        // Initialize the scope
        ScriptableObject scope = cx.initStandardObjects();
        ScriptableObject.putProperty(scope, "from", address);
        ScriptableObject.putProperty(scope, "message", message);        

        // This is the line that throws the exception
        Object result;
        try{
        	result = cx.evaluateString(scope, script, "", 1, null);
        } catch (Throwable t){
        	return t.toString();
        }
        Context.exit();
        
        return result;
    }
	*/
	
	public static BoaService getThis(){ return s_this; }
	
	public void setScript(String script){
		m_script = script;
	}
	
    private AndroidBoaServer m_server;
    private SMSModem m_modem;
    private static BoaService s_this;
    private String m_script;
}
