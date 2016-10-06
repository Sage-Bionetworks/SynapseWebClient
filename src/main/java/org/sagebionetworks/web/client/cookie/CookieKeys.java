package org.sagebionetworks.web.client.cookie;

/**
 * The cookie key master.  Don't tell the cookie monster!
 *
 * @author jmhill
 *
 */
public class CookieKeys {

	/**
	 * Key for the selected filters applied on the datasets table.
	 */
	public static String APPLIED_DATASETS_FILTERS = "org.sagebionetworks.datasets.applied.filters";

	public static String USER_LOGGED_IN_RECENTLY = "org.sagebionetworks.security.user.login.recently";
	
	/**
	 * Login token
	 */
	public static String USER_LOGIN_TOKEN = "org.sagebionetworks.security.user.login.token";

	/**
	 * Last Place in the app
	 */
	public static String LAST_PLACE = "org.sagebionetworks.synapse.place.last.place";
	
	/**
	 * Current Place in the app
	 */
	public static String CURRENT_PLACE = "org.sagebionetworks.synapse.place.current.place";

	/**
	 * LinkedIn requestToken key
	 */
	public static String LINKEDIN = "org.sagebionetworks.synapse.linkedin";
}
