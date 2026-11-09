package org.sagebionetworks.web.server.servlet;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.time.Instant;
import java.util.Arrays;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Accepts CSP violation reports from the browser and writes each one to stdout (shipped to CloudWatch) as a
 * single JSON line, in the same OpenTelemetry format as LogServlet. Accepts both the report-to format
 * (application/reports+json) and the older report-uri format (application/csp-report).
 */
public class CSPReportServlet extends HttpServlet {

  private static final long serialVersionUID = 1L;
  // Limits how many log lines a single request can produce. Entries past this are ignored
  public static final int MAX_REPORTS_PER_REQUEST = 50;

  // Same logger as LogServlet; see CLIENT_LOG_NAME
  private final transient Logger clientLog;

  public CSPReportServlet() {
    this(LogManager.getLogger(LogServlet.CLIENT_LOG_NAME));
  }

  /**
   * Unit test can pass in a mock logger.
   */
  public CSPReportServlet(Logger clientLog) {
    this.clientLog = clientLog;
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) {
    // valueOf turns a missing content type into "null", which matches neither format below
    String contentType = String.valueOf(request.getContentType());
    String userAgent = request.getHeader("User-Agent");
    String clientAddress = request.getHeader(
      SynapseClientBase.X_FORWARDED_FOR_HEADER
    );
    try {
      String body = IOUtils.toString(request.getReader());
      // startsWith, not equals, in case the browser adds extras like "; charset=utf-8" after the type
      if (contentType.startsWith("application/reports+json")) {
        // report-to: a JSON array of reports (https://w3c.github.io/reporting/).
        // The array can also hold other report types (e.g. deprecation), which are skipped.
        // Malformed entries are skipped too, so one bad entry doesn't fail the whole batch.
        JSONArray reports = new JSONArray(body);
        int n = Math.min(reports.length(), MAX_REPORTS_PER_REQUEST);
        for (int i = 0; i < n; i++) {
          JSONObject report = reports.optJSONObject(i);
          if (
            report == null || !"csp-violation".equals(report.optString("type"))
          ) continue;
          JSONObject reportBody = report.optJSONObject("body");
          if (reportBody == null) continue;
          // age is how many milliseconds ago the violation happened. Clamped at 0 in case of clock drift
          long ageMs = Math.max(0, report.optLong("age", 0));
          log(
            Instant.now().minusMillis(ageMs),
            reportBody,
            report.optString("url", null),
            userAgent,
            clientAddress
          );
        }
      } else if (contentType.startsWith("application/csp-report")) {
        // report-uri (older browsers): {"csp-report": {...}}
        // (https://developer.mozilla.org/en-US/docs/Web/HTTP/Reference/Headers/Content-Security-Policy/report-uri)
        JSONObject cspReport = new JSONObject(body).getJSONObject("csp-report");
        log(
          // This format has no age, so use the time the report was received
          Instant.now(),
          cspReport,
          cspReport.optString("document-uri", null),
          userAgent,
          clientAddress
        );
      } else {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        return;
      }
    } catch (IOException | JSONException e) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }
    // Browsers treat any non-2xx response as a failure and eventually stop sending to this endpoint
    response.setStatus(HttpServletResponse.SC_NO_CONTENT);
  }

  /**
   * Writes one CSP violation as a LogServlet-format log line.
   */
  private void log(
    Instant timestamp,
    JSONObject reportBody,
    String url,
    String userAgent,
    String clientAddress
  ) {
    JSONObject attributes = new JSONObject();
    attributes.put("url.template", toUrlTemplate(url));
    attributes.put("url.domain", toDomain(url));
    // Read before scrubbing, which replaces the body's referrer with its template
    attributes.put(
      "synapse.referrer.origin",
      toOrigin(reportBody.optString("referrer", null))
    );
    scrubReportBody(reportBody);

    JSONObject logRecord = new JSONObject();
    logRecord.put("timestamp", timestamp.toString());
    logRecord.put("severityText", "WARN");
    logRecord.put("eventName", "csp.violation");
    logRecord.put("body", reportBody);
    logRecord.put("attributes", attributes);
    clientLog.info(
      LogServlet.toLogLine(logRecord, userAgent, clientAddress).toString()
    );
  }

  /**
   * Removes sensitive values from the report's URLs. Each list has both the report-to (e.g. documentURL) and
   * report-uri (e.g. document-uri) field names.
   */
  private static void scrubReportBody(JSONObject reportBody) {
    // Page URLs: the route can hold a token (e.g. /PasswordResetSignedToken:abc), so keep only the template
    for (String key : new String[] {
      "documentURL",
      "document-uri",
      "referrer",
    }) {
      if (reportBody.has(key)) {
        reportBody.put(key, toUrlTemplate(reportBody.optString(key)));
      }
    }

    // Resource URLs: keep the path, drop the query and fragment, which can hold secrets (e.g. S3 signatures)
    for (String key : new String[] {
      "blockedURL",
      "blocked-uri",
      "sourceFile",
      "source-file",
    }) {
      if (reportBody.has(key)) {
        reportBody.put(key, reportBody.optString(key).split("[?#]", 2)[0]);
      }
    }
  }

  // '/PasswordResetSignedToken:abc…' → '/PasswordResetSignedToken:{token}'
  // '/Synapse:syn123/wiki/456'       → '/Synapse:{token}'
  // '/'                              → '/'
  public static String toUrlTemplate(String href) {
    try {
      URI uri = new URI(href);

      if (!uri.isAbsolute()) {
        return null;
      }

      String first = Arrays
        .stream(uri.getRawPath().split("/"))
        .filter(segment -> !segment.isEmpty())
        .findFirst()
        .orElse("");

      int colon = first.indexOf(':');

      return colon == -1
        ? "/" + first
        : "/" + first.substring(0, colon) + ":{token}";
    } catch (Exception e) {
      return null;
    }
  }

  /** Returns the URL's host (e.g. www.synapse.org), or null if it isn't an absolute URL. */
  public static String toDomain(String href) {
    try {
      return new URI(href).getHost();
    } catch (Exception e) {
      return null;
    }
  }

  /** Returns the URL's origin (e.g. https://www.synapse.org), or null if it isn't an absolute URL. */
  public static String toOrigin(String href) {
    try {
      URI uri = new URI(href);
      return uri.isAbsolute()
        ? uri.getScheme() + "://" + uri.getAuthority()
        : null;
    } catch (Exception e) {
      return null;
    }
  }
}
