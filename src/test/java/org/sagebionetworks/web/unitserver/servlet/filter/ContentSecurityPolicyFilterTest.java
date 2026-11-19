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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
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
    } else {
      systemProperties.remove(IS_DEV_MODE);
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
    List<String> scriptSrc = parse(
      createFilter(false).getReportOnlyPolicy("staging.synapse.org")
    )
      .get("script-src");

    // Exactly these: a new script source must be a deliberate change here. No wildcard hosts, and nothing that serves
    // user-uploaded or arbitrary content (S3, Synapse storage, Google Cloud Storage, data:, blob:)
    assertEquals(
      Set.of(
        "'self'",
        "https://cdn-staging.synapse.org",
        "'unsafe-inline'",
        "'unsafe-eval'",
        "https://www.googletagmanager.com",
        "https://www.google.com",
        "https://googleads.g.doubleclick.net",
        "https://www.googleadservices.com",
        "https://maps.googleapis.com",
        "https://t.contentsquare.net",
        "https://script.hj.contentsquare.net",
        "https://sagebionetworks.jira.com"
      ),
      new HashSet<>(scriptSrc)
    );
    assertEquals(scriptSrc.size(), new HashSet<>(scriptSrc).size());
  }

  @Test
  public void siteWithoutCdnGetsTheSamePolicyWithoutTheCdnSource() {
    ContentSecurityPolicyFilter filter = createFilter(false);

    // A local build (no CDN) differs from staging only by the CDN origin, in every directive that has it
    assertEquals(
      filter
        .getReportOnlyPolicy("staging.synapse.org")
        .replace(" https://cdn-staging.synapse.org", ""),
      filter.getReportOnlyPolicy("127.0.0.1:8888")
    );
  }

  // Whether an enforced source list allows a Report-Only source: exact match, https://* for https:, or the same
  // host (scheme and path ignored, since the enforced policy lists scheme-less hosts), including *. wildcards
  private static boolean allows(List<String> enforced, String source) {
    if (enforced.contains(source)) return true;
    if (source.equals("https:")) return enforced.contains("https://*");
    String host = source
      .replaceFirst("^[a-z]+://", "")
      .replaceFirst("/.*$", "");
    for (String allowed : enforced) {
      String allowedHost = allowed.replaceFirst("^[a-z]+://", "");
      if (
        allowedHost.equals(host) ||
        (allowedHost.startsWith("*.") &&
          host.endsWith(allowedHost.substring(1)))
      ) {
        return true;
      }
    }
    return false;
  }

  @Test
  public void enforcedPolicyAllowsEverythingTheAllowlistAllows() {
    // Until the allowlist is enforced, a source it allows but the enforced policy blocks is silently broken.
    // Known exception: video from any https host (the enforced default-src lists hosts, so external video is blocked
    // there and reported with disposition "enforce")
    Set<String> exceptions = Set.of("media-src https:");
    for (boolean isDevMode : new boolean[] { false, true }) {
      ContentSecurityPolicyFilter filter = createFilter(isDevMode);
      Map<String, List<String>> enforced = parse(filter.getEnforcedPolicy());
      Map<String, List<String>> reportOnly = parse(
        filter.getReportOnlyPolicy(
          isDevMode ? "127.0.0.1:8888" : "www.synapse.org"
        )
      );
      reportOnly.forEach((directive, sources) -> {
        // The enforced policy doesn't set these, so they don't fall back to default-src and nothing is blocked
        if (
          Set
            .of("base-uri", "form-action", "report-to", "report-uri")
            .contains(directive)
        ) return;
        String enforcedDirective = enforced.containsKey(directive)
          ? directive
          : "default-src";
        for (String source : sources) {
          if (
            source.equals("'none'") ||
            exceptions.contains(directive + " " + source)
          ) continue;
          assertTrue(
            "dev=" +
            isDevMode +
            ": Report-Only allows " +
            directive +
            " " +
            source +
            " but the enforced " +
            enforcedDirective +
            " blocks it",
            allows(enforced.get(enforcedDirective), source)
          );
        }
      });
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
