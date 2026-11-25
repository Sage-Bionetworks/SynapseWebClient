package org.sagebionetworks.web.server.servlet.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * X-Frame-Options header. No site may frame the web client, including the web client itself (the CSP also sends
 * frame-ancestors 'none'; this covers browsers that don't support it).
 */
public class XFrameOptionsFilter extends OncePerRequestFilter {

  public static final String X_FRAME_OPTIONS_HEADER = "X-Frame-Options";
  public static final String DENY = "DENY";

  @Override
  protected void doFilterInternal(
    HttpServletRequest request,
    HttpServletResponse response,
    FilterChain filterChain
  ) throws ServletException, IOException {
    response.addHeader(X_FRAME_OPTIONS_HEADER, DENY);
    filterChain.doFilter(request, response);
  }

  public void testFilter(
    HttpServletRequest mockRequest,
    HttpServletResponse mockResponse,
    FilterChain mockFilterChain
  ) throws ServletException, IOException {
    doFilterInternal(mockRequest, mockResponse, mockFilterChain);
  }
}
