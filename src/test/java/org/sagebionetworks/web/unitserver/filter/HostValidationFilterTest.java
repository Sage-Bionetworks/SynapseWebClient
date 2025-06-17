package org.sagebionetworks.web.unitserver.filter;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sagebionetworks.web.server.servlet.filter.CORSFilter.*;
import static org.sagebionetworks.web.server.servlet.filter.HostValidationFilter.INVALID_HOST_HEADER_MESSAGE;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.sagebionetworks.web.server.servlet.filter.HostValidationFilter;

@RunWith(MockitoJUnitRunner.Silent.class)
public class HostValidationFilterTest {

  HostValidationFilter filter;

  @Mock
  HttpServletRequest mockRequest;

  @Mock
  HttpServletResponse mockResponse;

  @Mock
  FilterChain mockFilterChain;

  @Captor
  ArgumentCaptor<String> stringCaptor;

  @Before
  public void setUp() {
    filter = new HostValidationFilter();
    when(mockRequest.getHeader(HOST_HEADER))
      .thenReturn("www" + SYNAPSE_ORG_SUFFIX); // www.synapse.org
  }

  @Test
  public void testSynapseOrg() throws ServletException, IOException {
    // we allow www.synapse.org
    filter.testFilter(mockRequest, mockResponse, mockFilterChain);

    verify(mockResponse, never()).sendError(anyInt(), anyString());
  }

  @Test
  public void testCdn() throws ServletException, IOException {
    when(mockRequest.getHeader(HOST_HEADER))
      .thenReturn("cdn-www" + SYNAPSE_ORG_SUFFIX); // cdn-www.synapse.org
    filter.testFilter(mockRequest, mockResponse, mockFilterChain);

    verify(mockResponse, never()).sendError(anyInt(), anyString());
  }

  @Test
  public void testAccountsSite() throws ServletException, IOException {
    when(mockRequest.getHeader(HOST_HEADER))
      .thenReturn("accounts" + SYNAPSE_ORG_SUFFIX); // accounts.synapse.org
    filter.testFilter(mockRequest, mockResponse, mockFilterChain);

    verify(mockResponse, never()).sendError(anyInt(), anyString());
  }

  @Test
  public void testDevSite() throws ServletException, IOException {
    when(mockRequest.getHeader(HOST_HEADER))
      .thenReturn("dev" + SYNAPSE_ORG_SUFFIX); // dev.synapse.org
    filter.testFilter(mockRequest, mockResponse, mockFilterChain);

    verify(mockResponse, never()).sendError(anyInt(), anyString());
  }

  @Test
  public void testLocalhostWithPort() throws ServletException, IOException {
    when(mockRequest.getHeader(HOST_HEADER)).thenReturn("127.0.0.1:8888");
    filter.testFilter(mockRequest, mockResponse, mockFilterChain);

    verify(mockResponse, never()).sendError(anyInt(), anyString());
  }

  @Test
  public void testLocalhost() throws ServletException, IOException {
    when(mockRequest.getHeader(HOST_HEADER)).thenReturn("localhost");
    filter.testFilter(mockRequest, mockResponse, mockFilterChain);

    verify(mockResponse, never()).sendError(anyInt(), anyString());
  }

  @Test
  public void testDisallowed() throws ServletException, IOException {
    when(mockRequest.getHeader(HOST_HEADER))
      .thenReturn("tenablewasx8Foy6j7GC.com");
    filter.testFilter(mockRequest, mockResponse, mockFilterChain);

    verify(mockResponse)
      .sendError(HttpServletResponse.SC_FORBIDDEN, INVALID_HOST_HEADER_MESSAGE);
  }
}
