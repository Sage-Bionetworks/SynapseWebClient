package org.sagebionetworks.web.server.servlet.filter;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * CORS filter, Access-Control-Allow-Origin
 */
public class CORSFilter extends OncePerRequestFilter {
	public static final String ORIGIN_HEADER = "origin";
	public static final String DEFAULT_ALLOW_ORIGIN = "*";
	public static final String ACCESS_CONTROL_ALLOW_ORIGIN_HEADER = "Access-Control-Allow-Origin";
	public static final String ACCESS_CONTROL_ALLOW_CREDENTIALS_HEADER = "Access-Control-Allow-Credentials";
	public static final String SYNAPSE_ORG_SUFFIX = ".synapse.org";

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
		String allowOrigin = DEFAULT_ALLOW_ORIGIN;
		String origin = request.getHeader(ORIGIN_HEADER);
		if (origin != null && origin.toLowerCase().endsWith(SYNAPSE_ORG_SUFFIX)) {
			allowOrigin = origin;
			response.addHeader(ACCESS_CONTROL_ALLOW_CREDENTIALS_HEADER, "true");
		}

		response.addHeader(ACCESS_CONTROL_ALLOW_ORIGIN_HEADER, allowOrigin);
		if (request.getHeader("Access-Control-Request-Method") != null && "OPTIONS".equals(request.getMethod())) {
			response.addHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE");
			// response.addHeader("Access-Control-Allow-Headers",
			// "Authorization");
			response.addHeader("Access-Control-Allow-Headers", "Content-Type");
			response.addHeader("Access-Control-Max-Age", "1");
		}

		filterChain.doFilter(request, response);
	}

	public void testFilter(HttpServletRequest mockRequest, HttpServletResponse mockResponse, FilterChain mockFilterChain) throws ServletException, IOException {
		doFilterInternal(mockRequest, mockResponse, mockFilterChain);
	}
}
