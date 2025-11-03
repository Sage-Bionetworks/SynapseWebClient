package org.sagebionetworks.web.server.servlet.filter;

import static org.sagebionetworks.web.client.widget.entity.renderer.PDFPreviewWidget.PDF_JS_VIEWER_PREFIX;
import static org.sagebionetworks.web.server.servlet.filter.CORSFilter.ORIGIN_HEADER;
import static org.sagebionetworks.web.server.servlet.filter.CORSFilter.isAllowedSynapseSubdomain;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
    // SWC-4915: if pdf.js, the allow iframe from the same origin
    String origin = request.getHeader(ORIGIN_HEADER);
    // SWC-7536: if no origin header, use referer
    if (origin == null) {
      origin = request.getHeader(REFERER_HEADER);
    }
    // if allowed synapse subdomain, do not add X-Frame-Options header
    if (!isAllowedSynapseSubdomain(origin)) {
      String queryString = request.getQueryString();
      String requestWithQueryString = queryString == null
        ? request.getRequestURL().toString()
        : request.getRequestURL().toString() + "?" + queryString;
      if (requestWithQueryString.contains(PDF_JS_VIEWER_PREFIX)) {
        response.addHeader(X_FRAME_OPTIONS_HEADER, SAMEORIGIN);
      } else {
        response.addHeader(X_FRAME_OPTIONS_HEADER, DENY);
      }
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
