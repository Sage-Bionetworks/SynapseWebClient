package org.sagebionetworks.web.server.servlet.filter;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * CORS filter, Access-Control-Allow-Origin
 */
public class CORSFilter extends OncePerRequestFilter {

  public static final String ORIGIN_HEADER = "origin";
  public static final String DEFAULT_ALLOW_ORIGIN = "*";
  public static final String ACCESS_CONTROL_ALLOW_ORIGIN_HEADER =
    "Access-Control-Allow-Origin";
  public static final String ACCESS_CONTROL_ALLOW_CREDENTIALS_HEADER =
    "Access-Control-Allow-Credentials";

  // DNS Records pulled from: https://github.com/Sage-Bionetworks/Synapse-Stack-Builder/tree/develop/src/main/resources/templates/dns
  // Note that not all records need to have an explicitly-allowed origin; the subdomains in this list need only be the sites that should persist authentication state between *.synapse.org sites.
  // Since this list is manually maintained, new subdomains that should share auth state across *.synapse.org must be added to the list.
  public static final List<String> ALLOWED_SYNAPSE_SUBDOMAINS = Arrays.asList(
    "www",
    "staging",
    "tst",
    "signin",
    "staging-signin",
    "accounts.sagebionetworks",
    "staging.accounts.sagebionetworks",
    // Data portals
    "adknowledgeportal",
    "staging.adknowledgeportal",
    "alzdrugtool",
    "staging.alzdrugtool",
    "arkportal",
    "staging.arkportal",
    "bsmn",
    "staging.bsmn",
    "cancercomplexity",
    "staging.cancercomplexity",
    "www.cancercomplexity",
    "challenges",
    "staging.challenges",
    "covidrecoverycorpsresearcher",
    "staging.covidrecoverycorpsresearcher",
    "csbc-pson",
    "dhealth",
    "staging.dhealth",
    "eliteportal",
    "staging.eliteportal",
    "htan",
    "staging.htan",
    "nf",
    "staging.nf",
    "psychencode",
    "staging.psychencode",
    "shiny",
    "shinypro",
    "stopadportal",
    "staging.stopadportal"
  );

  public static final String SYNAPSE_ORG_SUFFIX = ".synapse.org";

  @Override
  protected void doFilterInternal(
    HttpServletRequest request,
    HttpServletResponse response,
    FilterChain filterChain
  ) throws ServletException, IOException {
    String allowOrigin = DEFAULT_ALLOW_ORIGIN;
    String origin = request.getHeader(ORIGIN_HEADER);
    if (origin != null && origin.toLowerCase().endsWith(SYNAPSE_ORG_SUFFIX)) {
      URL url = new URL(origin.toLowerCase());
      String subdomain = url
        .getHost()
        .substring(0, url.getHost().length() - SYNAPSE_ORG_SUFFIX.length());
      if (ALLOWED_SYNAPSE_SUBDOMAINS.contains(subdomain)) {
        allowOrigin = origin;
        response.addHeader(ACCESS_CONTROL_ALLOW_CREDENTIALS_HEADER, "true");
      }
    }

    response.addHeader(ACCESS_CONTROL_ALLOW_ORIGIN_HEADER, allowOrigin);
    if (
      request.getHeader("Access-Control-Request-Method") != null &&
      "OPTIONS".equals(request.getMethod())
    ) {
      response.addHeader(
        "Access-Control-Allow-Methods",
        "GET, POST, PUT, DELETE"
      );
      // response.addHeader("Access-Control-Allow-Headers",
      // "Authorization");
      response.addHeader("Access-Control-Allow-Headers", "Content-Type");
      response.addHeader("Access-Control-Max-Age", "1");
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
