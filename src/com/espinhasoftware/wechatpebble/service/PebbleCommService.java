package com.espinhasoftware.wechatpebble.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.json.JSONArray;
import org.json.JSONObject;

import com.espinhasoftware.wechatpebble.model.CharacterMatrix;
import com.espinhasoftware.wechatpebble.pebblecomm.PebbleMessage;
import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.PebbleKit.PebbleAckReceiver;
import com.getpebble.android.kit.PebbleKit.PebbleDataReceiver;
import com.getpebble.android.kit.PebbleKit.PebbleNackReceiver;
import com.getpebble.android.kit.util.PebbleDictionary;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;

public class PebbleCommService extends Service {
	private static PebbleMessage message;
	private static int timeout;

    /**
     * Command to the service to register a client, receiving callbacks
     * from the service.  The Message's replyTo field must be a Messenger of
     * the client where callbacks should be sent.
     */
    static final int MSG_SEND_DATA_TO_PEBBLE = 1;
    static final int MSG_SEND_FINISHED = 2;
    
    static final int TYPE_DATA_PBL_MSG = 1;
    static final int TYPE_DATA_STR = 2;
    
    static final String KEY_MESSAGE = "KEY_MESSAGE";

    /**
     * Handler of incoming messages from clients.
     */
    static class HandleWeChatIncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case PebbleCommService.MSG_SEND_DATA_TO_PEBBLE:
                	if (msg.arg1 == PebbleCommService.TYPE_DATA_PBL_MSG) {
                		PebbleMessage pb = (PebbleMessage)msg.getData().getSerializable(PebbleCommService.KEY_MESSAGE);
                		
                		PebbleCommService.timeout = msg.arg2;
                		
                		sendAlertToPebble(pb, true);
                	} else if (msg.arg1 == PebbleCommService.TYPE_DATA_STR) {
                		String s = msg.getData().getString(PebbleCommService.KEY_MESSAGE);
                		
                		sendAlertToPebble(s);
                	}
                	break;
                default:
                    super.handleMessage(msg);
                    break;
            }
        }
    }

    /**
     * Target we publish for clients to send messages to IncomingHandler.
     */
    final Messenger mMessengerHandleWeChat = new Messenger(new HandleWeChatIncomingHandler());
    static Context _context;
    
    @Override
    public void onCreate() {
    	message = new PebbleMessage();
    	_context = getApplicationContext();
    	
    	PebbleKit.registerReceivedDataHandler(getApplicationContext(), new PebbleDataReceiver(PebbleMessage.WECHATPEBBLE_UUID) {
    		@Override
    		public void receiveData(Context arg0, int arg1, PebbleDictionary arg2) {
    			Log.d("PB_RECEIVE", "Got data from Pebble");
    		}
    	});
    	
    	PebbleAckReceiver ack = new PebbleAckReceiver(PebbleMessage.WECHATPEBBLE_UUID) {
    		@Override
    		public void receiveAck(Context arg0, int arg1) {
    			Log.d("PB_ACK", "Pebble sent an ACK. Transaction ID: " + arg1);
    			
    			if (arg1 != 1) return;
    			
    			PebbleKit.sendAckToPebble(getApplicationContext(), arg1);
    			
    			
    			if (!message.hasMore()) {
    				System.out.println("Sending finish signal");
    				PebbleDictionary data = new PebbleDictionary();

    				data.addBytes(3, new byte[]{'a'});
    				
    				PebbleKit.sendDataToPebbleWithTransactionId(getApplicationContext(), PebbleMessage.WECHATPEBBLE_UUID, data, 2);
    				
    				new Thread(new Runnable() {
    					
    					@Override
    					public void run() {
    						try {
    							Thread.sleep(timeout);
    						} catch (InterruptedException e) {
    							Log.d("HandleWeChat", "Problem while sleeping");
    						}
    						PebbleKit.closeAppOnPebble(getApplicationContext(), PebbleMessage.WECHATPEBBLE_UUID);
    					}
    				}).run();

    				return;
    			}
    			
    			sendChunk(false);
    		}
    	};
    	
    	PebbleNackReceiver nack = new PebbleNackReceiver(PebbleMessage.WECHATPEBBLE_UUID) {
    		
    		@Override
    		public void receiveNack(Context arg0, int arg1) {
    			Log.d("PB_NACK", "Pebble sent an NACK. Transaction ID:" + arg1);
    		}
    	};
    	PebbleKit.registerReceivedAckHandler(getApplicationContext(), ack);
    	PebbleKit.registerReceivedNackHandler(getApplicationContext(), nack);
    }

    @Override
    public void onDestroy() {

    }

    /**
     * When binding to the service, we return an interface to our messenger
     * for sending messages to the service.
     */
    @Override
    public IBinder onBind(Intent intent) {
        return mMessengerHandleWeChat.getBinder();
    }

	  
	    
	    private static void sendAlertToPebble(String alert) {
	    	final Intent i = new Intent("com.getpebble.action.SEND_NOTIFICATION");

	        final Map<String, String> data = new HashMap<String, String>();
	        data.put("title", "WeChat");
	        data.put("body", alert);
	        final JSONObject jsonData = new JSONObject(data);
	        final String notificationData = new JSONArray().put(jsonData).toString();

	        i.putExtra("messageType", "PEBBLE_ALERT");
	        i.putExtra("sender", "MyAndroidApp");
	        i.putExtra("notificationData", notificationData);

	        _context.sendBroadcast(i);
	    }
	    
	    private static void sendAlertToPebble(PebbleMessage pm, boolean reset) {
	    	PebbleKit.startAppOnPebble(_context, PebbleMessage.WECHATPEBBLE_UUID);
      	  	try {
      	  		Thread.currentThread().sleep(1000);
      	  	} catch (InterruptedException e) {
      	  		e.printStackTrace();
      	  	}
      	  	
      	  	message = pm;
      	  	sendChunk(reset);
	    }
	    
	    public static boolean sendChunk(boolean reset) {
			int bytesWide = 18;
			
			// Defines how many bytes wide we have available
			int bytesLeft = 18;
			
			// Byte array for this particular sending
			// N.B.! The Pebble requires all images to have widths
			// multiple of 4. It has a 144 pixel screen which is
			// 18 bytes, but we need to effectively send 20 bytes
			// per line. 18 character bytes + padding.
			byte[] send = new byte[bytesWide+2];
			
			
			Stack<CharacterMatrix> putBack = new Stack<CharacterMatrix>();
			while (bytesLeft > 0) {
				CharacterMatrix current = message.getCharacterQueue().pollFirst();
				
				if (current == null) {
					break;
				}
				/*
				 *  0.0625 is the ratio between the number of bytes in total
				 *  and the size length of each character.
				 *  32 bytes (2 wide * 16 high)
				 *  16 bytes (1 wide * 16 high)
				 */
				int widthBytes = current.getWidthBytes();
				
				// Next character doesn't fit so put it back and return
				if (widthBytes > bytesLeft) {
					putBack.add(current);
					break;
				}
				
				List<Byte> b = current.getByteList();
			
				while(widthBytes>0) {
					int bytePos = Math.abs(bytesLeft - bytesWide);
					
					send[bytePos] = b.remove(0);
					
					bytesLeft--;
					widthBytes--;
				}
				
				if (b.size() > 0) {
					putBack.add(current);
				}
				
			}
			
			// If the putback stack has elements, we should add them
			// at the head of the characterQueue deque
			while(!putBack.empty()) {
				message.getCharacterQueue().addFirst(putBack.pop());
			}
			//putBack.clear();
			
			// If there are any bytes left, in case the next character didn't fit
			if (bytesLeft > 0) {
				int bytePos = Math.abs(bytesLeft - bytesWide);
				
				
				for(int p=bytePos;p<bytesWide+2;p++) {
					send[p] = (byte) 0xff;
				}
			}
			
			Log.d("PebbleMessage", "Sending " + PebbleMessage.toBinary(send));
			
			PebbleDictionary data = new PebbleDictionary();
			data.addBytes(PebbleMessage.PBL_MESSAGE,send);
			//data.addString(PebbleMessage.PBL_MESSAGE, new String(send));
			
			if (reset) {
				data.addBytes(PebbleMessage.PBL_RESET, new byte[]{'a'});
			}
			PebbleKit.sendDataToPebbleWithTransactionId(_context, PebbleMessage.WECHATPEBBLE_UUID, data, 1);
			
			return message.hasMore();
		}
	    ////////
}
