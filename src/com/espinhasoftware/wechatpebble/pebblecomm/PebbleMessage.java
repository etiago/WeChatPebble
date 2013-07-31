package com.espinhasoftware.wechatpebble.pebblecomm;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.UUID;

import android.content.Context;

import com.espinhasoftware.wechatpebble.model.CharacterMatrix;

public class PebbleMessage implements Serializable {
	private static final long serialVersionUID = 4225546406355455963L;

	public static final UUID WECHATPEBBLE_UUID = UUID.fromString("FE2B571C-2853-4A00-B4BC-8D754FCF738F");

	public static final int PBL_MESSAGE = 1;
	public static final int PBL_RESET = 2;

	private Deque<CharacterMatrix> _characterQueue;
	
	public PebbleMessage(Deque<CharacterMatrix> characterQueue) {
		this._characterQueue = characterQueue;
	}
	
	public PebbleMessage() {
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
