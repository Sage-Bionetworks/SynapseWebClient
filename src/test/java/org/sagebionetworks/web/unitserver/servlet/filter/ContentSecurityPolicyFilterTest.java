package org.sagebionetworks.web.unitserver.servlet.filter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sagebionetworks.web.server.StackEndpoints.IS_DEV_MODE;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.PropertyProvider;
import org.sagebionetworks.web.server.servlet.filter.ContentSecurityPolicyFilter;

public class ContentSecurityPolicyFilterTest {

  @Mock
  HttpServletRequest mockRequest;

  @Mock
  HttpServletResponse mockResponse;

  @Mock
  FilterChain mockFilterChain;

  @Mock
  PropertyProvider mockPropertyProvider;

  Properties systemProperties;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    systemProperties = new Properties();
    when(mockPropertyProvider.getSystemProperties())
      .thenReturn(systemProperties);
  }

  private ContentSecurityPolicyFilter createFilter(boolean isDevMode) {
    if (isDevMode) {
      systemProperties.setProperty(IS_DEV_MODE, "true");
    }
    return new ContentSecurityPolicyFilter(mockPropertyProvider);
  }

  // directive name -> sources
  private static Map<String, List<String>> parse(String policy) {
    Map<String, List<String>> directives = new HashMap<>();
    for (String directive : policy.split("; ")) {
      String[] parts = directive.split(" ");
      directives.put(
        parts[0],
        Arrays.asList(Arrays.copyOfRange(parts, 1, parts.length))
      );
    }
    return directives;
  }

  @Test
  public void setsHeaders() throws Exception {
    ContentSecurityPolicyFilter filter = createFilter(false);
    when(mockRequest.getHeader("Host")).thenReturn("www.synapse.org");

    filter.doFilter(mockRequest, mockResponse, mockFilterChain);

    verify(mockResponse)
      .setHeader("Reporting-Endpoints", "csp-endpoint=\"/Portal/csp-report\"");
    verify(mockResponse)
      .setHeader("Content-Security-Policy", filter.getEnforcedPolicy());
    verify(mockResponse)
      .setHeader(
        "Content-Security-Policy-Report-Only",
        filter.getReportOnlyPolicy("www.synapse.org")
      );
    verify(mockFilterChain).doFilter(mockRequest, mockResponse);
  }

  @Test
  public void reportOnlyScriptSourcesAreTheSiteCdnAndNamedHosts() {
    ContentSecurityPolicyFilter filter = createFilter(false);

    List<String> scriptSrc = parse(
      filter.getReportOnlyPolicy("staging.synapse.org")
    )
      .get("script-src");

    assertTrue(scriptSrc.contains("'self'"));
    assertTrue(scriptSrc.contains("https://cdn-staging.synapse.org"));
    assertFalse(scriptSrc.contains("https://cdn-www.synapse.org"));
    // Hosts and schemes that serve user-uploaded or arbitrary content must never supply script
    for (String source : scriptSrc) {
      assertFalse(source, source.contains("amazonaws.com"));
      assertFalse(source, source.contains("sagebase.org"));
      assertFalse(source, source.contains("*.googleapis.com"));
      assertFalse(source, source.equals("data:") || source.equals("blob:"));
      assertFalse(source, source.equals("https:") || source.equals("*"));
    }

    // A site without a CDN (e.g. a local build) gets no CDN source
    List<String> localScriptSrc = parse(
      filter.getReportOnlyPolicy("127.0.0.1:8888")
    )
      .get("script-src");
    for (String source : localScriptSrc) {
      assertFalse(source, source.contains("cdn-"));
    }
  }

  @Test
  public void reportOnlyDirectives() {
    Map<String, List<String>> policy = parse(
      createFilter(false).getReportOnlyPolicy("www.synapse.org")
    );

    assertEquals(List.of("'self'"), policy.get("default-src"));
    assertEquals(List.of("'none'"), policy.get("object-src"));
    assertEquals(List.of("'self'"), policy.get("base-uri"));
    assertEquals(List.of("'self'"), policy.get("form-action"));
    assertEquals(List.of("csp-endpoint"), policy.get("report-to"));
    assertEquals(List.of("/Portal/csp-report"), policy.get("report-uri"));
  }

  @Test
  public void vimeoEmbedsAllowedInBothPolicies() {
    ContentSecurityPolicyFilter filter = createFilter(false);

    // SWC-8150: the video widget embeds https://player.vimeo.com/video/<id>
    assertTrue(
      parse(filter.getEnforcedPolicy())
        .get("default-src")
        .contains("player.vimeo.com")
    );
    assertTrue(
      parse(filter.getReportOnlyPolicy("www.synapse.org"))
        .get("frame-src")
        .contains("https://player.vimeo.com")
    );
  }

  @Test
  public void productionPoliciesHaveNoDevelopmentHosts() {
    ContentSecurityPolicyFilter filter = createFilter(false);

    for (String policy : List.of(
      filter.getEnforcedPolicy(),
      filter.getReportOnlyPolicy("www.synapse.org")
    )) {
      assertFalse(policy, policy.contains("localhost"));
      assertFalse(policy, policy.contains("127.0.0.1"));
      // 'self' keeps the e2e build (production mode at http://127.0.0.1:8888) able to call its own server
      assertTrue(parse(policy).get("connect-src").contains("'self'"));
    }
  }

  @Test
  public void devModeAddsDevelopmentHostsToBothPolicies() {
    ContentSecurityPolicyFilter filter = createFilter(true);

    Map<String, List<String>> enforced = parse(filter.getEnforcedPolicy());
    assertTrue(enforced.get("default-src").contains("127.0.0.1:9876"));
    assertTrue(enforced.get("default-src").contains("localhost:5173"));
    assertTrue(enforced.get("connect-src").contains("ws://localhost:5173"));
    assertTrue(enforced.get("connect-src").contains("http://localhost:8080"));

    Map<String, List<String>> reportOnly = parse(
      filter.getReportOnlyPolicy("127.0.0.1:8888")
    );
    assertTrue(reportOnly.get("script-src").contains("http://127.0.0.1:9876"));
    assertTrue(reportOnly.get("script-src").contains("http://localhost:5173"));
    assertTrue(reportOnly.get("connect-src").contains("ws://localhost:5173"));
  }

  @Test
  public void noSiteMayFrameTheWebClient() {
    // Production first: createFilter(true) sets the dev mode property for the rest of the test
    for (boolean isDevMode : new boolean[] { false, true }) {
      ContentSecurityPolicyFilter filter = createFilter(isDevMode);
      assertEquals(
        List.of("'none'"),
        parse(filter.getEnforcedPolicy()).get("frame-ancestors")
      );
      assertEquals(
        List.of("'none'"),
        parse(filter.getReportOnlyPolicy("www.synapse.org"))
          .get("frame-ancestors")
      );
    }
  }
}
