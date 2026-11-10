package org.sagebionetworks.web.unitserver.servlet.filter;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
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
    assertTrue(policy.contains("report-to csp-endpoint"));
    assertTrue(policy.contains("report-uri /Portal/csp-report"));
    assertTrue(policy.contains("frame-ancestors 'self' localhost "));
    assertTrue(policy.contains(" https://www.synapse.org"));
    assertFalse(policy.contains("https://*.synapse.org"));
  }
}
