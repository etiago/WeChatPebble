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
	
	public void btnTestClick(View v) {
		PebbleDictionary data = new PebbleDictionary();
		data.addString(1, "blah");
		
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
