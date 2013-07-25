package com.espinhasoftware.wechatpebble.model;

public class Font {

	private String _codepoint;
	private String _hex;
	
	public Font(String codepoint, String hex) {
		this._hex = hex;
		this._codepoint = codepoint;
	}

	public String getCodepoint() {
		return _codepoint;
	}

	public void setCodepoint(String unicode) {
		this._codepoint = unicode;
	}

	public String getHex() {
		return _hex;
	}

	public void setHex(String hex) {
		this._hex = hex;
	}

}
