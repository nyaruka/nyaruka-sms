package com.nyaruka.sms;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.nyaruka.http.HttpService;

/**
 * BootStrapper just makes sure our service is running after the phone is booted
 */
public class BootStrapper extends BroadcastReceiver {
	
	public static final String TAG = BootStrapper.class.getSimpleName();
	
	@Override
	public void onReceive(Context context, Intent intent){
		checkService(context);
	}
		
	public static boolean checkService(Context context){
		// If we haven't yet initialized our modem, do so                                    
		if (!isServiceRunning(context)){
			startService(context);
			return false;
		} else {
			return true;
		}
	}
	
	private static void startService(Context context){
		Intent serviceIntent = new Intent(context, HttpService.class);
		context.startService(serviceIntent);	
	}
	
	private static boolean isServiceRunning(Context context) {
	    ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
	    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
	        if ("com.nyaruka.sms.HttpService".equals(service.service.getClassName())) {
	            return true;
	        }
	    }
	    return false;
	}	
}