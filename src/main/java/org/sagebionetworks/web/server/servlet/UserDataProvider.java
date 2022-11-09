package org.sagebionetworks.web.server.servlet;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.sagebionetworks.web.client.cookie.CookieKeys;

/**
 * Helper to get the UserData from the thread local cookie.
 *
 * @author jmhill
 *
 */
public class UserDataProvider {

  private static Logger logger = LogManager.getLogger(UserDataProvider.class);

  /**
   * Get the user data from the Cookies of the ThreadLocalRequest. Will return null if the cookie does
   * not exist.
   *
   * @return
   */
  public static String getThreadLocalUserToken(
    HttpServletRequest threadLocalRequest
  ) {
    if (threadLocalRequest == null) return null;
    Cookie[] cookies = threadLocalRequest.getCookies();
    if (cookies != null) {
      // Find the cookie
      for (Cookie cookie : cookies) {
        if (CookieKeys.USER_LOGIN_TOKEN.equals(cookie.getName())) {
          String value = cookie.getValue();
          if (value == null) return null;
          try {
            return URLDecoder.decode(value, "UTF-8");
          } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException(e);
          }
        }
      }
    }
    // Cannot find user login data in the cookies. Treating as anonymous.;
    return null;
  }
}
