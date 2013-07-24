package com.espinhasoftware.wechatpebble;

import java.util.UUID;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.PebbleKit.PebbleAckReceiver;
import com.getpebble.android.kit.PebbleKit.PebbleNackReceiver;
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
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
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
		String hao = "100010FC10041008FC102420242025FE24204820282010202820442084A00040";
		
		byte[][] character = new byte[16][2];
		
		int i = 0;
		while (hao.length() > 0) {
            String temp = hao.substring(0,4);
            
            hao = hao.substring(4);
            
            int value = Integer.parseInt(temp, 16);  
            
            byte[] end = new byte[2];
            
            end[1] = (byte) (value & 0xFF);
            end[0] = (byte) ((value >>> 8) & 0xFF);   
            
            character[i] = end;
            
            i++;
        }
		
		data.addBytes(1, );
		PebbleAckReceiver ack = new PebbleAckReceiver(WECHATPEBBLE_UUID) {
			
			@Override
			public void receiveAck(Context arg0, int arg1) {
				// TODO Auto-generated method stub
				System.out.println("Ack! "+arg1);
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
		
		//PebbleKit.sendDataToPebble(getApplicationContext(), WECHATPEBBLE_UUID, data);
		PebbleKit.sendDataToPebbleWithTransactionId(getApplicationContext(), WECHATPEBBLE_UUID, data, 1);
		
	}
}
