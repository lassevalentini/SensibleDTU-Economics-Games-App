package dk.dtu.sensible.economicsgames;

public final class SharedConstants {

    /**
     * IP of Raman, for testing
     */
    public static final String DOMAIN_URL = "https://raman.compute.dtu.dk/lasse/";

    /**
     * Production address
     */
//    public static final String DOMAIN_URL = "https://www.sensible.dtu.dk/";
    
    public static final String DATABASE_NAME = "economics_games_db";
    
    
    public static final String CONNECTOR_URL = DOMAIN_URL + "sensible-dtu/connectors/connector_economics/";
    
    // TODO: better name
    public static final String STUDY_NAME = "Sensible economics games";

	public static String[] scopes = new String[]{"connector_economics.submit_data","push_notifications"};
	
	public static String getJoinedScopes() {
		String res = scopes[0];
		for (int i = 1; i < scopes.length; i++) {
			res += ","+scopes[i];	
		}
		return res;
	}
}
