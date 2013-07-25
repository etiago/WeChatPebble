package com.espinhasoftware.wechatpebble.db;

import java.util.ArrayList;
import java.util.List;

import com.espinhasoftware.wechatpebble.model.Font;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

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
 
    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
 
    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_HEX + "("
                + KEY_CODEPOINT + " TEXT PRIMARY KEY," + KEY_HEX + " TEXT "+ ")";
        
        db.execSQL(CREATE_CONTACTS_TABLE);
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
        SQLiteDatabase db = this.getWritableDatabase();
 
        ContentValues values = new ContentValues();
        values.put(KEY_CODEPOINT, font.getCodepoint()); // Contact Name
        values.put(KEY_HEX, font.getHex()); // Contact Phone
 
        // Inserting Row
        db.insert(TABLE_HEX, null, values);
        db.close(); // Closing database connection
    }
 
    // Getting single contact
    public Font getFont(String codepoint) {
        SQLiteDatabase db = this.getReadableDatabase();
 
        Cursor cursor = db.query(TABLE_HEX, new String[] { KEY_CODEPOINT,
                KEY_HEX }, KEY_CODEPOINT + "=?",
                new String[] { codepoint }, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();
 
        Font font = new Font(cursor.getString(0), cursor.getString(1));
        
        // return contact
        return font;
    }
     
//    // Getting All Contacts
//    public List<Font> getAllFonts() {
//        List<Font> contactList = new ArrayList<Font>();
//        // Select All Query
//        String selectQuery = "SELECT  * FROM " + TABLE_HEX;
// 
//        SQLiteDatabase db = this.getWritableDatabase();
//        Cursor cursor = db.rawQuery(selectQuery, null);
// 
//        // looping through all rows and adding to list
//        if (cursor.moveToFirst()) {
//            do {
//                Font font = new Font();
//                contact.setID(Integer.parseInt(cursor.getString(0)));
//                contact.setName(cursor.getString(1));
//                contact.setPhoneNumber(cursor.getString(2));
//                // Adding contact to list
//                contactList.add(contact);
//            } while (cursor.moveToNext());
//        }
// 
//        // return contact list
//        return contactList;
//    }
 
    // Updating single contact
    public int updateFont(Font font) {
        SQLiteDatabase db = this.getWritableDatabase();
 
        ContentValues values = new ContentValues();
        values.put(KEY_CODEPOINT, font.getCodepoint());
 
        // updating row
        return db.update(TABLE_HEX, values, KEY_CODEPOINT + " = ?",
                new String[] { font.getCodepoint() });
    }
 
    // Deleting single contact
    public void deleteFont(Font font) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_HEX, KEY_CODEPOINT + " = ?",
                new String[] { font.getCodepoint() });
        db.close();
    }
 
 
    // Getting contacts Count
    public int getFontCount() {
        String countQuery = "SELECT  * FROM " + TABLE_HEX;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        cursor.close();
 
        // return count
        return cursor.getCount();
    }
}
