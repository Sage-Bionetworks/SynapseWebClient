package org.sagebionetworks.web.unitserver.servlet.filter;

import static org.mockito.Mockito.verify;
import static org.sagebionetworks.web.server.servlet.filter.HSTSFilter.HSTS_PRELOAD_SUFFIX;
import static org.sagebionetworks.web.server.servlet.filter.HSTSFilter.MAX_AGE;
import static org.sagebionetworks.web.server.servlet.filter.HSTSFilter.MAX_AGE_SECONDS;
import static org.sagebionetworks.web.server.servlet.filter.HSTSFilter.STRICT_TRANSPORT_SECURITY;
import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.web.server.servlet.filter.HSTSFilter;

public class HSTSFilterTest {
	HSTSFilter filter;
	@Mock
	HttpServletRequest mockRequest;
	@Mock
	HttpServletResponse mockResponse;
	@Mock
	FilterChain mockFilterChain;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		filter = new HSTSFilter();
	}

	@Test
	public void testDoFilter() throws IOException, ServletException {
		filter.doFilter(mockRequest, mockResponse, mockFilterChain);
		verify(mockResponse).setHeader(STRICT_TRANSPORT_SECURITY, MAX_AGE + MAX_AGE_SECONDS + HSTS_PRELOAD_SUFFIX);
		verify(mockFilterChain).doFilter(mockRequest, mockResponse);
	}
}
