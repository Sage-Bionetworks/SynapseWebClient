package org.sagebionetworks.web.server.servlet.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

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
