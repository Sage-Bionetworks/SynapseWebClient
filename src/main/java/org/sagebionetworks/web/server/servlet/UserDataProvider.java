package org.sagebionetworks.web.server.servlet;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.schema.adapter.org.json.EntityFactory;
import org.sagebionetworks.web.client.cookie.CookieKeys;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.springframework.http.HttpHeaders;

/**
 * Helper to get the UserData from the thread local cookie.
 * 
 * @author jmhill
 *
 */
public class UserDataProvider {
	
	static private Log logger = LogFactory.getLog(UserDataProvider.class);
	
	/**
	 * The key used to put the session token in a header.
	 */
	
	public static final String SESSION_TOKEN_KEY = "sessionToken";
	/**
	 * Get the user data from the Cookies of the ThreadLocalRequest.
	 * Will return null if the cookie does not exist. 
	 * @return
	 */
	public static String getThreadLocalUserToken(HttpServletRequest threadLocalRequest) {
		if (threadLocalRequest == null)	return null;
		Cookie[] cookies = threadLocalRequest.getCookies();
		if (cookies != null) {
			// Find the cookie
			for (Cookie cookie : cookies) {
				if (CookieKeys.USER_LOGIN_TOKEN.equals(cookie.getName())) {
					String value = cookie.getValue();
					if (value == null)
						return null;
					try {
						return URLDecoder.decode(value, "UTF-8");
					} catch (UnsupportedEncodingException e) {
						throw new IllegalArgumentException(e);
					}

				}
			}
		}
		logger.info("Cannot find user login data in the cookies using cookie.name="	+ CookieKeys.USER_LOGIN_DATA);
		return null;
	}

	public static UserProfile getThreadLocalUserProfile(HttpServletRequest threadLocalRequest) {
		UserProfile currentUser = null;
		if (threadLocalRequest == null)	return null;
		Cookie[] cookies = threadLocalRequest.getCookies();
		if (cookies != null) {
			// Find the cookie
			for (Cookie cookie : cookies) {
				if (CookieKeys.USER_LOGIN_DATA.equals(cookie.getName())) {
					String value = cookie.getValue();
					if (value == null)
						return null;
					try {
						String userSessionDataJson = URLDecoder.decode(value, "UTF-8");
						if(userSessionDataJson != null) {
								UserSessionData userSessionData = EntityFactory.createEntityFromJSONString(userSessionDataJson, UserSessionData.class);
								if (userSessionData != null && userSessionData.getProfile() != null){
									currentUser = userSessionData.getProfile();
									break;
								}
						}
					} catch (Throwable e) {
						//invalid user
						e.printStackTrace();
					}

				}
			}
		}
		return currentUser;
	}

	
	
	/**
	 * Add the user data to the header if it exists.
	 * @param threadLocalRequest
	 * @param headers
	 */
	public static void addUserDataToHeader(HttpServletRequest threadLocalRequest, HttpHeaders headers){
		// First try to get the user data from the cookies
		String token = getThreadLocalUserToken(threadLocalRequest);
		if(token != null){
			headers.add(SESSION_TOKEN_KEY, token);
		}
	}

	/**
	 * Add the user data to the header if it exists.
	 * @param token
	 * @param headers
	 */
	public static void addUserDataToHeader(String token, HttpHeaders headers){
		// Add the token to the header if it is not null
		if(token != null){
			headers.add(SESSION_TOKEN_KEY, token);
		}
	}
}
