package org.sagebionetworks.web.server.servlet.filter;

import static org.sagebionetworks.web.server.servlet.SynapseClientBase.LOCAL_HOSTS_REGEX;
import static org.sagebionetworks.web.server.servlet.filter.CORSFilter.ALLOWED_SYNAPSE_SUBDOMAINS;
import static org.sagebionetworks.web.server.servlet.filter.CORSFilter.HOST_HEADER;
import static org.sagebionetworks.web.server.servlet.filter.CORSFilter.SYNAPSE_ORG_SUFFIX;

import com.google.inject.Inject;
import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.sagebionetworks.web.server.servlet.PortalPropertiesProvider;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * See SWC-7369: This filter validates the Host header in incoming HTTP requests.
 * It allows requests from localhost, or from specific subdomains of synapse.org.
 * If the Host header does not match the allowed patterns, it returns a 403 Forbidden response.
 * This is to prevent Host header attacks and ensure that only requests from trusted origins are processed.
 */
public class HostValidationFilter extends OncePerRequestFilter {

  public static final String INVALID_HOST_HEADER_MESSAGE =
    "Invalid Host Header";

  private final PortalPropertiesProvider propertiesProvider;

  @Inject
  public HostValidationFilter(PortalPropertiesProvider propertiesProvider) {
    this.propertiesProvider = propertiesProvider;
  }

  @Override
  protected void doFilterInternal(
    HttpServletRequest request,
    HttpServletResponse response,
    FilterChain filterChain
  ) throws ServletException, IOException {
    // Only filter if not in dev mode
    if (!propertiesProvider.getIsDevMode()) {
      String host = request.getHeader(HOST_HEADER);
      boolean allowed = false;
      if (host != null) {
        if (LOCAL_HOSTS_REGEX.matcher(host).matches()) {
          allowed = true;
        } else if (host.toLowerCase().endsWith(SYNAPSE_ORG_SUFFIX)) {
          String subdomain = host.substring(
            0,
            host.length() - SYNAPSE_ORG_SUFFIX.length()
          );
          if (subdomain.startsWith("cdn-")) {
            subdomain = subdomain.substring("cdn-".length()); // Remove 'cdn-' prefix
          }
          allowed =
            ALLOWED_SYNAPSE_SUBDOMAINS.contains(subdomain.toLowerCase());
        }
      }
      if (!allowed) {
        // If the host is not allowed, return a 403 Forbidden response
        response.sendError(
          HttpServletResponse.SC_FORBIDDEN,
          INVALID_HOST_HEADER_MESSAGE
        );
        return;
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
