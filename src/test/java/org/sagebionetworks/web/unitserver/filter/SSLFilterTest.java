package org.sagebionetworks.web.unitserver.filter;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import javax.servlet.http.HttpServletRequest;
import org.junit.Test;
import org.mockito.Mockito;
import org.sagebionetworks.web.server.servlet.filter.SSLFilter;

public class SSLFilterTest {

	@Test
	public void testIsForwardedHttpRequestMissing() {
		HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
		when(request.getHeader(SSLFilter.HEADER_X_FORWARDED_PROTO)).thenReturn(null);
		assertFalse(SSLFilter.isForwardedHttpRequest(request));
	}

	@Test
	public void testIsForwardedHttpRequestHTTP() {
		HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
		when(request.getHeader(SSLFilter.HEADER_X_FORWARDED_PROTO)).thenReturn("HTTP");
		assertTrue(SSLFilter.isForwardedHttpRequest(request));
	}

	@Test
	public void testIsForwardedHttpRequestHTTPS() {
		HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
		when(request.getHeader(SSLFilter.HEADER_X_FORWARDED_PROTO)).thenReturn("HTTPS");
		assertFalse(SSLFilter.isForwardedHttpRequest(request));
	}

}
