package org.sagebionetworks.web.unitserver.filter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sagebionetworks.web.server.servlet.filter.PlacesRedirectFilter;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;

@RunWith(MockitoJUnitRunner.class)
public class PlacesRedirectFilterTest {

  PlacesRedirectFilter filter;

  @Mock
  HttpServletRequest mockRequest;

  @Mock
  HttpServletResponse mockResponse;

  @Mock
  FilterChain mockFilterChain;

  @Before
  public void setUp() throws RestServiceException, IOException {
    filter = new PlacesRedirectFilter();
  }

  @Test
  public void testRedirect()
    throws ServletException, IOException, RestServiceException {
    String in = "https://www.synapse.org/Synapse:syn123";
    String expectedOut = "https://www.synapse.org/#!Synapse:syn123";
    when(mockRequest.getRequestURL()).thenReturn(new StringBuffer(in));

    filter.doFilter(mockRequest, mockResponse, mockFilterChain);

    verify(mockResponse).encodeRedirectURL(expectedOut);
    verify(mockResponse).sendRedirect(anyString());
  }

  @Test
  public void testNoRedirectForBot()
    throws ServletException, IOException, RestServiceException {
    when(mockRequest.getHeader("User-Agent")).thenReturn("Googlebot/2.1");
    String in = "https://www.synapse.org/Synapse:syn123";
    when(mockRequest.getRequestURL()).thenReturn(new StringBuffer(in));

    filter.doFilter(mockRequest, mockResponse, mockFilterChain);

    verify(mockResponse, never()).encodeRedirectURL(anyString());
    verify(mockResponse, never()).sendRedirect(anyString());
  }

  @Test
  public void testFixPath() {
    PlacesRedirectFilter filter = new PlacesRedirectFilter();
    assertEquals("", filter.fixPath(""));
    assertNull(filter.fixPath(null));
    assertEquals("/#!Synapse:syn1234", filter.fixPath("/Synapse:syn1234"));
    assertEquals(
      "/#!Synapse:syn123/wiki/2222",
      filter.fixPath("/Synapse:syn123/wiki/2222")
    );
    assertEquals("NoChange", filter.fixPath("NoChange"));
  }
}
