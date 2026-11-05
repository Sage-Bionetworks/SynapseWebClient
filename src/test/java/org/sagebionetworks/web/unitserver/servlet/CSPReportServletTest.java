package org.sagebionetworks.web.unitserver.servlet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.StringReader;
import java.time.Instant;
import java.util.List;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.web.server.servlet.CSPReportServlet;

public class CSPReportServletTest {

  // report-to format: a csp-violation report followed by a non-CSP report that should be skipped
  public static final String REPORTS_JSON =
    "[{\"type\":\"csp-violation\",\"age\":10,\"url\":\"https://www.synapse.org/\",\"user_agent\":\"ua\"," +
    "\"body\":{\"documentURL\":\"https://www.synapse.org/\",\"blockedURL\":\"https://evil.example/x.js\"," +
    "\"effectiveDirective\":\"script-src-elem\",\"disposition\":\"enforce\",\"statusCode\":200}}," +
    "{\"type\":\"deprecation\",\"age\":0,\"url\":\"https://www.synapse.org/\",\"body\":{}}]";
  // report-uri format
  public static final String CSP_REPORT_JSON =
    "{\"csp-report\":{\"document-uri\":\"https://www.synapse.org/\",\"blocked-uri\":\"https://evil.example/x.js\"," +
    "\"violated-directive\":\"script-src-elem\",\"disposition\":\"enforce\"}}";

  @Mock
  HttpServletRequest mockRequest;

  @Mock
  HttpServletResponse mockResponse;

  @Mock
  Logger mockLogger;

  CSPReportServlet servlet;

  @Captor
  ArgumentCaptor<String> lineCaptor;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    servlet = new CSPReportServlet(mockLogger);
    when(mockRequest.getHeader("User-Agent")).thenReturn("mockUserAgent");
    when(mockRequest.getHeader("X-Forwarded-For")).thenReturn("1.2.3.4");
  }

  private void mockRequestBody(String contentType, String body)
    throws Exception {
    when(mockRequest.getContentType()).thenReturn(contentType);
    when(mockRequest.getReader())
      .thenReturn(new BufferedReader(new StringReader(body)));
  }

  @Test
  public void logsReportToViolation() throws Exception {
    mockRequestBody("application/reports+json", REPORTS_JSON);

    servlet.doPost(mockRequest, mockResponse);

    // Only once, since the deprecation report is skipped
    verify(mockLogger).info(lineCaptor.capture());
    JSONObject line = new JSONObject(lineCaptor.getValue());
    assertEquals("csp.violation", line.getString("eventName"));
    assertEquals("WARN", line.getString("severityText"));
    assertEquals(
      "https://evil.example/x.js",
      line.getJSONObject("body").getString("blockedURL")
    );
    JSONObject attributes = line.getJSONObject("attributes");
    assertEquals("/", attributes.getString("url.template"));
    assertEquals("mockUserAgent", attributes.getString("user_agent.original"));
    assertEquals("1.2.3.4", attributes.getString("client.address"));
    verify(mockResponse).setStatus(HttpServletResponse.SC_NO_CONTENT);
  }

  @Test
  public void malformedEntriesSkipped() throws Exception {
    // A valid report, a non-object, a report without a body, a future (negative) age, and another valid report
    String reports =
      "[{\"type\":\"csp-violation\",\"age\":0,\"body\":{\"blockedURL\":\"a\"}}," +
      "\"not an object\"," +
      "{\"type\":\"csp-violation\",\"age\":0}," +
      "{\"type\":\"csp-violation\",\"age\":-60000,\"body\":{\"blockedURL\":\"b\"}}]";
    mockRequestBody("application/reports+json", reports);

    servlet.doPost(mockRequest, mockResponse);

    verify(mockLogger, times(2)).info(lineCaptor.capture());
    List<String> lines = lineCaptor.getAllValues();
    assertEquals(
      "a",
      new JSONObject(lines.get(0)).getJSONObject("body").getString("blockedURL")
    );
    JSONObject second = new JSONObject(lines.get(1));
    assertEquals("b", second.getJSONObject("body").getString("blockedURL"));
    // The negative age is clamped, so the timestamp isn't in the future
    assertFalse(
      Instant.parse(second.getString("timestamp")).isAfter(Instant.now())
    );
    verify(mockResponse).setStatus(HttpServletResponse.SC_NO_CONTENT);
  }

  @Test
  public void reportsPastLimitIgnored() throws Exception {
    StringBuilder reports = new StringBuilder("[");
    for (int i = 0; i <= CSPReportServlet.MAX_REPORTS_PER_REQUEST; i++) {
      if (i > 0) {
        reports.append(",");
      }
      reports.append(
        "{\"type\":\"csp-violation\",\"age\":0,\"body\":{\"blockedURL\":\"" +
        i +
        "\"}}"
      );
    }
    reports.append("]");
    mockRequestBody("application/reports+json", reports.toString());

    servlet.doPost(mockRequest, mockResponse);

    verify(mockLogger, times(CSPReportServlet.MAX_REPORTS_PER_REQUEST))
      .info(anyString());
    verify(mockResponse).setStatus(HttpServletResponse.SC_NO_CONTENT);
  }

  @Test
  public void logsReportUriViolation() throws Exception {
    // A parameter after the content type is still accepted
    mockRequestBody("application/csp-report; charset=utf-8", CSP_REPORT_JSON);

    servlet.doPost(mockRequest, mockResponse);

    verify(mockLogger).info(lineCaptor.capture());
    JSONObject line = new JSONObject(lineCaptor.getValue());
    assertEquals(
      "script-src-elem",
      line.getJSONObject("body").getString("violated-directive")
    );
    assertEquals(
      "/",
      line.getJSONObject("attributes").getString("url.template")
    );
    verify(mockResponse).setStatus(HttpServletResponse.SC_NO_CONTENT);
  }

  // Protects a security property: tokens in the page URL must never reach the logs
  @Test
  public void sensitiveUrlsScrubbed() throws Exception {
    String reports =
      "[{\"type\":\"csp-violation\",\"age\":0," +
      "\"url\":\"https://www.synapse.org/PasswordResetSignedToken:secret\"," +
      "\"body\":{\"documentURL\":\"https://www.synapse.org/PasswordResetSignedToken:secret\"," +
      "\"referrer\":\"https://www.synapse.org/Synapse:syn123/wiki/456\"," +
      "\"blockedURL\":\"https://bucket.s3.amazonaws.com/f?X-Amz-Signature=secret\"," +
      "\"sourceFile\":\"https://www.synapse.org/a.js#secret\"}}]";

    mockRequestBody("application/reports+json", reports);

    servlet.doPost(mockRequest, mockResponse);

    verify(mockLogger).info(lineCaptor.capture());
    assertFalse(lineCaptor.getValue().contains("secret"));
    JSONObject line = new JSONObject(lineCaptor.getValue());
    JSONObject attributes = line.getJSONObject("attributes");
    assertEquals(
      "/PasswordResetSignedToken:{token}",
      attributes.getString("url.template")
    );
    assertEquals("www.synapse.org", attributes.getString("url.domain"));
    assertEquals(
      "https://www.synapse.org",
      attributes.getString("synapse.referrer.origin")
    );
    JSONObject body = line.getJSONObject("body");
    assertEquals(
      "/PasswordResetSignedToken:{token}",
      body.getString("documentURL")
    );
    assertEquals("/Synapse:{token}", body.getString("referrer"));
    assertEquals(
      "https://bucket.s3.amazonaws.com/f",
      body.getString("blockedURL")
    );
    assertEquals("https://www.synapse.org/a.js", body.getString("sourceFile"));
  }

  @Test
  public void badRequests() throws Exception {
    String[] contentTypes = {
      // An object where the report-to array is expected
      "application/reports+json",
      // Unsupported content type
      "application/json",
      // Missing content type, which RPCValidationFilter replaces with this
      "text/x-gwt-rpc; charset=utf-8",
    };
    for (String contentType : contentTypes) {
      reset(mockResponse);
      mockRequestBody(contentType, CSP_REPORT_JSON);

      servlet.doPost(mockRequest, mockResponse);

      verify(mockResponse).setStatus(HttpServletResponse.SC_BAD_REQUEST);
    }
    verifyNoInteractions(mockLogger);
  }
}
