package org.sagebionetworks.web.server.servlet;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
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

  // Must match the logger in log4j2.xml, which writes these lines to stdout (shipped to CloudWatch Logs) as bare JSON
  private final transient Logger clientLog;

  public CSPReportServlet() {
    this(LogManager.getLogger("org.sagebionetworks.web.ClientLog"));
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
    try {
      String body = IOUtils.toString(request.getReader());
      // startsWith, not equals, in case the browser adds extras like "; charset=utf-8" after the type
      if (contentType.startsWith("application/reports+json")) {
        // report-to: a JSON array of reports (https://w3c.github.io/reporting/).
        // The array can also hold other report types (e.g. deprecation), which are skipped
        JSONArray reports = new JSONArray(body);
        for (int i = 0; i < reports.length(); i++) {
          JSONObject report = reports.getJSONObject(i);
          if ("csp-violation".equals(report.optString("type"))) {
            // age is how many milliseconds ago the violation happened
            log(
              Instant.now().minusMillis(report.optLong("age", 0)),
              report.getJSONObject("body"),
              report.optString("url", null),
              userAgent
            );
          }
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
          userAgent
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
    String userAgent
  ) {
    JSONObject attributes = new JSONObject();
    attributes.put("url.full", url);

    JSONObject logRecord = new JSONObject();
    logRecord.put("timestamp", timestamp.toString());
    logRecord.put("severityText", "WARN");
    logRecord.put("eventName", "csp.violation");
    logRecord.put("body", reportBody);
    logRecord.put("attributes", attributes);
    clientLog.info(LogServlet.toLogLine(logRecord, userAgent).toString());
  }
}
