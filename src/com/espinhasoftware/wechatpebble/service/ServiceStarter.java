package com.espinhasoftware.wechatpebble.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ServiceStarter extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		//we double check here for only boot complete event
		 if(intent.getAction().equalsIgnoreCase(Intent.ACTION_BOOT_COMPLETED))
		   {
		     //here we start the service             
		     Intent serviceIntent = new Intent(context, MessageProcessingService.class);
		     context.startService(serviceIntent);
		     
		     serviceIntent = new Intent(context, PebbleCommService.class);
		     context.startService(serviceIntent);
		   }
	}

}
