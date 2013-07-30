package com.espinhasoftware.wechatpebble.db;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.espinhasoftware.wechatpebble.MainActivity;
import com.espinhasoftware.wechatpebble.model.Font;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHandler extends SQLiteOpenHelper {
	 
    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;
 
    // Database Name
    private static final String DATABASE_NAME = "unicodeStorage";
 
    // Contacts table name
    private static final String TABLE_HEX = "unicodeToHex";
 
    // Contacts Table Columns names
    private static final String KEY_CODEPOINT = "unicode_codepoint";
    private static final String KEY_HEX = "unicode_hex";
 
    private static final int FILE_LINECOUNT = 63446;
    
    private static Context _context;
 
    private SQLiteDatabase _db;
    
    private MainActivity _causingActivity;
    
    public DatabaseHandler(Context context, MainActivity causingActivity) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        
        DatabaseHandler._context = context;
        
        
        this._causingActivity = causingActivity;
    }
 
    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
    	final SQLiteDatabase dbThread = db;
    	
    	new Thread(new Runnable() {
    		private void addMultipleFontsx(List<Font> fonts, SQLiteDatabase db) {   	     	        
    			StringBuilder b = new StringBuilder();
    		
    			Font first = fonts.get(0);
    			b.append("INSERT INTO '"+TABLE_HEX+"' " +
    					 "SELECT '"+first.getCodepoint()+"' AS '"+KEY_CODEPOINT+"', " +
    					 		"'"+first.getHex()+"' AS '"+KEY_HEX+"' ");
    					 
    			fonts.remove(0);
    			
    	        for(Font f : fonts) {
    	        	b.append("UNION SELECT '"+f.getCodepoint()+"', '"+f.getHex()+"' ");
    	        }
    	        
    	        db.execSQL(b.toString());
    	        
    	        
    	    }
    		
            public void run() {
            	Log.i("Database","Starting db creation");
            	
            	_causingActivity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						_causingActivity.showWaitGenerateDB();
					}
				});
            	
                String CREATE_FONT_TABLE = "CREATE TABLE " + TABLE_HEX + "("
                        + KEY_CODEPOINT + " TEXT PRIMARY KEY," + KEY_HEX + " TEXT "+ ")";
                
                dbThread.execSQL(CREATE_FONT_TABLE);
                
                AssetManager assetManager = _context.getAssets();
        	    try {
        	        InputStream is = assetManager.open("unifont-5.1.20080820.hex");
        	        
        	        InputStreamReader sr = new InputStreamReader(is);
        	        
        	        BufferedReader br = new BufferedReader(sr);
        	        
        	        List<Font> fontBuff = new ArrayList<Font>(400);
        	        
        	        int count = 0;
        	        
        	        String line = null;
        	        while((line = br.readLine()) != null) {
        	        	String[] parts = line.split(":");
        	        	
        	        	fontBuff.add(new Font(parts[0], parts[1]));
        	        	
        	        	if (fontBuff.size() == 400) {
        	        		addMultipleFontsx(fontBuff, dbThread);
        	        		fontBuff.clear();

        	        		count += 400;
        	        	}
        	        	
        	        }
        	        count += fontBuff.size(); 
        	        
        	        if (fontBuff.size()>0) {
        	        	addMultipleFontsx(fontBuff, dbThread);
                		fontBuff.clear();
        	        }
        	        
        	        _causingActivity.runOnUiThread(new Runnable() {
						
						@Override
						public void run() {
							MainActivity m = (MainActivity)_causingActivity;
							m.hideWaitGenerateDB();
						}
					});
        	        Log.i("Database", "Finished inserting records"+count);
        	        
        	    } catch(Exception e) {
        	    	Log.e("Database", "Error inserting records! "+e.getMessage());
        	    }
            }
        }).start();
    	
    	
    }
 
    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_HEX);
 
        // Create tables again
        onCreate(db);
    }

    /**
     * All CRUD(Create, Read, Update, Delete) Operations
     */
 
    // Adding new contact
    public void addFont(Font font) {
    	List<Font> l = new ArrayList<Font>(1);
    	l.add(font);
    	
        addMultipleFonts(l);
    }
    
    public void addMultipleFonts(List<Font> fonts) {
    	//SQLiteDatabase db = this.getWritableDatabase();

    	addMultipleFonts(fonts, _db);
    }
    
    private void addMultipleFonts(List<Font> fonts, SQLiteDatabase db) {   	 
        ContentValues values = new ContentValues();
        
        for(Font f : fonts) {
        	values.put(KEY_CODEPOINT, f.getCodepoint()); // Contact Name
            values.put(KEY_HEX, f.getHex()); // Contact Phone
        }

        // Inserting Row
        db.insertOrThrow(TABLE_HEX, null, values);
        
    }
 
    // Getting single contact
    public Font getFont(String codepoint) {
        //SQLiteDatabase db = this.getReadableDatabase();
 
        Cursor cursor = _db.query(TABLE_HEX, new String[] { KEY_CODEPOINT,
                KEY_HEX }, KEY_CODEPOINT + "=?",
                new String[] { codepoint }, null, null, null, null);
        
        if (cursor != null) {
            if (cursor.moveToFirst()) {
		        Font font = new Font(cursor.getString(0), cursor.getString(1));
		        
		        // return contact
		        return font;
            }
    	}

        return null;
    }
    
    public boolean verifyIntegrity() {
		String tables = " SELECT name FROM sqlite_master " + " WHERE type='table'             "
		        + "   AND name LIKE '"+TABLE_HEX+"' ";
		
		 if (_db.rawQuery(tables, null).getCount()<1) {
			 return false;
		 }
         
    	String countQuery = "SELECT  * FROM " + TABLE_HEX;
		
        Cursor cursor = _db.rawQuery(countQuery, null);
        int cnt = cursor.getCount();
        cursor.close();
 
        // If the line count doesn't match, reload
        if (cnt != FILE_LINECOUNT) {
        	onUpgrade(_db, 1, 1);
        	return false;
        }
        
        return true;
    }
    
	public void open() throws SQLException {
		_db = this.getWritableDatabase();
	}

  public void close() {
    super.close();
  }
    // Getting All Contacts
    public List<Font> getAllFonts() {
        List<Font> fontList = new ArrayList<Font>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_HEX;
 
        //SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = _db.rawQuery(selectQuery, null);
 
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Font font = new Font(cursor.getString(0),cursor.getString(1));

                // Adding contact to list
                fontList.add(font);
            } while (cursor.moveToNext());
        }
 
        // return contact list
        return fontList;
    }
 
    // Updating single contact
    public int updateFont(Font font) {
        //SQLiteDatabase db = this.getWritableDatabase();
 
        ContentValues values = new ContentValues();
        values.put(KEY_CODEPOINT, font.getCodepoint());
 
        // updating row
        return _db.update(TABLE_HEX, values, KEY_CODEPOINT + " = ?",
                new String[] { font.getCodepoint() });
    }
 
    // Deleting single contact
    public void deleteFont(Font font) {
        //SQLiteDatabase db = this.getWritableDatabase();
        _db.delete(TABLE_HEX, KEY_CODEPOINT + " = ?",
                new String[] { font.getCodepoint() });
    }
 
 
    // Getting contacts Count
    public int getFontCount() {
        String countQuery = "SELECT  * FROM " + TABLE_HEX;
        //SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = _db.rawQuery(countQuery, null);
        int cnt = cursor.getCount();
        cursor.close();
 
        // return count
        return cnt;
    }
}
