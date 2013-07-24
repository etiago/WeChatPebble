package com.espinhasoftware.wechatpebble;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.PebbleKit.PebbleAckReceiver;
import com.getpebble.android.kit.PebbleKit.PebbleNackReceiver;
import com.getpebble.android.kit.PebbleKit.PebbleDataReceiver;

import com.getpebble.android.kit.util.PebbleDictionary;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	private static final UUID WECHATPEBBLE_UUID = UUID.fromString("FE2B571C-2853-4A00-B4BC-8D754FCF738F");
	
	private static Queue<Byte> characterBytes = new ArrayBlockingQueue<Byte>(64);
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		PebbleKit.registerReceivedDataHandler(getApplicationContext(), new PebbleDataReceiver(WECHATPEBBLE_UUID) {
		
			@Override
			public void receiveData(Context arg0, int arg1, PebbleDictionary arg2) {
				// TODO Auto-generated method stub
				System.out.println("Got it!");
				
				
			}
		});
		
		PebbleAckReceiver ack = new PebbleAckReceiver(WECHATPEBBLE_UUID) {
			
			@Override
			public void receiveAck(Context arg0, int arg1) {
				// TODO Auto-generated method stub
				System.out.println("Ack! "+arg1);
				
				if (arg1 != 1) return;
				
				Byte d = characterBytes.poll();
				Byte d2 = characterBytes.poll();
				Byte d3 = characterBytes.poll();
				Byte d4 = characterBytes.poll();
				
				PebbleKit.sendAckToPebble(getApplicationContext(), arg1);
				
				if (d==null || d2 ==null || d3 == null || d4 == null) {
					System.out.println("Sending finish signal");
					PebbleDictionary data = new PebbleDictionary();

					data.addBytes(3, new byte[]{'a'});
					
					
					PebbleKit.sendDataToPebbleWithTransactionId(getApplicationContext(), WECHATPEBBLE_UUID, data, 2);
					return;
				}
				
				
				
				PebbleDictionary data = new PebbleDictionary();

				data.addBytes(1, new byte[]{d,d2,d3,d4});
				
				System.out.println("Sending "+toBinary(new byte[]{d,d2,d3,d4}));
				PebbleKit.sendDataToPebbleWithTransactionId(getApplicationContext(), WECHATPEBBLE_UUID, data, 1);
				
			}
		};
		
		PebbleNackReceiver nack = new PebbleNackReceiver(WECHATPEBBLE_UUID) {
			
			@Override
			public void receiveNack(Context arg0, int arg1) {
				// TODO Auto-generated method stub
				System.out.println("Nack! "+arg1);
			}
		};
		PebbleKit.registerReceivedAckHandler(getApplicationContext(), ack);
		PebbleKit.registerReceivedNackHandler(getApplicationContext(), nack);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public void btnAccessibility_click(View v) {
		startActivityForResult(new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS), 0);
	}
	
	public boolean btnAbout_click(MenuItem mi) {
		Toast.makeText(getApplicationContext(), "Copyright \u00A9 2013 - Tiago Espinha", Toast.LENGTH_SHORT).show();
		
		return true;
	}
	
	public byte[] hex2Byte(String str)
    {
       byte[] bytes = new byte[str.length() / 2];
       for (int i = 0; i < bytes.length; i++)
       {
          bytes[i] = (byte) Integer
                .parseInt(str.substring(2 * i, 2 * i + 2), 16);
       }
       return bytes;
    }
	
	public void btnTestClick(View v) {
		PebbleDictionary data = new PebbleDictionary();
		//String hao = "100010FC10041008FC102420242025FE24204820282010202820442084A00040";
		String hex = "100010FC10041008FC102420242025FE24204820282010202820442084A00040";
		// ai String hex = "000801FC7E10221011207FFE420282047FF8040007F00A10112020C043301C0E";
		// mei String hex = "000021F01110111081104210540E180013F82208E108211020A0204021B00E0E";
		// shi String hex = "01000100FFFE01003FF821083FF801003FF80108FFFE01083FF8010005000200";
		// wo String hex = "04400E50784808480840FFFE084008440A440C48183068220852088A2B061002";
		// String hex = "08800880088011FE110232043420502091281124122412221422102010A01040";
		//byte[] character = new byte[32];
		
		ByteBuffer character = ByteBuffer.allocateDirect(32);
		//character.order(ByteOrder.BIG_ENDIAN);
		
		characterBytes.clear();
		
		
		//int i = 0;
		while (hex.length() > 0) {
            String temp = hex.substring(0,4);
            
            hex = hex.substring(4);
            
            
            int value = Integer.parseInt(temp, 16);  
            
            
            
            byte[] end = new byte[2];
            
            end[1] = (byte) (value & 0xFF);
            end[0] = (byte) ((value >>> 8) & 0xFF);   
            
            end[1] = (byte)(~end[1] & 0xff);
            end[0] = (byte)(~end[0] & 0xff);
            
            end[1] = (byte)(Integer.reverse(end[1]) >>> 24);
            end[0] = (byte)(Integer.reverse(end[0]) >>> 24);
            
            
            
            character.put(end);
            
            characterBytes.add(end[0]);
            characterBytes.add(end[1]);
            characterBytes.add((byte)0);
            characterBytes.add((byte)0);
            
            System.out.println("Adding "+toBinary(end));
            //i++;
        }
		
		character.rewind();
		byte[] b = new byte[character.remaining()];
		character.get(b);
		
		byte d = characterBytes.poll();
		byte d2 = characterBytes.poll();
		byte d3 = characterBytes.poll();
		byte d4 = characterBytes.poll();
		
		data.addBytes(1,new byte[]{d,d2,d3,d4});
		data.addBytes(2, new byte[]{'a'});
		PebbleKit.sendDataToPebbleWithTransactionId(getApplicationContext(), WECHATPEBBLE_UUID, data, 1);
		
		
		//PebbleKit.sendDataToPebble(getApplicationContext(), WECHATPEBBLE_UUID, data);
		
		
	}
	static String toBinary( byte[] bytes )
	{
	    StringBuilder sb = new StringBuilder(bytes.length * Byte.SIZE);
	    for( int i = 0; i < Byte.SIZE * bytes.length; i++ )
	        sb.append((bytes[i / Byte.SIZE] << i % Byte.SIZE & 0x80) == 0 ? '0' : '1');
	    return sb.toString();
	}
}
