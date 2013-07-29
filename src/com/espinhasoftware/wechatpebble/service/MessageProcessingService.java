package com.espinhasoftware.wechatpebble.service;

import java.util.ArrayDeque;
import java.util.Deque;

import com.espinhasoftware.wechatpebble.MainActivity;
import com.espinhasoftware.wechatpebble.db.DatabaseHandler;
import com.espinhasoftware.wechatpebble.model.CharacterMatrix;
import com.espinhasoftware.wechatpebble.pebblecomm.PebbleMessage;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

public class MessageProcessingService extends Service {
	public static final String KEY_ORIGINAL_MSG = "KEY_ORIGINAL_MSG";
	
	public static final String KEY_RPL_PBL_MSG = "KEY_RPL_PBL_MSG";
	public static final String KEY_RPL_STR = "KEY_RPL_STR";
	
	public static final int MSG_SEND_ORIGINAL_MSG = 1;
	public static final int MSG_REPLY_PROCESSED_MSG = 2;
	
	public static final int PROCESS_UNIFONT = 1;
	public static final int PROCESS_PINYIN = 2;
	
	private DatabaseHandler db;
	
	/**
	 * Handler of incoming messages from PebbleCommService.
	 */
	class HandleWeChatIncomingHandler extends Handler {
	    @Override
	    public void handleMessage(Message msg) {
	        switch (msg.what) {
	            case MessageProcessingService.MSG_SEND_ORIGINAL_MSG:
	            	Log.d("MessageProcessing", "Got message from HandleWeChat");
	            	
	            	String originalMessage = msg.getData().getString(MessageProcessingService.KEY_ORIGINAL_MSG);
	            	
	            	Messenger replyChannel = msg.replyTo;
	            	Message reply = new Message();
	            	
	            	reply.what = MessageProcessingService.MSG_REPLY_PROCESSED_MSG;
	            	
	                if (msg.arg1 == MessageProcessingService.PROCESS_UNIFONT) {
	                	PebbleMessage pb = processMessage(originalMessage);
	                	
	                	Bundle b = new Bundle();
	                	b.putSerializable(MessageProcessingService.KEY_RPL_PBL_MSG, pb);
	                	
	                	reply.setData(b);
		                
		                try {
		                	Log.d("MessageProcessing", "Replied to HandleWeChat");
							replyChannel.send(reply);
						} catch (RemoteException e) {
							Log.d("MessageProcessing", "Exception replying to HandleWeChat");
						}
	                } else if (msg.arg1 == MessageProcessingService.PROCESS_PINYIN) {
	                	// TODO: Just process pinyin
	                }
	                break;
	            default:
	                super.handleMessage(msg);
	        }
	    }
	    
	    private PebbleMessage processMessage(String originalMessage) {
	    	PebbleMessage message = new PebbleMessage(getApplicationContext());
	    	
			// Clear the characterQueue, just in case
			Deque<CharacterMatrix> characterQueue = new ArrayDeque<CharacterMatrix>();
			
			while(originalMessage.length()>0) {
				int codepoint = originalMessage.codePointAt(0);

				String codepointStr = Integer.toHexString(codepoint).toUpperCase();
				codepointStr = ("0000" + codepointStr).substring(codepointStr.length());
				
				String originalHex = db.getFont(codepointStr).getHex();
				
				CharacterMatrix c = new CharacterMatrix(originalHex);
				
				characterQueue.add(c);

				originalMessage = originalMessage.substring(1);
			}

			message.setCharacterQueue(characterQueue);
	    	
			return message;
	    }
	}
	
	/**
	 * Target we publish for clients to send messages to IncomingHandler.
	 */
	final Messenger mMessengerHandleWeChat = new Messenger(new HandleWeChatIncomingHandler());
	
    @Override
    public void onCreate() {
    	db = new DatabaseHandler(getApplicationContext(), new MainActivity());
    	
    	db.open();
    }
    
	@Override
	public IBinder onBind(Intent arg0) {
		return mMessengerHandleWeChat.getBinder();
	}

}
