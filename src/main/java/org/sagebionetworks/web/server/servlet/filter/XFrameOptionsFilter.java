package org.sagebionetworks.web.server.servlet.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.filter.OncePerRequestFilter;

/**
 * X-Frame-Options header
 */
public class XFrameOptionsFilter extends OncePerRequestFilter {
	public static final String X_FRAME_OPTIONS_HEADER = "X-Frame-Options";
	public static final String DENY = "DENY";
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		response.addHeader(X_FRAME_OPTIONS_HEADER, DENY);
		filterChain.doFilter(request, response);
	}
	public void testFilter(HttpServletRequest mockRequest, HttpServletResponse mockResponse, FilterChain mockFilterChain) throws ServletException, IOException {
		doFilterInternal(mockRequest, mockResponse, mockFilterChain);
	}
}
