package org.sagebionetworks.web.server.servlet.filter;

import java.io.IOException;
import java.net.URL;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This filter redirects traffic heading to /xxx to an external URL (hard coded in SWC).  This overrides any Project Alias definition that may have the same value.
 */
public abstract class ExternalRedirectFilter implements Filter {

  @Override
  public void destroy() {
    // nothing to do
  }

  @Override
  public void doFilter(
    ServletRequest rqst,
    ServletResponse rspn,
    FilterChain chain
  ) throws IOException, ServletException {
    HttpServletResponse httpRsp = (HttpServletResponse) rspn;
    httpRsp.sendRedirect(getTargetURL());
  }

  @Override
  public void init(FilterConfig config) throws ServletException {}

  protected abstract String getTargetURL();
}
