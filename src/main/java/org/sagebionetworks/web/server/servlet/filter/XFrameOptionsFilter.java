package org.sagebionetworks.web.server.servlet.filter;

import static org.sagebionetworks.web.server.servlet.filter.CORSFilter.ORIGIN_HEADER;
import static org.sagebionetworks.web.server.servlet.filter.CORSFilter.isAllowedSynapseSubdomain;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * X-Frame-Options header
 */
public class XFrameOptionsFilter extends OncePerRequestFilter {

  public static final String X_FRAME_OPTIONS_HEADER = "X-Frame-Options";
  public static final String DENY = "DENY";
  public static final String SAMEORIGIN = "SAMEORIGIN";
  public static final String REFERER_HEADER = "Referer";

  @Override
  protected void doFilterInternal(
    HttpServletRequest request,
    HttpServletResponse response,
    FilterChain filterChain
  ) throws ServletException, IOException {
    String origin = request.getHeader(ORIGIN_HEADER);
    // SWC-7536: if no origin header, use referer
    if (origin == null) {
      origin = request.getHeader(REFERER_HEADER);
    }
    // if allowed synapse subdomain, do not add X-Frame-Options header
    if (!isAllowedSynapseSubdomain(origin)) {
      response.addHeader(X_FRAME_OPTIONS_HEADER, DENY);
    } else {
      // set to deprecated ALLOW-FROM value because app server will set if empty:
      // https://github.com/Sage-Bionetworks/Synapse-Stack-Builder/blob/develop/src/main/resources/templates/repo/ebextensions/security.conf#L1
      response.addHeader(X_FRAME_OPTIONS_HEADER, "ALLOW-FROM " + origin);
    }

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
