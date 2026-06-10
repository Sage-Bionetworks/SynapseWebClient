package org.sagebionetworks.web.server.servlet.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpStatus;

/**
 * This filter implements a simplistic authorization scheme. It looks for the presence of a
 * particular cookie with a particular value which is used to indicate that the client was
 * authorized to make the request. If found, it allows the request to proceed. Otherwise it returns
 * 401 Not Authorized.
 *
 * @author deflaux
 *
 */
public class SimpleAuthFilter implements Filter {

  @Override
  public void destroy() {}

  @Override
  public void doFilter(
    ServletRequest servletRequest,
    ServletResponse servletResponse,
    FilterChain chain
  ) throws IOException, ServletException {
    HttpServletRequest request = (HttpServletRequest) servletRequest;
    HttpServletResponse response = (HttpServletResponse) servletResponse;
    Boolean requestIsAuthorized = false;

    Cookie[] cookies = request.getCookies();
    if (null != cookies) {
      for (int i = 0; i < cookies.length; i++) {
        if (
          SimpleAuthController.SIMPLE_AUTH_COOKIE_NAME.equals(
            cookies[i].getName()
          ) &&
          SimpleAuthController.SIMPLE_AUTH_COOKIE_VALUE.equals(
            cookies[i].getValue()
          )
        ) {
          requestIsAuthorized = true;
          break;
        }
      }
    }

    if (
      requestIsAuthorized ||
      request.getRequestURI().endsWith(SimpleAuthController.SIMPLE_AUTH_URI)
    ) {
      // proceed if the request is authorized or if someone is trying to
      // get authorized
      chain.doFilter(request, response);
    } else {
      response.setStatus(HttpStatus.FORBIDDEN.value());
      response.setHeader("WWW-Authenticate", "authenticate simpleAuth");
    }
  }

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {}
}
