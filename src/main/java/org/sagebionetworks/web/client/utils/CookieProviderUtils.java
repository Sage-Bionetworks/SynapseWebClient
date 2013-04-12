package org.sagebionetworks.web.client.utils;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.cookie.CookieProvider;
/**
 * Extracted from DispalyUtils
 * @author jmhill
 *
 */
public class CookieProviderUtils {

	public static boolean isInTestWebsite(CookieProvider cookies) {
		return cookies.getCookie(DisplayUtils.SYNAPSE_TEST_WEBSITE_COOKIE_KEY) != null;
	}

	public static void setTestWebsite(boolean testWebsite, CookieProvider cookies) {
		if (testWebsite && !isInTestWebsite(cookies)) {
			//set the cookie
			cookies.setCookie(DisplayUtils.SYNAPSE_TEST_WEBSITE_COOKIE_KEY, "true");
		} else{
			cookies.removeCookie(DisplayUtils.SYNAPSE_TEST_WEBSITE_COOKIE_KEY);
		}
	}
}
