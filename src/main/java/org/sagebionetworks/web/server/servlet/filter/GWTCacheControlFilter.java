package org.sagebionetworks.web.server.servlet.filter;

import java.io.IOException;
import java.util.Date;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * very basic filter to browser scripts
 * 
 * @author jayhodgson
 * 
 */
public class GWTCacheControlFilter implements Filter {
	
	private FilterConfig filterConfig;
	

	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain filterChain) throws IOException, ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		String requestURI = httpRequest.getRequestURI();
		if (!requestURI.contains(".nocache.")) {
			long today = new Date().getTime();
			HttpServletResponse httpResponse = (HttpServletResponse) response;
			//6 hours
			httpResponse.setDateHeader("Expires", today+(1000*60*60*6));
		}
		filterChain.doFilter(request, response);
	}

	public void init(FilterConfig config) throws ServletException {
		this.filterConfig = config;
	}

	@Override
	public void destroy() {
		this.filterConfig = null;
	}
}
