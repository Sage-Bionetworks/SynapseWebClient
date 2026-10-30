package org.sagebionetworks.web.server.servlet;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Accepts log entries from the browser and writes each one to stdout (shipped to CloudWatch) as a single
 * JSON line, using OpenTelemetry field names. Replaces the previous backend POST /repo/v1/log service.
 */
public class LogServlet extends HttpServlet {

  private static final long serialVersionUID = 1L;
  public static final String SERVICE_NAME = "synapse-web-client";
  // OpenTelemetry SeverityNumber for each SeverityText
  public static final Map<String, Integer> SEVERITY_NUMBERS = Map.of(
    "TRACE",
    1,
    "DEBUG",
    5,
    "INFO",
    9,
    "WARN",
    13,
    "ERROR",
    17,
    "FATAL",
    21
  );

  // Must match the logger in log4j2.xml, which writes these lines to stdout (shipped to CloudWatch Logs) as bare JSON
  private Logger clientLog = LogManager.getLogger(
    "org.sagebionetworks.web.ClientLog"
  );

  /**
   * Unit test can override this.
   */
  public void setLogger(Logger clientLog) {
    this.clientLog = clientLog;
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) {
    JSONObject record;
    try {
      record = new JSONObject(IOUtils.toString(request.getReader()));
    } catch (IOException | JSONException e) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }

    clientLog.info(
      toLogLine(record, request.getHeader("User-Agent")).toString()
    );
    response.setStatus(HttpServletResponse.SC_NO_CONTENT);
  }

  /**
   * Returns the OpenTelemetry SeverityText for the requested severity, or null if it's missing or not one of
   * SEVERITY_NUMBERS (the log line then has no severity, which OpenTelemetry treats as unspecified).
   */
  static String toValidSeverityOrNull(String requestedSeverity) {
    String normalizedSeverity = requestedSeverity.trim().toUpperCase();
    return SEVERITY_NUMBERS.containsKey(normalizedSeverity)
      ? normalizedSeverity
      : null;
  }

  /**
   * Builds an OpenTelemetry log line from the given record and user agent. put() with a null value leaves the
   * key out.
   */
  static JSONObject toLogLine(JSONObject record, String userAgent) {
    String severityText = toValidSeverityOrNull(
      record.optString("severityText", "")
    );

    JSONObject attributes = record.optJSONObject("attributes");
    if (attributes == null) {
      attributes = new JSONObject();
    }
    // Set by the server, overriding anything the client sent
    attributes.put("user_agent.original", userAgent);

    JSONObject resource = new JSONObject();
    resource.put("service.name", SERVICE_NAME);
    resource.put(
      "service.version",
      SynapseClientImpl.PortalVersionHolder.getVersionInfo()
    );

    JSONObject logLine = new JSONObject();
    logLine.put("timestamp", record.optString("timestamp", null));
    logLine.put("observedTimestamp", Instant.now().toString());
    // Map.of() maps throw on a null key, so only look up the number when there's a severity
    if (severityText != null) {
      logLine.put("severityText", severityText);
      logLine.put("severityNumber", SEVERITY_NUMBERS.get(severityText));
    }
    logLine.put("eventName", record.optString("eventName", null));
    logLine.put("body", record.opt("body"));
    logLine.put("attributes", attributes);
    logLine.put("resource", resource);
    return logLine;
  }
}
