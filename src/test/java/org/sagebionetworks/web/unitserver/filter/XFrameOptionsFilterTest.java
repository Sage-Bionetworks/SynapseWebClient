package org.sagebionetworks.web.unitserver.filter;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sagebionetworks.web.server.servlet.filter.CORSFilter.ORIGIN_HEADER;
import static org.sagebionetworks.web.server.servlet.filter.XFrameOptionsFilter.DENY;
import static org.sagebionetworks.web.server.servlet.filter.XFrameOptionsFilter.X_FRAME_OPTIONS_HEADER;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
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

  @Before
  public void setUp() {
    filter = new XFrameOptionsFilter();
  }

  @Test
  public void testDeniesFramingByAnySite()
    throws ServletException, IOException {
    // No site may frame the web client, including Synapse subdomains (portals) and the web client itself
    for (String origin : new String[] {
      "https://malicious.example",
      "https://eliteportal.synapse.org",
      "https://www.synapse.org",
      null,
    }) {
      when(mockRequest.getHeader(ORIGIN_HEADER)).thenReturn(origin);

      filter.testFilter(mockRequest, mockResponse, mockFilterChain);
    }

    verify(mockResponse, times(4)).addHeader(X_FRAME_OPTIONS_HEADER, DENY);
    verify(mockFilterChain, times(4)).doFilter(mockRequest, mockResponse);
  }
}
