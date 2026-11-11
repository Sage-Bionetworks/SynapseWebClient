package org.sagebionetworks.web.unitserver.servlet.filter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.web.server.servlet.filter.CORSFilter;
import org.sagebionetworks.web.server.servlet.filter.ContentSecurityPolicyFilter;

public class ContentSecurityPolicyFilterTest {

  ContentSecurityPolicyFilter filter;

  @Mock
  HttpServletRequest mockRequest;

  @Mock
  HttpServletResponse mockResponse;

  @Mock
  FilterChain mockFilterChain;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    filter = new ContentSecurityPolicyFilter();
  }

  @Test
  public void setsHeaders() throws Exception {
    filter.doFilter(mockRequest, mockResponse, mockFilterChain);

    verify(mockResponse)
      .setHeader("Reporting-Endpoints", "csp-endpoint=\"/Portal/csp-report\"");
    verify(mockResponse)
      .setHeader(
        "Content-Security-Policy",
        ContentSecurityPolicyFilter.CONTENT_SECURITY_POLICY
      );
    verify(mockFilterChain).doFilter(mockRequest, mockResponse);
  }

  @Test
  public void policyContent() {
    String policy = ContentSecurityPolicyFilter.CONTENT_SECURITY_POLICY;

    // frame-ancestors allows exactly the same subdomains as XFrameOptionsFilter, plus our own site and localhost
    StringBuilder expectedFrameAncestors = new StringBuilder(
      "'self' localhost"
    );
    for (String subdomain : CORSFilter.ALLOWED_SYNAPSE_SUBDOMAINS) {
      expectedFrameAncestors
        .append(" https://")
        .append(subdomain)
        .append(CORSFilter.SYNAPSE_ORG_SUFFIX);
    }
    assertEquals(
      expectedFrameAncestors.toString(),
      ContentSecurityPolicyFilter.FRAME_ANCESTORS
    );
    assertTrue(
      policy.contains(
        "frame-ancestors " + ContentSecurityPolicyFilter.FRAME_ANCESTORS + ";"
      )
    );

    // report-to must name the endpoint defined in Reporting-Endpoints, or browsers send no reports
    assertTrue(
      policy.contains(
        "report-to " +
        ContentSecurityPolicyFilter.CSP_REPORTING_ENDPOINT_NAME +
        ";"
      )
    );
    assertTrue(
      ContentSecurityPolicyFilter.REPORTING_ENDPOINTS_VALUE.startsWith(
        ContentSecurityPolicyFilter.CSP_REPORTING_ENDPOINT_NAME + "="
      )
    );
  }
}
