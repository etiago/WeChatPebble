package com.espinhasoftware.wechatpebble;

import com.espinhasoftware.wechatpebble.pebblecomm.PebbleMessage;
import com.espinhasoftware.wechatpebble.service.MessageProcessingService;
import com.espinhasoftware.wechatpebble.service.PebbleCommService;
import com.getpebble.android.kit.PebbleKit;

import com.getpebble.android.kit.util.PebbleDictionary;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends Activity {
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		Intent serviceIntent = new Intent(getApplicationContext(), MessageProcessingService.class);
		getApplicationContext().startService(serviceIntent);
	     
	     serviceIntent = new Intent(getApplicationContext(), PebbleCommService.class);
	     getApplicationContext().startService(serviceIntent);
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
	
	public boolean btnSettings_click(MenuItem mi) {
		Intent intent = new Intent(this, SettingsActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
		
		startActivity(intent);
		
		return true;
	}
	
	public boolean btnAbout_click(MenuItem mi) {
		//Toast.makeText(getApplicationContext(), "Blah ", Toast.LENGTH_SHORT).show();
		
		Toast.makeText(getApplicationContext(), "Copyright \u00A9 2013 - Tiago Espinha", Toast.LENGTH_SHORT).show();
		
		return true;
	}
	
	public void btnRefreshClick(View v) {
		PebbleDictionary data = new PebbleDictionary();
		
		//data.addString(PebbleMessage.PBL_MESSAGE, new String(send));
		data.addBytes(PebbleMessage.PBL_RESET, new byte[]{'a'});
		
		PebbleKit.sendDataToPebbleWithTransactionId(getApplicationContext(), PebbleMessage.WECHATPEBBLE_UUID, data, 2);
		Log.d("Main","Manual refresh");
	}
//	public void btnTestClick(View v) {
//
//		// Get input from UI
//		EditText mEdit = (EditText)findViewById(R.id.editText1);
//		String input = mEdit.getText().toString();
//		
//		// Clear the characterQueue, just in case
//		Deque<CharacterMatrix> characterQueue = new ArrayDeque<CharacterMatrix>();
//		
//		while(input.length()>0) {
//			int codepoint = input.codePointAt(0);
//
//			String codepointStr = Integer.toHexString(codepoint).toUpperCase();
//			codepointStr = ("0000" + codepointStr).substring(codepointStr.length());
//			
//			String originalHex = db.getFont(codepointStr).getHex();
//			
//			CharacterMatrix c = new CharacterMatrix(originalHex);
//			
//			characterQueue.add(c);
//
//			input = input.substring(1);
//		}
//
//		message.setCharacterQueue(characterQueue);
//		
//		//message.sendChunk(true);
//		//int width = 32;
//		
//
//		
//		
//		//PebbleKit.sendDataToPebble(getApplicationContext(), WECHATPEBBLE_UUID, data);
//		
//		
//	}
	
}
