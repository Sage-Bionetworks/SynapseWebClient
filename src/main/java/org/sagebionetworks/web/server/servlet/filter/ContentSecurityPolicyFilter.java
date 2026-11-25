package org.sagebionetworks.web.server.servlet.filter;

import static org.sagebionetworks.web.server.StackEndpoints.IS_DEV_MODE;
import static org.sagebionetworks.web.server.servlet.filter.CORSFilter.HOST_HEADER;

import com.google.inject.Inject;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.sagebionetworks.PropertyProvider;
import org.sagebionetworks.web.server.servlet.filter.ContentSecurityPolicy.AllowedSource;
import org.sagebionetworks.web.shared.WebConstants;

/**
 * This filter adds the Content Security Policy to every response, and tells the browser to send violation reports to
 * the CSP report endpoint (CSPReportServlet).
 *
 * Two policies are sent:
 * - Content-Security-Policy: the permissive policy formerly in the Portal.html <meta> tag, enforced.
 * - Content-Security-Policy-Report-Only: the per-directive allowlist in ContentSecurityPolicy, which only reports.
 *   It replaces the enforced policy once its reports have been reviewed (SWC-8037).
 * Development hosts are added to both only when the server runs in development mode. Both send
 * frame-ancestors 'none': no site may frame the web client, including the web client itself.
 */
public class ContentSecurityPolicyFilter implements Filter {

  // Must match between Reporting-Endpoints and the policy's report-to, or the browser silently sends no reports
  public static final String CSP_REPORTING_ENDPOINT_NAME = "csp-endpoint";
  public static final String CSP_REPORT_PATH =
    "/Portal/" + WebConstants.CSP_REPORT_SERVLET;
  public static final String REPORTING_ENDPOINTS_HEADER = "Reporting-Endpoints";
  // Relative URL, resolved by the browser against the page URL
  public static final String REPORTING_ENDPOINTS_VALUE =
    CSP_REPORTING_ENDPOINT_NAME + "=\"" + CSP_REPORT_PATH + "\"";
  public static final String CSP_HEADER = "Content-Security-Policy";
  public static final String CSP_REPORT_ONLY_HEADER =
    "Content-Security-Policy-Report-Only";

  // Enforced policy. Every source is in default-src, so each host may supply any kind of content
  public static final String ENFORCED_DEFAULT_SRC =
    "'self' data: blob: 'unsafe-inline' 'unsafe-eval' *.synapse.org *.sagebase.org *.googleapis.com *.gstatic.com *.googletagmanager.com s3.amazonaws.com www.google-analytics.com *.youtube.com player.vimeo.com *.rstudio.com *.google.com *.shinyapps.io *.smartsheet.com sagebionetworks.jira.com googleads.g.doubleclick.net www.googleadservices.com *.contentsquare.net airtable.com *.airtable.com";
  public static final String ENFORCED_DEV_DEFAULT_SRC =
    " 127.0.0.1:9876 localhost:5173";
  public static final String ENFORCED_CONNECT_SRC =
    "'self' https://* wss://*.execute-api.us-east-1.amazonaws.com";
  public static final String ENFORCED_DEV_CONNECT_SRC =
    " http://localhost:5173 http://localhost:8080 http://127.0.0.1:8080 ws://localhost:5173";

  private final String enforcedPolicy;
  private final List<AllowedSource> reportOnlySources;
  // Report-Only policy by CDN host ("" when the site has no CDN). HtmlInjectionFilter.getCdnHost returns one of a
  // fixed set of hosts, so this stays small
  private final Map<String, String> reportOnlyPolicyByCdnHost =
    new ConcurrentHashMap<>();

  @Inject
  public ContentSecurityPolicyFilter(PropertyProvider propertyProvider) {
    boolean isDevMode =
      "true".equals(
          propertyProvider.getSystemProperties().getProperty(IS_DEV_MODE)
        );
    enforcedPolicy = buildEnforcedPolicy(isDevMode);
    reportOnlySources = new ArrayList<>(ContentSecurityPolicy.ALLOWED_SOURCES);
    if (isDevMode) {
      reportOnlySources.addAll(ContentSecurityPolicy.DEV_ALLOWED_SOURCES);
    }
  }

  // report-uri is the fallback for browsers that don't support report-to
  public static String buildEnforcedPolicy(boolean isDevMode) {
    return (
      "default-src " +
      ENFORCED_DEFAULT_SRC +
      (isDevMode ? ENFORCED_DEV_DEFAULT_SRC : "") +
      "; frame-ancestors 'none'" +
      "; img-src * data: blob:; connect-src " +
      ENFORCED_CONNECT_SRC +
      (isDevMode ? ENFORCED_DEV_CONNECT_SRC : "") +
      "; report-to " +
      CSP_REPORTING_ENDPOINT_NAME +
      "; report-uri " +
      CSP_REPORT_PATH
    );
  }

  public String getEnforcedPolicy() {
    return enforcedPolicy;
  }

  /**
   * @param host the request's Host header
   */
  public String getReportOnlyPolicy(String host) {
    String cdnHost = HtmlInjectionFilter.getCdnHost(host);
    String key = cdnHost == null ? "" : cdnHost;
    String policy = reportOnlyPolicyByCdnHost.get(key);
    if (policy == null) {
      policy =
        ContentSecurityPolicy.build(
          reportOnlySources,
          cdnHost == null ? null : "https://" + cdnHost,
          CSP_REPORTING_ENDPOINT_NAME,
          CSP_REPORT_PATH
        );
      reportOnlyPolicyByCdnHost.put(key, policy);
    }
    return policy;
  }

  @Override
  public void doFilter(
    ServletRequest request,
    ServletResponse response,
    FilterChain chain
  ) throws IOException, ServletException {
    HttpServletResponse httpResponse = (HttpServletResponse) response;
    String host = ((HttpServletRequest) request).getHeader(HOST_HEADER);
    httpResponse.setHeader(
      REPORTING_ENDPOINTS_HEADER,
      REPORTING_ENDPOINTS_VALUE
    );
    httpResponse.setHeader(CSP_HEADER, enforcedPolicy);
    httpResponse.setHeader(CSP_REPORT_ONLY_HEADER, getReportOnlyPolicy(host));
    chain.doFilter(request, response);
  }
}
