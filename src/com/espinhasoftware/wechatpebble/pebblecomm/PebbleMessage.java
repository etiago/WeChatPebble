package com.espinhasoftware.wechatpebble.pebblecomm;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.UUID;

import android.content.Context;
import android.util.Log;

import com.espinhasoftware.wechatpebble.model.CharacterMatrix;
import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;

public class PebbleMessage implements Serializable {
	private static final long serialVersionUID = 4225546406355455963L;

	public static final UUID WECHATPEBBLE_UUID = UUID.fromString("FE2B571C-2853-4A00-B4BC-8D754FCF738F");

	public static final int PBL_MESSAGE = 1;
	public static final int PBL_RESET = 2;

	private Deque<CharacterMatrix> _characterQueue;
	private Context _context;
	
	public PebbleMessage(Context c, Deque<CharacterMatrix> characterQueue) {
		this._context = c;
		this._characterQueue = characterQueue;
	}
	
	public PebbleMessage(Context c) {
		this._context = c;
		this._characterQueue = new ArrayDeque<CharacterMatrix>();
	}
	
	public Deque<CharacterMatrix> getCharacterQueue() {
		return _characterQueue;
	}

	public void setCharacterQueue(Deque<CharacterMatrix> characterQueue) {
		this._characterQueue = characterQueue;
	}

	public boolean hasMore() {
		if (_characterQueue.isEmpty()) {
			return false;
		}
		
		return _characterQueue.getFirst().getByteList().size() > 0;
	}
	
	
	
	public static String toBinary( byte[] bytes )
	{
	    StringBuilder sb = new StringBuilder(bytes.length * Byte.SIZE);
	    for( int i = 0; i < Byte.SIZE * bytes.length; i++ )
	        sb.append((bytes[i / Byte.SIZE] << i % Byte.SIZE & 0x80) == 0 ? '-' : '#');
	    return sb.toString();
	}
	
	public static String toBinary( Byte[] bytes )
	{
	    StringBuilder sb = new StringBuilder(bytes.length * Byte.SIZE);
	    for( int i = 0; i < Byte.SIZE * bytes.length; i++ )
	        sb.append((bytes[i / Byte.SIZE] << i % Byte.SIZE & 0x80) == 0 ? '-' : '#');
	    return sb.toString();
	}
}
