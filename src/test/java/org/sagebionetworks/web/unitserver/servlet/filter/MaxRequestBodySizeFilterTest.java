package org.sagebionetworks.web.unitserver.servlet.filter;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
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
  public void noContentLengthOverLimit() throws Exception {
    // getContentLengthLong() returns -1 when there's no Content-Length header (e.g. chunked)
    when(mockRequest.getContentLengthLong()).thenReturn(-1L);
    when(mockRequest.getInputStream()).thenReturn(inputStream("a".repeat(101)));

    filter.doFilter(mockRequest, mockResponse, mockFilterChain);

    verify(mockResponse)
      .setStatus(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE);
    verifyNoInteractions(mockFilterChain);
  }

  @Test
  public void noContentLengthAtLimit() throws Exception {
    String body = "a".repeat(100);
    when(mockRequest.getContentLengthLong()).thenReturn(-1L);
    when(mockRequest.getInputStream()).thenReturn(inputStream(body));

    filter.doFilter(mockRequest, mockResponse, mockFilterChain);

    ArgumentCaptor<ServletRequest> requestCaptor = ArgumentCaptor.forClass(
      ServletRequest.class
    );
    verify(mockFilterChain).doFilter(requestCaptor.capture(), eq(mockResponse));
    // The servlet reads the same body the filter already read
    assertEquals(body, IOUtils.toString(requestCaptor.getValue().getReader()));
  }

  @Test
  public void unreadableBody() throws Exception {
    when(mockRequest.getContentLengthLong()).thenReturn(-1L);
    when(mockRequest.getInputStream())
      .thenThrow(new IOException("disconnected"));

    filter.doFilter(mockRequest, mockResponse, mockFilterChain);

    verify(mockResponse).setStatus(HttpServletResponse.SC_BAD_REQUEST);
    verifyNoInteractions(mockFilterChain);
  }

  private static ServletInputStream inputStream(String body) {
    ByteArrayInputStream in = new ByteArrayInputStream(body.getBytes());
    return new ServletInputStream() {
      @Override
      public int read() {
        return in.read();
      }

      @Override
      public boolean isFinished() {
        return in.available() == 0;
      }

      @Override
      public boolean isReady() {
        return true;
      }

      @Override
      public void setReadListener(ReadListener listener) {}
    };
  }
}
