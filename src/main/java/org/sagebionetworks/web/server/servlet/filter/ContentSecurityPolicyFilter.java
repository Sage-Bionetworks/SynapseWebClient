package org.sagebionetworks.web.server.servlet.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.filter.OncePerRequestFilter;

public class ContentSecurityPolicyFilter extends OncePerRequestFilter {
	public static final String CONTENT_SECURITY_POLICY = "Content-Security-Policy";
	public static final String POLICY_VALUE = "frame-ancestors 'self'; ";

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
		HttpServletResponse httpResponse = (HttpServletResponse) response;
		httpResponse.setHeader(CONTENT_SECURITY_POLICY, POLICY_VALUE);
		filterChain.doFilter(request, response);
	}

	public void testFilter(HttpServletRequest mockRequest, HttpServletResponse mockResponse, FilterChain mockFilterChain) throws ServletException, IOException {
		doFilterInternal(mockRequest, mockResponse, mockFilterChain);
	}

}
