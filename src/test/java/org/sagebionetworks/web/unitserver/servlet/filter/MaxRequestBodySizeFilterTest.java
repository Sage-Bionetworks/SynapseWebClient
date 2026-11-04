package org.sagebionetworks.web.unitserver.servlet.filter;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.web.server.servlet.filter.MaxRequestBodySizeFilter;

public class MaxRequestBodySizeFilterTest {

  MaxRequestBodySizeFilter filter;

  @Mock
  HttpServletRequest mockRequest;

  @Mock
  HttpServletResponse mockResponse;

  @Mock
  FilterChain mockFilterChain;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    filter = new MaxRequestBodySizeFilter(100);
  }

  @Test
  public void overLimit() throws Exception {
    when(mockRequest.getContentLengthLong()).thenReturn(101L);

    filter.doFilter(mockRequest, mockResponse, mockFilterChain);

    verify(mockResponse)
      .setStatus(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE);
    verifyNoInteractions(mockFilterChain);
  }

  @Test
  public void atLimit() throws Exception {
    when(mockRequest.getContentLengthLong()).thenReturn(100L);

    filter.doFilter(mockRequest, mockResponse, mockFilterChain);

    verify(mockFilterChain).doFilter(mockRequest, mockResponse);
  }

  @Test
  public void noContentLength() throws Exception {
    // getContentLengthLong() returns -1 when there's no Content-Length header
    when(mockRequest.getContentLengthLong()).thenReturn(-1L);

    filter.doFilter(mockRequest, mockResponse, mockFilterChain);

    verify(mockFilterChain).doFilter(mockRequest, mockResponse);
  }
}
