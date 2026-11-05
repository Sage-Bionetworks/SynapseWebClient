package org.sagebionetworks.web.server.servlet.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Responds 413 if the request's Content-Length is larger than maxBytes. Requests without a Content-Length (e.g.
 * chunked) are not checked.
 */
public class MaxRequestBodySizeFilter implements Filter {

  private final long maxBytes;

  public MaxRequestBodySizeFilter(long maxBytes) {
    this.maxBytes = maxBytes;
  }

  @Override
  public void doFilter(
    ServletRequest request,
    ServletResponse response,
    FilterChain filterChain
  ) throws IOException, ServletException {
    if (((HttpServletRequest) request).getContentLengthLong() > maxBytes) {
      ((HttpServletResponse) response).setStatus(
          HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE
        );
      return;
    }
    filterChain.doFilter(request, response);
  }
}
