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

  public static final String HOST_HEADER = "Host";
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
    "dev",
    "signin",
    "staging-signin",
    "dev-signin",
    "accounts.sagebionetworks",
    "staging.accounts.sagebionetworks",
    "accounts",
    "staging.accounts",
    "dev.accounts",
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
    "staging.stopadportal",
    "genie",
    "staging.genie",
    "b2ai.standards",
    "staging.b2ai.standards",
    "ampals",
    "staging.ampals",
    "classicportal",
    "staging.classicportal",
    "arcusbio",
    "staging.arcusbio"
  );

  public static final String SYNAPSE_ORG_SUFFIX = ".synapse.org";

  // given an origin header, return true if it ends with .synapse.org and ALLOWED_SYNAPSE_SUBDOMAINS contains the subdomain
  public static boolean isAllowedSynapseSubdomain(String origin) {
    if (origin != null) {
      try {
        URL url = new URL(origin.toLowerCase());
        String host = url.getHost();
        if (!host.endsWith(SYNAPSE_ORG_SUFFIX)) {
          return false;
        }
        String subdomain = host.substring(
          0,
          url.getHost().length() - SYNAPSE_ORG_SUFFIX.length()
        );
        return ALLOWED_SYNAPSE_SUBDOMAINS.contains(subdomain);
      } catch (java.net.MalformedURLException e) {
        // ignore malformed URL
      }
    }
    return false;
  }

  @Override
  protected void doFilterInternal(
    HttpServletRequest request,
    HttpServletResponse response,
    FilterChain filterChain
  ) throws ServletException, IOException {
    String allowOrigin = DEFAULT_ALLOW_ORIGIN;
    String origin = request.getHeader(ORIGIN_HEADER);
    if (isAllowedSynapseSubdomain(origin)) {
      allowOrigin = origin;
      response.addHeader(ACCESS_CONTROL_ALLOW_CREDENTIALS_HEADER, "true");
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
