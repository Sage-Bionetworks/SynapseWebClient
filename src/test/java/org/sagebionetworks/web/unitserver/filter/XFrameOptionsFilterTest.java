package org.sagebionetworks.web.unitserver.filter;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sagebionetworks.web.server.servlet.filter.XFrameOptionsFilter.DENY;
import static org.sagebionetworks.web.server.servlet.filter.XFrameOptionsFilter.SAMEORIGIN;
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
import org.mockito.runners.MockitoJUnitRunner;
import org.sagebionetworks.web.server.servlet.filter.XFrameOptionsFilter;

@RunWith(MockitoJUnitRunner.class)
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

		filter.testFilter(mockRequest, mockResponse, mockFilterChain);

		// verify x frame options set to deny
		verify(mockResponse).addHeader(X_FRAME_OPTIONS_HEADER, DENY);
	}

	@Test
	public void testPdfJs() throws ServletException, IOException {
		StringBuffer sb = new StringBuffer();
		sb.append("https://www.synapse.org/pdf.js/web/viewer.html");
		when(mockRequest.getRequestURL()).thenReturn(sb);
		when(mockRequest.getQueryString()).thenReturn("file=xyz.pdf");

		filter.testFilter(mockRequest, mockResponse, mockFilterChain);

		// verify x frame options set to same origin
		verify(mockResponse).addHeader(X_FRAME_OPTIONS_HEADER, SAMEORIGIN);
	}

}
