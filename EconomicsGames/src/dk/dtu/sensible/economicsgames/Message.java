package dk.dtu.sensible.economicsgames;

import java.io.Serializable;

import android.util.Log;

public class Message implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8558026890757974575L;
	
	private static final String TAG = "Message";

	public Type type;
	
	public static enum Type {
		pdg,
		dg_responder,
		dg_proposer
	}
	
	public static Type typeFromString(String type) {
//		Log.d(TAG, type);
		if (type.equalsIgnoreCase("game-pdg")) {
//			Log.d(TAG, "pdg");
			return Type.pdg;
			
		} else if (type.equalsIgnoreCase("game-dg-proposer")) {
//			Log.d(TAG, "dg-prop");
			return Type.dg_proposer;
			
		} else if (type.equalsIgnoreCase("game-dg-responder")) {
//			Log.d(TAG, "dg-resp");
			return Type.dg_responder;
		}
		return null;
	}
	
	public String typeToString() {
		return typeToString(type);
	}

	public static String typeToString(Type type) {
		switch (type) {
			case pdg:
				return "game-pdg";
			case dg_proposer:
				return "game-dg-proposer";
			case dg_responder:
				return "game-dg-responder";
		}
		return "";
	}
}
