/**
 * This file is part of WeChat Pebble (http://github.com/wechatpebble/) 
 * and distributed under GNU GENERAL PUBLIC LICENSE (GPL).
 * 
 * pinyin4j is free software; you can redistribute it and/or modify 
 * it under the terms of the GNU General Public License as published by 
 * the Free Software Foundation; either version 3 of the License, or 
 * (at your option) any later version. 
 * 
 * WeChat Pebble is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the 
 * GNU General Public License for more details. 
 * 
 * You should have received a copy of the GNU General Public License 
 * along with WeChat Pebble. If not, see <http://www.gnu.org/licenses/>.
 */
package com.espinhasoftware.wechatpebble.service;

import com.espinhasoftware.wechatpebble.R;
import com.espinhasoftware.wechatpebble.pebblecomm.PebbleMessage;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Notification;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.preference.PreferenceManager;

import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

/**
 * @author Tiago Espinha (tiago@espinha.pt)
 *
 */
public class HandleWeChat extends AccessibilityService {

	private static final int PBL_IMG_TRANSACTION_ID = 1;
	
	
	/**
	 * Handler of incoming messages from PebbleCommService.
	 */
	class PebbleCommIncomingHandler extends Handler {
	    @Override
	    public void handleMessage(Message msg) {
	        switch (msg.what) {
	            case PebbleCommService.MSG_SEND_FINISHED:
	                Log.d("HandleWeChat", "Hooray! Message sent!");
	                break;
	            default:
	                super.handleMessage(msg);
	        }
	    }
	}
	
	/**
	 * Handler of incoming messages from PebbleCommService.
	 */
	class MessageProcessingIncomingHandler extends Handler {
	    @Override
	    public void handleMessage(Message msg) {
	    	Integer message_timeout = Integer.valueOf(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("message_timeout", "-100"));
			  
	        switch (msg.what) {
	            case MessageProcessingService.MSG_REPLY_PROCESSED_MSG:
	            	Bundle b = msg.getData();
	            	
	            	// The MessageProcessingService can reply with one of two objects
	            	// - An object of PebbleMessage
	            	// - A string with pinyin
	            	if (b.containsKey(MessageProcessingService.KEY_RPL_PBL_MSG)) {
	            		PebbleMessage message = (PebbleMessage)b.getSerializable(MessageProcessingService.KEY_RPL_PBL_MSG);
	            		
	            		sendMessageToPebbleComm(message, message_timeout);
	            	} else if (b.containsKey(MessageProcessingService.KEY_RPL_STR)) {
	            		String message = (String)b.getString(MessageProcessingService.KEY_RPL_STR);
	            		
	            		sendMessageToPebbleComm(message, message_timeout);
	            	}
	                break;
	            default:
	                super.handleMessage(msg);
	        }
	    }
	    
	    private void sendMessageToPebbleComm(PebbleMessage message, int timeout) {
			  try {
		            Message msg = Message.obtain(null,
		                    PebbleCommService.MSG_SEND_DATA_TO_PEBBLE);
		            msg.replyTo = mMessengerPebbleComm;
		            
		            msg.arg1 = PebbleCommService.TYPE_DATA_PBL_MSG;
		            
		            msg.arg2 = timeout * 1000;
		            
		            Bundle b = new Bundle();
		            b.putSerializable(PebbleCommService.KEY_MESSAGE, message);
		            
		            msg.setData(b);
		            
		            mServicePebbleComm.send(msg);
		        } catch (RemoteException e) {
		            // In this case the service has crashed before we could even
		            // do anything with it; we can count on soon being
		            // disconnected (and then reconnected if it can be restarted)
		            // so there is no need to do anything here.
		        }
	    }
	    
	    private void sendMessageToPebbleComm(String message, int timeout) {
			  try {
		            Message msg = Message.obtain(null,
		                    PebbleCommService.MSG_SEND_DATA_TO_PEBBLE);
		            msg.replyTo = mMessengerPebbleComm;
		            
		            msg.arg1 = PebbleCommService.TYPE_DATA_STR;
		            
		            msg.arg2 = timeout;
		            
		            Bundle b = new Bundle();
		            b.putString(PebbleCommService.KEY_MESSAGE, message);
		            
		            msg.setData(b);
		            
		            mServicePebbleComm.send(msg);
		        } catch (RemoteException e) {
		            // In this case the service has crashed before we could even
		            // do anything with it; we can count on soon being
		            // disconnected (and then reconnected if it can be restarted)
		            // so there is no need to do anything here.
		        }
	    }
	}
	
	/**
	 * Target we publish for clients to send messages to IncomingHandler.
	 */
	final Messenger mMessengerPebbleComm = new Messenger(new PebbleCommIncomingHandler());
	final Messenger mMessengerMessageProcessing = new Messenger(new MessageProcessingIncomingHandler());
	
	/** Messenger for communicating with PebbleCommService. */
	Messenger mServicePebbleComm = null;
	/** Messenger for communicating with PebbleCommService. */
	Messenger mServiceMessageProcessing = null;
	
	/** Flag indicating whether we have called bind on the service. */
	boolean mPebbleCommIsBound;
	
	boolean mMessageProcessingIsBound;
	
	/**
	 * Class for interacting with the main interface of the service.
	 */
	private ServiceConnection mConnectionPebbleComm = new ServiceConnection() {
	    public void onServiceConnected(ComponentName className,
	            IBinder service) {
	        // This is called when the connection with the service has been
	        // established, giving us the service object we can use to
	        // interact with the service.  We are communicating with our
	        // service through an IDL interface, so get a client-side
	        // representation of that from the raw service object.
	        mServicePebbleComm = new Messenger(service);

	        mPebbleCommIsBound = true;
	        
	    }

	    public void onServiceDisconnected(ComponentName className) {
	        // This is called when the connection with the service has been
	        // unexpectedly disconnected -- that is, its process crashed.
	        mServicePebbleComm = null;
	        
	        mPebbleCommIsBound = false;
	    }
	};
	
	/**
	 * Class for interacting with the main interface of the service.
	 */
	private ServiceConnection mConnectionMessageProcessing = new ServiceConnection() {
	    public void onServiceConnected(ComponentName className,
	            IBinder service) {
	        // This is called when the connection with the service has been
	        // established, giving us the service object we can use to
	        // interact with the service.  We are communicating with our
	        // service through an IDL interface, so get a client-side
	        // representation of that from the raw service object.
	        mServiceMessageProcessing = new Messenger(service);

	        mMessageProcessingIsBound = true;
	        
	    }

	    public void onServiceDisconnected(ComponentName className) {
	        // This is called when the connection with the service has been
	        // unexpectedly disconnected -- that is, its process crashed.
	    	mServiceMessageProcessing = null;
	        
	    	mMessageProcessingIsBound = false;
	    }
	};
	
//	private ComponentName mComponentPebbleComm;
//	private ComponentName mComponentMessageProcessing;
	
	private void setupDefaultPreferences() {
		PreferenceManager.setDefaultValues(this, R.xml.pref_general, false);
	}
	
    @Override
    public void onServiceConnected()
    {
//		mComponentPebbleComm = startService(new Intent(HandleWeChat.this, PebbleCommService.class));
//		mComponentMessageProcessing = startService(new Intent(HandleWeChat.this, MessageProcessingService.class));
		
    	setupDefaultPreferences();
    	
		bindService(new Intent(HandleWeChat.this, PebbleCommService.class), mConnectionPebbleComm, Context.BIND_AUTO_CREATE);
		bindService(new Intent(HandleWeChat.this, MessageProcessingService.class), mConnectionMessageProcessing, Context.BIND_AUTO_CREATE);
		
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED;
        info.notificationTimeout = 100;
        info.feedbackType = AccessibilityEvent.TYPES_ALL_MASK;
        info.packageNames = new String[]{"com.tencent.mm"};;

        setServiceInfo(info);
    }
    
    @Override
    public void onDestroy() {
    	super.onDestroy();
    	
    	unbindService(mConnectionMessageProcessing);
    	unbindService(mConnectionPebbleComm);
    	
//    	stopService(new Intent(HandleWeChat.this, PebbleCommService.class));
//    	stopService(new Intent(HandleWeChat.this, MessageProcessingService.class));
    }
	
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event)
    {
    	// Getting rid of toast events
    	if (event.getClassName().toString().contains("android.widget.Toast")) return;
    	
		if (!mPebbleCommIsBound) {
			Log.d("PBL_HandleWeChat", "Comm Service not bound! Can't send message.");
			return;
		}
		
		Notification notification = (Notification) event.getParcelableData();
		
		  String originalMsg = notification.tickerText.toString();
		  
		  Message msg = Message.obtain(null,
		          MessageProcessingService.MSG_SEND_ORIGINAL_MSG);
		  msg.replyTo = mMessengerMessageProcessing;
		  
		  String message_type = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("message_type", "FAIL");
		  
		  if (message_type.equals("STD_NO_PINYIN")) {
			  msg.arg1 = MessageProcessingService.PROCESS_NO_PINYIN;
		  } else if (message_type.equals("STD_PINYIN")) {
			  msg.arg1 = MessageProcessingService.PROCESS_PINYIN;
		  } else if (message_type.equals("UNICODE_BITMAP")) {
			  msg.arg1 = MessageProcessingService.PROCESS_UNIFONT;
		  } else {
			  Log.d("HandleWeChat", "Cancelling send... could not find send type. Message type: "+message_type);
			  return;
		  }
		  
		  
		  Bundle b = new Bundle();
		  b.putString(MessageProcessingService.KEY_ORIGINAL_MSG, originalMsg);
		  
		  msg.setData(b);
		  
		  try {
			  Log.d("HandleWeChat", "Sending message to message processing service");
			  mServiceMessageProcessing.send(msg);
		  } catch (RemoteException e) {
			  Log.d("HandleWeChat", "Exception while sending data to the MessageProcessing");
		  }
    }

    
    
	@Override
	public void onInterrupt() {
		
	}

}
