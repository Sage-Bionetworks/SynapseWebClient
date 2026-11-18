package org.sagebionetworks.web.server.servlet;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Accepts log records from the browser and logs each as an OpenTelemetry-style JSON line
 * to portal.log (shipped to CloudWatch). Each line is the portal.log prefix, then " - {json}".
 */
public class LogServlet extends HttpServlet {

  private static final long serialVersionUID = 1L;
  private static final String SERVICE_NAME = "synapse-web-client";
  // OpenTelemetry SeverityNumber for each SeverityText
  private static final Map<String, Integer> SEVERITY_NUMBERS = Map.of(
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

  // Must match the logger in log4j2.xml, which writes these lines to portal.log (shipped to CloudWatch Logs)
  public static final String CLIENT_LOG_NAME =
    "org.sagebionetworks.web.ClientLog";

  // The event names SWC sends. Anything else is rejected, so clients can't add arbitrary events to the log
  static final Set<String> ALLOWED_EVENT_NAMES = Set.of(
    "swc.uncaught_error",
    "swc.upload_error",
    "swc.module_load.error",
    "swc.module_load.inspection",
    "swc.module_load.recovery"
  );

  private final transient Logger clientLog;

  public LogServlet() {
    this(LogManager.getLogger(CLIENT_LOG_NAME));
  }

  /**
   * Unit test can pass in a mock logger.
   */
  public LogServlet(Logger clientLog) {
    this.clientLog = clientLog;
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) {
    JSONObject logRecord;
    try {
      logRecord = new JSONObject(IOUtils.toString(request.getReader()));
    } catch (IOException | JSONException e) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }
    if (!ALLOWED_EVENT_NAMES.contains(logRecord.optString("eventName"))) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }

    clientLog.info(
      toLogLine(
        logRecord,
        request.getHeader("User-Agent"),
        // Only the rightmost entry is trustworthy; clients can set the rest.
        // (On Beanstalk, getRemoteAddr() is the proxy.)
        request.getHeader(SynapseClientBase.X_FORWARDED_FOR_HEADER)
      )
        .toString()
    );
    response.setStatus(HttpServletResponse.SC_NO_CONTENT);
  }

  /**
   * Returns the OpenTelemetry SeverityText for the requested severity, or null if it's missing or not one of
   * SEVERITY_NUMBERS (the log line then has no severity, which OpenTelemetry treats as unspecified).
   */
  static String toValidSeverityOrNull(String requestedSeverity) {
    String normalizedSeverity = requestedSeverity
      .trim()
      .toUpperCase(Locale.ROOT);
    return SEVERITY_NUMBERS.containsKey(normalizedSeverity)
      ? normalizedSeverity
      : null;
  }

  /**
   * Builds an OpenTelemetry log line from the given record, user agent, and client address. put() with a null value leaves the
   * key out.
   */
  static JSONObject toLogLine(
    JSONObject logRecord,
    String userAgent,
    String clientAddress
  ) {
    String severityText = toValidSeverityOrNull(
      logRecord.optString("severityText", "")
    );

    JSONObject attributes = logRecord.optJSONObject("attributes");
    if (attributes == null) {
      attributes = new JSONObject();
    }
    // Set by the server, overriding anything the client sent
    attributes.put("user_agent.original", userAgent);
    attributes.put("client.address", clientAddress);
    attributes.put("client.timestamp", logRecord.optString("timestamp", null));

    JSONObject resource = new JSONObject();
    resource.put("service.name", SERVICE_NAME);
    resource.put(
      "service.version",
      SynapseClientImpl.PortalVersionHolder.getVersionInfo()
    );

    JSONObject logLine = new JSONObject();
    logLine.put("observedTimestamp", Instant.now().toString());
    // Map.of() maps throw on a null key, so only look up the number when there's a severity
    if (severityText != null) {
      logLine.put("severityText", severityText);
      logLine.put("severityNumber", SEVERITY_NUMBERS.get(severityText));
    }
    logLine.put("eventName", logRecord.optString("eventName", null));
    logLine.put("body", logRecord.opt("body"));
    logLine.put("attributes", attributes);
    logLine.put("resource", resource);
    return logLine;
  }
}
