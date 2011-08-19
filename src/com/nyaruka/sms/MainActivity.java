package com.nyaruka.sms;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.ast.Scope;

import com.nyaruka.http.HttpService;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class MainActivity extends Activity {
	
	private static final String TAG = MainActivity.class.getSimpleName();
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        startService(new Intent(this, HttpService.class));
        
        setContentView(R.layout.main);
        ((TextView)(findViewById(R.id.message))).setText("IP Address: " + getLocalIpAddress());
    }
    
    
    public String getLocalIpAddress() {
    	try {
    		for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
    			NetworkInterface intf = en.nextElement();
    			for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
    				InetAddress inetAddress = enumIpAddr.nextElement();
    				if (!inetAddress.isLoopbackAddress()) {
    					return inetAddress.getHostAddress().toString();
    				}
    			}
    		}
    	} catch (Throwable t) {
    		return t.toString();
    	}
    
    	return null;
    }
}