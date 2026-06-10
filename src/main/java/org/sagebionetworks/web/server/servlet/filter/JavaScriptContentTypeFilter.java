package org.sagebionetworks.web.server.servlet.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

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
