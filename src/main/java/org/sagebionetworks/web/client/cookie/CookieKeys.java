package org.sagebionetworks.web.client.cookie;

/**
 * The cookie key master. Don't tell the cookie monster!
 *
 * @author jmhill
 *
 */
public class CookieKeys {

	public static String USER_LOGGED_IN_RECENTLY = "org.sagebionetworks.security.user.login.recently";

	/**
	 * Login token
	 */
	public static String USER_LOGIN_TOKEN = "org.sagebionetworks.security.user.login.token";

	/**
	 * LinkedIn requestToken key
	 */
	public static String LINKEDIN = "org.sagebionetworks.synapse.linkedin";

	/**
	 * Showing UTC?
	 */
	public static String SHOW_DATETIME_IN_UTC = "org.sagebionetworks.synapse.datetime.utc";
	public static String COOKIES_ACCEPTED = "org.sagebionetworks.security.cookies.notification.okclicked";
	public static String PORTAL_CONFIG = "org.sagebionetworks.security.cookies.portal.config";

}
