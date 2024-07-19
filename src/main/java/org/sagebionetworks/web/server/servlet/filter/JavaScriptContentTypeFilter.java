package org.sagebionetworks.web.server.servlet.filter;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class JavaScriptContentTypeFilter implements Filter {

  @Override
  public void init(FilterConfig config) throws ServletException {}

  @Override
  public void destroy() {
    // nothing to do
  }

  @Override
  public void doFilter(
    ServletRequest request,
    ServletResponse response,
    FilterChain chain
  ) throws IOException, ServletException {
    // Cast the request and response to HttpServletRequest and HttpServletResponse
    HttpServletRequest httpRequest = (HttpServletRequest) request;
    HttpServletResponse httpResponse = (HttpServletResponse) response;

    // Get the requested URI
    String requestURI = httpRequest.getRequestURI();

    // Check if the request is for a JavaScript file
    if (requestURI.endsWith(".js")) {
      // Some of our JavaScript files are UTF-8; without this, the browser may interpret them as ASCII
      httpResponse.setContentType("application/javascript; charset=UTF-8");
    }

    // Continue the filter chain
    chain.doFilter(request, response);
  }
}
