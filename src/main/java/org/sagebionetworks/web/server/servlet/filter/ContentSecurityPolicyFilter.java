package org.sagebionetworks.web.server.servlet.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.sagebionetworks.web.shared.WebConstants;

/**
 * This filter adds the Content Security Policy to every response, and tells the browser to send violation reports to
 * the CSP report endpoint (CSPReportServlet).
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

  // Sites allowed to embed Synapse in an iframe. Built from the same list XFrameOptionsFilter uses, since browsers
  // ignore X-Frame-Options when frame-ancestors is present
  public static final String FRAME_ANCESTORS = buildFrameAncestors();

  // The policy formerly in the Portal.html <meta> tag (where report-to and frame-ancestors are ignored).
  // report-uri is the fallback for browsers that don't support report-to
  public static final String CONTENT_SECURITY_POLICY =
    "default-src 'self' data: blob: 'unsafe-inline' 'unsafe-eval' *.synapse.org *.sagebase.org *.googleapis.com *.gstatic.com *.googletagmanager.com cdn.statuspage.io s3.amazonaws.com www.google-analytics.com kh896k90gyvg.statuspage.io *.youtube.com *.rstudio.com *.google.com *.shinyapps.io *.smartsheet.com sagebionetworks.jira.com 127.0.0.1:9876 localhost:5173 googleads.g.doubleclick.net www.googleadservices.com *.contentsquare.net airtable.com *.airtable.com; frame-ancestors " +
    FRAME_ANCESTORS +
    "; " +
    "img-src * data: blob:; connect-src https://* http://127.0.0.1:8888 http://localhost:5173 http://localhost:8080 http://127.0.0.1:8080 ws://localhost:5173 wss://*.execute-api.us-east-1.amazonaws.com; " +
    "report-to " +
    CSP_REPORTING_ENDPOINT_NAME +
    "; " +
    "report-uri " +
    CSP_REPORT_PATH;

  private static String buildFrameAncestors() {
    StringBuilder sb = new StringBuilder("'self' localhost");
    for (String subdomain : CORSFilter.ALLOWED_SYNAPSE_SUBDOMAINS) {
      sb
        .append(" https://")
        .append(subdomain)
        .append(CORSFilter.SYNAPSE_ORG_SUFFIX);
    }
    return sb.toString();
  }

  @Override
  public void doFilter(
    ServletRequest request,
    ServletResponse response,
    FilterChain chain
  ) throws IOException, ServletException {
    HttpServletResponse httpResponse = (HttpServletResponse) response;
    httpResponse.setHeader(
      REPORTING_ENDPOINTS_HEADER,
      REPORTING_ENDPOINTS_VALUE
    );
    httpResponse.setHeader(CSP_HEADER, CONTENT_SECURITY_POLICY);
    chain.doFilter(request, response);
  }
}
