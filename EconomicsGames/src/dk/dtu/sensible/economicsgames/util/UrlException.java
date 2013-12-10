package dk.dtu.sensible.economicsgames.util;

public class UrlException extends Exception {
	private int code;
	
	public UrlException(int code) {
		super("HTTP error "+code);
		this.code = code;
	}

	public int getCode() {
		return code;
	}
}
