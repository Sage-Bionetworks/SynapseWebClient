package org.sagebionetworks.web.unitserver.servlet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.web.server.servlet.LogServlet;

public class LogServletTest {

  @Mock
  HttpServletRequest mockRequest;

  @Mock
  HttpServletResponse mockResponse;

  @Mock
  Logger mockLogger;

  LogServlet servlet;

  @Captor
  ArgumentCaptor<String> lineCaptor;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    servlet = new LogServlet(mockLogger);
  }

  @Test
  public void validRecord() throws Exception {
    String json =
      "{\"severityText\":\"error\",\"eventName\":\"swc.test\",\"body\":{\"buildId\":\"abc\"}}";
    BufferedReader br = new BufferedReader(new StringReader(json));
    when(mockRequest.getReader()).thenReturn(br);
    when(mockRequest.getHeader("User-Agent")).thenReturn("mockUserAgent");

    servlet.doPost(mockRequest, mockResponse);

    verify(mockLogger).info(lineCaptor.capture());
    JSONObject jsonObject = new JSONObject(lineCaptor.getValue());
    assertEquals("ERROR", jsonObject.get("severityText").toString());
    assertEquals("swc.test", jsonObject.get("eventName").toString());
    assertEquals(
      "mockUserAgent",
      jsonObject.getJSONObject("attributes").getString("user_agent.original")
    );

    verify(mockResponse).setStatus(HttpServletResponse.SC_NO_CONTENT);
  }

  @Test
  public void invalidJson() throws Exception {
    BufferedReader br = new BufferedReader(new StringReader("not json"));
    when(mockRequest.getReader()).thenReturn(br);

    servlet.doPost(mockRequest, mockResponse);

    verify(mockResponse).setStatus(HttpServletResponse.SC_BAD_REQUEST);
    verifyNoInteractions(mockLogger);
  }

  @Test
  public void unreadableBody() throws Exception {
    when(mockRequest.getReader()).thenThrow(new IOException("disconnected"));

    servlet.doPost(mockRequest, mockResponse);

    verify(mockResponse).setStatus(HttpServletResponse.SC_BAD_REQUEST);
    verifyNoInteractions(mockLogger);
  }

  @Test
  public void unknownSeverityIsLeftOut() throws Exception {
    String json = "{\"severityText\":\"LOUD\",\"eventName\":\"swc.test\"}";
    BufferedReader br = new BufferedReader(new StringReader(json));
    when(mockRequest.getReader()).thenReturn(br);

    servlet.doPost(mockRequest, mockResponse);

    verify(mockLogger).info(lineCaptor.capture());
    JSONObject jsonObject = new JSONObject(lineCaptor.getValue());
    assertFalse(jsonObject.has("severityText"));
    assertFalse(jsonObject.has("severityNumber"));

    verify(mockResponse).setStatus(HttpServletResponse.SC_NO_CONTENT);
  }
}
