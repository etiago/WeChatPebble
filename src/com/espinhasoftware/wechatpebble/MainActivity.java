package com.espinhasoftware.wechatpebble;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends Activity {

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
}
