package org.sagebionetworks.web.unitserver.filter;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sagebionetworks.web.server.servlet.filter.CORSFilter.ORIGIN_HEADER;
import static org.sagebionetworks.web.server.servlet.filter.XFrameOptionsFilter.DENY;
import static org.sagebionetworks.web.server.servlet.filter.XFrameOptionsFilter.REFERER_HEADER;
import static org.sagebionetworks.web.server.servlet.filter.XFrameOptionsFilter.X_FRAME_OPTIONS_HEADER;

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
import org.sagebionetworks.web.server.servlet.filter.XFrameOptionsFilter;

@RunWith(MockitoJUnitRunner.Silent.class)
public class XFrameOptionsFilterTest {

  XFrameOptionsFilter filter;

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
    filter = new XFrameOptionsFilter();
  }

  @Test
  public void testStandardRequest() throws ServletException, IOException {
    StringBuffer sb = new StringBuffer();
    sb.append("https://www.synapse.org/index.html");
    when(mockRequest.getRequestURL()).thenReturn(sb);
    when(mockRequest.getQueryString()).thenReturn(null);
    when(mockRequest.getHeader(ORIGIN_HEADER))
      .thenReturn("https://malicious.example");

    filter.testFilter(mockRequest, mockResponse, mockFilterChain);

    // verify x frame options set to deny
    verify(mockResponse).addHeader(X_FRAME_OPTIONS_HEADER, DENY);
  }

  @Test
  public void testAllowedSynapseSubdomainOrigin()
    throws ServletException, IOException {
    StringBuffer sb = new StringBuffer();
    sb.append("https://www.synapse.org/index.html");
    when(mockRequest.getRequestURL()).thenReturn(sb);
    when(mockRequest.getQueryString()).thenReturn(null);
    when(mockRequest.getHeader(ORIGIN_HEADER))
      .thenReturn("https://eliteportal.synapse.org");

    filter.testFilter(mockRequest, mockResponse, mockFilterChain);

    // verify x frame options header is set to ALLOW-FROM for an allowed origin
    verify(mockResponse)
      .addHeader(
        X_FRAME_OPTIONS_HEADER,
        "ALLOW-FROM https://eliteportal.synapse.org"
      );
  }

  @Test
  public void testFallbackToRefererHeader()
    throws ServletException, IOException {
    StringBuffer sb = new StringBuffer();
    sb.append("https://www.synapse.org/index.html");
    when(mockRequest.getRequestURL()).thenReturn(sb);
    when(mockRequest.getQueryString()).thenReturn(null);
    // Origin header is not set
    when(mockRequest.getHeader(ORIGIN_HEADER)).thenReturn(null);
    // Referer header contains allowed synapse subdomain
    when(mockRequest.getHeader(REFERER_HEADER))
      .thenReturn(
        "https://eliteportal.synapse.org/Data%20Access/AI_ML_Acceptable_Use_Policy"
      );

    filter.testFilter(mockRequest, mockResponse, mockFilterChain);

    // verify x frame options header is set to ALLOW-FROM when referer is an allowed origin
    verify(mockResponse)
      .addHeader(
        X_FRAME_OPTIONS_HEADER,
        "ALLOW-FROM https://eliteportal.synapse.org/Data%20Access/AI_ML_Acceptable_Use_Policy"
      );
  }
}
