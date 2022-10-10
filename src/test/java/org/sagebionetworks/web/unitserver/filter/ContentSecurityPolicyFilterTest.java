package org.sagebionetworks.web.unitserver.filter;

import static org.mockito.Mockito.verify;
import static org.sagebionetworks.web.server.servlet.filter.ContentSecurityPolicyFilter.CONTENT_SECURITY_POLICY;
import static org.sagebionetworks.web.server.servlet.filter.ContentSecurityPolicyFilter.POLICY_VALUE;

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
import org.sagebionetworks.web.server.servlet.filter.ContentSecurityPolicyFilter;

@RunWith(MockitoJUnitRunner.class)
public class ContentSecurityPolicyFilterTest {
	ContentSecurityPolicyFilter filter;
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
		filter = new ContentSecurityPolicyFilter();
	}

	@Test
	public void testStandardRequest() throws ServletException, IOException {
		filter.testFilter(mockRequest, mockResponse, mockFilterChain);

		// verify csp
		verify(mockResponse).setHeader(CONTENT_SECURITY_POLICY, POLICY_VALUE);
	}
}
