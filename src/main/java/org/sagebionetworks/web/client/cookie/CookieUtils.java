package org.sagebionetworks.web.client.cookie;

/**
 * Simple helper utilities for storing data in cookies
 * 
 * @author jmhill
 *
 */
public class CookieUtils {
	/**
	 * Return the domain based on the current hostname (which may be the subdomain).
	 * 
	 * @param hostName
	 * @return Everything after the first occurrence of the character '.' (inclusive), if one exists. If
	 *         host name is undefined or does not contain the character, then this will return null.
	 */
	public static String getDomain(String hostName) {
		if (hostName == null || "127.0.0.1".equals(hostName)) {
			return null;
		}
		int dotIndex = hostName.indexOf('.');
		if (dotIndex > -1) {
			return hostName.substring(dotIndex);
		}
		return null;
	}

}
